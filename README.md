# GeetolSDK
The android sdk of geetol company

## usage

* step 1
```groovy
allprojects {
    repositories {
        // Add it in your root build.gradle at the end of repositories:
        maven { url 'https://jitpack.io' }
    }
}
```
or in gradle 7.1.2
```groovy
dependencyResolutionManagement {
    repositories {
        // Add it in your root build.gradle at the end of repositories:
        maven { url 'https://jitpack.io' }
    }
}
groovy

* step 2
```groovy
dependencies {
    // add sdk lib to your dependencies
    implementation 'com.github.pslilysm.GeetolSDK:sdk:1.0.0'
}
```
