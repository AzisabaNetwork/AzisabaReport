plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "net.azisaba"
    version = "2.1.1"

    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
        plugin("com.github.johnrengelman.shadow")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    publishing {
        repositories {
            maven {
                name = "repo"
                credentials(PasswordCredentials::class)
                url = uri(
                    if (project.version.toString().endsWith("SNAPSHOT"))
                        project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "")
                    else
                        project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "")
                )
            }
        }

        publications {
            create<MavenPublication>("mavenJava${project.name.capitalize()}") {
                from(components["java"])
                artifact(tasks.getByName("sourcesJar"))
            }
        }
    }

    tasks {
        processResources {
            from(sourceSets.main.get().resources.srcDirs) {
                include("**")
                val tokenReplacementMap = mapOf(
                    "version" to project.version,
                )
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }

        compileJava {
            options.encoding = "UTF-8"
        }

        javadoc {
            options.encoding = "UTF-8"
        }

        test {
            useJUnitPlatform()
        }

        shadowJar {
            relocate("redis.clients", "net.azisaba.azisabareport.libs.redis.clients")
            relocate("io.netty", "net.azisaba.azisabareport.libs.io.netty")
            relocate("org.mariadb.jdbc", "net.azisaba.azisabareport.libs.org.mariadb.jdbc")
            relocate("com.zaxxer.hikari", "net.azisaba.azisabareport.libs.com.zaxxer.hikari")
            relocate("xyz.acrylicstyle.util", "net.azisaba.azisabareport.libs.xyz.acrylicstyle.util")
            relocate("org.apache.http", "net.azisaba.azisabareport.libs.org.apache.http")
        }
    }
}

subprojects {
    tasks {
        shadowJar {
            archiveBaseName.set("${parent!!.name}-${project.name}")
        }
    }
}
