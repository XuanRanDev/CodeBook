import java.text.SimpleDateFormat
import java.util.*
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.xuanran.codebook"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.xuanran.codebook"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 自定义 BuildConfig 字段
        buildConfigField("String", "BUILD_TIME", "\"${getCurrentTime()}\"")
        buildConfigField("String", "GIT_HASH", "\"${getGitHash()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation("org.bouncycastle:bcprov-jdk15on:1.68") // Bouncy Castle 加密库

    // Room dependencies
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // AES 加密库
    implementation("org.bouncycastle:bcprov-jdk15on:1.69")

    // Fingerprint authentication
    implementation("androidx.biometric:biometric:1.1.0")

    // Material Components and UI Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}


// 获取当前时间的函数
fun getCurrentTime(): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(Date())
}

// 获取 Git 提交哈希的函数
fun getGitHash(): String {
    return "git rev-parse --short HEAD".runCommand()
}

// 执行命令并返回结果的扩展函数
fun String.runCommand(): String {
    return try {
        val process = Runtime.getRuntime().exec(this)
        val reader = process.inputStream.bufferedReader()
        reader.readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}