// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.idea.IJIgnore
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexAction
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModelManager
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModule
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexNamedSymbol
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStore
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreContext
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VuexTestStructure {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/vuex/structure")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testNuxtJs(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.NuxtJs)
    myFixture.configureFromTempProjectFile("store/pages/index.vue")
    doTestStructure(false)
  }

  @Test
  fun testNuxtJs2(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.NuxtJs)
    myFixture.configureByText("foo.vue", "<script>export default{}</script>")
    assertNull(VuexModelManager.getVuexStoreContext(myFixture.file))
  }

  @Test
  fun testStorefront(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.Storefront)
    doTestStructure()
  }

  @Test
  fun testShoppingCart(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.ShoppingCart)
    doTestStructure()
  }

  @Test
  fun testCounterHot(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.CounterHot)
    doTestStructure()
  }

  @Test
  fun testSimpleStore(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.SimpleStore)
    doTestStructure()
  }

  @IJIgnore(issue = "AT-3959")
  @Test
  fun testFunctionInit(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.FunctionInit)
    doTestStructure()
  }

  @Test
  fun testComics(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.Comics)
    doTestStructure()
  }

  @Test
  fun testCompositionCounter(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.CompositionCounter)
    doTestStructure()
  }

  @Test
  fun testCompositionShoppingCart(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.CompositionShoppingCart)
    doTestStructure()
  }

  @Test
  fun testWithRecursiveProp(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureStore(VuexTestStore.RecursiveStore)
    doTestStructure()
  }

  private fun doTestStructure(createFile: Boolean = true) {
    if (createFile) {
      myFixture.configureByText("foo.vue", "<script>export default{}</script>")
    }
    val context = VuexModelManager.getVuexStoreContext(myFixture.file)!!
    myFixture.configureByText("check.txt", printContext(context))
    myFixture.checkResultByFile(hybrid.getTestName(false) + ".txt")
  }

  private fun printContext(context: VuexStoreContext): String {
    val result = StringBuilder()
    result.append("VuexStoreContext: {\n")
    if (context.rootStores.isNotEmpty()) {
      result.append("  stores: [\n")
      context.rootStores.forEach {
        printContainer(result, it, 4)
      }
      result.append("  ],\n")
    }
    if (context.registeredModules.isNotEmpty()) {
      result.append("  registeredModules: [\n")
      context.registeredModules.forEach {
        printContainer(result, it, 4)
      }
      result.append("  ]\n")
    }
    return result.append("}\n").toString()
  }

  private fun printContainer(result: StringBuilder, container: VuexContainer, indent: Int) {
    printIndent(result, indent)
    if (container is VuexStore) {
      result.append(container.javaClass.simpleName).append(" ")
    }
    else if (container is VuexModule) {
      result.append(container.javaClass.simpleName).append(" ")
        .append(container.name)
        .append(if (container.isNamespaced) " [namespaced] " else " [flat] ")
    }
    printSource(result, container.source)
    result.append(" {\n")

    printMembers(result, "state", container.state, indent + 2)
    printMembers(result, "getters", container.getters, indent + 2)
    printMembers(result, "actions", container.actions, indent + 2)
    printMembers(result, "mutations", container.mutations, indent + 2)

    if (container.modules.isNotEmpty()) {
      printIndent(result, indent + 2)
      result.append("modules: [\n")
      container.modules.forEach {
        printContainer(result, it.value, indent + 4)
      }
      printIndent(result, indent + 2)
      result.append("]\n")
    }
    printIndent(result, indent)
    result.append("}\n")
  }

  private fun printMembers(result: StringBuilder, name: String, members: Map<String, VuexNamedSymbol>, indent: Int) {
    if (members.isEmpty()) return
    printIndent(result, indent)
    result.append(name).append(": {\n")
    members.forEach { (name, value) ->
      printIndent(result, indent + 2)
      result.append(name).append(": ")
      printSource(result, value.source)
      if (value is VuexAction && value.isRoot) {
        result.append(" [root]")
      }
      result.append(",\n")
    }
    printIndent(result, indent)
    result.append("}\n")
  }

  private fun printIndent(result: StringBuilder, tabs: Int) {
    for (i in 1..tabs) {
      result.append(" ")
    }
  }

  private fun printSource(result: StringBuilder, element: PsiElement) {
    result.append("<")
    if (element !is PsiFileSystemItem)
      result.append(element.containingFile?.parent?.name)
        .append("/").append(element.containingFile?.name)
        .append(":").append(element.textOffset)
        .append(":")

    result
      .append(element.toString().replace('\\', '/'))
      .append(">")
  }

}