import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val dusseldorfKtorVersion = "3.2.1.2-93aa998"
val ktorVersion = ext.get("ktorVersion").toString()
val pdfBoxVersion = "2.0.27"
val imageIOVersion = "3.9.4"
val jsonassertVersion = "1.5.1"
val fuelVersion = "2.3.1"
val tokenSupportVersion = "2.1.6"
val mockOauth2ServerVersion = "0.5.6"

val mainClass = "no.nav.helse.K9JoarkKt"

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
    apply("https://raw.githubusercontent.com/navikt/dusseldorf-ktor/3bd7664b0352f77c0d6c124d1d03c57bc0284690/gradle/dusseldorf-ktor.gradle.kts")
}

dependencies {
    // Server
    implementation("no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")

    // Client
    implementation("no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")

    // Token validation
    implementation("no.nav.security:token-validation-ktor-v2:$tokenSupportVersion")

    // Bilde til PNG
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")
    implementation("com.twelvemonkeys.imageio:imageio-jpeg:$imageIOVersion")

    // Test
    testImplementation("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.skyscreamer:jsonassert:$jsonassertVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
}

repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    mavenCentral()
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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
    gradleVersion = "7.5.1"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
