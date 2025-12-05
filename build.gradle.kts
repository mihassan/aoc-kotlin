plugins {
  kotlin("jvm") version "2.1.20"
}

repositories {
  mavenCentral()
}

sourceSets.main {
  java.srcDirs("src")

  dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
  }
}

tasks {
  wrapper {
    gradleVersion = "8.13"
  }
}

tasks.register<JavaExec>("solve") {
  group = "application"
  description = "Solve AoC problem for a given year and day"
  classpath = sourceSets["main"].runtimeClasspath
  mainClass.set("tool.Solve")
}

tasks.register<JavaExec>("fetchInput") {
  group = "application"
  description = "Fetch input for a given year and day"
  classpath = sourceSets["main"].runtimeClasspath
  mainClass.set("tool.FetchInput")
}

tasks.register<JavaExec>("prepareYear") {
  group = "application"
  description = "Prepare solution templates for a new AoC year"
  classpath = sourceSets["main"].runtimeClasspath
  mainClass.set("tool.PrepareYear")
}
