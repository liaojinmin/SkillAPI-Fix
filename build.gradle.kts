import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.tabooproject.org/repository/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://jitpack.io")
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public/")
    }
}

apply<JavaPlugin>()
apply(plugin = "org.jetbrains.kotlin.jvm")


java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}



tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<ShadowJar> {
     //   this.archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
        archiveClassifier.set("")
        exclude("META-INF/maven/**")
        exclude("META-INF/tf/**")
        exclude("module-info.java")
        // hikari
        relocate("com.zaxxer.hikari", "com.zaxxer.hikari_4_0_3_skillapi")
    }
    build {
        dependsOn(shadowJar)
    }
}


dependencies {
    compileOnly(kotlin("stdlib"))

    implementation("com.zaxxer:HikariCP:4.0.3") {
        exclude("org.slf4j", "slf4j-api")
    }

    implementation("com.alibaba.fastjson2:fastjson2:2.0.31")

    compileOnly("me.neon.libs:NeonLibs:1.0.1.5-local")
    compileOnly("me.neon.flash:NeonFlash:1.0.1-SNAPSHOT-local")
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-1")

    compileOnly(kotlin("stdlib"))

    // Libraries
    compileOnly(fileTree("lib"))

}

