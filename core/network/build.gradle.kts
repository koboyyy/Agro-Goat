plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.agrogoat.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
    
    buildFeatures {
        buildConfig = true
    }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}
