object Versions {
    const val jackson = "2.12.1"
    const val spring = "5.3.3"

    const val mockk = "1.10.3"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    implementation(project(":sosialhjelp-common-api"))
    implementation(project(":sosialhjelp-common-client-utils"))
    implementation(project(":sosialhjelp-common-kotlin-utils"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    api("org.springframework:spring-web:${Versions.spring}")

    testImplementation("io.mockk:mockk:${Versions.mockk}")
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
                name.set("sosialhjelp-common-kommuneinfo-client")
                description.set("Felles klient for å hente kommuneinfo fra Fiks")
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