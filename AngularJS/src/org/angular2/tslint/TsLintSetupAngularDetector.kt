// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.tslint

import com.intellij.lang.javascript.linter.tslint.config.TsLintSetupDetector
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService.Provider.parseConfigFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFileBase
import org.angular2.cli.load
import org.angular2.lang.Angular2LangUtil

class TsLintSetupAngularDetector : TsLintSetupDetector {

  override fun detectSetup(project: Project, fileToBeLinted: VirtualFile): TsLintSetupDetector.TsLintSetup? {
    if (!Angular2LangUtil.isAngular2Context(project, fileToBeLinted))
      return null
    var file: VirtualFile? = fileToBeLinted
    while (file is LightVirtualFileBase) {
      file = file.originalFile
    }
    return load(project, file ?: return null)
      .getTsLintConfigurations()
      .find { it.getTsLintConfig() != null && it.accept(file) }
      ?.let {
        TsLintSetupDetector.builder(it.getTsLintConfig()!!)
          .setFormat(it.format)
          .setTsConfig(it.getTsConfigs().find { tsConfig ->
            parseConfigFile(project, tsConfig).include
              .accept(file)
          })
          .build()
      }
  }
}
