object Versions {
    const val jackson = "2.12.3"
    const val spring = "5.3.7"
    const val nimbusds = "9.10.1"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    implementation(project(":sosialhjelp-common-kotlin-utils"))
    implementation(project(":sosialhjelp-common-client-utils"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    api("org.springframework:spring-webflux:${Versions.spring}")
    api("com.nimbusds:nimbus-jose-jwt:${Versions.nimbusds}")
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
                name.set("sosialhjelp-common-idporten-client")
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
