plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.4.0"
}

repositories {
    mavenCentral()
}

group = "intellij.clion.embedded.platformio"
version = "1.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("CL-LATEST-EAP-SNAPSHOT")
    downloadSources.set(true)
    plugins.addAll("clion-embedded", "clion", "terminal")
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("test")
        resources.srcDirs("test-resources")
    }
}

tasks.patchPluginXml {
  changeNotes.set("""
<ul>
 <li>Custom PlatformIO utility location supported via Settings</li>
 <li>platformio.ini highlighting</li>
 <li>All strings are externalized</li>
 <li>Profiles and run configurations are created automatically when a new project is created</li>
 <li>UI changes and fixes</li>
 <li>The plugin now depends on ini4idea</li>
</ul>""")
}

