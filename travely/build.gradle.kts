// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        //classpath(libs.google.services)
        classpath ("com.google.gms:google-services:4.4.2")
    }
}
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
}