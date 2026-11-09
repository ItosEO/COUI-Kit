pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // shortcut: CI 下载此版本时会遇到 GitHub 资产 504。升级 Kotlin 后移除此路由。
        if (System.getenv("GITHUB_ACTIONS") == "true") {
            exclusiveContent {
                forRepository {
                    maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") }
                }
                filter {
                    includeVersion("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "2.4.20")
                }
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "COUI"
include(":demo")
include(":coui")
include(":coui-material3")
include(":coui-icons")
