package com.example.test

import groovy.xml.XmlParser
import groovy.util.NodeList
import groovy.util.Node
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * 用于添加包名的task
 */
open class AddQueriesTask : DefaultTask() {
  private var mManifests: List<File>? = null
  private var mPackageList: List<String>? = null

  @TaskAction
  fun doTaskAction() {
    println("doTaskAction")
    mManifests?.forEach {
      // 对Manifest文件添加包名
      handleManifestFile(it)
    }
  }

  private fun handleManifestFile(manifest: File) {
    if (!manifest.exists()) {
      return
    }
    println("parse file:$manifest")
    val xmlParser = XmlParser()
    val xmlNode = xmlParser.parse(manifest)
    ((xmlNode["queries"] as NodeList).firstOrNull() as? Node)?.apply {
      mPackageList?.forEach {
        // 为queries标签下添加package节点
        Node(
          this,
          "package",
          linkedMapOf("android:name" to it)
        )
      }
      writeToManifest(manifest, xmlNode)
    }
  }

  /**
   * 写回文件，保存更改
   */
  private fun writeToManifest(manifest:File, node:Node) {
    val result = XmlUtil.serialize(node)
    Files.write(manifest.toPath(), result.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    println("writeFinish")
  }

  fun setData(manifests: List<File>, packageList: MutableList<String>) {
    mManifests = manifests
    mPackageList = packageList
  }
}