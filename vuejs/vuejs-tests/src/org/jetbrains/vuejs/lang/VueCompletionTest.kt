// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.testFramework.web.filterOutStandardHtmlSymbols
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.RecursionManager
import com.intellij.polySymbols.testFramework.and
import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import org.jetbrains.vuejs.VueTestCase

class VueCompletionTest : VueTestCase("completion") {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  fun testCompleteCssClasses() =
    doLookupTest(dir = true)

  fun testCompleteAttributesWithVueInNodeModules() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  fun testCompleteAttributesWithVueInPackageJson() =
    doLookupTest(configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  fun testNoVueCompletionWithoutVue() =
    doLookupTest(configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  fun testCompleteImportedComponent() =
    doLookupTest(dir = true) {
      it.lookupString.startsWith("comp")
    }

  fun testCompleteWithImport() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  fun testCompleteWithImportNoExtension() =
    withTempCodeStyleSettings {
      it.getCustomSettings(JSCodeStyleSettings::class.java).USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.NEVER
      doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }

  fun testCompleteNoImportIfSettingIsOffJs() {
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseJavaScriptAutoImport
    jsApplicationSettings.isUseJavaScriptAutoImport = false
    try {
      doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }
    finally {
      jsApplicationSettings.isUseJavaScriptAutoImport = before
    }
  }

  fun testCompleteNoImportIfSettingIsOffTs() {
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseTypeScriptAutoImport
    jsApplicationSettings.isUseTypeScriptAutoImport = false
    try {
      doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }
    finally {
      jsApplicationSettings.isUseTypeScriptAutoImport = before
    }
  }

  fun testCompleteWithImportCreateExport() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  fun testCompleteWithImportCreateScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  fun testCompleteWithImportCreateScriptNoExport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  fun testCompleteWithoutImportForRenamedGlobalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)


  fun testCompleteWithoutImportForGlobalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  fun testCompleteAttributesFromProps() =
    doLookupTest(dir = true) { it.priority > 10 }

  fun testCompletePropsInInterpolation() {
    doLookupTest(lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)
  }

  fun testCompleteComputedPropsInInterpolation() =
    doLookupTest(lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testCompleteMethodsInBoundAttributes() =
    doLookupTest(dir = true, lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testCompleteElementsFromLocalData() =
    doLookupTest(VueTestModule.VUE_2_5_3, lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testCompleteElementsFromLocalData2() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 10 }

  fun testSrcInStyleCompletion() =
    doLookupTest(dir = true)

  fun testSrcInStyleCompletionWithLang() =
    doLookupTest(dir = true)

  fun testInsertAttributeWithoutValue() =
    doLookupTest(typeToFinishLookup = "")

  fun testInsertAttributeWithValue() =
    doLookupTest(typeToFinishLookup = "")

  fun testMixinsInCompletion() =
    doLookupTest(configureFileName = "CompWithTwoMixins.vue", dir = true) { it.priority > 10 }

  fun testNoNotImportedMixinsInCompletion() =
    doLookupTest(dir = true) { it.priority > 10 }

  fun testNoCompletionInVueAttributes() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest()
    }

  fun testTypeScriptCompletionFromPredefined() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest()
    }

  fun testCustomDirectivesInCompletion() =
    doLookupTest(configureFileName = "CustomDirectives.vue", dir = true) { it.lookupString.startsWith("v-") }

  fun testCustomDirectivesLinkedFilesInCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "CustomDirectives.html", dir = true) { it.lookupString.startsWith("v-") }

  fun testDirectivesFromGlobalDirectives() {
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<main v-<caret>my-click-outside>",
        "<main v-my<caret>-click-outside>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )
  }

  fun testDirectivesWithModifiersFromGlobalDirectives() {
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<main v-my-click-outside.<caret>once>",
        "<main v-my-intersect.<caret>once>",
        "<main v-my-mutate.<caret>once>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )
  }

