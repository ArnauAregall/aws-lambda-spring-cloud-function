rootProject.name = "aws-lambda-spring-cloud-function"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springBootDependencyManagementVersion: String by settings
    val graalVMBuildToolsVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springBootDependencyManagementVersion
        id("org.graalvm.buildtools.native") version graalVMBuildToolsVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
    }

}