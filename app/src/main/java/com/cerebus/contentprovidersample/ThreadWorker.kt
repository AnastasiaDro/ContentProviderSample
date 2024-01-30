package com.cerebus.contentprovidersample

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class CustomThreadWorker : Thread() {
    val looper = Looper(this)

    override fun run() {
        looper.loop()
    }
}

class Looper(private val thread: Thread) {


    private val messageQueue: BlockingQueue<CustomMessage> = LinkedBlockingQueue()

    fun loop() {
        while (!thread.isInterrupted) {
            try {
                val task = messageQueue.take()
                task?.runnable?.run()
                task.handler.onReceiveMessage(task)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }


    fun addMessage(message: CustomMessage) {
        messageQueue.put(message)
    }
}

class CustomMessage(val handler: CustomHandler, val runnable: Runnable? = null, val desc: String)

abstract class CustomHandler(val looper: Looper?) {
    abstract fun onReceiveMessage(message: CustomMessage)

    fun post(message: CustomMessage) {
        looper?.addMessage(message)
    }
}