plugins {
    kotlin("jvm") version "1.5.20"
    java
    `java-library`
    `maven-publish`
}

group = "com.github.zmolab"
version = "0.1.0-SNAPSHOT"

val jsArrayVersion = "3f31768c3a"
val groupIdDef = group.toString()
val versionIdDef = version.toString()
val artifactIdDef = "js-object-wrapper"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.zimolab:js-array:$jsArrayVersion")
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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