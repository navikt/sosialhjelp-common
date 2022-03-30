object Versions {
    const val slf4j = "1.7.36"
    const val micrometer = "1.8.4"
    const val json = "20220320"
    const val spring = "5.3.17"
    const val commonsLang3 = "3.12.0"
    const val aspectj = "1.9.9"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {

    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    api("org.slf4j:jcl-over-slf4j:${Versions.slf4j}")
    api("io.micrometer:micrometer-registry-prometheus:${Versions.micrometer}")
    api("org.json:json:${Versions.json}")
    api("org.springframework:spring-context:${Versions.spring}")
    api("org.springframework:spring-aop:${Versions.spring}")
    api("org.apache.commons:commons-lang3:${Versions.commonsLang3}")
    api("org.aspectj:aspectjrt:${Versions.aspectj}")

    runtimeOnly("org.aspectj:aspectjweaver:${Versions.aspectj}")
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
                name.set("sosialhjelp-common-metrics")
                description.set("Bibliotek for sensu/influx metrics i sosialhjelp-domene")
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
