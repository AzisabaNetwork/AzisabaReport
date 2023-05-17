repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://ci.emc.gs/nexus/content/repositories/aikar-release/") }
    maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
}

dependencies {
    api(project(":common"))
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.0")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("net.azisaba:RyuZUPluginChat:4.2.0")
    compileOnly("net.azisaba:lunachatplus:3.2.2") {
        exclude("org.bstats", "bstats-bukkit")
        exclude("org.bstats", "bstats-bungeecord")
    }
}
