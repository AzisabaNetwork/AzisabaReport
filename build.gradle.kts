import java.net.URLClassLoader
import java.util.ServiceLoader

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.2.2"
}

allprojects {
    group = "net.azisaba"
    version = "2.3.0"

    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
        plugin("com.gradleup.shadow")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    publishing {
        repositories {
            val deployUrl = if (project.version.toString().endsWith("SNAPSHOT"))
                project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "")
            else
                project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "")
            if (deployUrl.toString().isNotBlank()) maven {
                name = "repo"
                credentials(PasswordCredentials::class)
                url = uri(deployUrl)
            }
        }

        publications {
            create<MavenPublication>("mavenJava${project.name.replaceFirstChar { it.uppercaseChar() }}") {
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
            // Relocate service descriptors and provider names along with the driver classes.
            filesMatching("META-INF/services/**") {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
            mergeServiceFiles()
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
    if (name == "velocity" || name == "spigot") {
        val shadedJar = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar")
        val verifyShadedAuthentication by tasks.registering {
            group = "verification"
            description = "Verifies MariaDB authentication discovery inside the distribution JAR."
            dependsOn(shadedJar)
            inputs.file(shadedJar.flatMap { it.archiveFile })
            doLast {
                val jar = shadedJar.get().archiveFile.get().asFile
                // Isolate the JAR so dependencies on Gradle's classpath cannot hide packaging errors.
                URLClassLoader(arrayOf(jar.toURI().toURL()), ClassLoader.getPlatformClassLoader()).use { loader ->
                    val factory = loader.loadClass(
                        "net.azisaba.azisabareport.libs.org.mariadb.jdbc.plugin.AuthenticationPluginFactory"
                    )
                    val types = ServiceLoader.load(factory, loader)
                        .map { factory.getMethod("type").invoke(it).toString() }
                    check("mysql_native_password" in types) {
                        "${jar.name}: mysql_native_password authentication provider is missing (found $types)"
                    }
                }
            }
        }
        tasks.named("check") { dependsOn(verifyShadedAuthentication) }
    }
    tasks {
        shadowJar {
            archiveBaseName.set("${parent!!.name}-${project.name}")
        }
    }
}
