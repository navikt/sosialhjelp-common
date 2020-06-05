object Versions {
    const val jackson = "2.11.0"
    const val coroutines = "1.3.7"
    const val spring = "5.2.6.RELEASE"
    const val nimbusds = "8.19"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
//    kotlin-utils
    implementation(project(":sosialhjelp-common-kotlin-utils"))

//    Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

//    Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

//    Spring web
    implementation("org.springframework:spring-web:${Versions.spring}")

//    Nimbusds
    implementation("com.nimbusds:nimbus-jose-jwt:${Versions.nimbusds}")

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
                name.set("sosialhjelp-common-idporten-klient")
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