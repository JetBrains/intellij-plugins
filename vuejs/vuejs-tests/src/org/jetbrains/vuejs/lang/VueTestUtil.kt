package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.completion.BaseCompletionService
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSPatternBasedCompletionContributor
import com.intellij.lang.javascript.completion.ml.JSMLTrackingCompletionProvider
import com.intellij.lang.javascript.refactoring.JSRefactoringSettings
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.webSymbols.testFramework.LookupElementInfo
import org.jetbrains.vuejs.codeInsight.VueCompletionContributor
import java.io.File

private const val VUE_TEST_DATA_PATH = "/vuejs/vuejs-tests/testData"

fun getVueTestDataPath(): String =
  getContribPath() + VUE_TEST_DATA_PATH

fun vueRelativeTestDataPath(): String = "/contrib$VUE_TEST_DATA_PATH"

val filterOutMostOfGlobalJSSymbolsInVue: (item: LookupElementInfo) -> Boolean = { info ->
  info.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
  || info.lookupElement.getUserData(BaseCompletionService.LOOKUP_ELEMENT_CONTRIBUTOR).let {
    it !is VueCompletionContributor && it !is JSCompletionContributor && it !is JSPatternBasedCompletionContributor
  }
  || info.lookupString.startsWith("A")
}

private val commonJsProperties = setOf("constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString",
                                       "toString", "valueOf")

val filterOutJsKeywordsGlobalObjectsAndCommonProperties: (item: LookupElementInfo) -> Boolean = { info ->
  (info.priority > JSLookupPriority.MAX_PRIORITY.priorityValue
   && (info.priority.toInt() != JSLookupPriority.NESTING_LEVEL_REST.priorityValue
       || info.lookupString !in commonJsProperties))
  || info.lookupElement.getUserData(JSMLTrackingCompletionProvider.JS_PROVIDER_KEY) == null
}

val filterOutDollarPrefixedProperties: (item: LookupElementInfo) -> Boolean = { info ->
  !info.lookupString.startsWith("$")
}

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}

internal fun withRenameUsages(isEnabled: Boolean, action: () -> Unit) {
  val settings = JSRefactoringSettings.getInstance()
  val before = settings.RENAME_SEARCH_FOR_COMPONENT_USAGES
  settings.RENAME_SEARCH_FOR_COMPONENT_USAGES = isEnabled

  try {
    action()
  }
  finally {
    settings.RENAME_SEARCH_FOR_COMPONENT_USAGES = before
  }
}