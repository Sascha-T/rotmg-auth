import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("java-library")
    // id("org.gradlex.extra-java-module-info") version "1.8"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}


group = "de.saschat.rotmg"
version = "2.2"

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
    "steamworks4jCompileOnly"("com.code-disaster.steamworks4j:steamworks4j:1.9.0")

    "javasteamImplementation"(project(":"))
    "javasteamCompileOnly"("in.dragonbra:javasteam:1.4.0")// https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    "javasteamCompileOnly"("org.bouncycastle:bcprov-jdk18on:1.78.1")
    "javasteamCompileOnly"("com.google.protobuf:protobuf-java:4.26.1")


    implementation("com.google.code.gson:gson:2.11.0")
}

/*extraJavaModuleInfo {
    automaticModule("com.code-disaster.steamworks4j:steamworks4j", "steamworks4j")
    automaticModule("in.dragonbra:javasteam", "javasteam")
    failOnMissingModuleInfo.set(false)
}*/


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

/*shadowJar {
    relocate("com.code-disaster.steamworks4j", "de.saschat.rotmg.auth.shadow.steamworks4j");
    relocate("in.dragonbra.javasteam", "de.saschat.rotmg.auth.shadow.javasteam");

}*/
/*configurations.forEach({println(it.name); println(it.isCanBeResolved)})*/

/*configurations["javasteamCompileClasspath"].forEach({ println(it) })*/

/*tasks {
    val shadowSteamworks4j by registering(ShadowJar::class) {
        from(sourceSets["steamworks4j"].output)
        archiveClassifier.set("steamworks4j")

        for(file in project.configurations["steamworks4jCompileClasspath"]) {
            if(!file.endsWith("rotmg-auth-$version.jar"))
                from(file);
        }
    }
    val shadowJavasteam by registering(ShadowJar::class) {
        from(sourceSets["javasteam"].output)
        archiveClassifier.set("javasteam")

        for(file in project.configurations["javasteamCompileClasspath"]) {
            if(!file.endsWith("rotmg-auth-$version.jar"))
                from(file);
        }
    }
}*/
