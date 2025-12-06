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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Start Browser"
include(":app")
include(":core:navigation")
include(":feature:home")
include(":feature:onboarding")
include(":feature:browser")
include(":core:domain")
include(":core:datastore")
include(":core:data")
include(":core:webview")
include(":core:ui")
