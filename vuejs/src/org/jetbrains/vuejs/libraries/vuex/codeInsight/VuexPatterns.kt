// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight

import com.intellij.lang.javascript.patterns.JSElementPattern
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import org.jetbrains.annotations.NotNull
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ACTION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.CREATE_NAMESPACED_HELPERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTER_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MUTATION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.PROP_TYPE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE_DEC
import kotlin.reflect.KClass

object VuexPatterns {
  private val CALL_WITH_PLAIN_ARG: PsiElementPattern.Capture<JSCallExpression> =
    PlatformPatterns.psiElement(JSCallExpression::class.java)
      .withFirstChild(JSPatterns.jsReferenceExpression()
                        .withReferenceNames(MAP_GETTERS, MAP_STATE, MAP_ACTIONS, MAP_MUTATIONS, COMMIT, DISPATCH,
                                            CREATE_NAMESPACED_HELPERS))

  private val MAPPER_CALL: PsiElementPattern.Capture<JSCallExpression> =
    PlatformPatterns.psiElement(JSCallExpression::class.java)
      .withFirstChild(JSPatterns.jsReferenceExpression().withReferenceNames(MAP_GETTERS, MAP_STATE, MAP_ACTIONS, MAP_MUTATIONS))

  private val DECORATOR_CALL: @NotNull PsiElementPattern.Capture<JSCallExpression> =
    PlatformPatterns.psiElement(JSCallExpression::class.java)
      .withFirstChild(JSPatterns.jsReferenceExpression().withReferenceNames(GETTER_DEC, STATE_DEC, ACTION_DEC, MUTATION_DEC))
      .withParent(PlatformPatterns.psiElement(ES6Decorator::class.java))

  fun <T : JSElement> vuexCallArgumentPattern(elementClass: KClass<T>): PsiElementPattern.Capture<T> =
    PlatformPatterns.psiElement(elementClass.java)
      .withAncestor(2, CALL_WITH_PLAIN_ARG)

  fun <T : JSElement> vuexDecoratorArgumentPattern(elementClass: KClass<T>): PsiElementPattern.Capture<T> =
    PlatformPatterns.psiElement(elementClass.java)
      .withAncestor(2, DECORATOR_CALL)

  fun <T : JSElement> vuexArrayItemPattern(elementClass: KClass<T>): PsiElementPattern.Capture<T> =
    PlatformPatterns.psiElement(elementClass.java)
      .withParent(JSArrayLiteralExpression::class.java)
      .withAncestor(3, MAPPER_CALL)

  fun <T : JSElement> vuexObjectPropertyValuePattern(elementClass: KClass<T>): PsiElementPattern.Capture<T> =
    PlatformPatterns.psiElement(elementClass.java)
      .withParent(JSProperty::class.java)
      .withAncestor(4, MAPPER_CALL)

  fun <T : JSElement> vuexDispatchCommitObjectArgPattern(elementClass: KClass<T>): PsiElementPattern.Capture<T> =
    PlatformPatterns.psiElement(elementClass.java)
      .withParent(JSPatterns.jsProperty().withName(PROP_TYPE))
      .withAncestor(4, PlatformPatterns.psiElement(JSCallExpression::class.java)
        .withFirstChild(JSPatterns.jsReferenceExpression().withReferenceNames(COMMIT, DISPATCH)))

  val VUEX_INDEXED_ACCESS_LITERAL: JSElementPattern.Capture<JSLiteralExpression> =
    JSPatterns.jsLiteralExpression()
      .withParent(PlatformPatterns.psiElement(JSIndexedPropertyAccessExpression::class.java)
                    .withFirstChild(JSPatterns.jsReferenceExpression().withReferenceNames(GETTERS, ROOT_GETTERS, STATE, ROOT_STATE)))
}
