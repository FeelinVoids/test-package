plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    // id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    // id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "space.outbreak.outbreaklib"
version = "1.0-SNAPSHOT"
description = "OutbreakLib"

val jacksonVersion = "2.14.3"
val commandAPIVersion = "9.0.3"
val paperVersion = "1.20.1-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io") // Oraxen
    maven("https://repo.codemc.io/repository/maven-public/") // NBT-API
}

dependencies {
    compileOnlyApi("io.papermc.paper:paper-api:${paperVersion}")
    compileOnlyApi("org.apache.commons:commons-text:1.10.0")
    compileOnlyApi("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    compileOnlyApi("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    compileOnlyApi("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    compileOnlyApi("com.github.oraxen:oraxen:1.162.0")
    compileOnlyApi("de.tr7zw:item-nbt-api-plugin:2.12.0")
}