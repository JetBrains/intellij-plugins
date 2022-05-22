package com.jetbrains.lang.makefile

import com.intellij.openapi.application.PathManager
import java.io.File

/**
 * Test data directory
 */
internal val BASE_TEST_DATA_PATH: String = findTestDataPath()

private fun findTestDataPath(): String {
    val f = File("testData")
    return if (f.exists()) {
        f.absolutePath
    } else PathManager.getHomePath() + "/contrib/makefile/testData"
}
