plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = property("group") as String
version = property("version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Okaeri Configs
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.5")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.5")

    // Database
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Hooks (compile-only)
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(files("/home/sayan/IdeaProjects/LandClaimPlugin/target/LandClaimPlugin-2.0.3.jar"))

    // FastBoard (compile and shadow)
    implementation("fr.mrmicky:fastboard:2.1.5")

    // bStats (compile and shadow)
    implementation("org.bstats:bstats-bukkit:3.1.0")

    // Commands (Cloud V2)
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("eu.okaeri", "dev.velmax.velkoth.libs.okaeri")
        relocate("com.zaxxer.hikari", "dev.velmax.velkoth.libs.hikari")
        relocate("org.yaml.snakeyaml", "dev.velmax.velkoth.libs.snakeyaml")
        relocate("dev.dejvokep.boostedyaml", "dev.velmax.velkoth.libs.boostedyaml")
        relocate("org.incendo.cloud", "dev.velmax.velkoth.libs.cloud")
        relocate("fr.mrmicky.fastboard", "dev.velmax.velkoth.libs.fastboard")
        relocate("org.bstats", "dev.velmax.velkoth.libs.bstats")
        minimize()
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to (project.property("description") as String)
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters"))
    }
}
