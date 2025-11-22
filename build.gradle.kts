plugins {
    id("java")
    id("application")
}

group = "io.github.agorohovcom"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.8")

    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}

application {
    mainClass.set("io.github.agorohovcom.eonet.Main")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
    systemProperty("file.encoding", "UTF-8")
}