package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.completion.FusCompletionKeys
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSPatternBasedCompletionContributor
import com.intellij.lang.javascript.refactoring.JSRefactoringSettings
import com.intellij.polySymbols.testFramework.LookupElementInfo
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import org.jetbrains.vuejs.codeInsight.VueCompletionContributor
import java.io.File

private const val VUE_TEST_DATA_PATH = "/vuejs/vuejs-tests/testData"

fun getVueTestDataPath(): String =
  getContribPath() + VUE_TEST_DATA_PATH

fun vueRelativeTestDataPath(): String = "/contrib$VUE_TEST_DATA_PATH"

val filterOutAriaAttributes: (LookupElementInfo) -> Boolean = { !it.lookupString.contains("aria-") }

/**
 * `true` for items contributed by the generic JavaScript completion providers - keywords, global symbols,
 * object members, text references in string literals, and so on.
 */
internal val LookupElementInfo.isGenericJsItem: Boolean
  get() = lookupElement.getUserData(FusCompletionKeys.LOOKUP_ELEMENT_CONTRIBUTOR) is JSPatternBasedCompletionContributor

val filterOutMostOfGlobalJSSymbolsInVue: (item: LookupElementInfo) -> Boolean = { info ->
  info.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
  || info.lookupElement.getUserData(FusCompletionKeys.LOOKUP_ELEMENT_CONTRIBUTOR).let {
    it !is VueCompletionContributor && it !is JSCompletionContributor && it !is JSPatternBasedCompletionContributor
  }
  || info.lookupString.startsWith("A")
}

/** Members of `Object.prototype`, which are suggested for any JS/TS object. */
internal val commonJsProperties = setOf("constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString",
                                        "toString", "valueOf")

val filterOutJsKeywordsGlobalObjectsAndCommonProperties: (item: LookupElementInfo) -> Boolean = { info ->
  (info.priority > JSLookupPriority.MAX_PRIORITY.priorityValue
   && (info.priority.toInt() != JSLookupPriority.NESTING_LEVEL_REST.priorityValue
       || info.lookupString !in commonJsProperties))
  || !info.isGenericJsItem
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