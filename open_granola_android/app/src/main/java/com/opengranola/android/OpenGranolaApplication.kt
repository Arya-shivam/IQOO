package com.opengranola.android

import android.app.Application
import com.opengranola.android.sync.SyncScheduler

class OpenGranolaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedule(this)
    }
}
