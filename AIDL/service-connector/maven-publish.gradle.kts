plugins {
    id("maven-publish")
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

afterEvaluate {

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.yourcompany"  // Replace with your group ID
                artifactId = "your-library"  // Replace with your library name
                version = "1.0.0"  // Replace with your version

                pom {
                    name.set("Your Library Name")
                    description.set("A brief description of your library.")
                    url.set("https://your-library-url.com")
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("your-id")
                            name.set("Your Name")
                            email.set("your-email@example.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/yourcompany/your-library.git")
                        developerConnection.set("scm:git:ssh://github.com/yourcompany/your-library.git")
                        url.set("https://github.com/yourcompany/your-library")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Sonatype"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = sonatypeUsername ?: System.getenv("SONATYPE_USERNAME")
                    password = sonatypePassword ?: System.getenv("SONATYPE_PASSWORD")
                }
            }
        }
    }

}
