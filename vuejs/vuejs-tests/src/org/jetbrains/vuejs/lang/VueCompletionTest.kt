// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.javascript.testFramework.web.filterOutStandardHtmlSymbols
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.RecursionManager
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.and
import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private val FILTER_DEFAULT_ATTRIBUTES = (filterOutStandardHtmlSymbols
  and filterOutMostOfGlobalJSSymbolsInVue
  and { info -> info.lookupString.let { !it.contains("aria-") && !it.startsWith("on") } })

class VueCompletionTest :
  VueCompletionWithPluginTestBase() {

  @Ignore
  class WithLegacyPluginTest :
    VueCompletionTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueCompletionTestBase(testMode = VueTestMode.NO_PLUGIN) {

    override fun getExpectedItemsLocation(dir: Boolean): String {
      require(dir) { "Only `dir` option is supported!" }

      return super.getExpectedItemsLocation(dir) + "/items-no-service"
    }

    @Test
    fun testTypescriptVForItemCompletion() =
      doLookupTest(
        VueTestModule.VUE_2_7_14,
        lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties,
      )

    @Test
    fun testDestructuringVariableTypeInVFor() =
      doLookupTest(VueTestModule.VUE_2_5_3)

    @Test
    fun testComponentInsertionWithClassDefined() =
      doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")
  }
}

abstract class VueCompletionWithPluginTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueCompletionTestBase(testMode = testMode) {

  @Rule
  @JvmField
  val rule: TestRule = TrackFailedTestRule(
    "testComputedTypeJS",
    "testComputedTypeTS",
  )
}

@RunWith(JUnit4::class)
abstract class VueCompletionTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("completion", testMode = testMode) {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      addAll(super.adjustConfigurators(configurators))

