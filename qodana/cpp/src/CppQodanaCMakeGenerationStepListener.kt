package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.logger
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.CMakeRunner.CMakeOutput
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener
import java.util.concurrent.atomic.AtomicReference

class CppQodanaCMakeGenerationStepListener : CMakeWorkspaceListener {
    companion object {
        private val log = logger<CppQodanaCMakeGenerationStepListener>()

        /**
         * Stores the most recent CMake output. Each callback replaces the previous one,
         * so only the final (definitive) output is available for printing.
         *
         * This is the authoritative signal for CMake configuration success/failure.
         * The workspace state ([com.jetbrains.cidr.project.workspace.CidrWorkspaceState]) transitions
         * to `Loaded` regardless of whether CMake succeeded or failed, so workspace state alone cannot
         * detect failures. Check `lastOutput.exitCode` after workspaces are ready.
         *
         * Written by the listener on the CMake background thread, read by [QodanaCppStartupManager] on EDT.
         * Uses [AtomicReference] so that [consumeLastOutput] is an atomic get-and-set.
         */
        private val lastOutputRef = AtomicReference<CMakeOutput?>(null)

        val lastOutput: CMakeOutput? get() = lastOutputRef.get()

        fun consumeLastOutput(): CMakeOutput? = lastOutputRef.getAndSet(null)
    }

    override fun generationCMakeExited(output: CMakeOutput) {
        if (!PlatformUtils.isQodana()) return

        lastOutputRef.set(output)

        val text = output.output.toString().trim()
        if (output.exitCode != 0) {
            log.warn("CMake configure exited with code ${output.exitCode}:\n$text")
        } else {
            log.info("CMake configure completed successfully:\n$text")
        }
    }
}
