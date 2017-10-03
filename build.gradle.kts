import org.gradle.jvm.tasks.Jar

plugins {
  kotlin("jvm")
  application
}

group = "safarmer"
version = "1.0-SNAPSHOT"

application {
  applicationName = "ImageOrganiser"
  mainClassName = "au.com.nullpointer.images.organise.MainKt"
}

repositories {
  mavenCentral()
  jcenter()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  compile(kotlin("stdlib"))
  compile("com.drewnoakes:metadata-extractor:2.10.1")
}


val fatJar = task("fatJar", type = Jar::class) {
  baseName = "${project.name}-fat"
  manifest {
    attributes["Implementation-Title"] = "ImageOrganiser"
    attributes["Implementation-Version"] = version
    attributes["Main-Class"] = "au.com.nullpointer.images.organise.MainKt"
  }
  from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
  with(tasks["jar"] as CopySpec)
}

tasks {
  "build" {
    dependsOn(fatJar)
  }
}