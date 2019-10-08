// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.tslint

import com.intellij.lang.javascript.linter.tslint.config.TsLintConfigDetector
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil.configGraphIncludesFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFileBase
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.lang.Angular2LangUtil

class TsLintConfigAngularDetector : TsLintConfigDetector {

  override fun detectConfigs(project: Project, fileToBeLinted: VirtualFile): TsLintConfigDetector.TsLintConfigs? {
    if (!Angular2LangUtil.isAngular2Context(project, fileToBeLinted))
      return null
    var file: VirtualFile? = fileToBeLinted
    while (file is LightVirtualFileBase) {
      file = file.originalFile
    }
    return AngularConfigProvider.getAngularProject(project, file ?: return null)
      ?.tsLintConfigurations
      ?.map { Pair(it, it.getTsLintConfig(file)) }
      ?.firstOrNull { it.second != null }
      ?.let {
        TsLintConfigDetector.TsLintConfigs(
          it.second!!,
          it.first.tsConfigs.find { tsConfig -> configGraphIncludesFile(project, file, tsConfig) })
      }
  }
}
