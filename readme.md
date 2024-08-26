# RotMG Auth for Java

//@todo: gradle features, lol

## Login with Deca 
```java
    DecaAccount login = RealmAuth.login("USER", "PASSWORD");
```

## Login with Steam (steamworks4j)
```java
    SteamAccount login = RealmAuth.login(new Steamworks4JLoginProvider());
```
