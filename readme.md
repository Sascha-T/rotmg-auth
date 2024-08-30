# RotMG Auth for Java

This library allows you to generate authentication tokens for Realm of the Mad God.
RotMG has 2 main account types, those managed directly by Deca and those who are managed over Steam, both of which can be authenticated with this library.

To enable optional features (such as steamworks4j or javasteam), refer to documentation on [consuming feature variants.](https://docs.gradle.org/current/userguide/feature_variants.html#sec::consuming_feature_variants)

### Offered Feature Variants
Deca based login is included in the main library. \
`de.saschat.rotmg:auth-steamworks4j` and `de.saschat.rotmg:auth-javasteam`.


## Repository
This package is published to the GitHub Packages registry for Gradle, which requires you to [authenticate](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Sascha-T/rotmg-auth")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
   }
}
dependencies {
    implementation("de.saschat.rotmg:rotmg-auth:1.3")
    implementation("de.saschat.rotmg:rotmg-auth:1.3") {
        capabilities {
            requireCapability("de.saschat.rotmg:rotmg-auth-steamworks4j:1.3")
        }
    }
    implementation("de.saschat.rotmg:rotmg-auth:1.3") {
        capabilities {
            requireCapability("de.saschat.rotmg:rotmg-auth-javasteam:1.3")
        }
    }
}
```

## Code Examples

### Enable caching
```java
    RealmAuth.setCachingSettings(new FileCacheProcessor(new FileCacheProcessor.CachingSettings(new File("cacheFolder"))));
```
This will automatically store previously acquired access tokens and machine auth information if using javasteam.
 
### Login with Deca 
```java
    DecaAccount login = RealmAuth.login("USER", "PASSWORD");
```

### Login with Steam (steamworks4j)
```java
    SteamAccount login = RealmAuth.login(new Steamworks4JLoginProvider());
```

### Login with Steam (javasteam)
```java
    SteamAccount login = RealmAuth.login(new JavaSteamLoginProvider(new PasswordLoginStrategy("my@accou.nt", "passw0rd"), 10 /* timeout for user steamguard authentication */ ));
```
Note: a custom `de.saschat.rotmg.auth.javasteam.SteamLoginStrategy` would be required for console-less login
