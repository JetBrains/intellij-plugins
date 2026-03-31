// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.javascript.testFramework.web.WebFrameworkTestConfigurator
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.JSTestUtils.checkHighlightingWithSymbolNames
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.polySymbols.testFramework.disableAstLoadingFilter
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInInitMode
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import com.intellij.xml.util.CheckTagEmptyBodyInspection
import junit.framework.TestCase
import org.jetbrains.plugins.scss.inspections.SassScssResolvedByNameOnlyInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.libraries.nuxt.NuxtHighlightingTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/**
 * @see VueComponentTest
 * @see VueControlFlowTest
 * @see NuxtHighlightingTest
 */
@Ignore
class VueHighlightingTest :
  VueHighlightingTestBase() {

  @Ignore
  class WithLegacyPluginTest :
    VueHighlightingTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueHighlightingTestBase(testMode = VueTestMode.NO_PLUGIN)
}

@RunWith(JUnit4::class)
abstract class VueHighlightingTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("highlighting", testMode = testMode) {

  override val dirModeByDefault: Boolean = true

  override val defaultConfigurators: List<WebFrameworkTestConfigurator>
    get() = when (name) {
      // TODO: use base config instead 
      "testGlobalItemsAugmentedFromCompilerOptionsTypes",

        // TEMP
      "testDataTypeTS",
      "testScriptSetupScopePriority",
        -> emptyList()

      else -> listOf(
        VueTsConfigFile(),
      )
    }

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> =
    when (name) {
      "testWithPropsFromFunctionCall",
      "testWithPropsFromFunctionCall2",
      "testStandardBooleanAttributes",
      "testCommonJSSupport",
        // WA for `package.json`
      "testLocalWebTypes",
        -> modules

      else -> super.adjustModules(modules)
    }

  @Test
  fun testDirectivesWithoutParameters() {
    checkHighlighting()
  }

  @Test
  fun testVIfRequireParameter() {
    checkHighlighting()
  }

  @Test
  fun testArrowFunctionsAndExpressionsInTemplate() {
    checkHighlighting()
  }

  @Test
  fun testShorthandArrowFunctionInTemplate() {
    checkHighlighting()
  }

