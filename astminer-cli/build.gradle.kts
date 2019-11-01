import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.shadowJar

plugins {
    id("java")
    kotlin("jvm") version "1.3.41"
    id("application")
    id("tanvd.kosogor") version "1.0.6"
}

application {
    mainClassName = "cli.MainKt"
}

group = "io.github.vovak.astminer"
version = "0.3"

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/shibby09/astminer")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("io.github.shibby09.astminer", "astminer-dev", "0.5.4")
    compile("com.github.ajalt", "clikt", "2.1.0")
}

val shadowJar = shadowJar {
    jar {
        archiveName = "cli-$version.jar"
        mainClass = "cli.MainKt"
    }
}.apply {
    task.archiveClassifier.set("")
}

publishJar {
    publication {
        artifactId = "astminer-cli"
    }

    jar {
        components = { shadowJar.artifact(this) }
    }

    bintray {

        // If username and secretKey not set, will be taken from System environment param `bintray_user`, 'bintray_key'
        repository = "astminer"

        info {
            githubRepo = "shibby09/astminer"
            vcsUrl = "https://github.com/shibby09/astminer/astminer-cli"
            labels.addAll(listOf("mining", "ast", "ml4se", "code2vec", "path-based representations"))
            license = "MIT"
            description = "CLI for AstMiner library"
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType(BintrayUploadTask::class) {
    dependsOn("shadowJar")
}
