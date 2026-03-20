plugins {
    id("java")
}

version = "1.0-SNAPSHOT"
group = "it.danielebruni.wildadventure"

repositories {
    mavenCentral()
    maven {
        name = "WildCommons"
        url = uri("https://maven.pkg.github.com/danib150/WildCommons")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        name = "SportPaper"
        url = uri("https://maven.pkg.github.com/Electroid/SportPaper")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven {
        name = "Boosters"
        url = uri("https://maven.pkg.github.com/danib150/Boosters")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.enginehub.org/repo/")

}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    compileOnly("app.ashcon:sportpaper:1.8.8-R0.1-SNAPSHOT")
    compileOnly("it.danielebruni.wildadventure:wildcommons-core:1.0.1")
    compileOnly("io.github.danib150:boosters:1.0-SNAPSHOT")

}