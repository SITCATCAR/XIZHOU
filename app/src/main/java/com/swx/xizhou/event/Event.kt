package com.swx.xizhou.event

class Event<T> {
    private var subscribers = mutableSetOf<(T)-> Unit>()

    operator fun plusAssign(method:(T)-> Unit){
        subscribers.add(method)
    }

    operator fun minusAssign(method: (T) -> Unit){
        subscribers.remove(method)
    }

    fun invoke(value:T){
        for(method in subscribers){
            method.invoke(value)
        }
    }

}