  fun testPrettyLookup() =
    doLookupTest(renderTailText = true, lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testCompleteVBind() =
    doLookupTest(locations = listOf("<child-comp :<caret>>", "<a v-bind:<caret>>"), lookupItemFilter = filterOutAriaAttributes)

  fun testCompleteVBindUser() =
    doLookupTest(configureFileName = "User.vue", dir = true, lookupItemFilter = filterOutAriaAttributes)

  fun testVueOutObjectLiteral() =
    doLookupTest(VueTestModule.VUE_2_5_3, renderPriority = false, renderTypeText = false)

  fun testVueOutObjectLiteralTs() =
    doLookupTest(VueTestModule.VUE_2_5_3, renderPriority = false, renderTypeText = false)

  fun testVueOutObjectLiteralCompletionJsx() =
    doLookupTest(VueTestModule.VUE_2_5_3, renderPriority = false, renderTypeText = false)

  fun testNoDoubleCompletionForLocalComponent() =
    doLookupTest(dir = true) { it.priority > 1 }

  fun testElementUiCompletion() =
    doLookupTest(VueTestModule.ELEMENT_UI_2_0_5, fileContents = "<template><el-<caret></template>")

  fun testMintUiCompletion() =
    doLookupTest(VueTestModule.MINT_UI_2_2_3, fileContents = "<template><mt-<caret></template>")

  fun testVuetifyCompletion_017() =
    doLookupTest(VueTestModule.VUETIFY_0_17_2, fileContents = "<template><<caret></template>") { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  fun testVuetifyCompletion_137() =
    doLookupTest(VueTestModule.VUETIFY_1_3_7, fileContents = "<template><<caret></template>") { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  fun testVuetifyCompletion_1210() =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, fileContents = "<template><<caret></template>") { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  fun testIviewCompletion() =
    doLookupTest(VueTestModule.IVIEW_2_8_0, fileContents = "<template><a<caret></template>", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testIview3Completion() =
    doLookupTest(VueTestModule.IVIEW_3_5_4, locations = listOf("v-bind:<caret>", "v-on:<caret>"), lookupItemFilter = filterOutAriaAttributes)

  fun testBootstrapVueCompletion() =
    doLookupTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11, fileContents = "<template><<caret></template>", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testShardsVueCompletion() =
    doLookupTest(VueTestModule.SHARDS_VUE_1_0_5, fileContents = "<template><<caret></template>", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testWrongPropsNotInCompletion() =
    doCompletionAutoPopupTest(checkResult = false) {
      completeBasic()
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)

      type(":")
      assertLookupShown()
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)

      type("a")
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)
    }

  fun testBuefyCompletion() =
    doLookupTest(VueTestModule.BUEFY_0_6_2, fileContents = "<template><b-<caret></template>")

  fun testClassComponentCompletion() =
    doLookupTest(fileContents = "<template><<caret></template>", dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testClassComponentCompletionTs() =
    doLookupTest(fileContents = "<template><<caret></template>", dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testComponentInsertionNoScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  fun testComponentInsertionScriptNoImports() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  fun testComponentInsertionScriptVueImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  fun testComponentInsertionScriptVueClassComponentImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  fun testComponentInsertionWithClassDefined() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  fun testTypescriptVForItemCompletion() =
    doLookupTest(lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testTypescriptVForCompletionWebTypes() =
    doLookupTest(VueTestModule.VUE_2_5_3,
                 lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties and filterOutDollarPrefixedProperties)

  fun testLocalComponentsExtendsCompletion() =
    doLookupTest(dir = true) { it.priority > 10 }

  fun testCompletionWithRecursiveMixins() =
    doLookupTest(dir = true, locations = listOf("<<caret>div", "<HiddenComponent fr<caret>/>"), lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testNoImportInsertedForRecursivelyLocalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "")

  fun testCssClassInPug() =
    doLookupTest()

  fun testEventsAfterAt() =
    doCompletionAutoPopupTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11, checkResult = false) {
      type("@")
      assertLookupShown()
      checkLookupItems { item -> item.priority >= 10 || item.lookupString.let { it.startsWith("@a") || it.startsWith("@b") } }

      type("inp")
      checkLookupItems()

      completeBasic()
      checkLookupItems()

      moveToOffsetBySignature("<div @c<caret>")
      completeBasic()
      checkLookupItems()
    }

  fun testEventsAfterVOn() =
    doCompletionAutoPopupTest(checkResult = false) {
      type(":")
      assertLookupShown()

      type("cl")
      checkLookupItems()

      completeBasic()
      checkLookupItems()

      moveToOffsetBySignature("<div v-on<caret>")
      type(":")
      assertLookupShown()

      type("cl")
      checkLookupItems()
    }


  fun testEventModifiers() =
    doLookupTest(locations = listOf(
      // general modifiers only
      "<MyComponent @click123.<caret>",
      // general modifiers (except already used) + key modifiers + system modifiers
      "v-on:keyup.stop.passive.<caret>",
      // general modifiers (except already used) + mouse button modifiers + system modifiers
      "@click.capture.<caret>",
      // general modifiers + system modifiers
      "@drop.<caret>"
    ))

  fun testAutopopupAfterVOnSelection() =
    doCompletionAutoPopupTest {
      completeBasic()
      checkLookupItems()
      type("\n")
      assertLookupShown()
      type("\n")
    }

  fun testStyleAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  fun testTemplateAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  fun testNoVueTagsWithNamespace() =
    doLookupTest()

  fun testVueCompletionInsideScript() =
    doLookupTest(VueTestModule.VUE_2_5_3, lookupItemFilter = filterOutDollarPrefixedProperties)

  fun testVueCompletionInsideScriptLifecycleHooks() =
    doLookupTest(VueTestModule.VUE_2_5_3)

  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  fun testVueCompletionWithExtend() =
    doLookupTest(dir = true) { it.priority > 10 }

  fun testVueNoComponentNameAsStyleSelector() =
    doLookupTest(dir = true, locations = listOf(">\ninp<caret>", "<inp<caret>"))

  fun testCompletionPriorityAndHints() =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, VueTestModule.SHARDS_VUE_1_0_5, dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCompletionPriorityAndHintsBuiltInTags() =
    doLookupTest(VueTestModule.VUE_2_5_3, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testDirectiveCompletionOnComponent() =
    doLookupTest(VueTestModule.VUETIFY_1_3_7, renderPriority = false, renderTypeText = false, containsCheck = true)

  fun testBuiltInTagsAttributeCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority >= 10 && !it.lookupString.startsWith("v-") }

  fun testBindProposalsPriority() =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, VueTestModule.VUE_2_6_10, renderTypeText = false, renderProximity = true) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  fun testBindProposalsStdTag() =
    doLookupTest(VueTestModule.VUE_2_6_10, renderPriority = false, renderTypeText = false, lookupItemFilter = filterOutAriaAttributes)

  private fun doAttributeNamePriorityTest(vueVersion: VueTestModule) =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, vueVersion, renderTypeText = false, renderProximity = true,
                 fileContents = """<template><v-alert <caret><template>""") {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  fun testAttributeNamePriorityVue26() =
    doAttributeNamePriorityTest(VueTestModule.VUE_2_6_10)

  fun testAttributeNamePriorityVue30() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_0_0)

  fun testAttributeNamePriorityVue31() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_1_0)

  fun testAttributeNamePriorityVue32() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_2_2)

