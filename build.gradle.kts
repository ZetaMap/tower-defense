import com.xpdustry.toxopid.extension.anukeXpdustry
import com.xpdustry.toxopid.spec.ModMetadata
import com.xpdustry.toxopid.spec.ModPlatform
import com.xpdustry.toxopid.task.GithubAssetDownload
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.indra.common)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.shadow)
    alias(libs.plugins.toxopid)
    alias(libs.plugins.errorprone.gradle)
}

val metadata = ModMetadata.fromJson(rootProject.file("plugin.json"))
if (indraGit.headTag() == null) metadata.version += "-SNAPSHOT"
group = "com.xpdustry"
version = metadata.version
description = metadata.description

toxopid {
    compileVersion = "v${metadata.minGameVersion}"
    platforms = setOf(ModPlatform.SERVER)
}

repositories {
    mavenCentral()
    anukeXpdustry()
    maven("https://maven.xpdustry.com/releases") {
        name = "xpdustry-releases"
        mavenContent { releasesOnly() }
    }
    maven("https://maven.xpdustry.com/snapshots") {
        name = "xpdustry-snapshots"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    compileOnly(toxopid.dependencies.arcCore)
    compileOnly(toxopid.dependencies.mindustryCore)
    compileOnly(libs.distributor.api)
    compileOnlyApi(libs.jspecify)
    annotationProcessor(libs.nullaway)
    errorprone(libs.errorprone.core)
    implementation(libs.gestalt.core)
    implementation(libs.snakeyaml)
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
    javaVersions {
        target(21)
        minimumToolchain(21)
    }

    publishSnapshotsTo("xpdustry", "https://maven.xpdustry.com/snapshots")
    publishReleasesTo("xpdustry", "https://maven.xpdustry.com/releases")

    mitLicense()

    if (metadata.repository.isNotBlank()) {
        val repo = metadata.repository.split("/")
        github(repo[0], repo[1]) {
            ci(true)
            issues(true)
            scm(true)
        }
    }

    configurePublications {
        pom {
            organization {
                name = "xpdustry"
                url = "https://www.xpdustry.com"
            }
        }
    }
}

spotless {
    java {
        palantirJavaFormat()
        formatAnnotations()
        importOrder("", "\\#")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        bumpThisNumberIfACustomStepChanges(1)
    }
    kotlinGradle {
        ktlint()
    }
}

val generateMetadataFile by tasks.registering {
    inputs.property("metadata", metadata)
    val output = temporaryDir.resolve("plugin.json")
    outputs.file(output)
    doLast { output.writeText(ModMetadata.toJson(metadata)) }
}

tasks.shadowJar {
    fun relocate(pkg: String) = relocate(pkg, "com.xpdustry.tower.shadow.${pkg.split('.').last()}")
    archiveFileName = "${metadata.name}.jar"
    archiveClassifier = "plugin"
    from(generateMetadataFile)
    from(rootProject.file("LICENSE.md")) { into("META-INF") }
    relocate("org.yaml.snakeyaml")
    relocate("org.github.gestalt")
    minimize()
    mergeServiceFiles()
}

tasks.register<Copy>("release") {
    dependsOn(tasks.build)
    from(tasks.shadowJar)
    destinationDir = temporaryDir
}

tasks.withType<JavaCompile> {
    options.errorprone {
        disableWarningsInGeneratedCode = true
        disable("MissingSummary", "InlineMeSuggester")
        check("NullAway", if (name.contains("test", ignoreCase = true)) CheckSeverity.OFF else CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "com.xpdustry.tower")
    }
}

val downloadSlf4md by tasks.registering(GithubAssetDownload::class) {
    owner = "xpdustry"
    repo = "slf4md"
    asset = "slf4md.jar"
    version = "v${libs.versions.slf4md.get()}"
}

val downloadDistributorCommon by tasks.registering(GithubAssetDownload::class) {
    owner = "xpdustry"
    repo = "distributor"
    asset = "distributor-common.jar"
    version = "v${libs.versions.distributor.get()}"
}

tasks.runMindustryServer {
    mods.from(downloadSlf4md, downloadDistributorCommon)
}
