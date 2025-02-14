// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution

import com.intellij.javascript.testFramework.AbstractTestFileStructure
import com.intellij.javascript.testFramework.JsTestSelector
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructure
import com.intellij.javascript.testFramework.interfaces.mochaTdd.MochaTddFileStructureBuilder
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder
import com.intellij.javascript.testFramework.jasmine.JasmineSpecStructure
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder
import com.intellij.javascript.testFramework.util.JSTestNamePattern
import com.intellij.javascript.testing.JS_TEST_NAMES_INDEX_DELIMITER
import com.intellij.javascript.testing.detection.*
import com.intellij.javascript.testing.findTestFilesByTestSelector
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class KarmaDetector(): JsTestFrameworkDetector {
  override val frameworkName: String = "Karma"
  override val frameworkApiDesign: JsTestFrameworkApiDesign = JsTestFrameworkApiDesign.GLOBAL_VARIABLES

  private val indexDataCollector: CachingJsTestFileIndexDataCollector = CachingJsTestFileIndexDataCollector(this) { jsFile ->
    val names = mutableSetOf<String>()

    // collect all names from all structures for following old logic
    processAnyKarmaStructure(
      jsFile,
      { names.addAll(it.collectElementPaths(JS_TEST_NAMES_INDEX_DELIMITER)) },
      { names.addAll(it.collectElementPaths(JS_TEST_NAMES_INDEX_DELIMITER)) },
      { names.addAll(it.collectElementPaths(JS_TEST_NAMES_INDEX_DELIMITER)) },
    )

    if (names.isNotEmpty()) JsTestFileIndexData(names, true) else emptyJsTestFileIndexData()
  }

  override fun findFileBasedIndexData(jsFile: JSFile): JsTestFileIndexData {
    return indexDataCollector.getIndexData(jsFile)
  }

  override fun checkIsProbablyTestFile(jsFile: JSFile): Boolean {
    return indexDataCollector.getIndexData(jsFile).hasData
  }

  override fun findTestsStructure(jsFile: JSFile): AbstractTestFileStructure? {
    return processAnyKarmaStructure(jsFile, { it }, { it }, { it })
  }

  override fun findTestFilesInIndexesBySelector(project: Project, testSelector: JsTestSelector): List<VirtualFile> {
    return findTestFilesByTestSelector(project, testSelector, this)
  }

  private fun<T> processAnyKarmaStructure(
    jsFile: JSFile,
    handleJasmineStructure: (JasmineFileStructure) -> T,
    handleMochaTddStructure: (MochaTddFileStructure) -> T,
    handleQUnitStructure: (QUnitFileStructure) -> T,
  ): T? {
    // use this order for processing more specific structures at start
    val qUnitFileStructure = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
    if (!qUnitFileStructure.isEmpty) return handleQUnitStructure(qUnitFileStructure)

    val mochaTddStructure = MochaTddFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
    if (!mochaTddStructure.isEmpty) return handleMochaTddStructure(mochaTddStructure)

    val jasmineStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
    if (!jasmineStructure.isEmpty) return handleJasmineStructure(jasmineStructure)

    return null
  }

  fun findAllFileTestPatterns(jsFile: JSFile): List<List<JSTestNamePattern>> {
    val allTestsPatterns = collectJasmineTests(jsFile)
    if (!allTestsPatterns.isEmpty()) {
      return allTestsPatterns
    }

    val mochaAllTestsPatterns: MutableList<List<JSTestNamePattern>> = mutableListOf()
    val mochaTdd = MochaTddFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
    mochaTdd.forEachTest { test ->
      mochaAllTestsPatterns.add(test.testTreePathPatterns)
    }
    if (!mochaAllTestsPatterns.isEmpty()) {
      return mochaAllTestsPatterns
    }

    val qUnitAllTestsPatterns: MutableList<List<JSTestNamePattern>> = mutableListOf()
    val qunit = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
    qunit.forEachTest { test ->
      qUnitAllTestsPatterns.add(
        listOf(
          JSTestNamePattern.literalPattern(test.moduleStructure.name),
          JSTestNamePattern.literalPattern(test.name)
        )
      )
    }
    if (!qUnitAllTestsPatterns.isEmpty()) {
      return qUnitAllTestsPatterns
    }
    return emptyList()
  }

  companion object {
    val instance: KarmaDetector get() = JsTestFrameworkDetectors.getDetectorOrFail(KarmaDetector::class.java)
  }
}

private fun collectJasmineTests(jsFile: JSFile): List<List<JSTestNamePattern>> {
  val jasmineStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
  return jasmineStructure.children.map { element ->
    val patterns = element.getTestTreePathPatterns()
    if (element is JasmineSpecStructure) {
      return@map patterns
    }
    val anyTestPattern = JSTestNamePattern(listOf(JSTestNamePattern.anyRange("match all descendant suites/specs")))
    patterns + listOf(anyTestPattern)
  }
}
