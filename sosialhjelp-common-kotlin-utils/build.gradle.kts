object Versions {
    const val coroutines = "1.6.4"
    const val slf4j = "1.7.36"
    const val commons = "1.24.0"
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

    // convert to pdf
    implementation("org.apache.pdfbox:pdfbox:3.0.0")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.xmlgraphics:fop:2.9")

    implementation("org.docx4j:docx4j-JAXB-MOXy:11.4.9")
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.4.9")
    implementation("org.docx4j:docx4j-export-fo:11.4.9")
    implementation("org.apache.commons:commons-csv:1.10.0")

    testImplementation("org.assertj:assertj-core:3.23.1")

    constraints {
        implementation("org.apache.commons:commons-compress:1.24.0") {
            because("https://github.com/advisories/GHSA-cgwf-w82q-5jrr")
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
