plugins {
    kotlin("jvm")
    java
    `java-library`
    `maven-publish`
}

val jsArrayVersion: String by rootProject
val groupIdDef: String by rootProject
val versionIdDef: String by rootProject

group = groupIdDef
version = versionIdDef

val artifactIdDef = "js-object-wrapper-array"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.zimolab:js-array:$jsArrayVersion")

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
