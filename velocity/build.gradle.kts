java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    api(project(":common"))
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.10")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("redis.clients:jedis:7.5.3")
    compileOnly("com.velocitypowered:velocity-api:4.1.1")
    compileOnly("net.kyori:adventure-text-minimessage:5.2.0")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.1")

    testImplementation("com.velocitypowered:velocity-api:4.1.1")
    testImplementation("net.kyori:adventure-text-minimessage:5.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}
