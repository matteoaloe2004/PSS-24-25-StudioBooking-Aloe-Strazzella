plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.0.14" // Plugin JavaFX aggiornato
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

    // Connection Pool
    implementation("com.zaxxer:HikariCP:5.0.1")

    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.openjfx:javafx-controls:20.0.2")
    implementation("org.openjfx:javafx-fxml:20.0.2")

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