  fun testComplexComponentDecorator() =
    doLookupTest(configureFileName = "App.vue", dir = true) { it.priority > 10 }

  fun testComplexComponentDecoratorTs() =
    doLookupTest(configureFileName = "App.vue", dir = true) { it.priority > 10 }

  fun testComplexComponentDecorator8Ts() =
    doLookupTest(configureFileName = "App.vue", dir = true,
                 locations = listOf("<HW <caret>", "<<caret>HW"),
                 lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") })

  fun testDestructuringVariableTypeInVFor() =
    doLookupTest(VueTestModule.VUE_2_5_3)

  fun testWebTypesComplexSetup() {
    myFixture.copyDirectoryToProject("web-types", ".")
    listOf(Triple("root.vue",
                  listOf("root", "sub1", "root-sibling", "root-pkg", "foo1-pkg", "foo1Pkg"),
                  listOf("foo2-pkg", "foo2Pkg", "Foo1Pkg")),

           Triple("sub1/sub1.vue",
                  listOf("sub1", "su1a", "foo1-pkg", "foo2-pkg", "foo2Pkg"),
                  listOf("root", "root-pkg", "root-sibling", "sub2")),

           Triple("sub2/sub2.vue",
                  listOf("sub2", "sub2-pkg", "foo1-pkg", "bar1-pkg"),
                  listOf("root", "root-pkg", "root-sibling", "sub1", "foo2-pkg")))
      .forEach { (fileName, expected, notExpected) ->
        myFixture.configureFromTempProjectFile(fileName)
        myFixture.completeBasic()
        assertContainsElements(myFixture.lookupElementStrings!!, expected)
        assertDoesntContain(myFixture.lookupElementStrings!!, notExpected)
      }
  }

  fun testSlotProps() =
    doLookupTest(renderPriority = true, renderTypeText = false) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  fun testVueDefaultSymbolsWithDefinitions() =
    doLookupTest(VueTestModule.VUE_2_5_3, locations = listOf(
      "v-on:click=\"<caret>\"",
      "v-bind:about=\"<caret>\""
    )) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  fun testVueDefaultSymbols() =
    doLookupTest(locations = listOf(
      "v-on:click=\"<caret>\"",
      "v-bind:about=\"<caret>\""
    )) { info ->
      info.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
      || info.lookupString.startsWith("A") // Check also if we have some global symbols, but don't list all of them
    }

  fun testSlotTag() =
    doLookupTest(VueTestModule.VUE_2_5_3, containsCheck = true, renderPriority = true, renderTypeText = false)

  fun testSlotNames() =
    doLookupTest(
      configureFileName = "test.vue", renderPriority = false, renderTypeText = false, dir = true,
      namedLocations = listOf(
        "script-template-vue", "require-decorators", "x-template", "export-import", "some-lib", "no-script-section"
      ).flatMap { tag ->
        listOf(tag to "$tag><template v-slot:<caret></$tag", tag to "$tag><div slot=\"<caret>\"</$tag")
      })

  fun testFilters() =
    doLookupTest(dir = true, renderTypeText = false, renderPriority = false)

  fun testComplexThisContext() =
    doLookupTest(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_5_3,
                 renderTypeText = false, renderPriority = false, dir = true)

  fun testTopLevelTagsNoI18n() =
    doLookupTest(VueTestModule.VUE_2_5_3, fileContents = "<<caret>", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testTopLevelTags() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true, locations = listOf(
      "<<caret>i18n",
      "<i18n <caret>a",
      "<template <caret>a",
      "<script <caret>a",
      "<style <caret>a",
    ), lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") })

  fun testScriptLangAttributeWithAlreadyPresentCode() =
    doLookupTest(typeToFinishLookup = "\t") { it.priority > 0 }

  fun testComputedTypeTS() =
    doLookupTest(VueTestModule.VUE_2_6_10, renderPriority = false, locations = listOf(
      "{{ a<caret>",
      "this.<caret>"
    ))

  fun testComputedTypeJS() =
    doLookupTest(VueTestModule.VUE_2_6_10, renderPriority = false, locations = listOf(
      "{{ a<caret>",
      "this.<caret>"
    ))

  fun testDataTypeTS() =
    doLookupTest(VueTestModule.VUE_2_6_10, renderPriority = false, locations = listOf(
      "this.<caret>msg\"",
      "= this.<caret>userInput"
    ), lookupItemFilter = filterOutDollarPrefixedProperties)

  fun testCustomModifiers() =
    doLookupTest(dir = true, renderPriority = false, renderTypeText = false)

  fun testVue2CompositionApi() =
    doLookupTest(VueTestModule.VUE_2_6_10, VueTestModule.COMPOSITION_API_0_4_0, locations = listOf(
      "{{<caret>}}",
      "{{foo.<caret>}}",
      "{{state.<caret>count}}"
    )) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  fun testVue3CompositionApi() {
    // Used TS type is recursive in itself and recursion prevention is expected
    RecursionManager.disableAssertOnRecursionPrevention(testRootDisposable)
    RecursionManager.disableMissedCacheAssertions(testRootDisposable)

    doLookupTest(VueTestModule.VUE_3_0_0, locations = listOf(
      "{{<caret>}}",
      "{{foo.<caret>}}",
      "{{state.<caret>count}}"
    )) {
      // Ignore global objects and keywords
      it.priority > 10
    }
  }

  fun testDefineComponent() =
    doLookupTest(VueTestModule.VUE_2_5_3, VueTestModule.COMPOSITION_API_0_4_0,
                 dir = true, fileContents = """<template><<caret></template>""", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testDefineOptions() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true,
                 fileContents = """<template><<caret></template>""", lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testNoDuplicateCompletionProposals() =
    doCompletionAutoPopupTest(checkResult = false) {
      completeBasic()
      assertLookupContains("v-dir2", "v-on:", "v-bind:")
      assertLookupDoesntContain("v-dir1", "v-slot", "v-slot:")
      type("v-on:")

      assertLookupContains("click", "dblclick")
      type("\n\" :")

      assertLookupContains(":dir", ":bar")
      assertLookupDoesntContain(":foo", ":id")
    }

  fun testPropsDataOptionsJS() =
    doLookupTest(namedLocations = listOf(
      "props" to "{{this.\$props.<caret>", "props" to "{{\$props.<caret>", "props" to "this.\$props.<caret>foo",
      "data" to "{{this.\$data.<caret>", "data" to "{{\$data.<caret>", "data" to "this.\$data.<caret>foo",
      "options" to "{{this.\$options.<caret>", "options" to "{{\$options.<caret>", "options" to "this.\$options.<caret>foo",
      "this" to "this.<caret>\$options.foo"
    ), lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  fun testSassGlobalFunctions() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(renderTypeText = false)
    }

  fun testImportEmptyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "-")

  fun testImportFunctionPropertyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "-")

  fun testCastedObjectProps() =
    doLookupTest(VueTestModule.VUE_3_2_2, locations = listOf(
      "post.<caret>",
      "callback.<caret>",
      "message.<caret>"
    ))

  fun testImportVueExtend() =
    doLookupTest(dir = true)

  fun testScriptSetup() =
    doLookupTest(
      VueTestModule.VUE_3_2_2,
      renderPriority = false,
      locations = listOf(
        ":count=\"<caret>count\"",
        " v<caret>/>",
        ":<caret>foo=",
        "<<caret>Foo",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols
        and filterOutMostOfGlobalJSSymbolsInVue
        and { info -> info.lookupString.let { !it.contains("aria-") && !it.startsWith("on") } }
    )

  fun testScriptSetupTs() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doLookupTest(
      VueTestModule.VUE_3_2_2,
      renderPriority = false,
      locations = listOf(
        ":count=\"<caret>count\"",
        " v<caret>/>",
        ":<caret>foo=",
        "<<caret>Foo",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols
        and filterOutMostOfGlobalJSSymbolsInVue
        and { info -> info.lookupString.let { !it.contains("aria-") && !it.startsWith("on") } }
    )
  }

  fun testExpression() =
    doLookupTest(VueTestModule.VUE_2_6_10) {
      // Ignore global objects
      it.priority > 1
    }

  fun testObjectLiteralProperty() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testEnum() =
    doLookupTest(VueTestModule.VUE_3_2_2) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  fun testScriptSetupRef() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testScriptSetupGlobals() =
    doLookupTest(VueTestModule.VUE_3_2_2, typeToFinishLookup = "Pro")

  fun testScriptSetupGlobalsTs() =
    doLookupTest(VueTestModule.VUE_3_2_2, typeToFinishLookup = "Pro", dir = true)

  fun testTypedComponentsImportClassic() =
    doLookupTest(VueTestModule.VUE_3_0_0, VueTestModule.HEADLESS_UI_1_4_1, typeToFinishLookup = "")

  fun testTypedComponentsImportScriptSetup() =
    doLookupTest(VueTestModule.HEADLESS_UI_1_4_1, typeToFinishLookup = "\n")

  fun testTypedComponentsImportScriptSetup2() =
    doLookupTest(VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES, typeToFinishLookup = "\n")

  fun testTypedComponentsPropsAndEvents() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doLookupTest(VueTestModule.VUE_3_2_2,
                 VueTestModule.HEADLESS_UI_1_4_1,
                 VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
                 locations = listOf("Dialog v-bind:<caret>", "Dialog v-on:<caret>",
                                    "el-affix v-bind:<caret>", "el-affix v-on:<caret>"), lookupItemFilter = filterOutAriaAttributes)
  }

  fun testTypedComponentsList() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 VueTestModule.HEADLESS_UI_1_4_1,
                 VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES,
                 VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
                 fileContents = """<template><<caret></template>""",
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testStyleVBind() =
    doLookupTest(VueTestModule.VUE_3_2_2, renderPriority = false, renderTypeText = false)

  fun testStyleVBindScriptSetupCss() =
    doLookupTest(VueTestModule.VUE_3_2_2, renderPriority = false)

  fun testStyleVBindScriptSetupScss() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(VueTestModule.VUE_3_2_2, renderPriority = false)
    }

