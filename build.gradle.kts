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
  mainClass.set("SolveKt")
}

tasks.register<JavaExec>("fetchInput") {
  group = "application"
  description = "Fetch input for a given year and day"
  classpath = sourceSets["main"].runtimeClasspath
  mainClass.set("FetchInputKt")
}
