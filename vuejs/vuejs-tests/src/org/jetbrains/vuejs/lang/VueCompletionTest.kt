// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.javascript.testFramework.web.filterOutStandardHtmlSymbols
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TrackFailedTestExtension
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.RecursionManager
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.and
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

private val FILTER_DEFAULT_ATTRIBUTES = (filterOutStandardHtmlSymbols
  and filterOutMostOfGlobalJSSymbolsInVue
  and { info -> info.lookupString.let { !it.contains("aria-") && !it.startsWith("on") } })

@TestApplication
class VueCompletionTest :
  VueCompletionWithPluginTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    VueCompletionTestBase(
      testMode = VueTestMode.TS_GO_PROXY,
      getCodeCompletionExpectedItemsLocation = { dir, location ->
        require(dir) { "Only `dir` option is supported!" }

        val separateExpectedData = fixture.tempDirFixture.getFile("items-tsgo-proxy") != null
                                   || testMethodName == "testScriptSetupGlobalsTs"

        location
          .plus(if (separateExpectedData) "/items-tsgo-proxy" else "")
      },
    ) {

    @RegisterExtension
    @JvmField
    val rule: TrackFailedTestExtension = TrackFailedTestExtension(
      "testCastedObjectProps",
      "testComputedTypeJS",
      "testComputedTypeTS",
      "testScriptSetupGeneric",
      "testVueOutObjectLiteralTs",
    )
  }

  @TestApplication
  class WithoutServiceTest :
    VueCompletionTestBase(
      testMode = VueTestMode.NO_PLUGIN,
      getCodeCompletionExpectedItemsLocation = { dir, location ->
        require(dir) { "Only `dir` option is supported!" }

        location + "/items-no-service"
      },
    )
}

abstract class VueCompletionWithPluginTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueCompletionTestBase(testMode = testMode) {

  @RegisterExtension
  @JvmField
  val rule: TrackFailedTestExtension = TrackFailedTestExtension(
    "testComputedTypeJS",
    "testComputedTypeTS",
  )
}

/**
 * [getCodeCompletionExpectedItemsLocation] gets the default location of the expected lookup items and returns the location for the test.
 */
