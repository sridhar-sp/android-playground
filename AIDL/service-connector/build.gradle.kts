plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
    id("org.jreleaser")
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
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/kotlin")
            res.srcDirs("src/main/res")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

// Task to generate Javadoc
tasks.register<Javadoc>("androidJavadoc") {
    source = android.sourceSets.getByName("main").java.getSourceFiles()
    classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
    android.libraryVariants.all {
        if (name == "release") {
            classpath += javaCompileProvider.get().classpath
            println("**** Variant classpath: ${classpath.joinToString()}")
        }
    }

    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }

    exclude("**/R.html", "**/R.*.html", "**/index.html")
}

// Task to generate Javadoc JAR
tasks.register<Jar>("androidJavadocJar") {
    dependsOn("androidJavadoc")
    archiveClassifier.set("javadoc")
    from(tasks.named<Javadoc>("androidJavadoc").get().destinationDir)
}

// Task to generate Sources JAR
tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Software Component
                from(components["release"])

                // Add source and javadoc artifacts
//                artifact(tasks.named("androidSourcesJar"))
                artifact(tasks.named("androidJavadocJar"))

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

buildscript {
    configurations.all {
        resolutionStrategy {
//            force("org.apache.commons:commons-compress:1.22")
        }
    }
}


// FIX: Correct JReleaser configuration for Maven Central
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
            mavenCentral{
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