object Versions {
    const val jackson = "2.13.2"
    const val jacksonDatabind = "2.13.2.2"
    const val micrometer = "1.8.5"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
//    kotlin-utils
    implementation(project(":sosialhjelp-common-kotlin-utils"))

//    Jackson
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    api("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
//    Micrometer
    api("io.micrometer:micrometer-core:${Versions.micrometer}")
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("sosialhjelp-common-selftest")
                description.set("Bibliotek for selftest i sosialhjelp-domene")
                url.set("https://github.com/navikt/sosialhjelp-common")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/sosialhjelp-common.git")
                    developerConnection.set("scm:git:https://github.com/navikt/sosialhjelp-common.git")
                    url.set("https://github.com/navikt/sosialhjelp-common")
                }
            }
            from(components["java"])
        }
    }
}
