plugins {
  `kotlin-dsl`
  `maven-publish`
}

dependencies {
  compileOnly("com.android.tools.build:gradle:8.1.2")
  //网络请求库
  implementation("com.squareup.okhttp3:okhttp:4.10.0")
  //用于解析后台返回的json数据，也可以用groovy自带的json解析，但比较复杂。
  implementation("com.google.code.gson:gson:2.10")
}

gradlePlugin {
  plugins {
    register("MyClass") {
      group = "com.example.test"
      version = "1.0"
      id = "com.example.test.myplugin"
      implementationClass = "com.example.test.MyPlugin"
    }
  }
}

publishing {
  repositories {
    maven(url = "../repository")    // 发布到本地根目录下的 repositories 目录下，发布的 groupId, artifactsId, version 共用插件的字符
  }
}
