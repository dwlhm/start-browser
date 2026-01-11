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
        maven {
            url = uri("https://maven.mozilla.org/maven2/")
        }
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
include(":engine:gecko")
include(":feature:tabmanager")
include(":core:browser")
include(":shell:browser")
include(":shell:dashboardsession")
include(":core:utils")
include(":core:event")
include(":feature:sessions")