      if (none { it is VueTsConfigFile }) {
        add(VueTsConfigFile())
      }
    }

  override val dirModeByDefault: Boolean = true

  // TODO: check if we need sush tests
  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> {
    if (name == "testNoVueCompletionWithoutVue")
      return modules

    return super.adjustModules(modules)
  }

  @Test
  fun testCompleteCssClasses() =
    doLookupTest()

  @Test
  fun testCompleteAttributesWithVueInNodeModules() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteAttributesWithVueInPackageJson() =
    doLookupTest(configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testNoVueCompletionWithoutVue() =
    doLookupTest(configureFileName = "index.html") {
      it.lookupString.startsWith("v-")
    }

  @Test
  fun testCompleteImportedComponent() =
    doLookupTest {
      it.lookupString.startsWith("comp")
    }

  @Test
  fun testCompleteWithImport() =
    doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportNoExtension() =
    withTempCodeStyleSettings {
      it.getCustomSettings(JSCodeStyleSettings::class.java).USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.NEVER
      doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }

  @Test
  fun testCompleteNoImportIfSettingIsOffJs() {
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseJavaScriptAutoImport
    jsApplicationSettings.isUseJavaScriptAutoImport = false
    try {
      doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
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
      doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)
    }
    finally {
      jsApplicationSettings.isUseTypeScriptAutoImport = before
    }
  }

  @Test
  fun testCompleteWithImportCreateExport() =
    doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithImportCreateScriptNoExport() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteWithoutImportForRenamedGlobalComponent() =
    doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)


  @Test
  fun testCompleteWithoutImportForGlobalComponent() =
    doLookupTest(typeToFinishLookup = "", renderPriority = false, renderTypeText = false)

  @Test
  fun testCompleteAttributesFromProps() =
    doLookupTest { it.priority > 10 }

  @Test
  fun testCompletePropsInInterpolation() {
    doLookupTest(
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )
  }

  @Test
  fun testCompleteComputedPropsInInterpolation() =
    doLookupTest(
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteMethodsInBoundAttributes() =
    doLookupTest(lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties)

  @Test
  fun testCompleteElementsFromLocalData() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteElementsFromLocalData2() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 10 }

  @Test
  fun testSrcInStyleCompletion() =
    doLookupTest(configureFileName = "src/App.vue")

  @Test
  fun testSrcInStyleCompletionWithLang() =
    doLookupTest(configureFileName = "src/App.vue")

  @Test
  fun testInsertAttributeWithoutValue() =
    doLookupTest(typeToFinishLookup = "")

  @Test
  fun testInsertAttributeWithValue() =
    doLookupTest(typeToFinishLookup = "")

  @Test
  fun testMixinsInCompletion() =
    doLookupTest(configureFileName = "CompWithTwoMixins.vue") { it.priority > 10 }

  @Test
  fun testNoNotImportedMixinsInCompletion() =
    doLookupTest { it.priority > 10 }

  @Test
  fun testNoCompletionInVueAttributes() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest()
    }

  @Test
  fun testTypeScriptCompletionFromPredefined() =
    JSTestUtils.withNoLibraries(project) {
      doLookupTest()
    }

  @Test
  fun testCustomDirectivesInCompletion() =
    doLookupTest(configureFileName = "CustomDirectives.vue") { it.lookupString.startsWith("v-") }

  @Test
  fun testCustomDirectivesLinkedFilesInCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3, configureFileName = "CustomDirectives.html") { it.lookupString.startsWith("v-") }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    doLookupTest(
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
        VueTsConfigFile(enabled = false),
      ),
    )
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    doLookupTest(
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
      renderTailText = true,
      lookupItemFilter = filterOutDollarPrefixedProperties and filterOutJsKeywordsGlobalObjectsAndCommonProperties,
    )

  @Test
  fun testCompleteVBind() =
    doLookupTest(
      locations = listOf("<child-comp :<caret>>", "<a v-bind:<caret>>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testCompleteVBindUser() =
    doLookupTest(configureFileName = "User.vue", lookupItemFilter = filterOutAriaAttributes)

  @Test
  fun testVueOutObjectLiteral() =
    doLookupTest(
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralTs() =
    doLookupTest(
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testVueOutObjectLiteralCompletionJsx() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      renderPriority = false,
      renderTypeText = false,
    )

  @Test
  fun testNoDoubleCompletionForLocalComponent() =
    doLookupTest { it.priority > 1 }

  @Test
  fun testElementUiCompletion() =
    doLookupTest(
      VueTestModule.ELEMENT_UI_2_0_5,
      fileContents = "<template><el-<caret></template>",
    )

  @Test
  fun testMintUiCompletion() =
    doLookupTest(
      VueTestModule.MINT_UI_2_2_3,
      fileContents = "<template><mt-<caret></template>",
    )

  @Test
  fun testVuetifyCompletion_017() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_0_17_2,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_137() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_1_3_7,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testVuetifyCompletion_1210() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.VUETIFY_1_2_10,
      fileContents = "<template><<caret></template>",
    ) { item ->
      item.lookupString.let { it.startsWith("V") || it.startsWith("v") }
    }

  @Test
  fun testIviewCompletion() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.IVIEW_2_8_0,
      fileContents = "<template><a<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testIview3Completion() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      VueTestModule.IVIEW_3_5_4,
      locations = listOf("v-bind:<caret>", "v-on:<caret>"),
      lookupItemFilter = filterOutAriaAttributes,
    )

  @Test
  fun testBootstrapVueCompletion() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testShardsVueCompletion() =
    doLookupTest(
      VueTestModule.SHARDS_VUE_1_0_5,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testWrongPropsNotInCompletion() =
    doEditorTypingTest(checkResult = false) {
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
      fileContents = "<template><b-<caret></template>",
    )

  @Test
  fun testClassComponentCompletion() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testClassComponentCompletionTs() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      fileContents = "<template><<caret></template>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentInsertionNoScript() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptNoImports() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testComponentInsertionScriptVueClassComponentImport() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "")

  @Test
  fun testTypescriptVForCompletionWebTypes() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties and filterOutDollarPrefixedProperties,
    )

  @Test
  fun testLocalComponentsExtendsCompletion() =
    doLookupTest { it.priority > 10 }

  @Test
  fun testCompletionWithRecursiveMixins() =
    doLookupTest(
      locations = listOf(
        "<<caret>div",
        "<HiddenComponent fr<caret>/>",
      ),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoImportInsertedForRecursivelyLocalComponent() =
    doLookupTest(typeToFinishLookup = "")

  @Test
  fun testCssClassInPug() =
    doLookupTest()

  @Test
  fun testEventsAfterAt() =
    doEditorTypingTest(
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
    doEditorTypingTest(checkResult = false) {
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
    doEditorTypingTest {
      completeBasic()
      checkLookupItems()
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testStyleAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  @Test
  fun testTemplateAttributes() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority > 1 }

  @Test
  fun testNoVueTagsWithNamespace() =
    doLookupTest()

  @Test
  fun testVueCompletionInsideScript() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutDollarPrefixedProperties,
    )

  @Test
  fun testVueCompletionInsideScriptLifecycleHooks() =
    doLookupTest(VueTestModule.VUE_2_5_3)

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.lookupString.startsWith("$") }

  @Test
  fun testVueCompletionWithExtend() =
    doLookupTest { it.priority > 10 }

  @Test
  fun testVueNoComponentNameAsStyleSelector() =
    doLookupTest(locations = listOf(">\ninp<caret>", "<inp<caret>"))

  @Test
  fun testCompletionPriorityAndHints() =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.SHARDS_VUE_1_0_5,
      lookupItemFilter = filterOutStandardHtmlSymbols
    )

  @Test
  fun testCompletionPriorityAndHintsBuiltInTags() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDirectiveCompletionOnComponent() =
    doLookupTest(
      VueTestModule.VUETIFY_1_3_7,
      renderPriority = false,
      renderTypeText = false,
      containsCheck = true
    )

  @Test
  fun testBuiltInTagsAttributeCompletion() =
    doLookupTest(VueTestModule.VUE_2_5_3) { it.priority >= 10 && !it.lookupString.startsWith("v-") }

  @Test
  fun testBindProposalsPriority() =
    doLookupTest(
      VueTestModule.VUETIFY_1_2_10,
      VueTestModule.VUE_2_6_10,
      renderTypeText = false,
      renderProximity = true,
    ) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  @Test
  fun testBindProposalsStdTag() =
    doLookupTest(
      VueTestModule.VUE_2_6_10,
      renderPriority = false,
      renderTypeText = false,
      lookupItemFilter = filterOutAriaAttributes,
    )

  private fun doAttributeNamePriorityTest(vueVersion: VueTestModule) =
    doLookupTest(
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
    doLookupTest(configureFileName = "App.vue") { it.priority > 10 }

  @Test
  fun testComplexComponentDecoratorTs() =
    doLookupTest(configureFileName = "App.vue") { it.priority > 10 }

  @Test
  fun testComplexComponentDecorator8Ts() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<HW <caret>", "<<caret>HW"),
      lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") },
    )

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
    doLookupTest(renderPriority = true, renderTypeText = false) {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testVueDefaultSymbolsWithDefinitions() =
    doLookupTest(
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
    doLookupTest(
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
    doLookupTest(renderTypeText = false, renderPriority = false)

  @Test
  fun testComplexThisContext() =
    doLookupTest(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_5_3,
                 renderTypeText = false, renderPriority = false)

  @Test
  fun testTopLevelTagsNoI18n() =
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      fileContents = "<<caret>",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testTopLevelTags() =
    doLookupTest(VueTestModule.VUE_2_5_3, locations = listOf(
      "<<caret>i18n",
      "<i18n <caret>a",
      "<template <caret>a",
      "<script <caret>a",
      "<style <caret>a",
    ), lookupItemFilter = filterOutStandardHtmlSymbols and { it.priority > 0 && !it.lookupString.startsWith("v-") })

  @Test
  fun testScriptLangAttributeWithAlreadyPresentCode() =
    doLookupTest(typeToFinishLookup = "\t") { it.priority > 0 }

  @Test
  fun testComputedTypeTS() =
    doLookupTest(
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
    doLookupTest(
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
    doLookupTest(
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
    doLookupTest(renderPriority = false, renderTypeText = false)

  @Test
  fun testVue2CompositionApi() =
    doLookupTest(
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
    RecursionManager.disableAssertOnRecursionPrevention(testRootDisposable)
    RecursionManager.disableMissedCacheAssertions(testRootDisposable)

    doLookupTest(
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
    doLookupTest(
      VueTestModule.VUE_2_5_3,
      VueTestModule.COMPOSITION_API_0_4_0,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testDefineOptions() =
    doLookupTest(
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testNoDuplicateCompletionProposals() =
    doEditorTypingTest(checkResult = false) {
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
      doLookupTest(renderTypeText = false)
    }

  @Test
  fun testImportEmptyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "-")

  @Test
  fun testImportFunctionPropertyObjectInitializerComponent() =
    doLookupTest(VueTestModule.VUE_2_6_10, typeToFinishLookup = "-")

  @Test
  fun testCastedObjectProps() =
    doLookupTest(
      VueTestModule.VUE_2_7_14,
      locations = listOf(
        "post.<caret>",
        "callback.<caret>",
        "message.<caret>"
      ),
    )

  @Test
  fun testImportVueExtend() =
    doLookupTest()

  @Test
  fun testScriptSetup() =
    doLookupTest(
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
    doLookupTest(
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
    doLookupTest(VueTestModule.VUE_2_6_10) {
      // Ignore global objects
      it.priority > 1
    }

  @Test
  fun testObjectLiteralProperty() =
    doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testEnum() =
    doLookupTest {
      // Ignore global objects and keywords
      it.priority > 10
    }

  @Test
  fun testScriptSetupRef() =
    doLookupTest()

  @Test
  fun testScriptSetupGlobals() =
    doLookupTest(typeToFinishLookup = "Pro")

  @Test
  fun testScriptSetupGlobalsTs() =
    doLookupTest(typeToFinishLookup = "Pro")

  @Test
  fun testTypedComponentsImportClassic() =
    doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      typeToFinishLookup = ""
    )

  @Test
  fun testTypedComponentsImportScriptSetup() =
    doLookupTest(VueTestModule.HEADLESS_UI_1_4_1, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsImportScriptSetup2() =
    doLookupTest(VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES, typeToFinishLookup = "\n")

  @Test
  fun testTypedComponentsPropsAndEvents() {
    doLookupTest(
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
    doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES,
      VueTestModule.ELEMENT_PLUS_2_1_11_NO_WEB_TYPES,
      fileContents = """<template><<caret></template>""",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testStyleVBind() =
    doLookupTest(renderPriority = false, renderTypeText = false)

  @Test
  open /* temp */
  fun testStyleVBindScriptSetupCss() =
    doLookupTest(renderPriority = false)

  @Test
  open /* temp */
  fun testStyleVBindScriptSetupScss() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(renderPriority = false)
    }

  @Test
  open /* temp */
  fun testStyleVBindScriptSetupSass() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doLookupTest(renderPriority = false)
    }

  @Test
  open /* temp */
  fun testStyleVBindScriptSetupLess() =
    doLookupTest(renderPriority = false)

  @Test
  fun testComponentFromFunctionPlugin() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() =
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf("<<caret>MyButton", "<My<caret>Label"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testComponentsWithTwoScriptTags() =
    doLookupTest(
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
      configureFileName = "index.html",
      locations = listOf("<<caret>Boo", "<div v-<caret>", "w<<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppIncludedComponent() =
    doLookupTest(
      configureFileName = "foo.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppRootComponent() =
    doLookupTest(
      configureFileName = "App.vue",
      locations = listOf("<<caret>Boo", "<div v-<caret>"),
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppImportedByRootComponent() =
    doLookupTest(
      configureFileName = "ImportedByRoot.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testCreateAppNotImported() =
    doLookupTest(
      configureFileName = "NotImported.vue",
      lookupItemFilter = filterOutStandardHtmlSymbols,
    )

  @Test
  fun testSlotsWithPatterns() =
    doLookupTest(
      renderPriority = false,
      renderPresentedText = true,
      locations = listOf(
        "<template #<caret>",
      ),
    )

  @Test
  fun testSlotTypes() =
    doLookupTest(
      VueTestModule.VUE_3_1_0,
      VueTestModule.QUASAR_2_6_5,
      renderPriority = false,
      locations = listOf("props.<caret>key", "{<caret>selected,")
    )

  @Test
  fun testAutoImportInsertion() =
    doLookupTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      typeToFinishLookup = "al\n",
    )

  @Test
  fun testScriptKeywordsJS() =
    doLookupTest {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testScriptKeywordsTS() =
    doLookupTest {
      it.priority >= JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testExpressionOperationKeywordsJS() =
    doLookupTest()

  @Test
  fun testExpressionOperationKeywordsTS() =
    doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testExpressionOperationKeywordsNestedJS() =
    doLookupTest()

  @Test
  fun testExpressionOperationKeywordsNestedTS() =
    doLookupTest {
      // ignore global objects and keywords
      it.priority > 4
    }

  @Test
  fun testComponentEmitsDefinitions() =
    doLookupTest(
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
    doEditorTypingTest {
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
    doLookupTest(configureFileName = "Check.vue", typeToFinishLookup = "")

  @Test
  fun testImportNoScriptOrScriptSetupComponentInCode() =
    doEditorTypingTest(configureFileName = "test.ts") {
      completeBasic()
      type("ScriptC\n")
      type(",\nbar: Sc")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testImportNoScriptOrScriptSetupComponentImports() =
    doEditorTypingTest(configureFileName = "imports.ts") {
      completeBasic()
      type("ScriptC\n")
      moveToOffsetBySignature("import Sc<caret>")
      completeBasic()
      type("riptSet\n")
    }

  @Test
  fun testExternalScriptComponentEdit() =
    doEditorTypingTest(configureFileName = "foo.vue") {
      completeBasic()
      type("Col\n.r\n")
    }

  @Test
  fun testExternalScriptComponentImport() =
    doEditorTypingTest(configureFileName = "test.vue") {
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
    doConfiguredTest(
      configureFileName = "apps/vue-app/src/App.vue",
      configurators = listOf(
        VueTsConfigFile(enabled = false),
      ),
    ) {
      completeBasic()
      checkResultByFile("$testName/apps/vue-app/src/App.after.vue")
    }

  @Test
  fun testImportComponentUsedInApp() =
    doConfiguredTest(configureFileName = "src/components/CheckImportComponent.vue") {
      completeBasic()
      checkResultByFile("$testName/src/components/CheckImportComponent.after.vue")
    }

  @Test
  fun testVueTscComponent() =
    doLookupTest(renderPriority = true) {
      it.isItemTextBold
    }

  @Test
  fun testVueTscComponentQualifiedComponentType() =
    doLookupTest(typeToFinishLookup = "")

  @Test
  fun testVueTscComponentWithSlots() =
    doLookupTest { it.priority > 10 }

  @Test
  fun testVueTscComponentAliasedExport() =
    doLookupTest(renderTailText = true) { it.priority >= 100 }

  @Test
  fun testWatchProperty() =
    doEditorTypingTest(VueTestModule.VUE_2_6_10) {
      completeBasic()
      type("doubleA")
      selectLookupItem("doubleAge.")
      type("\n")
      assertLookupShown()
      type("\n")
    }

  @Test
  fun testNamespacedComponents() =
    doEditorTypingTest(configureFileName = "scriptSetup.vue") {
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
    doLookupTest()

  @Test
  fun testScriptSetupGeneric() =
    doLookupTest(
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
    doLookupTest(configureFileName = "app.vue") { it.priority > 10 }

  @Test
  fun testInjectInLiterals() =
    doLookupTest(configureFileName = "InjectInLiterals.vue")

  @Test
  fun testInjectInLiteralsUnique() =
    doLookupTest(configureFileName = "InjectInLiteralsUnique.vue")

  @Ignore
  @Test
  fun testInjectInLiteralsUnquoted() =
    doEditorTypingTest(configureFileName = "InjectInLiteralsUnquoted.vue") {
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
    doEditorTypingTest(configureFileName = "InjectInScriptSetupLiteralsUnquoted.vue") {
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
    doLookupTest(configureFileName = "InjectInProperties.vue") {
      it.priority > 10
    }

  @Test
  fun testDefineSlotsSlotName() =
    doLookupTest()

  @Test
  fun testComponentCustomProperties() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomProperties.vue") {
      it.priority > 100
    }

  @Test
  fun testComponentCustomPropertiesWithFunctionOverloads() =
    doLookupTest(VueTestModule.VUE_2_6_10, configureFileName = "ComponentCustomPropertiesWithFunctionOverloads.vue",
                 containsCheck = true)

  @Test
  fun testDefineSlotsProperties() =
    doLookupTest {
      it.priority >= 10 && !it.lookupString.startsWith("v-")
    }

  @Test
  fun testDefineExpose() =
    doLookupTest(VueTestModule.VUE_3_3_4)

  @Test
  fun testPropsBindings() =
    doLookupTest(renderPriority = false)

  @Test
  fun testCompleteComponentWithDefineOptions() =
    doLookupTest(configureFileName = "Component.vue", typeToFinishLookup = "\n")

  @Test
  fun testVaporAttribute() {
    doLookupTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
      locations = listOf(
        "<script setup v<caret>a>",
        "<script setup va<caret>>",
      ),
    ) { it.priority >= 10 }
  }
}

