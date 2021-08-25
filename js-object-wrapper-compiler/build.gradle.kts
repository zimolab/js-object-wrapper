plugins {
    kotlin("jvm")
    java
    `java-library`
    `maven-publish`
}

val kspVersion: String by rootProject
val groupIdDef: String by rootProject
val versionIdDef: String by rootProject
val jsArrayVersion: String by rootProject

group = groupIdDef
version = versionIdDef

val artifactIdDef = "js-object-wrapper-compiler"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }

    flatDir {
        dir("libs")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:$kspVersion")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation("com.github.zimolab:js-array:$jsArrayVersion")

    implementation(project(":js-object-wrapper-core"))
    implementation(project(":js-object-wrapper-annotation"))
    implementation(files("libs/formatter.jar"))
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
