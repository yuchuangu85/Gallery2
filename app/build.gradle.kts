plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.android.gallery3d"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.gallery3d"
        minSdk = 24
        targetSdk = 34
        versionCode = 40030
        versionName = "1.1.40030"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        ndk {
//            abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
////            abiFilters += setOf("armeabi-v7a", "arm64-v8a")  // 根据需要调整
//        }

//        externalNativeBuild {
//            cmake {
//                cppFlags += "-std=c++11"
//                cppFlags("-fexceptions", "-frtti")
//                // 生成多个版本的so库
//                abiFilters("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//            }
//        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

//    sourceSets {
//        getByName("main") {
//            jniLibs.srcDirs("libs")
//            java.srcDirs("src", "src_pd")
//            res.srcDirs("res")
//            manifest.srcFile("src/main/AndroidManifest.xml")
//            jni.srcDirs("src/main/cpp/jniLibs/")
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

//        getByName("debug") {
//
//        }
//        getByName("release") {
//
//        }

    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    ndkVersion = "27.1.12297006"

}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.core.ui)
    implementation(libs.androidx.legacy.support.v13)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.annotation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    api(project(":gallerycommon"))
}