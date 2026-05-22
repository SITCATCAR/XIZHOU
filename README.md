###总结
- BaseActivity和BaseFragment初始化了viewbinding，不需要麻烦的每次都使用findviewbyid
- 数据库使用一个数据库两张表
- HistoryFragment中使用了TabLayout和ViewPager2，不需要手写回调改变布局的状态
- 项目中没有使用回调的方式而是事件订阅的方式处理，在event下Event(类似C#提供的事件类型)
- 历史记录界面的长按功能改为了修改标题和更新时间戳，删除功能点击顶部栏图标使用
- 历史记录界面删除，和正常点击进入展示页使用了简单的状态机，定义了两种简单的状态
- 扫码逻辑使用了camerax
