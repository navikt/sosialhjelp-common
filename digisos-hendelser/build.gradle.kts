plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

kotlin {
    jvm()
    jvmToolchain(21)
    js {
        nodejs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.sosialhjelp.filformat.kmp)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.mockk)
                implementation(libs.assertj.core)
            }
        }
        jsMain {
            dependencies {
                implementation(
                    npm(
                        "@js-joda/timezone",
                        libs.versions.js.joda.timezone
                            .get(),
                    ),
                )
            }
        }
    }
}

val githubUser: String? by project
val githubPassword: String? by project

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
            credentials {
                username = githubUser
                password = githubPassword
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("digisos-hendelser")
            description.set("Kotlin Multiplatform library for folding Digisos application events")
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
    }
}

val verifyJsExports by tasks.registering {
    val dts =
        layout.buildDirectory
            .file("dist/js/productionLibrary/${rootProject.name}-${project.name}.d.ts")
    dependsOn(tasks.named("jsNodeProductionLibraryDistribution"))
    inputs.file(dts)

    doLast {
        val file = dts.get().asFile
        val text = file.readText()
        val required =
            listOf(
                "function foldJson(",
                "class SoknadMetadata",
                "static create(",
                "class Soknad {",
                "class FoldResult",
                "interface SoknadHendelse",
                "abstract class SoknadsStatus",
            )
        val missing = required.filterNot { it in text }
        check(missing.isEmpty()) {
            "Generated TypeScript declarations are missing: $missing\n" +
                "Most likely an @JsExport annotation was dropped. See ${file.absolutePath}"
        }
    }
}

tasks.named("check") { dependsOn(verifyJsExports) }