abstract class VueCompletionTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
  getCodeCompletionExpectedItemsLocation: VueFixture.(dir: Boolean, location: String) -> String = { _, location -> location },
) {

  protected val vue: VueFixture by vueFixture(testMode) { VueCompletionFixture(it, testMode, getCodeCompletionExpectedItemsLocation) }

  @Test
  fun testCompleteCssClasses() =
    vue.doLookupTest()

  @Test
  fun testCompleteAttributesWithVueInNodeModules() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteAttributesWithVueInPackageJson() =
    vue.doLookupTest(configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testNoVueCompletionWithoutVue() =
    vue.doLookupTest(configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteImportedComponent() =
    vue.doLookupTest {
      it.lookupString.startsWith("comp")
    }

  @Test
  fun testCompleteWithImport() =
    vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportNoExtension() =
    vue.withTempCodeStyleSettings {
      it.getCustomSettings(JSCodeStyleSettings::class.java).USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.NEVER
      vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }

  @Test
  fun testCompleteNoImportIfSettingIsOffJs() {
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseJavaScriptAutoImport
    jsApplicationSettings.isUseJavaScriptAutoImport = false
    try {
      vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
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
      vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }
    finally {
      jsApplicationSettings.isUseTypeScriptAutoImport = before
    }
  }

  @Test
  fun testCompleteWithImportCreateExport() =
    vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScript() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScriptNoExport() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithoutImportForRenamedGlobalComponent() =
    vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)


  @Test
  fun testCompleteWithoutImportForGlobalComponent() =
    vue.doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteAttributesFromProps() =
    vue.doLookupTest { it.priority > 10 }

  @Test
  fun testCompletePropsInInterpolation() {
    vue.doLookupTest(
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )
  }

  @Test
  fun testCompleteComputedPropsInInterpolation() =
    vue.doLookupTest(
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteMethodsInBoundAttributes() =
    vue.doLookupTest(lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  @Test
  fun testCompleteElementsFromLocalData() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteElementsFromLocalData2() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 10 }

  @Test
  fun testSrcInStyleCompletion() =
    vue.doLookupTest(configureFileName = "src/App.vue")

  @Test
  fun testSrcInStyleCompletionWithLang() =
    vue.doLookupTest(configureFileName = "src/App.vue")

  @Test
  fun testInsertAttributeWithoutValue() =
    vue.doLookupTest(typeToFinishLookup = "")

  @Test
  fun testInsertAttributeWithValue() =
    vue.doLookupTest(typeToFinishLookup = "")

  @Test
  fun testMixinsInCompletion() =
    vue.doLookupTest(configureFileName = "CompWithTwoMixins.vue") { it.priority > 10 }

  @Test
  fun testNoNotImportedMixinsInCompletion() =
    vue.doLookupTest { it.priority > 10 }

  @Test
  fun testNoCompletionInVueAttributes() =
    JSTestUtils.withNoLibraries(vue.project) {
      vue.doLookupTest()
    }

  @Test
  fun testTypeScriptCompletionFromPredefined() =
    JSTestUtils.withNoLibraries(vue.project) {
      vue.doLookupTest()
    }

  @Test
  fun testCustomDirectivesInCompletion() =
    vue.doLookupTest(configureFileName = "CustomDirectives.vue") { it.lookupString.startsWith("v-") }

  @Test
  fun testCustomDirectivesLinkedFilesInCompletion() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "CustomDirectives.html") { it.lookupString.startsWith("v-") }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    vue.doLookupTest(
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
      configurators = listOf(
        VueTsConfigFile(types = listOf("my-vue-items-library/dist/volar")),
      ),
    )
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    vue.doLookupTest(
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
    vue.doLookupTest(
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
    vue.doLookupTest(
      renderTailText = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteVBind() =
    vue.doLookupTest(
      locations = listOf("<child-comp :<caret>>", "<a v-bind:<caret>>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testCompleteVBindUser() =
    vue.doLookupTest(configureFileName = "User.vue", lookupItemFilter = filterOutAriaAttributes)

  @Test
  fun testVueOutObjectLiteral() =
    vue.doLookupTest(
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralTs() =
    vue.doLookupTest(
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralCompletionJsx() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testNoDoubleCompletionForLocalComponent() =
    vue.doLookupTest { it.priority > 1 }

  @Test
  fun testElementUiCompletion() =
    vue.doLookupTest(
      VueTestModule.ELEMENT_UI_2_0_5,
      fileContents = "<template><el-<caret></template>",
    )

  @Test
  fun testMintUiCompletion() =
    vue.doLookupTest(
      VueTestModule.MINT_UI_2_2_3,
      fileContents = "<template><mt-<caret></template>",
    )

  @Test
  fun testVuetifyCompletion_017() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_0_17_2,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_137() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_1_3_7,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_1210() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_1_2_10,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testIviewCompletion() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.IVIEW_2_8_0,
      fileContents = "<template><a<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testIview3Completion() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.IVIEW_3_5_4,
      locations = listOf("v-bind:<caret>", "v-on:<caret>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testBootstrapVueCompletion() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testShardsVueCompletion() =
    vue.doLookupTest(
      VueTestModule.SHARDS_VUE_1_0_5,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testWrongPropsNotInCompletion() =
    vue.doEditorTypingTest(checkResult = false) {
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
    vue.doLookupTest(
      VueTestModule.BUEFY_0_6_2,
      fileContents = "<template><b-<caret></template>",
    )

  @Test
  fun testClassComponentCompletion() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testClassComponentCompletionTs() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentInsertionNoScript() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptNoImports() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueImport() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueClassComponentImport() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testTypescriptVForCompletionWebTypes() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties and filterOutDollarPrefixedProperties,
    )

  @Test
  fun testLocalComponentsExtendsCompletion() =
    vue.doLookupTest { it.priority > 10 }

  @Test
  fun testCompletionWithRecursiveMixins() =
    vue.doLookupTest(
      locations = listOf(
        "<<caret>div",
        "<HiddenComponent fr<caret>/>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoImportInsertedForRecursivelyLocalComponent() =
    vue.doLookupTest(typeToFinishLookup = "")

  @Test
  fun testCssClassInPug() =
    vue.doLookupTest()

  @Test
  fun testEventsAfterAt() =
    vue.doEditorTypingTest(
      VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11,
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
    vue.doEditorTypingTest(checkResult = false) {
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
    vue.doLookupTest(
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
    vue.doEditorTypingTest {
      completeBasic()
      checkLookupItems()
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testStyleAttributes() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  @Test
  fun testTemplateAttributes() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  @Test
  fun testNoVueTagsWithNamespace() =
    vue.doLookupTest()

  @Test
  fun testVueCompletionInsideScript() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutDollarPrefixedProperties,
    )

  @Test
  fun testVueCompletionInsideScriptLifecycleHooks() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3)

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionWithExtend() =
    vue.doLookupTest { it.priority > 10 }

  @Test
  fun testVueNoComponentNameAsStyleSelector() =
    vue.doLookupTest(locations = listOf(">\ninp<caret>", "<inp<caret>"))

  @Test
  fun testCompletionPriorityAndHints() =
    vue.doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.SHARDS_VUE_1_0_5,
      lookupItemFilter = filterOutStandardHtmlSymbols
    )

  @Test
  fun testCompletionPriorityAndHintsBuiltInTags() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectiveCompletionOnComponent() =
    vue.doLookupTest(
      VueTestModule.VUETIFY_1_3_7,
      renderPriority = false,
      renderTypeText = false,
      containsCheck = true
    )

  @Test
  fun testBuiltInTagsAttributeCompletion() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3) { it.priority >= 10 && !it.lookupString.startsWith("v-") }

  @Test
  fun testBindProposalsPriority() =
    vue.doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.VUE_2_6_10,
      renderTypeText = false,
      renderProximity = true,
    ) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  @Test
  fun testBindProposalsStdTag() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      renderPriority = false,
      renderTypeText = false,
      lookupItemFilter = filterOutAriaAttributes,
    )

  private fun doAttributeNamePriorityTest(vueVersion: VueTestModule) =
    vue.doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      vueVersion,
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
    vue.doLookupTest(configureFileName = "App.vue") { it.priority > 10 }

  @Test
  fun testComplexComponentDecoratorTs() =
    vue.doLookupTest(configureFileName = "App.vue") { it.priority > 10 }

  @Test
  fun testComplexComponentDecorator8Ts() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<HW <caret>", "<<caret>HW"),
      lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") },
    )

  @Test
  fun testWebTypesComplexSetup() {
    vue.fixture.copyDirectoryToProject("web-types", ".")
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
        vue.fixture.configureFromTempProjectFile(fileName)
        vue.fixture.completeBasic()
        assertThat(vue.fixture.lookupElementStrings!!).containsAll(expected)
        assertThat(vue.fixture.lookupElementStrings!!).doesNotContainAnyElementsOf(notExpected)
      }
  }

  @Test
  fun testSlotProps() =
    vue.doLookupTest(renderPriority = true, renderTypeText = false) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testVueDefaultSymbolsWithDefinitions() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
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
    vue.doLookupTest(
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
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      containsCheck = true,
      renderPriority = true,
      renderTypeText = false,
    )

  @Test
  fun testSlotNames() =
    vue.doLookupTest(
      configureFileName = "test.vue",
      renderPriority = false,
      renderTypeText = false,
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
    vue.doLookupTest(renderTypeText = false, renderPriority = false)

  @Test
  fun testComplexThisContext() =
    vue.doLookupTest(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_5_3,
                 renderTypeText = false, renderPriority = false)

  @Test
  fun testTopLevelTagsNoI18n() =
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      fileContents = "<<caret>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testTopLevelTags() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3, locations = listOf(
      "<<caret>i18n",
      "<i18n <caret>a",
      "<template <caret>a",
      "<script <caret>a",
      "<style <caret>a",
    ), lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") })

  @Test
  fun testScriptLangAttributeWithAlreadyPresentCode() =
    vue.doLookupTest(typeToFinishLookup = "\t") { it.priority > 0 }

  @Test
  fun testComputedTypeTS() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      renderPriority = false,
      renderTailText = true,
      locations = listOf(
        "{{ a<caret>",
        "this.<caret>"
      ),
    )

  @Test
  fun testComputedTypeJS() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      renderPriority = false,
      renderTailText = true,
      locations = listOf(
        "{{ a<caret>",
        "this.<caret>"
      ),
    )

  @Test
  fun testDataTypeTS() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      renderPriority = false,
      renderTailText = true,
      locations = listOf(
        "this.<caret>msg\"",
        "= this.<caret>userInput"
      ),
      lookupItemFilter = filterOutDollarPrefixedProperties,
    )

  @Test
  fun testCustomModifiers() =
    vue.doLookupTest(renderPriority = false, renderTypeText = false)

  @Test
  fun testVue2CompositionApi() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_0_4_0,
      renderTailText = true,
      locations = listOf(
        "{{<caret>}}",
        "{{foo.<caret>}}",
        "{{state.<caret>count}}"
      ),
    ) {
      // Ignore global objects and keywords
      it.priority > 10
      // Ignore properties with unstable types
      && it.lookupString !in setOf("toLocaleLowerCase", "toLocaleUpperCase", "match", "replace", "search", "split")
    }

  @Test
  fun testVue3CompositionApi() {
    // Used TS type is recursive in itself and recursion prevention is expected
    RecursionManager.disableAssertOnRecursionPrevention(vue.testRootDisposable)
    RecursionManager.disableMissedCacheAssertions(vue.testRootDisposable)

    vue.doLookupTest(
      VueTestModule.VUE_3_0_0,
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
    vue.doLookupTest(
      VueTestModule.VUE_2_5_3,
      VueTestModule.COMPOSITION_API_0_4_0,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDefineOptions() =
    vue.doLookupTest(
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoDuplicateCompletionProposals() =
    vue.doEditorTypingTest(checkResult = false) {
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
    vue.doLookupTest(
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
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(vue.project) {
      vue.doLookupTest(renderTypeText = false)
    }

  @Test
  fun testImportEmptyObjectInitializerComponent() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "-")

  @Test
  fun testImportFunctionPropertyObjectInitializerComponent() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "-")

  @Test
  fun testCastedObjectProps() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      locations = listOf(
        "post.<caret>",
        "callback.<caret>",
        "message.<caret>"
      ),
    )

  @Test
  fun testImportVueExtend() =
    vue.doLookupTest()

  @Test
  fun testScriptSetup() =
    vue.doLookupTest(
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
    vue.doLookupTest(
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
    vue.doLookupTest(VueTestModule.VUE_2_6_10) {
      // Ignore global objects
      it.priority > 1
    }

  @Test
  fun testObjectLiteralProperty() =
    vue.doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testEnum() =
    vue.doLookupTest {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testScriptSetupRef() =
    vue.doLookupTest(
      configurators = listOf(
        VueTsConfigFile(
          vueCompilerOptions = VueCompilerOptions(
            inferTemplateDollarRefs = true,
          ),
        ),
      ),
    )

  @Test
  fun testScriptSetupGlobals() =
    vue.doLookupTest(typeToFinishLookup = "Pro")

  @Test
  fun testScriptSetupGlobalsTs() =
    vue.doLookupTest(typeToFinishLookup = "Pro")

  @Test
  fun testTypedComponentsImportClassic() =
    vue.doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      typeToFinishLookup = ""
    )

  @Test
  fun testTypedComponentsImportScriptSetup() =
    vue.doLookupTest(VueTestModule.HEADLESS_UI_1_4_1, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsImportScriptSetup2() =
    vue.doLookupTest(VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsPropsAndEvents() {
    vue.doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
      locations = listOf(
        "Dialog v-bind:<caret>",
        "Dialog v-on:<caret>",
        "el-affix v-bind:<caret>",
        "el-affix v-on:<caret>"
      ),
      lookupItemFilter = filterOutAriaAttributes,
      configurators = listOf(
        VueTsConfigFile(enabled = false),
      ),
    )
  }

  @Test
  fun testTypedComponentsList() =
    vue.doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES,
      VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testStyleVBind() =
    vue.doLookupTest(renderPriority = false, renderTypeText = false)

  @Test
  fun testStyleVBindScriptSetupCss() =
    vue.doLookupTest(renderPriority = false)

  @Test
  fun testStyleVBindScriptSetupScss() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(vue.project) {
      vue.doLookupTest(renderPriority = false)
    }

  @Test
  fun testStyleVBindScriptSetupSass() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(vue.project) {
      vue.doLookupTest(renderPriority = false)
    }

  @Test
  fun testStyleVBindScriptSetupLess() =
    vue.doLookupTest(renderPriority = false)

  @Test
  fun testComponentFromFunctionPlugin() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromFunctionPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentsWithTwoScriptTags() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf(
        "<<caret>MyButton",
        "<My<caret>Label",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentsWithTwoScriptTags_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf(
        "<<caret>MyButton",
        "<My<caret>Label",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testPropsOfComponentsWithTwoScriptTags() =
    vue.doLookupTest(
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
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
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
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
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
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf(
        "<MyButtonFromPlugin v-<caret>my-",
        "<MyButtonFromPlugin v-my-<caret>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() =
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
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
    vue.doLookupTest(
      configureFileName = "index.html",
      locations = listOf("<<caret>Boo", "<div v-<caret>", "w<<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppIncludedComponent() =
    vue.doLookupTest(
      configureFileName = "foo.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppRootComponent() =
    vue.doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppImportedByRootComponent() =
    vue.doLookupTest(
      configureFileName = "ImportedByRoot.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppNotImported() =
    vue.doLookupTest(
      configureFileName = "NotImported.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testSlotsWithPatterns() =
    vue.doLookupTest(
      renderPriority = false,
      renderPresentedText = true,
      locations = listOf(
        "<template #<caret>",
      ),
    )

  @Test
  fun testSlotTypes() =
    vue.doLookupTest(
      VueTestModule.VUE_3_1_0,
      VueTestModule.QUASAR_2_6_5,
      renderPriority = false,
      locations = listOf("props.<caret>key", "{<caret>selected,")
    )

  @Test
  fun testAutoImportInsertion() =
    vue.doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      typeToFinishLookup = "al\n",
    )

  @Test
  fun testScriptKeywordsJS() =
    vue.doLookupTest {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testScriptKeywordsTS() =
    vue.doLookupTest {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testExpressionOperationKeywordsJS() =
    vue.doLookupTest()

  @Test
  fun testExpressionOperationKeywordsTS() =
    vue.doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testExpressionOperationKeywordsNestedJS() =
    vue.doLookupTest()

  @Test
  fun testExpressionOperationKeywordsNestedTS() =
    vue.doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testComponentEmitsDefinitions() =
    vue.doLookupTest(
      renderPriority = true,
      renderTypeText = false,
      locations = listOf(
        "define-emits @<caret>",
        "define-component @<caret>",
        "export-component @<caret>",
        "define-emits-with-type @<caret>",
      ),
    ) {
      it.isItemTextBold
    }

  @Test
  fun testExternalSymbolsImport() =
    doTestExternalSymbolsImport()

  @Test
  fun testExternalSymbolsImportClassic() =
    doTestExternalSymbolsImport()

  private fun doTestExternalSymbolsImport() =
    vue.doEditorTypingTest {
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
    vue.doLookupTest(configureFileName = "Check.vue", typeToFinishLookup = "")

  @Test
  fun testImportNoScriptOrScriptSetupComponentInCode() =
    vue.doEditorTypingTest(configureFileName = "test.ts") {
      completeBasic()
      type("ScriptC\n")
      type(",\nbar: Sc")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testImportNoScriptOrScriptSetupComponentImports() =
    vue.doEditorTypingTest(configureFileName = "imports.ts") {
      completeBasic()
      type("ScriptC\n")
      moveToOffsetBySignature("import Sc<caret>")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testExternalScriptComponentEdit() =
    vue.doEditorTypingTest(configureFileName = "foo.vue") {
      completeBasic()
      type("Col\n.r\n")
    }

  @Test
  fun testExternalScriptComponentImport() =
    vue.doEditorTypingTest(configureFileName = "test.vue") {
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
    vue.doConfiguredTest(
      configureFileName = "apps/vue-app/src/App.vue",
      configurators = listOf(
        VueTsConfigFile(enabled = false),
      ),
    ) {
      completeBasic()
      checkResultByFile("${vue.testName}/apps/vue-app/src/App.after.vue")
    }

  @Test
  fun testImportComponentUsedInApp() =
    vue.doConfiguredTest(configureFileName = "src/components/CheckImportComponent.vue") {
      completeBasic()
      checkResultByFile("${vue.testName}/src/components/CheckImportComponent.after.vue")
    }

  @Test
  fun testVueTscComponent() =
    vue.doLookupTest(renderPriority = true) {
      it.isItemTextBold
    }

  @Test
  fun testVueTscComponentQualifiedComponentType() =
    vue.doLookupTest(typeToFinishLookup = "")

  @Test
  fun testVueTscComponentWithSlots() =
    vue.doLookupTest { it.priority > 10 }

  @Test
  fun testVueTscComponentAliasedExport() =
    vue.doLookupTest(renderTailText = true) { it.priority >= 100 }

  @Test
  fun testGlobalComponentFromVueFileIndexedAccess() =
    vue.doLookupTest(configureFileName = "src/App.vue") { it.priority >= 100 }

  @Test
  fun testGlobalComponentFromVueFileDefaultProperty() =
    vue.doLookupTest(configureFileName = "src/App.vue") { it.priority >= 100 }

  @Test
  fun testWatchProperty() =
    vue.doEditorTypingTest(VueTestModule.VUE_2_6_10) {
      completeBasic()
      type("doubleA")
      selectLookupItem("doubleAge.")
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testNamespacedComponents() =
    vue.doEditorTypingTest(configureFileName = "scriptSetup.vue") {
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
    vue.doLookupTest()

  @Test
  fun testScriptSetupGeneric() =
    vue.doLookupTest(
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
    vue.doLookupTest(configureFileName = "app.vue") { it.priority > 10 }

  @Test
  fun testInjectInLiterals() =
    vue.doLookupTest(configureFileName = "InjectInLiterals.vue")

  @Test
  fun testInjectInLiteralsUnique() =
    vue.doLookupTest(configureFileName = "InjectInLiteralsUnique.vue")

  @Disabled
  @Test
  fun testInjectInLiteralsUnquoted() =
    vue.doEditorTypingTest(configureFileName = "InjectInLiteralsUnquoted.vue") {
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
    vue.doEditorTypingTest(configureFileName = "InjectInScriptSetupLiteralsUnquoted.vue") {
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
    vue.doLookupTest(configureFileName = "InjectInProperties.vue") {
      it.priority > 10
    }

  @Test
  fun testDefineSlotsSlotName() =
    vue.doLookupTest()

  @Test
  fun testComponentCustomProperties() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomProperties.vue") {
      it.priority > 100
    }

  @Test
  fun testComponentCustomPropertiesWithFunctionOverloads() =
    vue.doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomPropertiesWithFunctionOverloads.vue",
                 containsCheck = true)

  @Test
  fun testDefineSlotsProperties() =
    vue.doLookupTest {
      it.priority >= 10 && !it.lookupString.startsWith("v-")
    }

  @Test
  fun testDefineExpose() =
    vue.doLookupTest(VueTestModule.VUE_3_3_4)

  @Test
  fun testPropsBindings() =
    vue.doLookupTest(renderPriority = false)

  @Test
  fun testCompleteComponentWithDefineOptions() =
    vue.doLookupTest(configureFileName = "Component.vue", typeToFinishLookup = "\n")

  @Test
  fun testVaporAttribute() {
    vue.doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf(
        "<script setup v<caret>a>",
        "<script setup va<caret>>",
      ),
    ) { it.priority >= 10 }
  }

  @Test
  fun testTypescriptVForItemCompletion() =
    vue.doLookupTest(
      VueTestModule.VUE_2_7_14,
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testDestructuringVariableTypeInVFor() =
    vue.doLookupTest(VueTestModule.VUE_2_5_3)

  @Test
  fun testComponentInsertionWithClassDefined() =
    vue.doLookupTest(
      VueTestModule.VUE_2_6_10,
      typeToFinishLookup = "",
    )
}

/**
 * The state of [VueCompletionTestBase]: a [VueFixture] in the dir mode that checks the idempotence of each cache.
 * The fixture does not add the Vue modules to the `testNoVueCompletionWithoutVue` test.
 */
private class VueCompletionFixture(
  context: HybridFixtureContext,
  testMode: VueTestMode,
  private val expectedItemsLocation: VueFixture.(dir: Boolean, location: String) -> String,
) : VueFixture(context, "completion", testMode, dirModeByDefault = true) {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  // TODO: check if we need sush tests
  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> {
    if (testMethodName == "testNoVueCompletionWithoutVue")
      return modules

    return super.adjustModules(modules)
  }

  override fun getCodeCompletionExpectedItemsLocation(dir: Boolean, dirName: String): String =
    expectedItemsLocation(dir, super.getCodeCompletionExpectedItemsLocation(dir, dirName))
}
