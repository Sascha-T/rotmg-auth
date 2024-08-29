plugins {
    id("java")
    id("java-library")
    id("org.gradlex.extra-java-module-info") version "1.8"
    id("maven-publish")
}


group = "de.saschat.rotmg"
version = "1.3"

repositories {
    mavenCentral()
}

sourceSets {
    create("steamworks4j") {
        java {
            srcDir("src/steamworks4j/java")
        }
    }
    create("javasteam") {
        java {
            srcDir("src/javasteam/java")
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18

    registerFeature("steamworks4j") {
        usingSourceSet(sourceSets["steamworks4j"])
    }
    registerFeature("javasteam") {
        usingSourceSet(sourceSets["javasteam"])
    }
}

dependencies {

    "steamworks4jImplementation"(project(":"))
    "steamworks4jImplementation"("com.code-disaster.steamworks4j:steamworks4j:1.9.0")

    "javasteamImplementation"(project(":"))
    "javasteamImplementation"("in.dragonbra:javasteam:1.4.0")// https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    "javasteamImplementation"("org.bouncycastle:bcprov-jdk18on:1.78.1")
    "javasteamImplementation"("com.google.protobuf:protobuf-java:4.26.1")


    implementation("com.google.code.gson:gson:2.11.0")
}

extraJavaModuleInfo {
    automaticModule("com.code-disaster.steamworks4j:steamworks4j", "steamworks4j")
    automaticModule("in.dragonbra:javasteam", "javasteam")
    failOnMissingModuleInfo.set(false)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Sascha-T/rotmg-auth")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("rotmgAuth") {
            from(components["java"])
        }
    }
}
