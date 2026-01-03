plugins {
    java
    id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cl.gma"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Requisito del taller
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Para conectarse a ActiveMQ Artemis (Messaging)
    implementation("org.springframework.boot:spring-boot-starter-artemis")

    // Para consumir servicios REST del Marketplace (Web Client)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JAXB: Para entender el XML que llega de la Tienda Web
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.glassfish.jaxb:jaxb-runtime")

    // GSON: Para manejar el JSON del Marketplace
    implementation("com.google.code.gson:gson")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    // Para consumir servicios SOAP
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    // Para que JAXB funcione bien en Java 21 sin llorar
    implementation("org.glassfish.jaxb:jaxb-runtime")
    
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    jvmArgs("-Dfile.encoding=UTF-8")
}