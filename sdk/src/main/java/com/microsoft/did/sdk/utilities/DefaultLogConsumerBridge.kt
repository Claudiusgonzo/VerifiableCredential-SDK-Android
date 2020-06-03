package com.microsoft.did.sdk.utilities

import android.util.Log

class DefaultLogConsumerBridge: SdkLog.ConsumerBridge {

    override fun log(logLevel: SdkLog.Level, message: String, throwable: Throwable?, tag: String) {
        if (throwable == null) {
            Log.println(getAndroidLogLevel(logLevel), tag, message)
        } else {
            Log.println(getAndroidLogLevel(logLevel), tag, message + "\n" + Log.getStackTraceString(throwable))
        }
    }

    private fun getAndroidLogLevel(logLevel: SdkLog.Level): Int {
        return logLevel.severity() + 2
    }
}