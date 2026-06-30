plugins {
    alias(libs.plugins.android.library)
    
}

android {
    namespace = "com.agrogoat.core.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(project(":core:model"))
}
