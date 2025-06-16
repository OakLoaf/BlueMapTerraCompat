plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("8.3.0")
    id("xyz.jpenilla.run-paper") version("2.2.4")
}

group = "org.lushplugins"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.codemc.org/repository/maven-public/") // Terra
    maven("https://repo.bluecolored.de/releases") // BlueMap
}

dependencies {
    // Dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    compileOnly("com.dfsek.terra:api:6.6.1-BETA+83bc2c902")
    compileOnly("com.dfsek.terra:manifest-addon-loader:1.0.0-BETA+fd6decc70")
    compileOnly("com.dfsek.terra:v1_21_3:6.6.1-BETA+83bc2c902")

    compileOnly("de.bluecolored:bluemap-api:2.7.4")
    compileOnly("de.bluecolored:bluemap-common:5.7")

    // Soft Dependencies


    // Libraries
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")
    implementation("com.fasterxml.jackson.core:jackson-core:2.19.0") // Jackson
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.19.1") // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0") // Jackson
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
        filesMatching("terra.addon.yml") {
            expand(project.properties)
        }

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
        filesMatching("terra.addon.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21.1")

        downloadPlugins {
            modrinth("bluemap", "5.7-paper")
            modrinth("terra", "6.5.0-BETA-bukkit")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "lushReleases"
            url = uri("https://repo.lushplugins.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "lushSnapshots"
            url = uri("https://repo.lushplugins.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}
