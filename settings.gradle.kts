pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        maven("https://libraries.minecraft.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.msrandom.net/repository/cloche/")
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "TemperAndTang"
dependencyResolutionManagement {
    versionCatalogs.create("libs") { from(files("libraries.toml")) }
}
