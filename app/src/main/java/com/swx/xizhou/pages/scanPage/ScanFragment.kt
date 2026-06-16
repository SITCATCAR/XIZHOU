package com.swx.xizhou.pages.scanPage

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.swx.xizhou.BaseFragment
import com.swx.xizhou.R
import com.swx.xizhou.database.HistoryDBHelper
import com.swx.xizhou.database.HistoryItemDTO
import com.swx.xizhou.database.HistoryMapper
import com.swx.xizhou.database.HistoryType
import com.swx.xizhou.databinding.ScanFragmentBinding
import com.swx.xizhou.model.CalendarQRModel
import com.swx.xizhou.model.FacebookQRModel
import com.swx.xizhou.model.XQRModel
import com.swx.xizhou.model.YoutubeQRModel
import com.swx.xizhou.activity.ScanResultActivity
import com.swx.xizhou.model.YoutubeType
import com.swx.xizhou.pages.historyPage.HistoryPagerFragment
import com.swx.xizhou.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment: BaseFragment<ScanFragmentBinding>(ScanFragmentBinding::inflate) {

    private lateinit var historyMapper: HistoryMapper
    private var isScanning = false
    private var isCameraStarted = false
    private val pickImageLauncher=registerForActivityResult(ActivityResultContracts.GetContent()){
            uri: Uri? ->uri?.let { fetchImageFromUri(uri) }
    }
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val scanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient()
    }

    private var soundPool: SoundPool?=null
    private var scanSuccessSoundId:Int = 0
    private var isSoundLoaded=false

    override fun initView() {
        cameraExecutor= Executors.newSingleThreadExecutor()

        initScanSound()

        PermissionHelper.onPermissionResult += ::onPermissionResult

        if (PermissionHelper.isGranted(requireContext(), PermissionHelper.PermissionType.CAMERA)) {
            startCamera()
        } else {
            PermissionHelper.request(this, PermissionHelper.PermissionType.CAMERA)
        }

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    override fun loadData() {
        historyMapper= HistoryMapper(activity)
    }

    override fun onResume() {
        super.onResume()
        isScanning = false
    }

    //销毁时释放相机等资源
    override fun onDestroyView() {
        super.onDestroyView()
        imageAnalyzer?.clearAnalyzer()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        scanner.close()

        soundPool?.release()
        soundPool=null
        isSoundLoaded=false

        isCameraStarted = false
    }

    override fun onDestroy() {
        PermissionHelper.onPermissionResult -= ::onPermissionResult
        super.onDestroy()
    }

    private fun onPermissionResult(result: PermissionHelper.PermissionResult) {
        if (result.type == PermissionHelper.PermissionType.CAMERA) {
            if (result.granted) {
                startCamera()
            } else {
                requireActivity().finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        PermissionHelper.handleResult(requestCode, grantResults)
    }


    private fun startCamera(){

        if (isCameraStarted) return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            if (!isBindingAvailable) return@addListener
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.preView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            //设置Analyzer处理扫码
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                .also {
                    it.setAnalyzer(cameraExecutor){imageProxy -> processImagefromCamera(imageProxy)}
                }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(viewLifecycleOwner,cameraSelector,preview,imageAnalyzer)
                isCameraStarted = true
            }catch (e: Exception){
                Log.e("ScanFragment","Use case binding failed",e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processImagefromCamera(imageProxy: ImageProxy){
        // 防止重复扫描
        if (isScanning) {
            imageProxy.close()
            return
        }
        val mImage = imageProxy.image
        if(mImage!=null){
            val image =
                InputImage.fromMediaImage(mImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener {
                    barcodes -> if(barcodes.isNotEmpty()){
                        isScanning = true
                        handleBarcodes(barcodes)
                }
            }.addOnFailureListener {
                Log.e("ScanFragment","Scanning failed",it)
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }else
            imageProxy.close()
    }

    private fun fetchImageFromUri(uri: Uri){
        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            scanner.process(image).addOnSuccessListener {
                    barcodes -> if(barcodes.isNotEmpty()){
                    isScanning = true
                    handleBarcodes(barcodes)
                }
            }.addOnFailureListener {
                Log.e("ScanFragment","Scanning failed",it)
            }
        }catch (e: Exception){
            Log.e("ScanFragment","error fetch image",e)
        }
    }

    //处理扫描出的码
    private fun handleBarcodes(barcodes: List<Barcode>){
        // 确保在主线程执行操作
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            activity?.runOnUiThread { handleBarcodes(barcodes) }
            return
        }

        val barcode = barcodes.firstOrNull() ?: return
        val value = barcode.rawValue ?: return
        //成功提示音
        playScanSuccessSound()

        // ML Kit的valueType进行分类
        val type = when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                when {
                    YoutubeQRModel.isYoutubeLink(value) -> ScanResultActivity.TYPE_YOUTUBE
                    XQRModel.isXLink(value) -> ScanResultActivity.TYPE_X
                    FacebookQRModel.isFacebookLink(value) -> ScanResultActivity.TYPE_FACEBOOK
                    else -> ScanResultActivity.TYPE_TEXT
                }
            }
            Barcode.TYPE_CALENDAR_EVENT -> ScanResultActivity.TYPE_CALENDAR
            else -> ScanResultActivity.TYPE_TEXT
        }

        //异步添加到DB
        lifecycleScope.launch(Dispatchers.IO) {
            addToDB(value, type)
            withContext(Dispatchers.Main) {
                HistoryPagerFragment.onItemChangeEvent.invoke(Unit)
            }
        }

        val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
            putExtra(ScanResultActivity.EXTRA_SCAN_RESULT, value)
            putExtra(ScanResultActivity.EXTRA_SCAN_TYPE, type)
        }
        startActivity(intent)
    }

    private fun addToDB(value: String, type: Int){
        when (type) {
            ScanResultActivity.TYPE_YOUTUBE -> {
                val model= YoutubeQRModel()
                model.input=value
                model.type= YoutubeType.URL
                val dto = HistoryItemDTO(
                    model.input, HistoryType.YOUTUBE, model.getID(),
                    System.currentTimeMillis()
                )
                historyMapper.insert(dto, HistoryDBHelper.S_TABLE_NAME)
            }
            ScanResultActivity.TYPE_X -> {
                val model = XQRModel()
                model.input = value
                val dto = HistoryItemDTO(
                    value, HistoryType.X, model.getID(),
                    System.currentTimeMillis()
                )
                historyMapper.insert(dto, HistoryDBHelper.S_TABLE_NAME)
            }
            ScanResultActivity.TYPE_FACEBOOK -> {
                val model = FacebookQRModel()
                model.input = value
                val dto = HistoryItemDTO(
                    value, HistoryType.FACEBOOK, model.getID(),
                    System.currentTimeMillis()
                )
                historyMapper.insert(dto, HistoryDBHelper.S_TABLE_NAME)
            }
            ScanResultActivity.TYPE_CALENDAR -> {
                val model = CalendarQRModel.fromString(value)
                val dto= HistoryItemDTO(
                    value, HistoryType.CALENDAR, model?.getID() ?: value.take(20),
                    System.currentTimeMillis()
                )
                historyMapper.insert(dto, HistoryDBHelper.S_TABLE_NAME)
            }
            else -> {
                // 纯文本或其他类型
                val dto = HistoryItemDTO(
                    value, HistoryType.TEXT, value.take(20),
                    System.currentTimeMillis()
                )
                historyMapper.insert(dto, HistoryDBHelper.S_TABLE_NAME)
            }
        }
    }

    private fun initScanSound(){
        val audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        soundPool= SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build()

        scanSuccessSoundId=soundPool?.load(requireContext(),R.raw.beep,1)?:0
        soundPool?.setOnLoadCompleteListener { _,simpleId, status ->
            if(status==0 && simpleId==scanSuccessSoundId){
                isSoundLoaded=true
            }
        }
    }


    private fun playScanSuccessSound(){
        if(isSoundLoaded && scanSuccessSoundId!=0){
            soundPool?.play(scanSuccessSoundId,1.0f,1.0f,1,0,1.0f)
        }
    }

    //MainActivity手动关闭/开启相机
    fun openCamera() {
        startCamera()
    }

    fun closeCamera() {
        imageAnalyzer?.clearAnalyzer()
        cameraProvider?.unbindAll()
        isCameraStarted = false
    }
}
