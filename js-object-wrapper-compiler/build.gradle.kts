plugins {
    kotlin("jvm")
    java
    `java-library`
    `maven-publish`
}

val kspVersion: String by rootProject
val groupIdDef: String by rootProject
val versionIdDef: String by rootProject

group = groupIdDef
version = versionIdDef

val artifactIdDef = "js-object-wrapper-compiler"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:$kspVersion")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation(project(":js-object-wrapper-array"))
    implementation(project(":js-object-wrapper-annotation"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = groupIdDef
            version = versionIdDef
            artifactId = artifactIdDef
            from(components["kotlin"])
        }
    }
}
