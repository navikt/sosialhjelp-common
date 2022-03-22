object Versions {
    const val slf4j = "1.7.36"
    const val spring = "5.3.16"
    const val jackson = "2.13.2"
    const val jjwtVersion = "0.11.2"
    const val log4j = "2.17.1"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
    implementation(project(":sosialhjelp-common-kotlin-utils"))
    implementation(project(":sosialhjelp-common-client-utils"))

    api("org.springframework:spring-webflux:${Versions.spring}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    api("org.slf4j:slf4j-api:${Versions.slf4j}")

    implementation("io.jsonwebtoken:jjwt-api:${Versions.jjwtVersion}")
    implementation("io.jsonwebtoken:jjwt-jackson:${Versions.jjwtVersion}")
    implementation("io.jsonwebtoken:jjwt-impl:${Versions.jjwtVersion}")

    constraints {
        implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.apache.logging.log4j:log4j-to-slf4j:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
    }
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
                name.set("sosialhjelp-common-maskinporten-client")
                description.set("Bibliotek for maskinporten client i sosialhjelp-domene")
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
