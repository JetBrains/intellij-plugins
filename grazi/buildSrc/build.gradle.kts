repositories {
    jcenter()
}

plugins {
    id("tanvd.kosogor") version "1.0.4" apply true
    `kotlin-dsl` apply true
}


dependencies {
    compileOnly(gradleApi())
}

