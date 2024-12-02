// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.TestDataPath
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.WebSymbol
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies

@TestDataPath("\$R#_COMPLETION_TEST_ROOT/Angular2")
class Angular2CodeCompletionTest : Angular2ReSharperCompletionTestBase() {
  override fun shouldSkipItem(element: LookupElement): Boolean {
    if (element.getLookupString().startsWith("[(")) {
      return true
    }
    if (element.getLookupString().startsWith("ion-")) {
      return false
    }
    if (HIGH_PRIORITY_ONLY.contains(name)) {
      return (element !is PrioritizedLookupElement<*>
              || element.priority < WebSymbol.Priority.HIGH.value)
    }
    if (CAMEL_CASE_MATCH_ONLY.contains(name)) {
      val el = myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1)
      val prefix = el!!.getText()
      if (CamelHumpMatcher(prefix).matchingDegree(element.getLookupString()) < 800) {
        return true
      }
    }
    return super.shouldSkipItem(element)
  }

  override fun isExcluded(): Boolean {
    return TESTS_TO_SKIP.contains(name)
  }

  override fun doSingleTest(testFile: String, path: String) {
    if (name.startsWith("external")) {
      myFixture.configureDependencies(
        Angular2TestModule.ANGULAR_COMMON_4_0_0, Angular2TestModule.ANGULAR_CORE_4_0_0,
        Angular2TestModule.ANGULAR_PLATFORM_BROWSER_4_0_0, Angular2TestModule.ANGULAR_ROUTER_4_0_0, Angular2TestModule.ANGULAR_FORMS_4_0_0,
        Angular2TestModule.IONIC_ANGULAR_3_0_1)
    }
    else {
      myFixture.configureDependencies()
    }
    super.doSingleTest(testFile, path)
  }

  override fun doGetExtraFiles(): List<String> {
    val extraFiles = super.doGetExtraFiles()
    if (name.startsWith("external")) {
      return extraFiles + "external/module.ts"
    }
    return extraFiles
  }

  companion object {
    private val TESTS_TO_SKIP: Set<String> = ContainerUtil.newHashSet(
      "test004",  // improve ngFor completions
      "external/test004"
    )
    private val HIGH_PRIORITY_ONLY: Set<String> = ContainerUtil.newHashSet(
      "external/test003"
    )
    private val CAMEL_CASE_MATCH_ONLY: Set<String> = ContainerUtil.newHashSet(
      "external/test002",
      "external/test004",
      "external/test006"
    )
  }
}