  fun testStyleVBindScriptSetupSass() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(VueTestModule.VUE_3_2_2, renderPriority = false)
    }

  fun testStyleVBindScriptSetupLess() =
    doLookupTest(VueTestModule.VUE_3_2_2, renderPriority = false)

  fun testComponentFromFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testComponentFromNestedFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testComponentFromNestedFunctionPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testComponentFromObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testComponentFromNestedObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testComponentFromNestedObjectPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_4_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  fun testCreateAppIndex() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 dir = true,
                 configureFileName = "index.html",
                 locations = listOf("<<caret>Boo", "<div v-<caret>", "w<<caret>"),
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCreateAppIncludedComponent() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 dir = true,
                 configureFileName = "foo.vue",
                 locations = listOf("<<caret>Boo", "<div v-<caret>"),
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCreateAppRootComponent() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 dir = true,
                 configureFileName = "App.vue",
                 locations = listOf("<<caret>Boo", "<div v-<caret>"),
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCreateAppImportedByRootComponent() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 dir = true,
                 configureFileName = "ImportedByRoot.vue",
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCreateAppNotImported() =
    doLookupTest(VueTestModule.VUE_3_2_2,
                 dir = true,
                 configureFileName = "NotImported.vue",
                 lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testSlotsWithPatterns() =
    doLookupTest(dir = true, renderPriority = false, renderPresentedText = true, locations = listOf("<template #<caret>"))

  fun testSlotTypes() =
    doLookupTest(VueTestModule.VUE_3_2_2, VueTestModule.QUASAR_2_6_5,
                 dir = true, renderPriority = false,
                 locations = listOf("props.<caret>key", "{<caret>selected,"))

  fun testAutoImportInsertion() =
    doLookupTest(VueTestModule.VUE_3_2_2, VueTestModule.HEADLESS_UI_1_4_1, typeToFinishLookup = "al\n")

  fun testScriptKeywordsJS() =
    doLookupTest(VueTestModule.VUE_3_2_2) {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  fun testScriptKeywordsTS() =
    doLookupTest(VueTestModule.VUE_3_2_2) {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  fun testExpressionOperationKeywordsJS() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testExpressionOperationKeywordsTS() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testExpressionOperationKeywordsNestedJS() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testExpressionOperationKeywordsNestedTS() =
    doLookupTest(VueTestModule.VUE_3_2_2)

  fun testComponentEmitsDefinitions() =
    doLookupTest(VueTestModule.VUE_3_2_2, dir = true, renderPriority = true, renderTypeText = false,
                 locations = listOf("define-emits @<caret>", "define-component @<caret>", "export-component @<caret>",
                                    "define-emits-with-type @<caret>")) {
      it.isItemTextBold
    }

  fun testExternalSymbolsImport() =
    doTestExternalSymbolsImport()

  fun testExternalSymbolsImportClassic() =
    doTestExternalSymbolsImport()

  private fun doTestExternalSymbolsImport() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_2_2, dir = true) {
      moveToOffsetBySignature(":style=\"<caret>\"")
      completeBasic()
      type("Col\n.r")
      assertLookupShown()
      type("\n")

      moveToOffsetBySignature(" {{ <caret> }}")
      completeBasic()
      type("getTe\n")

      moveToOffsetBySignature("key) in i<caret>")
      completeBasic()
      type("tems\n")
    }

  fun testImportComponentFromTextContext() =
    doLookupTest(dir = true, configureFileName = "Check.vue", typeToFinishLookup = "")

  fun testImportNoScriptOrScriptSetupComponentInCode() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_2_2, configureFileName = "test.ts", dir = true) {
      completeBasic()
      type("ScriptC\n")
      type(",\nbar: Sc")
      completeBasic()
      type("riptSet\n")
    }

  fun testImportNoScriptOrScriptSetupComponentImports() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_2_2, configureFileName = "imports.ts", dir = true) {
      completeBasic()
      type("ScriptC\n")
      moveToOffsetBySignature("import Sc<caret>")
      completeBasic()
      type("riptSet\n")
    }

  fun testExternalScriptComponentEdit() =
    doCompletionAutoPopupTest(configureFileName = "foo.vue", dir = true) {
      completeBasic()
      type("Col\n.r\n")
    }

  fun testExternalScriptComponentImport() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_2_2, configureFileName = "test.vue", dir = true) {
      type("<")
      assertLookupShown()
      type("fo\n :")
      assertLookupShown()
      type("ms\n")
    }

  fun testAliasedComponentImport() =
    doAliasedComponentImportTest()

  fun testAliasedComponentImportKebabCase() =
    doAliasedComponentImportTest()

  fun testAliasedComponentImportOptionsApi() =
    doAliasedComponentImportTest()

  private fun doAliasedComponentImportTest() =
    doConfiguredTest(dir = true, configureFileName = "apps/vue-app/src/App.vue") {
      completeBasic()
      checkResultByFile("$testName/apps/vue-app/src/App.after.vue")
    }

  fun testImportComponentUsedInApp() =
    doConfiguredTest(dir = true, configureFileName = "src/components/CheckImportComponent.vue") {
      completeBasic()
      checkResultByFile("$testName/src/components/CheckImportComponent.after.vue")
    }

  fun testVueTscComponent() =
    doLookupTest(VueTestModule.VUE_3_2_2, dir = true, renderPriority = true) {
      it.isItemTextBold
    }

  fun testVueTscComponentQualifiedComponentType() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true, typeToFinishLookup = "")

  fun testVueTscComponentWithSlots() =
    doLookupTest(VueTestModule.VUE_3_4_0, dir = true) { it.priority > 10 }

  fun testVueTscComponentAliasedExport() =
    doLookupTest(VueTestModule.VUE_3_4_0, dir = true) { it.priority >= 100 }

  fun testWatchProperty() =
    doCompletionAutoPopupTest(VueTestModule.VUE_2_6_10) {
      completeBasic()
      type("doubleA")
      selectItem("doubleAge.")
      type("\n")
      assertLookupShown()
      type("\n")
    }

  fun testNamespacedComponents() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_2_2, configureFileName = "scriptSetup.vue", dir = true) {
      type("Forms.")
      assertLookupShown()
      type("Fo\n")
      type(".")
      assertLookupShown()
      type("In\n :")
      assertLookupShown()
      type("fo\n")
    }

  fun testNoFilterForOnProps() =
    doLookupTest()

  fun testScriptSetupGeneric() =
    doLookupTest(VueTestModule.VUE_3_3_4, locations = listOf(
      "clearable.<caret>", "value.<caret>", "Clearable).<caret>\">", "Clearable).<caret> }}", "foo.<caret>;"))

  fun testDefineModelAttribute() =
    doLookupTest(VueTestModule.VUE_3_3_4, configureFileName = "app.vue", dir = true) { it.priority > 10 }

  fun testInjectInLiterals() =
    doLookupTest(VueTestModule.VUE_3_3_4, configureFileName = "InjectInLiterals.vue", dir = true)

  fun testInjectInLiteralsUnique() =
    doLookupTest(VueTestModule.VUE_3_3_4, configureFileName = "InjectInLiteralsUnique.vue", dir = true)

  fun testInjectInLiteralsUnquoted() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_3_4, configureFileName = "InjectInLiteralsUnquoted.vue", dir = true) {
      completeBasic()
      assertLookupContains(
        "provideGlobal",
        "provideInApp",
        "provideLiteral",
      )
      assertLookupDoesntContain("\"provideGlobal\"")

      type("provideIn")
      completeBasic()
    }

  fun testInjectInScriptSetupLiteralsUnquoted() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_3_4, configureFileName = "InjectInScriptSetupLiteralsUnquoted.vue", dir = true) {
      completeBasic()
      assertLookupContains(
        "provideGlobal",
        "provideInApp",
        "provideLiteral",
      )
      type("provideG\n")
    }

  fun testInjectInProperties() =
    doLookupTest(VueTestModule.VUE_3_3_4, configureFileName = "InjectInProperties.vue", dir = true) {
      it.priority > 10
    }

  fun testDefineSlotsSlotName() =
    doLookupTest(VueTestModule.VUE_3_3_4)

  fun testComponentCustomProperties() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomProperties.vue", dir = true) {
      it.priority > 100
    }

  fun testComponentCustomPropertiesWithFunctionOverloads() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomPropertiesWithFunctionOverloads.vue", dir = true,
                 containsCheck = true)

  fun testDefineSlotsProperties() =
    doLookupTest(VueTestModule.VUE_3_3_4) {
      it.priority >= 10 && !it.lookupString.startsWith("v-")
    }

  fun testDefineExpose() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true)

  fun testPropsBindings() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true, renderPriority = false)

  fun testCompleteComponentWithDefineOptions() =
    doLookupTest(VueTestModule.VUE_3_3_4, configureFileName = "Component.vue", dir = true, typeToFinishLookup = "\n")
}

