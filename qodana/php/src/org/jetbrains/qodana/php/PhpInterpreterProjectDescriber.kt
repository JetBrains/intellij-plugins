// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpBundle
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.php.config.interpreters.PhpInterpreter
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class PhpInterpreterProjectDescriber : QodanaProjectDescriber {
  override val id: String = PhpBundle.message("PhpInterpreter.php.sdk.type")

  override suspend fun description(project: Project): InterpreterDescription {
    val instance = PhpProjectConfigurationFacade.getInstance(project)
    return InterpreterDescription(instance.interpreter, instance.languageLevel)
  }

  @Suppress("unused")
  class InterpreterDescription(interpreter: PhpInterpreter?, level: PhpLanguageLevel) {
    val isRemote: Boolean? = interpreter?.isRemote
    val isProjectLevel: Boolean? = interpreter?.isProjectLevel
    val pathToPhpExecutable: String? = interpreter?.pathToPhpExecutable
    val homePath: String? = interpreter?.homePath
    val level: String = level.presentableName
  }
}