  @Test
  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    checkHighlighting()
  }

  @Test
  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
    checkHighlighting()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsArray() {
    checkHighlighting()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsObject() {
    checkHighlighting()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsObjectRef() {
    checkHighlighting()
  }

  @Test
  fun testCompRequiredAttributesTest() {
    checkHighlighting()
  }

  @Test
  fun testCompRequiredAttributesTestTS() {
    checkHighlighting()
  }

  @Test
  fun testRequiredAttributeWithModifierTest() {
    checkHighlighting()
  }

  @Test
  fun testRequiredAttributeWithVModel() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testRequiredAttributeWithVModel3() {
    checkHighlighting()
  }

  @Test
  fun testVueAttributeInCustomTag() {
    checkHighlighting()
  }

  @Test
  fun testVFor() {
    runInInitMode {
      checkHighlighting()
    }
  }

  @Test
  fun testVForInPug() {
    runInInitMode {
      checkHighlighting()
    }
  }

  @Test
  fun testTopLevelThisInInjection() {
    checkHighlighting()
  }

  @Test
  fun testTextarea() {
    checkHighlighting()
  }

  @Test
  fun testGlobalComponentLiteral() {
    checkHighlighting {
      // Tree access disabled
      //  /index.js
      disableAstLoadingFilter()
    }
  }

  @Test
  fun testExternalMixin() {
    checkHighlighting()
  }

  @Test
  fun testTwoExternalMixins() {
    checkHighlighting()
  }

  @Test
  fun testTwoGlobalMixins() {
    checkHighlighting()
  }

  @Test
  fun testNotImportedComponentIsUnknown() {
    checkHighlighting()
  }

  @Test
  fun testNoDoubleSpellCheckingInAttributesWithEmbeddedContents() {
    checkHighlighting(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testNoSpellcheckInEnumeratedAttributes() {
    checkHighlighting(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testSpellchecking() {
    checkHighlighting(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testTypeScriptTypesAreResolved() {
    checkHighlighting()
  }

  @Test
  fun testVBindVOnHighlighting() {
    checkHighlighting()
  }

  @Test
  fun testComponentNameAsStringTemplate() {
    checkHighlighting()
  }

  @Test
  fun testTypeScriptTypesInVue() {
    checkHighlighting(
      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java),
    )
  }

  @Test
  fun testCustomDirectives() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")
    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    checkHighlighting(
      configureFileName = "App.vue",
      additionalDependencies = mapOf("my-vue-items-library" to "*"),
    )
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesWithModifiersFromGlobalDirectives() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testEmptyAttributeValue() {
    checkHighlighting()
  }

  @Test
  fun testNoCreateVarQuickFix() {
    doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.variable.intention.name", "someNonExistingReference2389"),
      )

      assertEmpty(intentions)
    }
  }

  @Test
  fun testNoCreateFunctionQuickFix() {
    doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.function.intention.name", "notExistingF"),
      )

      assertEmpty(intentions)
    }
  }

  @Test
  fun testNoCreateClassQuickFix() {
    doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.class.intention.name", "NotExistingClass"),
      )

      assertEmpty(intentions)
    }
  }

  @Test
  fun testNoSplitTagInsideInjection() {
    myFixture.configureByText("NoSplitTagInsideInjection.vue", """
<template>
{{ <caret>injection }}
</template>
""")
    var intentions = myFixture.filterAvailableIntentions("Split current tag")
    TestCase.assertTrue(intentions.isEmpty())

    //but near
    myFixture.configureByText("NoSplitTagInsideInjection2.vue", """
<template>
{{ injection }} here <caret>we can split
</template>
""")
    intentions = myFixture.filterAvailableIntentions("Split current tag")
    TestCase.assertFalse(intentions.isEmpty())
  }

  @Test
  fun testEmptyTagsForVueAreAllowed() {
    checkHighlighting()
  }

  @Test
  fun testBuiltinTagsHighlighting() {
    checkHighlighting(VueTestModule.VUE_2_5_3)
  }

  @Test
  fun testNonPropsAttributesAreNotHighlighted() {
    checkHighlighting()
  }

  @Test
  fun testVueAttributeWithoutValueWithFollowingAttribute() {
    checkHighlighting()
  }

  @Test
  fun testTsxIsNormallyParsed() {
    checkHighlighting()
  }

  @Test
  fun testJadeWithVueShortcutAttributes() {
    checkHighlighting()
  }

  @Test
  fun testComponentsNamedLikeHtmlTags() {
    checkHighlighting()
  }

  @Test
  fun testClassComponentAnnotationWithLocalComponent() {
    myFixture.configureVueDependencies()
    createTwoClassComponents(myFixture)
    checkHighlighting()
  }

  @Test
  fun testClassComponentAnnotationWithLocalComponentTs() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    checkHighlighting()
  }

  @Test
  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  @Test
  fun testLocalComponentExtendsInClassSyntax() {
    checkHighlighting()
  }

  @Test
  fun testLocalComponentInClassSyntax() {
    checkHighlighting()
  }

  @Test
  fun testLocalComponentInMixin() {
    checkHighlighting()
  }

  @Test
  fun testLocalComponentInMixinRecursion() {
    checkHighlighting()
  }

  @Test
  fun testBooleanProps() {
    checkHighlighting()
  }

  @Test
  fun testRecursiveMixedMixins() {
    defineRecursiveMixedMixins(myFixture)
    myFixture.configureByText("RecursiveMixedMixins.vue", """
        <template>
          <<warning descr="Element HiddenComponent doesn't have required attribute from-d"><warning descr="Element HiddenComponent doesn't have required attribute from-hidden">HiddenComponent</warning></warning>/>
          <<warning descr="Element OneMoreComponent doesn't have required attribute from-d"><warning descr="Element OneMoreComponent doesn't have required attribute from-one-m-ore">OneMoreComponent</warning></warning>/>
        </template>
      """)
    myFixture.checkHighlighting()
  }

  @Test
  fun testTopLevelTags() {
    checkHighlighting()
  }

  @Test
  fun testEndTagNotForbidden() {
    checkHighlighting()
  }

  @Test
  fun testColonInEventName() {
    checkHighlighting()
  }

  @Test
  fun testNoVueTagErrorsInPlainXml() {
    myFixture.addFileToProject("any.vue", "") // to make sure that Vue support works for the project
    myFixture.configureByText("foo.xml", "<component><foo/></component>".trimMargin())
    myFixture.checkHighlighting()
  }

  @Test
  fun testSemanticHighlighting() {
    doConfiguredTest {
      checkHighlightingWithSymbolNames(this, false, false, true)
    }
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  fun testVSlotSyntax() {
    checkHighlighting()
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  fun testSlotSyntax() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testSlotName() {
    checkHighlighting()
  }

  @Test
  fun testSlotNameBinding() {
    checkHighlighting()
  }

  @Test
  fun testVueExtendSyntax() {
    checkHighlighting(
      VueTestModule.VUE_2_5_3,
    ) {
      // Tree access disabled
      //  /a-component.vue
      disableAstLoadingFilter()
    }
  }

  @Test
  fun testBootstrapVue() {
    checkHighlighting(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testDestructuringPatternsInVFor() {
    checkHighlighting()
  }

  @Test
  fun testDirectivesWithParameters() {
    checkHighlighting()
  }

  @Test
  fun testDirectiveWithModifiers() {
    checkHighlighting(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testIsAttributeSupport() {
    checkHighlighting()
  }

  @Test
  fun testKeyAttributeSupport() {
    checkHighlighting()
  }

  @Test
  fun testPropsWithOptions() {
    checkHighlighting()
  }

  @Test
  fun testFilters() {
    checkHighlighting()
  }

  @Test
  fun testEmptyTags() {
    myFixture.configureVueDependencies()
    myFixture.enableInspections(CheckTagEmptyBodyInspection())
    myFixture.copyDirectoryToProject("emptyTags", ".")
    for (file in listOf("test.vue", "test-html.html", "test-reg.html")) {
      myFixture.configureFromTempProjectFile(file)
      myFixture.checkHighlighting()
    }
  }

  @Test
  fun testComputedPropType() {
    checkHighlighting()
  }

  @Test
  fun testPseudoSelectors() {
    checkHighlighting(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testPrivateMembersHighlighting() {
    checkHighlighting(
      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java),
    )
  }

  @Test
  fun testMultipleScriptTagsInHTML() {
    checkHighlighting(configureFileName = "multipleScriptTagsInHTML.html")
  }

  @Test
  fun testMultipleScriptTagsInVue() {
    checkHighlighting()
  }

  @Test
  fun testCompositionApiBasic_0_4_0() {
    checkHighlighting(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_0_4_0,
      configureFileName = "compositionComponent1.vue",
    )
  }

  @Test
  fun testCompositionApiBasic_1_0_0() {
    checkHighlighting(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_1_0_0,
      configureFileName = "compositionComponent1.vue",
    )
  }

  @Test
  fun testSimpleVueHtml() {
    // Tree access disabled
    //  /xml/xml-psi-impl/psi-impl.jar!/standardSchemas/xml.xsd
    disableAstLoadingFilter()

    for (suffix in listOf("cdn", "cdn2", "cdn3", "cdn.js", "cdn@", "js", "deep")) {
      myFixture.configureByFile("simple-vue/simple-vue-${suffix}.html")
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
  fun testCommonJSSupport() {
    checkHighlighting(VueTestModule.VUEX_3_1_0)
  }

  @Test
  fun testComputedTypeTS() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testComputedTypeJS() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testDataTypeTS() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testScssBuiltInModules() {
    // Tree access disabled
    //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
    disableAstLoadingFilter()

    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      checkHighlighting(
        inspections = listOf(
          CssInvalidFunctionInspection::class.java,
          SassScssResolvedByNameOnlyInspection::class.java,
          SassScssUnresolvedVariableInspection::class.java,
        ),
      )
    }
  }

  @Test
  fun testSassBuiltInModules() {
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      checkHighlighting(
        inspections = listOf(
          CssInvalidFunctionInspection::class.java,
          SassScssResolvedByNameOnlyInspection::class.java,
          SassScssUnresolvedVariableInspection::class.java,
        ),
      ) {
        // Tree access disabled
        //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
        disableAstLoadingFilter()
      }
    }
  }

  @Test
  fun testIndirectExport() {
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testAsyncSetup() {
    checkHighlighting(VueTestModule.VUE_3_0_0)
  }

  @Test
  fun testScriptSetup() {
    checkHighlighting(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testScriptSetupComplexImports() {
    checkHighlighting(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testMissingLabelSuppressed() {
    checkHighlighting(
      inspections = listOf(HtmlFormInputWithoutLabelInspection::class.java),
    )
  }

  @Test
  fun testSuperComponentMixin() {
    checkHighlighting()
  }

  @Test
  fun testCompositionPropsJS() {
    checkHighlighting()
  }

  @Test
  fun testCssSelectors() {
    checkHighlighting(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testCssUnusedPseudoSelector() {
    checkHighlighting {
      enableInspections(CssUnusedSymbolInspection())
    }
  }

  @Test
  fun testScriptSetupScopePriority() {
    checkHighlighting()
  }

  @Test
  fun testBindingToDataAttributes() {
    checkHighlighting()
  }

  @Test
  fun testPropsValidation() {
    checkHighlighting(
      additionalDependencies = mapOf("lib" to "*"),
    )
  }

  @Test
  fun testScriptSetupRef() {
    checkHighlighting(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testScriptSetupImportedDirective() {
    checkHighlighting(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testTypedComponentsScriptSetup() {
    checkHighlighting(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testTypedComponentsScriptSetup2() {
    checkHighlighting(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testCssVBind() {
    checkHighlighting(
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testCssVBindVue31() {
    checkHighlighting(
      VueTestModule.VUE_3_1_0,
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testGlobalSymbols() {
    checkHighlighting()
  }

  @Test
  fun testStandardBooleanAttributes() {
    checkHighlighting()
  }

  @Test
  fun testRefUnwrap() {
    checkHighlighting()
  }

  @Test
  fun testVModelWithMixin() {
    checkHighlighting(configureFileName = "MyForm.vue")
  }

  @Test
  fun testScriptSetupSymbolsHighlighting() {
    doConfiguredTest {
      checkHighlightingWithSymbolNames(this, true, true, true)
    }
  }

  @Test
  fun testSlotTypes() {
    checkHighlighting(
      VueTestModule.QUASAR_2_6_5,
      VueTestModule.VUE_3_5_0,
      configureFileName = "MyTable.vue",
    )
  }

  @Test
  fun testGlobalScriptSetup() {
    checkHighlighting(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testDynamicArguments() {
    checkHighlighting(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testWithPropsFromFunctionCall() {
    checkHighlighting()
  }

  @Test
  fun testWithPropsFromFunctionCall2() {
    checkHighlighting()
  }

  @Test
  fun testInferPropType() {
    checkHighlighting(
      VueTestModule.VUE_3_2_2,
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
    )
  }

  @Test
  fun testLocalWebTypes() {
    // Tree access disabled
    //  /Test2.vue
    disableAstLoadingFilter()

    checkHighlighting(
      configureFileName = "main.vue",
    )
  }

  @Test
  fun testPropertyReferenceInLambda() {
    checkHighlighting()
  }

  @Test
  fun testSourceScopedSlots() {
    checkHighlighting(configureFileName = "Catalogue.vue")
  }

  @Test
  fun testCustomEvents() {
    checkHighlighting()
  }

  @Test
  fun testCustomEventsTypedComponent() {
    checkHighlighting()
  }

  @Test
  fun testLifecycleEventsVue2ClassComponent() {
    checkHighlighting(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue2VueExtend() {
    checkHighlighting(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3Options() {
    checkHighlighting(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3DefineComponent() {
    checkHighlighting(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testIdIndexer() {
    checkHighlighting(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testVueCreateApp() {
    checkHighlighting(configureFileName = "test.html")
  }

  @Test
  fun testInstanceMountedOnElement() {
    checkHighlighting(configureFileName = "test.html")
  }

  @Test
  fun testScriptCaseSensitivity() {
    checkHighlighting()
  }

  @Test
  fun testVPre() {
    checkHighlighting()
  }

  @Test
  fun testHtmlTagOmission() {
    checkHighlighting(configureFileName = "htmlTagOmission.html")
  }

  @Test
  fun testVueNoTagOmission() {
    checkHighlighting()
  }

  @Test
  fun testScriptSetupGeneric() {
    checkHighlighting()
  }

  @Test
  fun testGenericComponentUsage() {
    checkHighlighting()
  }

  @Test
  fun testComponentFromFunctionPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromFunctionPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromObjectPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromObjectPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromFunctionPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromFunctionPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromObjectPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromObjectPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle() {
    checkHighlighting(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testStdTagsInspections() {
    checkHighlighting(
      inspections = listOf(
        HtmlRequiredTitleElementInspection::class.java,
        HtmlRequiredAltAttributeInspection::class.java,
      ),
    )
  }

  @Test
  fun testPropTypeJsDoc() {
    checkHighlighting(
      inspections = listOf(JSValidateTypesInspection::class.java),
    )
  }

  @Test
  fun testPropsWithDefaults() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    checkHighlighting()
  }

  @Test
  fun testPropsWithDefaultsInTs() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    checkHighlighting(configureFileName = "propsWithDefaultsInTs.ts")
  }

  @Test
  fun testVuetifyWebTypesWithTrailingNewLine() {
    checkHighlighting(VueTestModule.VUETIFY_3_3_3)
  }

  @Test
  fun testBindShorthandAttribute() {
    checkHighlighting()
  }

  @Test
  fun testWatchProperty() {
    checkHighlighting(configureFileName = "watchProperty.js")
  }

  @Test
  fun testTypedMixins() {
    checkHighlighting(configureFileName = "index.js")
  }

  @Test
  fun testVaporSimpleApplication() {
    checkHighlighting(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }
}

fun createTwoClassComponents(fixture: CodeInsightTestFixture, tsLang: Boolean = false) {
  val lang = if (tsLang) " lang=\"ts\"" else ""
  fixture.configureByText("LongComponent.vue",
                          """
  <script$lang>
  import { Component, Vue } from 'vue-property-decorator';
  @Component({
    name: 'long-vue'
  })
  export default class LongComponent extends Vue {
  }
  </script>
  """)
  fixture.configureByText("ShortComponent.vue",
                          """
  <script$lang>
  import { Component, Vue } from 'vue-property-decorator';
  @Component
  export default class ShortComponent extends Vue {
  }
  </script>
  """)
}

fun createLocalComponentsExtendsData(fixture: CodeInsightTestFixture, withMarkup: Boolean = true) {
  fixture.configureByText("CompA.vue", """
  <template>
      <div>{{ propFromA }}</div>
  </template>

  <script>
      export default {
          name: "CompA",
          props: {
              propFromA: {
                  required: true
              }
          }
      }
  </script>
  """)

  val nameWithMarkup = if (withMarkup)
    "<warning descr=\"Element CompB doesn't have required attribute prop-from-a\">CompB</warning>"
  else
    "CompB"

  fixture.configureByText("CompB.vue", """
  <template>
      <$nameWithMarkup <caret>/>
  </template>

  <script>
      import CompA from 'CompA'
      export default {
          name: "CompB",
          extends: CompA
      }
  </script>
  """)
}

fun defineRecursiveMixedMixins(fixture: CodeInsightTestFixture) {
  fixture.configureByText("hidden-component.vue", """
  <script>
      export default {
          name: "hidden-component",
          props: {
              fromHidden: {
                  required: true
              }
          }
      }
  </script>
        """)
  fixture.configureByText("d-component.vue", """
  <template>
      <hidden-component/>
  </template>

  <script>
      import HiddenComponent from "./hidden-component";
      export default {
          name: "d-component",
          components: {HiddenComponent},
          props: {
              fromD: {
                  required: true
              }
          }
      }
  </script>
        """)
  fixture.configureByText("OneMoreComponent.vue", """
          <script>
            @Component({
              props: {
                fromOneMOre: {
                  required: true
                }
              }
            })
            export default class Kuku extends Vue {

            }
          </script>
        """)
  fixture.configureByText("GlobalMixin.js", """
          import OneMoreComponent from './OneMoreComponent.vue'
          import DComponent from './d-component.vue'
          Vue.mixin({
            components: { OneMoreComponent },
            mixins: [ DComponent ]
          })
        """)
}
