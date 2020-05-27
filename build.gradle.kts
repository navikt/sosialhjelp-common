import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val kotlin = "1.3.72"
    const val coroutines = "1.3.7"

    // Test only
    const val junitJupiter = "5.6.0"
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.github.ben-manes.versions") version "0.28.0"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo.spring.io/plugins-release/")
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}

subprojects {
    group = "no.nav.sosialhjelp"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.github.ben-manes.versions")

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

//        Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

    //    Test
        testImplementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xjsr305=strict", "-XXLanguage:+InlineClasses")
            }
        }

        withType<Test> {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            testLogging {
                events("skipped", "failed")
            }
        }

        withType<ShadowJar> {
            classifier = ""
            mergeServiceFiles()
        }
    }
}
