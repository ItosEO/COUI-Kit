import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    workingDir(rootDir)
}.standardOutput.asText.map { it.trim().toInt() }

val signingKeys = listOf("KEYSTORE_FILE", "KEYSTORE_PASSWORD", "KEYSTORE_ALIAS", "KEYSTORE_ALIAS_PASSWORD")
val signingEnvironment = signingKeys.associateWith { providers.environmentVariable(it).orNull }
val hasSigningEnvironment = signingEnvironment.values.any { it != null }
val signingPropertiesFile = rootProject.file("signing.properties")
val signingProperties = Properties().apply {
    if (!hasSigningEnvironment && signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}
val signingValues = if (hasSigningEnvironment || signingPropertiesFile.exists()) {
    signingKeys.associateWith { key ->
        val value = if (hasSigningEnvironment) signingEnvironment[key] else signingProperties.getProperty(key)
        require(!value.isNullOrBlank()) { "Demo 签名缺少 $key" }
        value
    }
} else {
    emptyMap()
}

android {
    namespace = "com.itos.coui.demo"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.itos.coui.demo"
        minSdk = 31
        targetSdk = 37
        versionCode = gitCommitCount.get()
        versionName = "0.0.1-alpha11"
    }
    buildFeatures { compose = true }
    val demoSigning = if (signingValues.isNotEmpty()) {
        signingConfigs.create("demo") {
            storeFile = rootProject.file(signingValues.getValue("KEYSTORE_FILE")).also {
                require(it.isFile) { "Demo 签名密钥文件不存在" }
            }
            storePassword = signingValues.getValue("KEYSTORE_PASSWORD")
            keyAlias = signingValues.getValue("KEYSTORE_ALIAS")
            keyPassword = signingValues.getValue("KEYSTORE_ALIAS_PASSWORD")
        }
    } else {
        signingConfigs.getByName("debug")
    }
    buildTypes {
        debug {
            signingConfig = demoSigning
        }
        release {
            optimization {
                enable = true
            }
            signingConfig = demoSigning
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":coui"))
    implementation(project(":coui-icons"))
    implementation(project(":coui-material3"))
    implementation(libs.capsule)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
