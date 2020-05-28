import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val coroutines = "1.3.7"

    // Test only
    const val junitJupiter = "5.6.0"
    const val kluent = "1.61"
}

plugins {
    kotlin("jvm") version "1.3.72"
}

allprojects {
    group = "no.nav.sosialhjelp-common"
    version = properties["version"] ?: "local-build"

    repositories {
        mavenCentral()
        jcenter()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configurations {
        "testImplementation" {
            exclude(group = "junit", module = "junit")
        }
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

//        Test
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
        testImplementation("org.amshove.kluent:kluent:${Versions.kluent}")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
            }
        }

        withType<Test> {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
