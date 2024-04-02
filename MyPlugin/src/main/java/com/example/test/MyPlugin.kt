package com.example.test

import com.android.build.gradle.AppExtension
import com.android.build.gradle.tasks.ManifestProcessorTask
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 添加plugin，自动创建task向Manifest.xml文件中添加<queries>包名
 */
class MyPlugin : Plugin<Project> {
  private val variantNames = mutableListOf<String>()
  private val packageList = mutableListOf<String>()

  override fun apply(target: Project) {
    println("Hello from MyPlugin!$target")
    if (!isAssembleTask(target)) {
      println("不是打包过程，跳过")
      return
    }
    // 发送网络请求，从网络拉取需要添加的包名
    sendRequest()
    if (packageList.isEmpty()) {
      println("packageList isEmpty()")
      return
    }
    println("get packages:$packageList")
    var applicationId = ""
    target.afterEvaluate {
      val type = target.extensions.findByType(AppExtension::class.java)
      // 遍历所有的变体 正常为Debug和Release
      type?.applicationVariants?.forEach {
        if (applicationId != it.applicationId) {
          applicationId = it.applicationId
        }
        variantNames.add(it.name)
      }
      target.afterEvaluate {
        variantNames.forEach {
          // 为process${variantName}MainManifest后添加自定义task
          addProcessManifestTask(target, it.capitalized())
        }
      }
    }
  }

  fun sendRequest() {
    val client = OkHttpClient()
    // 模拟网络请求
    val url = "https://www.baidu.com"
    val request = Request.Builder()
      .url(url)
      .build()
    val countDownLatch = CountDownLatch(1)
    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        println("failure")
        // 请求失败处理
        e.printStackTrace()
        countDownLatch.countDown()
      }

      override fun onResponse(call: Call, response: Response) {
        // 请求成功处理
        val responseData = response.body?.string()
        response.close()
        // mock数据
        packageList.add("com.voicemaker.android")
        countDownLatch.countDown()
      }
    })
    try {
      countDownLatch.await(1, TimeUnit.SECONDS)
    } catch (e: InterruptedException) {
      e.printStackTrace()
    }
  }

  fun addProcessManifestTask(project: Project, name: String) {
    val processManifestTask = project.tasks.getByName(
      String.format(
        "process%sMainManifest",
        name
      )
    ) as ManifestProcessorTask
    val mainFiles = processManifestTask.outputs.files
    // 过滤出所有的xml文件
    val manifests = mainFiles.filter {
      it.name.endsWith("xml")
    }.map {
      it
    }
    processManifestTask.finalizedBy(
      // 在processManifestTask之后执行自定义task
      project.tasks.create(
        "MyProcessManifest$name",
        AddQueriesTask::class.java
      ).apply {
        setData(manifests, packageList)
      }
    )
  }

  fun isAssembleTask(project: Project): Boolean {
    var isAssembleTask = false
    val requests = project.gradle.startParameter.taskRequests
    if (requests.size != 0) {
      val args = requests[0].args
      for (argValue in args) {
        //判断是打包Task
        if (argValue.contains("assemble")) {
          isAssembleTask = true
          break
        }
      }
      if (isAssembleTask) {
        return true
      }
    }
    return false
  }

}