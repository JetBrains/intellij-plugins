// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.resolve

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.impl.GrLiteralClassType

class CustomWorldType(val stepFile: GroovyFile) : GrLiteralClassType(LanguageLevel.HIGHEST, stepFile) {

  override fun isValid(): Boolean = stepFile.isValid
  override fun getJavaClassName(): String = "cucumber.runtime.groovy.GroovyWorld"
  override fun getParameters(): Array<PsiType> = PsiType.EMPTY_ARRAY
  override fun setLanguageLevel(languageLevel: LanguageLevel): PsiClassType = error("unsupported")
}
