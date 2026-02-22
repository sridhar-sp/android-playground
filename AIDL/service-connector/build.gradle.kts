plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
    id("org.jreleaser")
    id("org.jetbrains.dokka")
}

android {
    namespace = "io.github.sridhar_sp.service_connector"
    compileSdk = 36

    version = "0.0.3"

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kotlin {
        jvmToolchain(17)
    }

    publishing { // Configure what goes in SoftwareComponent, this will be used by the maven-publish
        singleVariant("release") {
            withJavadocJar() // Along with .aar this doc jar file can be retrieved using components("release")
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Software Component
                from(components["release"])

                // Add source and javadoc artifacts
//                artifact(tasks.named("androidSourcesJar"))

                groupId = "io.github.sridhar-sp"
                artifactId = "aidl-service-connector"
                version = project.version.toString()

                pom {
                    name.set("aidl-service-connector")
                    description.set("AIDL Service Connector Android library")
                    url.set("https://github.com/sridhar-sp/android-playground/tree/main/AIDL/service-connector")
                    inceptionYear.set("2025")

                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://spdx.org/licenses/Apache-2.0.html")
                        }
                    }

                    developers {
                        developer {
                            id.set("sridhar-sp")
                            name.set("Sridhar Subramani")
                            email.set("sridharthechosenone@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/sridhar-sp/android-playground.git")
                        developerConnection.set("scm:git:ssh://git@github.com:sridhar-sp/android-playground.git")
                        url.set("https://github.com/sridhar-sp/android-playground/tree/main/AIDL/service-connector")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "staging"
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

jreleaser {
    gitRootSearch.set(true)
    project {
        authors.set(listOf("Sridhar Subramani <sridharthechosenone@gmail.com>"))
        license.set("Apache-2.0")
        inceptionYear.set("2025")
        description.set("AIDL Service Connector Android library")

        println("**** Project Version: ${project.version.get()}")

        version.set(project.version.get())
        versionPattern.set("SEMVER")
    }

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
        verify.set(true)
        mode.set(org.jreleaser.model.Signing.Mode.COMMAND)
    }

    release {
        github {
            enabled.set(true)
            skipTag.set(false)
            skipRelease.set(false)
            repoOwner.set("sridhar-sp")
        }
    }

    deploy {
        maven {
            mavenCentral {
                create("maven-central") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    applyMavenCentralRules.set(false) // Wait for fix: https://github.com/kordamp/pomchecker/issues/21
                    verifyPom.set(false)
                    stagingRepositories.add("build/staging-deploy")
                    sign.set(true)
                    checksums.set(true)
                    retryDelay = 60
                }
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}