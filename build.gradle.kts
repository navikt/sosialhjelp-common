import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Test only
    const val junitJupiter = "5.8.1"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

ktlint {
    this.version.set("0.42.1")
}

allprojects {
    group = "no.nav.sosialhjelp"
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

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
