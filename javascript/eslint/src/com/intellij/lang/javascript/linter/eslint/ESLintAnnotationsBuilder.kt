package com.intellij.lang.javascript.linter.eslint

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult
import com.intellij.lang.javascript.linter.JSLinterAnnotationsBuilder
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation
import com.intellij.lang.javascript.linter.JSLinterInspection
import com.intellij.lang.javascript.linter.JSLinterStandardFixes
import com.intellij.lang.javascript.linter.UntypedJSLinterConfigurable
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls

class ESLintAnnotationsBuilder(
  private val file: PsiFile,
  annotationResult: JSLinterAnnotationResult,
  holder: AnnotationHolder,
  configurable: UntypedJSLinterConfigurable,
  errorPrefix: @Nls String,
  inspectionClass: Class<out JSLinterInspection>,
  fixes: JSLinterStandardFixes,
) : JSLinterAnnotationsBuilder(file, annotationResult, holder, configurable, errorPrefix, inspectionClass, fixes) {
  override fun applyFileLevelAnnotation(annotation: JSLinterFileLevelAnnotation?) {
    EslintLanguageServiceManager.getInstance(file.getProject()).applyFileLevelAnnotation(file, annotation)
  }
}