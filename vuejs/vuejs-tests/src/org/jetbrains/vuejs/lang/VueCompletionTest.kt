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
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private val FILTER_DEFAULT_ATTRIBUTES = (filterOutStandardHtmlSymbols
  and filterOutMostOfGlobalJSSymbolsInVue
  and { info -> info.lookupString.let { !it.contains("aria-") && !it.startsWith("on") } })

@Ignore
class VueCompletionTest :
  VueCompletionTestBase() {

  class WithoutServiceTest :
    VueCompletionTestBase(useTsc = false) {

    override fun getExpectedItemsLocation(dir: Boolean): String {
      require(dir) { "Only `dir` option is supported!" }
      
      return super.getExpectedItemsLocation(dir) + "/items-no-service"
    }
  }
}

@RunWith(JUnit4::class)
abstract class VueCompletionTestBase(
  useTsc: Boolean = true,
) : VueTestCase("completion", useTsc = useTsc) {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  @Test
  fun testCompleteCssClasses() =
    doLookupTest(dir = true)

  @Test
  fun testCompleteAttributesWithVueInNodeModules() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteAttributesWithVueInPackageJson() =
    doLookupTest(configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testNoVueCompletionWithoutVue() =
    doLookupTest(configureFileName = "index.html", dir = true) {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteImportedComponent() =
    doLookupTest(dir = true) {
      it.lookupString.startsWith("comp")
    }

  @Test
  fun testCompleteWithImport() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportNoExtension() =
    withTempCodeStyleSettings {
      it.getCustomSettings(JSCodeStyleSettings::class.java).USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.NEVER
      doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }

  @Test
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

  @Test
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

  @Test
  fun testCompleteWithImportCreateExport() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScriptNoExport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithoutImportForRenamedGlobalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)


  @Test
  fun testCompleteWithoutImportForGlobalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteAttributesFromProps() =
    doLookupTest(dir = true) { it.priority > 10 }

  @Test
  fun testCompletePropsInInterpolation() {
    doLookupTest(
      dir = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )
  }

  @Test
  fun testCompleteComputedPropsInInterpolation() =
    doLookupTest(
      dir = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteMethodsInBoundAttributes() =
    doLookupTest(dir = true, lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  @Test
  fun testCompleteElementsFromLocalData() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteElementsFromLocalData2() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.priority > 10 }

  @Test
  fun testSrcInStyleCompletion() =
    doLookupTest(dir = true, configureFileName = "src/App.vue")

  @Test
  fun testSrcInStyleCompletionWithLang() =
    doLookupTest(dir = true, configureFileName = "src/App.vue")

  @Test
  fun testInsertAttributeWithoutValue() =
    doLookupTest(dir = true, typeToFinishLookup = "")

  @Test
  fun testInsertAttributeWithValue() =
    doLookupTest(dir = true, typeToFinishLookup = "")

  @Test
  fun testMixinsInCompletion() =
    doLookupTest(configureFileName = "CompWithTwoMixins.vue", dir = true) { it.priority > 10 }

  @Test
  fun testNoNotImportedMixinsInCompletion() =
    doLookupTest(dir = true) { it.priority > 10 }

  @Test
  fun testNoCompletionInVueAttributes() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest(dir = true)
    }

  @Test
  fun testTypeScriptCompletionFromPredefined() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest(dir = true)
    }

  @Test
  fun testCustomDirectivesInCompletion() =
    doLookupTest(configureFileName = "CustomDirectives.vue", dir = true) { it.lookupString.startsWith("v-") }

  @Test
  fun testCustomDirectivesLinkedFilesInCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "CustomDirectives.html", dir = true) { it.lookupString.startsWith("v-") }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        // components
        "<My<caret>SpecialButton>",
        "<MySpecial<caret>Button>",
        "<MyVery<caret>RegularLabel>",

        // directives
        "<main v-<caret>my-click-outside>",
        "<main v-my<caret>-click-outside>",

        // directive modifiers
        "<main v-my-click-outside.<caret>once>",
        "<main v-my-intersect.<caret>once>",
        "<main v-my-mutate.<caret>once>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )
  }

  @Test
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

  @Test
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

  @Test
  fun testPrettyLookup() =
    doLookupTest(
      dir = true,
      renderTailText = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteVBind() =
    doLookupTest(
      dir = true,
      locations = listOf("<child-comp :<caret>>", "<a v-bind:<caret>>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testCompleteVBindUser() =
    doLookupTest(configureFileName = "User.vue", dir = true, lookupItemFilter = filterOutAriaAttributes)

  @Test
  fun testVueOutObjectLiteral() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralTs() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralCompletionJsx() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testNoDoubleCompletionForLocalComponent() =
    doLookupTest(dir = true) { it.priority > 1 }

  @Test
  fun testElementUiCompletion() =
    doLookupTest(
      VueTestModule.ELEMENT_UI_2_0_5,
      dir = true,
      fileContents = "<template><el-<caret></template>",
    )

  @Test
  fun testMintUiCompletion() =
    doLookupTest(
      VueTestModule.MINT_UI_2_2_3,
      dir = true,
      fileContents = "<template><mt-<caret></template>",
    )

  @Test
  fun testVuetifyCompletion_017() =
    doLookupTest(
      VueTestModule.VUETIFY_0_17_2,
      dir = true,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_137() =
    doLookupTest(
      VueTestModule.VUETIFY_1_3_7,
      dir = true,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_1210() =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      dir = true,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testIviewCompletion() =
    doLookupTest(
      VueTestModule.IVIEW_2_8_0,
      dir = true,
      fileContents = "<template><a<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testIview3Completion() =
    doLookupTest(
      VueTestModule.IVIEW_3_5_4,
      dir = true,
      locations = listOf("v-bind:<caret>", "v-on:<caret>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testBootstrapVueCompletion() =
    doLookupTest(
      VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11,
      dir = true,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testShardsVueCompletion() =
    doLookupTest(
      VueTestModule.SHARDS_VUE_1_0_5,
      dir = true,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testWrongPropsNotInCompletion() =
    doCompletionAutoPopupTest(dir = true, checkResult = false) {
      completeBasic()
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)

      type(":")
      assertLookupShown()
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)

      type("a")
      checkLookupItems(lookupItemFilter = filterOutAriaAttributes)
    }

  @Test
  fun testBuefyCompletion() =
    doLookupTest(
      VueTestModule.BUEFY_0_6_2,
      dir = true,
      fileContents = "<template><b-<caret></template>",
    )

  @Test
  fun testClassComponentCompletion() =
    doLookupTest(fileContents = "<template><<caret></template>", dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  @Test
  fun testClassComponentCompletionTs() =
    doLookupTest(fileContents = "<template><<caret></template>", dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  @Test
  fun testComponentInsertionNoScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptNoImports() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueClassComponentImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionWithClassDefined() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "")

  @Test
  fun testTypescriptVForItemCompletion() =
    doLookupTest(
      dir = true,
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testTypescriptVForCompletionWebTypes() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties and filterOutDollarPrefixedProperties,
    )

  @Test
  fun testLocalComponentsExtendsCompletion() =
    doLookupTest(dir = true) { it.priority > 10 }

  @Test
  fun testCompletionWithRecursiveMixins() =
    doLookupTest(
      dir = true,
      locations = listOf(
        "<<caret>div",
        "<HiddenComponent fr<caret>/>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoImportInsertedForRecursivelyLocalComponent() =
    doLookupTest(dir = true, typeToFinishLookup = "")

  @Test
  fun testCssClassInPug() =
    doLookupTest(dir = true)

  @Test
  fun testEventsAfterAt() =
    doCompletionAutoPopupTest(
      VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11,
      dir = true,
      checkResult = false,
    ) {
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

  @Test
  fun testEventsAfterVOn() =
    doCompletionAutoPopupTest(dir = true, checkResult = false) {
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


  @Test
  fun testEventModifiers() =
    doLookupTest(
      dir = true,
      locations = listOf(
        // general modifiers only
        "<MyComponent @click123.<caret>",
        // general modifiers (except already used) + key modifiers + system modifiers
        "v-on:keyup.stop.passive.<caret>",
        // general modifiers (except already used) + mouse button modifiers + system modifiers
        "@click.capture.<caret>",
        // general modifiers + system modifiers
        "@drop.<caret>"
      ),
    )

  @Test
  fun testAutopopupAfterVOnSelection() =
    doCompletionAutoPopupTest(dir = true) {
      completeBasic()
      checkLookupItems()
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testStyleAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.priority > 1 }

  @Test
  fun testTemplateAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.priority > 1 }

  @Test
  fun testNoVueTagsWithNamespace() =
    doLookupTest(dir = true)

  @Test
  fun testVueCompletionInsideScript() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      lookupItemFilter = filterOutDollarPrefixedProperties,
    )

  @Test
  fun testVueCompletionInsideScriptLifecycleHooks() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true)

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionWithExtend() =
    doLookupTest(dir = true) { it.priority > 10 }

  @Test
  fun testVueNoComponentNameAsStyleSelector() =
    doLookupTest(dir = true, locations = listOf(">\ninp<caret>", "<inp<caret>"))

  @Test
  fun testCompletionPriorityAndHints() =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.SHARDS_VUE_1_0_5,
      dir = true,
      lookupItemFilter = filterOutStandardHtmlSymbols
    )

  @Test
  fun testCompletionPriorityAndHintsBuiltInTags() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectiveCompletionOnComponent() =
    doLookupTest(
      VueTestModule.VUETIFY_1_3_7,
      dir = true,
      renderPriority = false,
      renderTypeText = false,
      containsCheck = true
    )

  @Test
  fun testBuiltInTagsAttributeCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true) { it.priority >= 10 && !it.lookupString.startsWith("v-") }

  @Test
  fun testBindProposalsPriority() =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.VUE_2_6_10,
      dir = true,
      renderTypeText = false,
      renderProximity = true,
    ) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  @Test
  fun testBindProposalsStdTag() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      dir = true,
      renderPriority = false,
      renderTypeText = false,
      lookupItemFilter = filterOutAriaAttributes,
    )

  private fun doAttributeNamePriorityTest(vueVersion: VueTestModule) =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      vueVersion,
      dir = true,
      renderTypeText = false,
      renderProximity = true,
      fileContents = """<template><v-alert <caret><template>""",
    ) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  @Test
  fun testAttributeNamePriorityVue26() =
    doAttributeNamePriorityTest(VueTestModule.VUE_2_6_10)

  @Test
  fun testAttributeNamePriorityVue30() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_0_0)

  @Test
  fun testAttributeNamePriorityVue31() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_1_0)

  @Test
  fun testAttributeNamePriorityVue32() =
    doAttributeNamePriorityTest(VueTestModule.VUE_3_2_2)

  @Test
  fun testComplexComponentDecorator() =
    doLookupTest(configureFileName = "App.vue", dir = true) { it.priority > 10 }

  @Test
  fun testComplexComponentDecoratorTs() =
    doLookupTest(configureFileName = "App.vue", dir = true) { it.priority > 10 }

  @Test
  fun testComplexComponentDecorator8Ts() =
    doLookupTest(
      configureFileName = "App.vue",
      dir = true,
      locations = listOf("<HW <caret>", "<<caret>HW"),
      lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") },
    )

  @Test
  fun testDestructuringVariableTypeInVFor() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true)

  @Test
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

  @Test
  fun testSlotProps() =
    doLookupTest(dir = true, renderPriority = true, renderTypeText = false) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testVueDefaultSymbolsWithDefinitions() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      locations = listOf(
        "v-on:click=\"<caret>\"",
        "v-bind:about=\"<caret>\""
      )
    ) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testVueDefaultSymbols() =
    doLookupTest(
      dir = true,
      locations = listOf(
        "v-on:click=\"<caret>\"",
        "v-bind:about=\"<caret>\""
      ),
    ) { info ->
      info.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
      || info.lookupString.startsWith("A") // Check also if we have some global symbols, but don't list all of them
    }

  @Test
  fun testSlotTag() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      containsCheck = true,
      renderPriority = true,
      renderTypeText = false,
    )

  @Test
  fun testSlotNames() =
    doLookupTest(
      configureFileName = "test.vue",
      renderPriority = false,
      renderTypeText = false,
      dir = true,
      namedLocations = listOf(
        "script-template-vue",
        "require-decorators",
        "x-template",
        "export-import",
        "some-lib",
        "no-script-section",
      ).flatMap { tag ->
        listOf(
          tag to "$tag><template v-slot:<caret></$tag",
          tag to "$tag><div slot=\"<caret>\"</$tag",
        )
      }
    )

  @Test
  fun testFilters() =
    doLookupTest(dir = true, renderTypeText = false, renderPriority = false)

  @Test
  fun testComplexThisContext() =
    doLookupTest(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_5_3,
                 renderTypeText = false, renderPriority = false, dir = true)

  @Test
  fun testTopLevelTagsNoI18n() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      dir = true,
      fileContents = "<<caret>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testTopLevelTags() =
    doLookupTest(VueTestModule.VUE_2_5_3, dir = true, locations = listOf(
      "<<caret>i18n",
      "<i18n <caret>a",
      "<template <caret>a",
      "<script <caret>a",
      "<style <caret>a",
    ), lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") })

  @Test
  fun testScriptLangAttributeWithAlreadyPresentCode() =
    doLookupTest(dir = true, typeToFinishLookup = "\t") { it.priority > 0 }

  @Test
  fun testComputedTypeTS() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      dir = true,
      renderPriority = false,
      locations = listOf(
        "{{ a<caret>",
        "this.<caret>"
      ),
    )

  @Test
  fun testComputedTypeJS() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      dir = true,
      renderPriority = false,
      locations = listOf(
        "{{ a<caret>",
        "this.<caret>"
      ),
    )

  @Test
  fun testDataTypeTS() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      dir = true,
      renderPriority = false,
      locations = listOf(
        "this.<caret>msg\"",
        "= this.<caret>userInput"
      ),
      lookupItemFilter = filterOutDollarPrefixedProperties,
    )

  @Test
  fun testCustomModifiers() =
    doLookupTest(dir = true, renderPriority = false, renderTypeText = false)

  @Test
  fun testVue2CompositionApi() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_0_4_0,
      dir = true,
      locations = listOf(
        "{{<caret>}}",
        "{{foo.<caret>}}",
        "{{state.<caret>count}}"
      ),
    ) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testVue3CompositionApi() {
    // Used TS type is recursive in itself and recursion prevention is expected
    RecursionManager.disableAssertOnRecursionPrevention(testRootDisposable)
    RecursionManager.disableMissedCacheAssertions(testRootDisposable)

    doLookupTest(
      VueTestModule.VUE_3_0_0,
      dir = true,
      locations = listOf(
        "{{<caret>}}",
        "{{foo.<caret>}}",
        "{{state.<caret>count}}"
      ),
    ) {
      // Ignore global objects and keywords
      it.priority > 10
    }
  }

  @Test
  fun testDefineComponent() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      VueTestModule.COMPOSITION_API_0_4_0,
      dir = true,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDefineOptions() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoDuplicateCompletionProposals() =
    doCompletionAutoPopupTest(dir = true, checkResult = false) {
      completeBasic()
      assertLookupContains("v-dir2", "v-on:", "v-bind:")
      assertLookupDoesntContain("v-dir1", "v-slot", "v-slot:")
      type("v-on:")

      assertLookupContains("click", "dblclick")
      type("\n\" :")

      assertLookupContains(":dir", ":bar")
      assertLookupDoesntContain(":foo", ":id")
    }

  @Test
  fun testPropsDataOptionsJS() =
    doLookupTest(
      dir = true,
      namedLocations = listOf(
        "props" to "{{this.\$props.<caret>",
        "props" to "{{\$props.<caret>",
        "props" to "this.\$props.<caret>foo",
        "data" to "{{this.\$data.<caret>",
        "data" to "{{\$data.<caret>",
        "data" to "this.\$data.<caret>foo",
        "options" to "{{this.\$options.<caret>",
        "options" to "{{\$options.<caret>",
        "options" to "this.\$options.<caret>foo",
        "this" to "this.<caret>\$options.foo"
      ),
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testSassGlobalFunctions() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(dir = true, renderTypeText = false)
    }

  @Test
  fun testImportEmptyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "-")

  @Test
  fun testImportFunctionPropertyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true, typeToFinishLookup = "-")

  @Test
  fun testCastedObjectProps() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      locations = listOf(
        "post.<caret>",
        "callback.<caret>",
        "message.<caret>"
      ),
    )

  @Test
  fun testImportVueExtend() =
    doLookupTest(dir = true)

  @Test
  fun testScriptSetup() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      renderPriority = false,
      locations = listOf(
        ":count=\"<caret>count\"",
        " v<caret>/>",
        ":<caret>foo=",
        "<<caret>Foo",
      ),
      lookupItemFilter = FILTER_DEFAULT_ATTRIBUTES,
    )

  @Test
  fun testScriptSetupTs() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
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

  @Test
  fun testExpression() =
    doLookupTest(VueTestModule.VUE_2_6_10, dir = true) {
      // Ignore global objects
      it.priority > 1
    }

  @Test
  fun testObjectLiteralProperty() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testEnum() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testScriptSetupRef() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testScriptSetupGlobals() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, typeToFinishLookup = "Pro")

  @Test
  fun testScriptSetupGlobalsTs() =
    doLookupTest(VueTestModule.VUE_3_5_0, typeToFinishLookup = "Pro", dir = true)

  @Test
  fun testTypedComponentsImportClassic() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      VueTestModule.HEADLESS_UI_1_4_1,
      dir = true,
      typeToFinishLookup = ""
    )

  @Test
  fun testTypedComponentsImportScriptSetup() =
    doLookupTest(VueTestModule.HEADLESS_UI_1_4_1, dir = true, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsImportScriptSetup2() =
    doLookupTest(VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES, dir = true, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsPropsAndEvents() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)

    doLookupTest(
      VueTestModule.VUE_3_5_0,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
      dir = true,
      locations = listOf(
        "Dialog v-bind:<caret>",
        "Dialog v-on:<caret>",
        "el-affix v-bind:<caret>",
        "el-affix v-on:<caret>"
      ),
      lookupItemFilter = filterOutAriaAttributes,
    )
  }

  @Test
  fun testTypedComponentsList() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES,
      VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
      dir = true,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testStyleVBind() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false, renderTypeText = false)

  @Test
  fun testStyleVBindScriptSetupCss() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false)

  @Test
  fun testStyleVBindScriptSetupScss() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false)
    }

  @Test
  fun testStyleVBindScriptSetupSass() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false)
    }

  @Test
  fun testStyleVBindScriptSetupLess() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false)

  @Test
  fun testComponentFromFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentsWithTwoScriptTags() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<<caret>MyButton",
        "<My<caret>Label",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentsWithTwoScriptTags_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<<caret>MyButton",
        "<My<caret>Label",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testPropsOfComponentsWithTwoScriptTags() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyCardC <caret>ad/>",
        "<MyCardC a<caret>d/>",
        "<MyCardC ad<caret>/>",
      ),
      lookupItemFilter = FILTER_DEFAULT_ATTRIBUTES
        and { info -> info.priority > 10 },
    )

  @Test
  fun testPropsOfComponentsWithTwoScriptTags_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyCardC <caret>ad/>",
        "<MyCardC a<caret>d/>",
        "<MyCardC ad<caret>/>",
      ),
      lookupItemFilter = FILTER_DEFAULT_ATTRIBUTES
        and { info -> info.priority > 10 },
    )

  @Test
  fun testDirectivesFromFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyLabelFromPlugin v-my-intersect.<caret>pass",
        "<MyLabelFromPlugin v-my-intersect.pa<caret>ss",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
        "<MyLabelFromPlugin v-my-intersect.<caret>once",
        "<MyLabelFromPlugin v-my-intersect.on<caret>ce",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyLabelFromPlugin v-my-intersect.<caret>pass",
        "<MyLabelFromPlugin v-my-intersect.pa<caret>ss",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
        "<MyLabelFromPlugin v-my-intersect.<caret>once",
        "<MyLabelFromPlugin v-my-intersect.on<caret>ce",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPlugin() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
        "<MyButtonFromPlugin v-my-mutate.<caret>attr",
        "<MyButtonFromPlugin v-my-mutate.attr.<caret>sub",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppIndex() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "index.html",
      locations = listOf("<<caret>Boo", "<div v-<caret>", "w<<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppIncludedComponent() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "foo.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppRootComponent() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppImportedByRootComponent() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "ImportedByRoot.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppNotImported() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      configureFileName = "NotImported.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testSlotsWithPatterns() =
    doLookupTest(
      dir = true,
      renderPriority = false,
      renderPresentedText = true,
      locations = listOf(
        "<template #<caret>",
      ),
    )

  @Test
  fun testSlotTypes() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      VueTestModule.QUASAR_2_6_5,
      dir = true,
      renderPriority = false,
      locations = listOf("props.<caret>key", "{<caret>selected,")
    )

  @Test
  fun testAutoImportInsertion() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      VueTestModule.HEADLESS_UI_1_4_1,
      dir = true,
      typeToFinishLookup = "al\n",
    )

  @Test
  fun testScriptKeywordsJS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testScriptKeywordsTS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testExpressionOperationKeywordsJS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testExpressionOperationKeywordsTS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testExpressionOperationKeywordsNestedJS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testExpressionOperationKeywordsNestedTS() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testComponentEmitsDefinitions() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = true, renderTypeText = false,
                 locations = listOf("define-emits @<caret>", "define-component @<caret>", "export-component @<caret>",
                                    "define-emits-with-type @<caret>")) {
      it.isItemTextBold
    }

  @Test
  fun testExternalSymbolsImport() =
    doTestExternalSymbolsImport()

  @Test
  fun testExternalSymbolsImportClassic() =
    doTestExternalSymbolsImport()

  private fun doTestExternalSymbolsImport() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, dir = true) {
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

  @Test
  fun testImportComponentFromTextContext() =
    doLookupTest(dir = true, configureFileName = "Check.vue", typeToFinishLookup = "")

  @Test
  fun testImportNoScriptOrScriptSetupComponentInCode() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "test.ts", dir = true) {
      completeBasic()
      type("ScriptC\n")
      type(",\nbar: Sc")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testImportNoScriptOrScriptSetupComponentImports() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "imports.ts", dir = true) {
      completeBasic()
      type("ScriptC\n")
      moveToOffsetBySignature("import Sc<caret>")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testExternalScriptComponentEdit() =
    doCompletionAutoPopupTest(configureFileName = "foo.vue", dir = true) {
      completeBasic()
      type("Col\n.r\n")
    }

  @Test
  fun testExternalScriptComponentImport() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "test.vue", dir = true) {
      type("<")
      assertLookupShown()
      type("fo\n :")
      assertLookupShown()
      type("ms\n")
    }

  @Test
  fun testAliasedComponentImport() =
    doAliasedComponentImportTest()

  @Test
  fun testAliasedComponentImportKebabCase() =
    doAliasedComponentImportTest()

  @Test
  fun testAliasedComponentImportOptionsApi() =
    doAliasedComponentImportTest()

  private fun doAliasedComponentImportTest() =
    doConfiguredTest(dir = true, configureFileName = "apps/vue-app/src/App.vue") {
      completeBasic()
      checkResultByFile("$testName/apps/vue-app/src/App.after.vue")
    }

  @Test
  fun testImportComponentUsedInApp() =
    doConfiguredTest(dir = true, configureFileName = "src/components/CheckImportComponent.vue") {
      completeBasic()
      checkResultByFile("$testName/src/components/CheckImportComponent.after.vue")
    }

  @Test
  fun testVueTscComponent() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = true) {
      it.isItemTextBold
    }

  @Test
  fun testVueTscComponentQualifiedComponentType() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, typeToFinishLookup = "")

  @Test
  fun testVueTscComponentWithSlots() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) { it.priority > 10 }

  @Test
  fun testVueTscComponentAliasedExport() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) { it.priority >= 100 }

  @Test
  fun testWatchProperty() =
    doCompletionAutoPopupTest(VueTestModule.VUE_2_6_10, dir = true) {
      completeBasic()
      type("doubleA")
      selectItem("doubleAge.")
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testNamespacedComponents() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "scriptSetup.vue", dir = true) {
      type("Forms.")
      assertLookupShown()
      type("Fo\n")
      type(".")
      assertLookupShown()
      type("In\n :")
      assertLookupShown()
      type("fo\n")
    }

  @Test
  fun testNoFilterForOnProps() =
    doLookupTest(dir = true)

  @Test
  fun testScriptSetupGeneric() =
    doLookupTest(
      VueTestModule.VUE_3_5_0,
      dir = true,
      locations = listOf(
        "clearable.<caret>",
        "value.<caret>",
        "Clearable).<caret>\">",
        "Clearable).<caret> }}",
        "foo.<caret>;",
      ),
    )

  @Test
  fun testDefineModelAttribute() =
    doLookupTest(VueTestModule.VUE_3_5_0, configureFileName = "app.vue", dir = true) { it.priority > 10 }

  @Test
  fun testInjectInLiterals() =
    doLookupTest(VueTestModule.VUE_3_5_0, configureFileName = "InjectInLiterals.vue", dir = true)

  @Test
  fun testInjectInLiteralsUnique() =
    doLookupTest(VueTestModule.VUE_3_5_0, configureFileName = "InjectInLiteralsUnique.vue", dir = true)

  @Ignore
  @Test
  fun testInjectInLiteralsUnquoted() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "InjectInLiteralsUnquoted.vue", dir = true) {
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

  @Test
  fun testInjectInScriptSetupLiteralsUnquoted() =
    doCompletionAutoPopupTest(VueTestModule.VUE_3_5_0, configureFileName = "InjectInScriptSetupLiteralsUnquoted.vue", dir = true) {
      completeBasic()
      assertLookupContains(
        "provideGlobal",
        "provideInApp",
        "provideLiteral",
      )
      type("provideG\n")
    }

  @Test
  fun testInjectInProperties() =
    doLookupTest(VueTestModule.VUE_3_5_0, configureFileName = "InjectInProperties.vue", dir = true) {
      it.priority > 10
    }

  @Test
  fun testDefineSlotsSlotName() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true)

  @Test
  fun testComponentCustomProperties() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomProperties.vue", dir = true) {
      it.priority > 100
    }

  @Test
  fun testComponentCustomPropertiesWithFunctionOverloads() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomPropertiesWithFunctionOverloads.vue", dir = true,
                 containsCheck = true)

  @Test
  fun testDefineSlotsProperties() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true) {
      it.priority >= 10 && !it.lookupString.startsWith("v-")
    }

  @Test
  fun testDefineExpose() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true)

  @Test
  fun testPropsBindings() =
    doLookupTest(VueTestModule.VUE_3_5_0, dir = true, renderPriority = false)

  @Test
  fun testCompleteComponentWithDefineOptions() =
    doLookupTest(VueTestModule.VUE_3_5_0, configureFileName = "Component.vue", dir = true, typeToFinishLookup = "\n")

  @Test
  fun testVaporAttribute() {
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      configureFileName = "App.vue",
      locations = listOf(
        "<script setup v<caret>a>",
        "<script setup va<caret>>",
      ),
    ) { it.priority >= 10 }
  }
}

