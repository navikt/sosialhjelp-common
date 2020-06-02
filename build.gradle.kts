import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Test only
    const val junitJupiter = "5.6.0"
    const val kluent = "1.61"
}

repositories {
    mavenCentral()
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

    configurations {
        "testImplementation" {
            exclude(group = "junit", module = "junit")
        }
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

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
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
