plugins {
    id("org.jetbrains.dokka") version "2.1.0"
}

dependencies {
    implementation("io.github.legendshot414:kommand-api:1.0.3")
    implementation("io.github.monun:invfx-api:3.3.2")
}

tasks {
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    register<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaGenerateHtml")

        from("$buildDir/dokka/html/") {
            include("**")
        }
    }

    register<Jar>("paperJar") {
        archiveVersion.set("")
        archiveBaseName.set("Psychics")
        from(sourceSets["main"].output)

        doLast {
            copy {
                from(archiveFile)
                val plugins = File(rootDir, ".debug/plugins/")
                into(if (File(plugins, archiveFileName.get()).exists()) File(plugins, "update") else plugins)
            }
        }
    }
}
