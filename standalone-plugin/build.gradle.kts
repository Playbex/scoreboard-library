plugins {
  id("net.megavex.scoreboardlibrary.java-conventions")
  id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
  id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {
  api(project(":implementation"))

  implementation(project(":v1_8_R3"))
  implementation(project(":v1_19_R1"))
}

bukkit {
  name = "ScoreboardLibrary"
  main = "net.megavex.scoreboardlibrary.plugin.ScoreboardLibraryPlugin"
  apiVersion = "1.19"
  description = "https://github.com/MegavexNetwork/scoreboard-library"
  author = "VytskaLT"
}

tasks {
  build {
    dependsOn(shadowJar)
  }
}
