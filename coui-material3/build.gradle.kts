import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

android {
    namespace = "com.itos.coui.material3"
    compileSdk = 37
    defaultConfig {
        minSdk = 31
        aarMetadata {
            minCompileSdk = 37
            minAgpVersion = "9.1.0"
        }
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {
    // 根属性仍由核心使用；适配模块必须有独立的 artifactId。
    coordinates(artifactId = "coui-material3")
    configure(AndroidSingleVariantLibrary(
        variant = "release", sourcesJar = SourcesJar.Sources(),
        javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
    ))
    pom {
        name.set("COUI-Kit Material 3")
        description.set("COUI 组件的可选 Material 3 主题与内容作用域适配")
        url.set("https://github.com/ItosEO/COUI-Kit")
        licenses {
            license {
                name.set("Mozilla Public License 2.0")
                url.set("https://www.mozilla.org/MPL/2.0/")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("ItosEO")
                name.set("ItosEO")
                url.set("https://github.com/ItosEO")
            }
        }
        scm {
            url.set("https://github.com/ItosEO/COUI-Kit")
            connection.set("scm:git:https://github.com/ItosEO/COUI-Kit.git")
            developerConnection.set("scm:git:ssh://git@github.com/ItosEO/COUI-Kit.git")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repository"))
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/itoseo/COUI-Kit")
            credentials(PasswordCredentials::class)
        }
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.files("LICENSE", "NOTICE", "TRADEMARKS.md")) { into("META-INF/coui") }
}

dependencies {
    api(project(":coui"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
}
