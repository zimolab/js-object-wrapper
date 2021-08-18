plugins {
    kotlin("jvm") version "1.5.20"
    java
    `java-library`
    `maven-publish`
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.5.20"))
    }
}

val groupIdDef: String by rootProject
val versionIdDef: String by rootProject

group = groupIdDef
version = versionIdDef


val artifactIdDef = "js-object-wrapper"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
//    implementation(kotlin("stdlib"))
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

//java {
//    withSourcesJar()
//    withJavadocJar()
//}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = groupIdDef
//            version = versionIdDef
//            artifactId = artifactIdDef
//            from(components["kotlin"])
//        }
//    }
//}