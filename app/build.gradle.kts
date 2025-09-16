plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0" // Plugin JavaFX
}

group = "com.studiobooking"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // MySQL Connector
    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.mindrot:jbcrypt:0.4")

    // JUnit 5 per i test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    // Classe principale del progetto
    mainClass.set("com.example.studiobooking.MainApp")
}

// Configurazione JavaFX
javafx {
    version = "20.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

// Configurazione test con JUnit 5
tasks.named<Test>("test") {
    useJUnitPlatform()
}
