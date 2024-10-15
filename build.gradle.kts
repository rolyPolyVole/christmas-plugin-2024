import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.7.3"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "host.carbon"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-beta.17")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.21.1")
    }
}

kotlin {
    compilerOptions {
        javaParameters = true // Lamp CMD Framework
    }
}

application {
    mainClass.set("ChristmasEventPluginKt")
}