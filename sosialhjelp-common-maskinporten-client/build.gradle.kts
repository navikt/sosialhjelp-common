object Versions {
    const val slf4j = "1.7.32"
    const val coroutines = "1.6.0"
    const val springBoot = "2.6.3"
    const val tokenValidation = "1.3.10"
    const val jackson = "2.13.1"
    const val jjwtVersion = "0.11.2"
    const val nimbusJoseJwt = "9.19"
    const val log4j = "2.17.1"
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

    //    Spring
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${Versions.springBoot}")

    //    Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    //    Token-validering
    implementation("no.nav.security:token-validation-spring:${Versions.tokenValidation}")

    //    Json webtokens
    implementation("io.jsonwebtoken:jjwt-api:${Versions.jjwtVersion}")
    implementation("io.jsonwebtoken:jjwt-jackson:${Versions.jjwtVersion}")
    implementation("io.jsonwebtoken:jjwt-impl:${Versions.jjwtVersion}")

    //    Sosialhjelp-common
    implementation(project(":sosialhjelp-common-kotlin-utils"))
    implementation(project(":sosialhjelp-common-client-utils"))

    constraints {
        implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.apache.logging.log4j:log4j-to-slf4j:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }

        implementation("com.nimbusds:nimbus-jose-jwt:${Versions.nimbusJoseJwt}")
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
