object Versions {
    const val coroutines = "1.6.4"
    const val slf4j = "1.7.36"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
//    Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

//    Slf4j
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
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
                name.set("sosialhjelp-common-kotlin-utils")
                description.set("Bibliotek med utils-funksjoner som kan brukes i sosialhjelp-appene")
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
