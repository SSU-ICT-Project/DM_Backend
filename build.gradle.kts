plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.dm"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // netty
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty")
    developmentOnly("io.netty:netty-all:4.1.100.Final")

    // jjwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.google.api-client:google-api-client:2.2.0")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // aws s3
    implementation(platform("software.amazon.awssdk:bom:2.24.0"))
    implementation("software.amazon.awssdk:s3")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    implementation("io.github.cdimascio:dotenv-java:2.2.0")

    // kafka
    implementation("org.springframework.kafka:spring-kafka")

    // fcm
    implementation("com.google.firebase:firebase-admin:9.5.0")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    // LangChain4j
    // 1) BOM으로 버전 정렬
    implementation(platform("dev.langchain4j:langchain4j-bom:1.3.0"))
    // 2) 모듈들은 버전 제거 (BOM이 관리)
    implementation("dev.langchain4j:langchain4j")
    implementation("dev.langchain4j:langchain4j-core")
    implementation("dev.langchain4j:langchain4j-open-ai")
    implementation("dev.langchain4j:langchain4j-rag")
    implementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox")
    // 3) HTTP 클라이언트 구현 ⇒ Spring RestClient만 남김
    implementation("dev.langchain4j:langchain4j-http-client-spring-restclient")
    configurations.all {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    // 4) Spring Boot Starter는 최신 베타 버전 사용
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot-starter:1.3.0-beta9")
    // 5) pgvector 추가
    implementation("dev.langchain4j:langchain4j-pgvector:1.3.0-beta9")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
