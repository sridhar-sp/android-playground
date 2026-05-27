pluginManagement {

    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // External plugins can be resolved in dependencies section
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "remote-compose"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // Enable usage like implementation(projects.core.designsystem) in gradle

include(":client")
include(":server")
