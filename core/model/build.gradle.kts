plugins {
    alias(libs.plugins.android.library)
    
}

android {
    namespace = "com.agrogoat.core.model"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation("androidx.annotation:annotation:1.9.1")
}
