import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = ext.get("ktorVersion").toString()
val dusseldorfKtorVersion = "1.3.2.df87a57"
val pdfBoxVersion = "2.0.19"
val mainClass = "no.nav.helse.K9JoarkKt"

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

buildscript {
    apply("https://raw.githubusercontent.com/navikt/dusseldorf-ktor/df87a57d6656a9280cf990bfd0b3085047c4d1bf/gradle/dusseldorf-ktor.gradle.kts")
}

dependencies {
    // Server
    compile ( "no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    compile ( "no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    compile ( "no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    compile ( "no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    compile ( "no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")
    
    // Client
    compile ( "no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    compile ( "no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")

    // Bilde til PNG
    compile("org.apache.pdfbox:pdfbox:$pdfBoxVersion")

    // Test
    testCompile ( "no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testCompile ("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testCompile ("org.skyscreamer:jsonassert:1.5.0")
}

repositories {
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://packages.confluent.io/maven/")

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    jcenter()
    mavenLocal()
    mavenCentral()
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to mainClass
            )
        )
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.3"
}
