plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.5.21-1.0.0-beta07" apply true
    java
}

val groupIdDef: String by rootProject
val versionIdDef: String by rootProject
val jsArrayVersion: String by rootProject

group = groupIdDef
version = versionIdDef

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("com.github.zimolab:js-array:$jsArrayVersion")
    implementation(project(":js-object-wrapper-core"))
    implementation(project(":js-object-wrapper-annotation"))
    implementation(project(":js-object-wrapper-compiler"))
    ksp(project(":js-object-wrapper-compiler"))
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}