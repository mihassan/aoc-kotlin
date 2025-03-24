plugins {
  kotlin("jvm") version "2.1.20"
  application
}

repositories {
  mavenCentral()
}

sourceSets.main {
  java.srcDirs("src")
}

application {
  mainClass = "MainKt"
}
