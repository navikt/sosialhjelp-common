import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Test only
    const val junitJupiter = "5.7.0"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

ktlint {
    this.version.set("0.41.0")
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
