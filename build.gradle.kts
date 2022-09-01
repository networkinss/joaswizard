plugins {
    java
    `maven-publish`
    application
}

application {
    mainClass.set("ch.inss.openapi.joaswizard.Main")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("com.github.jknack:handlebars:4.3.2-FORK")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.poi:poi-ooxml:5.1.0")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

group = "ch.inss.joaswizard"
version = "0.9.10-SNAPSHOT"
description = "Jo as wizard"
java.sourceCompatibility = JavaVersion.VERSION_11

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "1G"
}
