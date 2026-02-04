import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Test only
    const val JUNIT_JUPITER = "5.9.2"
}

repositories {
    mavenCentral()
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

ktlint {
    this.version.set("1.2.1")
}

allprojects {
    group = "no.nav.sosialhjelp"
    version = properties["version"] ?: "local-build"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        this.version.set("1.2.1")
    }

    dependencies {
//        Test
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT_JUPITER}")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}
