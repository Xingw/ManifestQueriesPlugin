package com.example.manifestqueriesplugin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DefaultActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    baseContext.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }, 0).forEach {
      // 查找对应应用并拉起进行测试
      if ("com.voicemaker.android" == it.activityInfo.packageName) {
        val className = it.activityInfo.name
        baseContext.startActivity(Intent().apply {
          setClassName("com.voicemaker.android", className)
          setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
      }
    }
  }
}