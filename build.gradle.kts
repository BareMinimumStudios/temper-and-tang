import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
plugins {
    `maven-publish`
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.cloche)
}
group = providers.gradleProperty("maven_group").get()
version = providers.gradleProperty("mod_version").get()
repositories {
    cloche.librariesMinecraft()
    mavenCentral()
    cloche { main(); mavenFabric(); mavenNeoforgedMeta(); mavenNeoforged(); mavenParchment() }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { content { includeGroup("thedarkcolour") } }
    maven("https://maven.fzzyhmstrs.me/")
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
}
cloche {
    metadata {
        modId = "temperandtang"
        name = "Temper & Tang"
        icon = "assets/temperandtang/icon.png"
        description = "Standalone medieval blacksmithing, skill-influenced equipment quality, and hands-on forging."
        license = "BML-1.0"
        author("Bare Minimum Studios")
        contributor("pokesmells")
        url = "https://github.com/BareMinimumStudios/temper-and-tang"
        sources = "https://github.com/BareMinimumStudios/temper-and-tang"
        issues = "https://github.com/BareMinimumStudios/temper-and-tang/issues"
        dependencies {
            dependency { modId = "minecraft"; version { start = "1.21.1"; end = "1.21.2"; endExclusive = true } }
        }
    }
    common {
        mappings { official(); parchment(libs.versions.parchment) }
        mixins.from("src/main/resources/temperandtang.mixins.json")
        sourceSet.resources.exclude("temperandtang.mixins.json")
    }
    fabric("fabric:1.21.1") {
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.fabric.loader
        includedClient()
        runs { server(); client() }
        dependencies {
            fabricApi(libs.versions.fabric.api)
            modImplementation(libs.fabric.language.kotlin)
            modImplementation(libs.fzzy.config.fabric)
            modImplementation(libs.diegesis.fabric)
            modCompileOnly(libs.emi.fabric)
            modCompileOnly(libs.jei.fabric)
            compileOnly(libs.mixinextras)
        }
        metadata {
            dependencies {
                dependency { modId = "java"; version("21") }
                dependency { modId = "fabric-api"; version(libs.versions.fabric.api.get()) }
                dependency { modId = "fabric-language-kotlin"; version(libs.versions.fabric.language.kotlin.get()) }
                dependency { modId = "fzzy_config"; version(libs.versions.fzzy.fabric.get()) }
                dependency { modId = "diegesis"; version("1.0.0-beta.19") }
            }
            entrypoint("emi") { value.set("com.bareminimumstudios.temperandtang.compat.viewer.TemperEmiPlugin") }
            entrypoint("jei_mod_plugin") { value.set("com.bareminimumstudios.temperandtang.compat.viewer.TemperJeiPlugin") }
            entrypoint("client") {
                adapter.set("kotlin")
                value.set("com.bareminimumstudios.temperandtang.platform.TemperFabricClient")
            }
            entrypoint("main") {
                adapter.set("kotlin")
                value.set("com.bareminimumstudios.temperandtang.platform.TemperFabric")
            }
        }
    }
    neoforge("neoforge:1.21.1") {
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.neoforge.loader
        runs { server(); client() }
        dependencies {
            modImplementation(libs.neoforge.language.kotlin)
            modImplementation(libs.fzzy.config.neoforge)
            modImplementation(libs.diegesis.neoforge)
            modCompileOnly(libs.emi.neoforge)
            modCompileOnly(libs.jei.neoforge)
            compileOnly(libs.mixinextras)
        }
        metadata {
            modLoader = "kotlinforforge"
            loaderVersion { start = libs.versions.neoforge.language.kotlin.get() }
            blurLogo = false
            dependencies {
                dependency { modId = "neoforge"; version(libs.versions.neoforge.loader.get()) }
                dependency { modId = "kotlinforforge"; version(libs.versions.neoforge.language.kotlin.get()) }
                dependency { modId = "fzzy_config"; version(libs.versions.fzzy.neoforge.get()) }
                dependency { modId = "diegesis"; version("1.0.0-beta.19") }
            }
        }
    }
}
configurations.matching { it.name == "apiElements" || it.name == "runtimeElements" }.configureEach {
    outgoing.capability("$group:${project.name}-server:$version")
}
tasks.matching { it.name.startsWith("accessWidenFabric1211") }.configureEach {
    dependsOn("generateFabric1211MappingsArtifact")
}
kotlin {
    jvmToolchain(21)
    sourceSets.named("fabric1211") { kotlin.srcDirs("src/fabric/1.21.1/kotlin", "src/loaderShared/kotlin") }
    sourceSets.named("neoforge1211") { kotlin.srcDirs("src/neoforge/1.21.1/kotlin", "src/loaderShared/kotlin") }
    @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
    compilerVersion.set("2.2.20")
    coreLibrariesVersion = "2.2.20"
}
tasks.withType<net.msrandom.stubs.GenerateStubApi>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("generated/$name"))
}
dependencies {
    testImplementation(kotlin("test-junit5", "2.2.20"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
tasks.withType<Test>().configureEach { useJUnitPlatform() }
tasks.withType<Jar>().matching { it.name == "fabric1211Jar" || it.name == "neoforge1211Jar" }.configureEach {
    from(rootProject.file("LICENSE")) { into("META-INF/licenses"); rename { "temperandtang-LICENSE" } }
    from(rootProject.file("THIRD_PARTY_NOTICES.txt")) { into("META-INF/licenses") }
}
