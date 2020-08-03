object Versions {
    const val slf4j = "1.7.30"

    const val kotlin = "1.3.70"
    const val coroutines = "1.3.7"
    const val springBoot = "2.3.1.RELEASE"
    const val sosialhjelpCommon = "1.a615c63"
    const val logback = "1.2.3"
    const val logstash = "6.3"
    const val filformat = "1.2020.01.09-15.55-f18d10d7d76a"
    const val micrometerRegistry = "1.5.1"
    const val prometheus = "0.8.1"
    const val tokenValidation = "latest.release"
    const val jackson = "2.11.0"
    const val guava = "28.2-jre"
    const val swagger = "2.9.2"
    const val konfig = "1.6.10.0"
    const val commonsCodec = "1.14"
    const val commonsIo = "2.6"
    const val fileUpload = "1.4"
    const val tika = "1.23"
    const val pdfBox = "2.0.19"
    const val fiksKryptering = "1.0.9"
    const val lettuce = "5.3.1.RELEASE"
    const val nettyCodec = "4.1.50.Final"
    const val ksFiksClient = "1.0.15"
    const val jjwtVersion = "0.10.7"
    const val nimbusJoseVersion = "7.7"
    const val auth0Version = "0.8.3"
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
    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
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
    implementation("no.nav.sosialhjelp:sosialhjelp-common-selftest:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-api:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-client-utils:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-kommuneinfo-client:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-idporten-client:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-kotlin-utils:${Versions.sosialhjelpCommon}")

}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/plugins-release/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
        credentials {
            username = githubUser
            password = githubPassword
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