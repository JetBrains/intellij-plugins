// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.vuex.model.store.*

class VuexTestStructure : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuex/structure"

  fun testNuxtJs() {
    myFixture.configureStore(VuexTestStore.NuxtJs)
    myFixture.configureFromTempProjectFile("store/pages/index.vue")
    doTestStructure()
  }

  fun testNuxtJs2() {
    myFixture.configureStore(VuexTestStore.NuxtJs)
    myFixture.configureByText("foo.vue", "<script>export default{}</script>")
    TestCase.assertNull(VuexModelManager.getVuexStoreContext(myFixture.file))
  }

  fun testStorefront() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doTestStructure(true)
  }

  fun testShoppingCart() {
    myFixture.configureStore(VuexTestStore.ShoppingCart)
    doTestStructure(true)
  }

  fun testCounterHot() {
    myFixture.configureStore(VuexTestStore.CounterHot)
    doTestStructure(true)
  }

  fun testSimpleStore() {
    myFixture.configureStore(VuexTestStore.SimpleStore)
    doTestStructure(true)
  }

  fun testFunctionInit() {
    myFixture.configureStore(VuexTestStore.FunctionInit)
    doTestStructure(true)
  }

  fun testCompositionCounter() {
    myFixture.configureStore(VuexTestStore.CompositionCounter)
    doTestStructure(true)
  }

  fun testCompositionShoppingCart() {
    myFixture.configureStore(VuexTestStore.CompositionShoppingCart)
    doTestStructure(true)
  }

  private fun doTestStructure(createFile: Boolean = false) {
    if (createFile) {
      myFixture.configureByText("foo.vue", "<script>export default{}</script>")
    }
    val context = VuexModelManager.getVuexStoreContext(myFixture.file)!!
    myFixture.configureByText("check.txt", printContext(context))
    myFixture.checkResultByFile(getTestName(false) + ".txt")
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