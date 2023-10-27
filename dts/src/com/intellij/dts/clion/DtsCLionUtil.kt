package com.intellij.dts.clion

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface DtsCLionUtil {
    companion object {
        val EP_NAME = ExtensionPointName.create<DtsCLionUtil>("com.intellij.clion.dtsUtil")
    }

    fun isCMakeAvailable(project: Project): Boolean
}