/*
 *
 *  * Copyright (C) 2023 Cobblemon Contributors
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utilities.ACCESS_WIDENER

plugins {
    id("java")
    id("java-library")

    id("dev.yumi.gradle.licenser")
    id("dev.architectury.loom-no-remap")
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(rootProject.property("java_version").toString().toInt()))
    }
}

repositories {
    mavenCentral()
    //JEI
    maven("https://maven.blamejared.com/")
    maven("https://maven.parchmentmc.org")
}

license {
    rule(rootProject.file("HEADER"))
    include("**/*.kt", "**/*.java")
}

loom {
    accessWidenerPath.set(project(":common").file(ACCESS_WIDENER))
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("mc_version")}")
    // Minecraft 26.x is distributed with official names; no Parchment/remap layer.
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(rootProject.property("java_version").toString().toInt())
        options.compilerArgs.add("-Xlint:-processing,-classfile,-serial")
    }

    withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(rootProject.property("java_version").toString()))
    }

    withType<Jar> {
        from(rootProject.file("LICENSE"))
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    // Dev/intermediate jars only feed remapJar, which writes the compressed release jar.
    // Deflating ~250MB of already-compressed assets at every stage is wasted time.
    named<Jar>("jar") {
        entryCompression = ZipEntryCompression.STORED
    }

    // Assets/data are ~250MB and already ship in the main jar; copying and remapping them
    // into sources jars cost minutes per build.
    withType<Jar>().named { it == "sourcesJar" }.configureEach {
        exclude("assets/**", "data/**")
    }
}
