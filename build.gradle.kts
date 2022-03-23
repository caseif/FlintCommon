import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import blue.lapis.methodremapper.gradle.RemapTask

plugins {
    `java-library`
    `maven-publish`
    eclipse
    idea
    checkstyle
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.caseif.methodremapper") version "0.3"
}

defaultTasks("clean", "updateLicenses", "build", "shadowJar")

evaluationDependsOnChildren()

// Project information
group = "net.caseif.flint.common"
version = "1.3.6"
description = "Code shared across implementations of Flint."

// Extended project information
val inceptionYear: String by extra { "2015" }
val packaging: String by extra { "jar" }
val author: String by extra { "Max Roncace" }

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

// Project repositories
repositories {
    mavenCentral()
}

// Project dependencies
dependencies {
    shadow("com.google.guava:guava:17.0")
    shadow("com.google.code.gson:gson:2.2.4")
    api(project("flint"))
}

// Read source files using UTF-8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Copy>("processResources") {
    from("LICENSE")
}

// License header formatting
license {
    include("**/*.java")
    ignoreFailures.set(false)
}

// check code style
checkstyle {
    configFile = file("etc/checkstyle.xml")
}

tasks.withType<Checkstyle> {
    exclude("**/*.properties")
    exclude("**/*.yml")
}

tasks.withType<Javadoc> {
    enabled = false
}

tasks.withType<Jar> {
    archiveClassifier.set("base")
    manifest {
        attributes["Created-By"] = "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})"
        attributes["Specification-Title"] = project("flint").name
        attributes["Specification-Version"] = project("flint").version
        attributes["Specification-Vendor"] = project("flint").extra["author"]
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = project.extra["author"]
    }
}

tasks.withType<ShadowJar> {
    dependencies {
        include(project("flint"))
    }
    archiveClassifier.set("")

    finalizedBy(tasks.remap)
}

tasks.create<Jar>("sourceJar") {
    from(sourceSets["main"].java)
    from(sourceSets["main"].resources)
    archiveClassifier.set("sources")
}

tasks.named<RemapTask>("remap") {
    inputJar = (tasks["shadowJar"] as Jar).archiveFile.get().getAsFile()
    config = project.file("etc/remap.txt")
}

artifacts {
    archives(tasks["shadowJar"])
    archives(tasks["sourceJar"])
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                groupId = project.group as String
                version = project.version as String
                artifactId = project.name

                packaging = packaging
                description.set(project.description)
                url.set("http://github.com/caseif/FlintCommon")
                inceptionYear.set(project.extra["inceptionYear"] as String)

                scm {
                    url.set("https://github.com/caseif/FlintCommon")
                    connection.set("scm:git:git://github.com/caseif/FlintCommon.git")
                    developerConnection.set("scm:git:git@github.com:caseif/FlintCommon.git")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("http://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.1"
}
