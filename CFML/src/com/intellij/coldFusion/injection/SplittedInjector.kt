package com.intellij.coldFusion.injection

import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement

/**
 * @author Sergey Karashevich
 */
interface SplittedInjector {
  // returns true if injected
  fun getLanguagesToInject(registrar: MultiHostRegistrar, vararg splittedElements: PsiElement): Boolean
}