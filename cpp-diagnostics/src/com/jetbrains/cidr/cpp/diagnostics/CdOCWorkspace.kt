package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.lang.workspace.OCWorkspace

/**
 * OCWorkspace holds information about C/C++ files known to a build system - compiler flags, include paths, preprocessor defines, etc -
 * but abstracts the specific build system away.
 */
fun collectOCWorkspace(project: Project): String {
  val ocWorkspace = OCWorkspace.getInstance(project)
  val log = CdIndenter()
  val configurations = ocWorkspace.configurations
  log.put("Resolve configurations: ", configurations.size)
  log.scope {
    // "resolve configuration" is a set of files with common compiler flags (i.e. coming from the same CMake target)
    for (config in configurations) {
      val sources = config.sources
      // `variant` can be used to figure out from what "build type" (aka "profile" for CMake) this file is coming from (i.e. Debug/Release)
      log.put("Configuration: ${config.displayName} (${config.name}: ${config.variant}), ${sources.size} source file(s)")
      for (source in sources) {
        log.scope {
          val kind = config.getDeclaredLanguageKind(source)
          log.put(source, " ", "[${kind ?: "UNKNOWN"}]")
          if (kind != null) {
            log.scope {
              // if you pass `kind` other than declared in the build system (i.e. `CPP` for `C` file),
              // language-default setting would be returned.
              // todo: this is handy in some cases, but not exactly straightforward.
              val settings = config.getCompilerSettings(kind, source)
              val compilerPath = settings.compilerExecutable?.absolutePath ?: "UNKNOWN"

              // note that input and output files are stripped, and not stored. Otherwise this should match compiler flags
              // provided by the build system.
              // Note that `CidrCompilerSwitches.toString` uses Format.BASH_SHELL for convenience, the escaping of the switches
              // might differ depending on the toolchain.
              log.put(compilerPath, " ", settings.compilerSwitches)

              // `settings` also contain other information, both declared in the build system, or calculated by CLion
              // (effective preprocessor defines, integer type sizes, enabled compiler features, etc)
            }
          }
        }
      }
    }
  }
  return log.result
}
