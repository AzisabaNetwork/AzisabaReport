repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://ci.emc.gs/nexus/content/repositories/aikar-release/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly("com.github.AzisabaNetwork:RyuZUPluginChat:v4.2.0")
    compileOnly("com.github.AzisabaNetwork:LunaChatPlus:v3.2.0") {
        exclude("org.bstats", "bstats-bukkit")
        exclude("org.bstats", "bstats-bungeecord")
    }
}
