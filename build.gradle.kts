plugins {
	java
	id("org.springframework.boot") version "3.4.1" // <--- CORREGIDO
    id("io.spring.dependency-management") version "1.1.7"
}

group = "cl.iplacex"
version = "0.0.1-SNAPSHOT"
description = "Tienda Web TechNova SpA"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("com.sun.xml.bind:jaxb-ri:4.0.5")

	// Dependencias para integración con JMS y ActiveMQ
	// Spring Boot gestionará las versiones de estas 3 automáticamente

    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.glassfish.jaxb:jaxb-runtime")

	// Soporte para XML (JAXB) - Obligatorio en Java 21
	implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
	implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")

	// Soporte para Mensajería (Artemis)
	implementation("org.springframework.boot:spring-boot-starter-artemis")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaExec> {
    jvmArgs("-Dfile.encoding=UTF-8")
}
