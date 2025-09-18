package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.diagnostics.model.OCWorkspaceSection
import com.jetbrains.cidr.cpp.diagnostics.model.ResolveConfigurationInfo
import com.jetbrains.cidr.cpp.diagnostics.model.SourceInfo
import com.jetbrains.cidr.lang.workspace.OCWorkspace

/**
 * OCWorkspace holds information about C/C++ files known to a build system - compiler flags, include paths, preprocessor defines, etc -
 * but abstracts the specific build system away.
 */
fun collectOCWorkspace(project: Project): OCWorkspaceSection {
  val ocWorkspace = OCWorkspace.getInstance(project)
  // "resolve configuration" is a set of files with common compiler flags (i.e. coming from the same CMake target)
  val configurations = ocWorkspace.configurations.map { config ->
    // `variant` can be used to figure out from what "build type" (aka "profile" for CMake) this file is coming from (i.e. Debug/Release)
    val sources = config.sources.map { source ->
      val kind = config.getDeclaredLanguageKind(source)
      var compilerPath: String? = null
      var switches: String? = null
      if (kind != null) {
        // if you pass `kind` other than declared in the build system (i.e. `CPP` for `C` file),
        // language-default setting would be returned.
        // todo: this is handy in some cases, but not exactly straightforward.
        val settings = config.getCompilerSettings(kind, source)
        compilerPath = settings.compilerExecutable?.absolutePath
        // note that input and output files are stripped, and not stored. Otherwise this should match compiler flags
        // provided by the build system.
        // Note that `CidrCompilerSwitches.toString` uses Format.BASH_SHELL for convenience, the escaping of the switches
        // might differ depending on the toolchain.
        switches = settings.compilerSwitches.toString()
        // `settings` also contain other information, both declared in the build system, or calculated by CLion
        // (effective preprocessor defines, integer type sizes, enabled compiler features, etc)
      }
      SourceInfo(source.toString(), kind?.toString(), compilerPath, switches)
    }
    ResolveConfigurationInfo(
      displayName = config.displayName,
      name = config.name,
      variant = config.variant?.toString(),
      sources = sources
    )
  }
  return OCWorkspaceSection(configurations)
}
