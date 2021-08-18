plugins {
    kotlin("jvm")
    java
    `java-library`
    `maven-publish`
}

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
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.21-1.0.0-beta07")
    implementation(project(rootProject.path))
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
