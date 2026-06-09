import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    register("reloadPlugin") {
        group = "minecraft"
        description = "Build + copie le jar + reload via RCON (serveur doit tourner)"
        dependsOn("jar")
        doLast {
            val jarName = "${project.name}-${project.version}.jar"
            val src = file("build/libs/$jarName")
            val dest = file("run/plugins/${project.name}.jar")
            src.copyTo(dest, overwrite = true)
            println("Jar copie : ${dest.absolutePath}")
            rconCommand("reload confirm")
        }
    }
}

fun rconCommand(command: String) {
    val password = "devpassword"
    try {
        Socket("localhost", 25575).use { socket ->
            socket.soTimeout = 3000
            val out = socket.getOutputStream()
            val inp = socket.getInputStream()

            fun packet(id: Int, type: Int, body: String): ByteArray {
                val b = body.toByteArray(Charsets.UTF_8)
                val buf = ByteBuffer.allocate(14 + b.size).order(ByteOrder.LITTLE_ENDIAN)
                buf.putInt(10 + b.size)
                buf.putInt(id)
                buf.putInt(type)
                buf.put(b)
                buf.put(0); buf.put(0)
                return buf.array()
            }

            fun read() {
                val len = ByteBuffer.wrap(inp.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
                inp.readNBytes(len)
            }

            out.write(packet(1, 3, password)); out.flush(); read()
            out.write(packet(2, 2, command)); out.flush(); read()
            println("RCON : '$command' envoye avec succes.")
        }
    } catch (e: Exception) {
        println("RCON echoue (serveur demarre ?) : ${e.message}")
    }
}
