// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.JSTestUtils.checkHighlightingWithSymbolNames
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.disableAstLoadingFilter
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInInitMode
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import com.intellij.xml.util.CheckTagEmptyBodyInspection
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
  VueHighlightingWithPluginTestBase() {

  @Ignore
  class WithLegacyPluginTest :
    VueHighlightingWithPluginTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueHighlightingTestBase(testMode = VueTestMode.NO_PLUGIN)
}

abstract class VueHighlightingWithPluginTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueHighlightingTestBase(testMode = testMode)

@RunWith(JUnit4::class)
abstract class VueHighlightingTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("highlighting", testMode = testMode) {

  override val dirModeByDefault: Boolean = true

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      addAll(super.adjustConfigurators(configurators))

      if (none { it is VueTsConfigFile }) {
        add(VueTsConfigFile())
      }
    }

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> {
    // WA for `package.json`
    if (name == "testLocalWebTypes")
      return modules

    return super.adjustModules(modules)
  }

  @Test
  fun testDirectivesWithoutParameters() {
    doHighlightingTest()
  }

  @Test
  fun testVIfRequireParameter() {
    doHighlightingTest()
  }

  @Test
  fun testArrowFunctionsAndExpressionsInTemplate() {
    doHighlightingTest()
  }

  @Test
  fun testShorthandArrowFunctionInTemplate() {
    doHighlightingTest()
  }

  @Test
  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    doHighlightingTest()
  }

  @Test
  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
    doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsArray() {
    doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsObject() {
    doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsObjectRef() {
    doHighlightingTest()
  }

  @Test
  fun testCompRequiredAttributesTest() {
    doHighlightingTest()
  }

  @Test
  fun testCompRequiredAttributesTestTS() {
    doHighlightingTest()
  }

  @Test
  fun testRequiredAttributeWithModifierTest() {
    doHighlightingTest()
  }

  @Test
  fun testRequiredAttributeWithVModel() {
    doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testRequiredAttributeWithVModel3() {
    doHighlightingTest()
  }

  @Test
  fun testVueAttributeInCustomTag() {
    doHighlightingTest()
  }

  @Test
  fun testVFor() {
    runInInitMode {
      doHighlightingTest()
    }
  }

  @Test
  fun testVForInPug() {
    runInInitMode {
      doHighlightingTest()
    }
  }

  @Test
  fun testTopLevelThisInInjection() {
    doHighlightingTest()
  }

  @Test
  fun testTextarea() {
    doHighlightingTest()
  }

  @Test
  fun testGlobalComponentLiteral() {
    doHighlightingTest {
      // Tree access disabled
      //  /index.js
      disableAstLoadingFilter()
    }
  }

  @Test
  fun testExternalMixin() {
    doHighlightingTest()
  }

  @Test
  fun testTwoExternalMixins() {
    doHighlightingTest()
  }

  @Test
  fun testTwoGlobalMixins() {
    doHighlightingTest()
  }

  @Test
  fun testNotImportedComponentIsUnknown() {
    doHighlightingTest()
  }

  @Test
  fun testNoDoubleSpellCheckingInAttributesWithEmbeddedContents() {
    doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testNoSpellcheckInEnumeratedAttributes() {
    doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testSpellchecking() {
    doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testTypeScriptTypesAreResolved() {
    doHighlightingTest()
  }

  @Test
  fun testVBindVOnHighlighting() {
    doHighlightingTest()
  }

  @Test
  fun testComponentNameAsStringTemplate() {
    doHighlightingTest()
  }

  @Test
  fun testTypeScriptTypesInVue() {
    doHighlightingTest(
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
    doHighlightingTest(
      configureFileName = "App.vue",
      additionalDependencies = mapOf("my-vue-items-library" to "*"),
      configurators = listOf(
        VueTsConfigFile(types = listOf("my-vue-items-library/dist/volar")),
      ),
    ) {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesWithModifiersFromGlobalDirectives() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testEmptyAttributeValue() {
    doHighlightingTest()
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
    assertEmpty(intentions)

    //but near
    myFixture.configureByText("NoSplitTagInsideInjection2.vue", """
<template>
{{ injection }} here <caret>we can split
</template>
""")
    intentions = myFixture.filterAvailableIntentions("Split current tag")
    assertNotEmpty(intentions)
  }

  @Test
  fun testEmptyTagsForVueAreAllowed() {
    doHighlightingTest()
  }

  @Test
  fun testBuiltinTagsHighlighting() {
    doHighlightingTest(VueTestModule.VUE_2_5_3)
  }

  @Test
  fun testNonPropsAttributesAreNotHighlighted() {
    doHighlightingTest()
  }

  @Test
  fun testVueAttributeWithoutValueWithFollowingAttribute() {
    doHighlightingTest()
  }

  @Test
  fun testTsxIsNormallyParsed() {
    doHighlightingTest()
  }

  @Test
  fun testJadeWithVueShortcutAttributes() {
    doHighlightingTest()
  }

  @Test
  fun testComponentsNamedLikeHtmlTags() {
    doHighlightingTest()
  }

  @Test
  fun testClassComponentAnnotationWithLocalComponent() {
    myFixture.configureVueDependencies()
    createTwoClassComponents(myFixture)
    doHighlightingTest()
  }

  @Test
  fun testClassComponentAnnotationWithLocalComponentTs() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    doHighlightingTest()
  }

  @Test
  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  @Test
  fun testLocalComponentExtendsInClassSyntax() {
    doHighlightingTest()
  }

  @Test
  fun testLocalComponentInClassSyntax() {
    doHighlightingTest()
  }

  @Test
  fun testLocalComponentInMixin() {
    doHighlightingTest()
  }

  @Test
  fun testLocalComponentInMixinRecursion() {
    doHighlightingTest()
  }

  @Test
  fun testBooleanProps() {
    doHighlightingTest()
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
    doHighlightingTest()
  }

  @Test
  fun testEndTagNotForbidden() {
    doHighlightingTest()
  }

  @Test
  fun testColonInEventName() {
    doHighlightingTest()
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
    doHighlightingTest()
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  fun testSlotSyntax() {
    doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testSlotName() {
    doHighlightingTest()
  }

  @Test
  fun testSlotNameBinding() {
    doHighlightingTest()
  }

  @Test
  fun testVueExtendSyntax() {
    doHighlightingTest(
      VueTestModule.VUE_2_5_3,
    ) {
      // Tree access disabled
      //  /a-component.vue
      disableAstLoadingFilter()
    }
  }

  @Test
  fun testBootstrapVue() {
    doHighlightingTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testDestructuringPatternsInVFor() {
    doHighlightingTest()
  }

  @Test
  fun testDirectivesWithParameters() {
    doHighlightingTest()
  }

  @Test
  fun testDirectiveWithModifiers() {
    doHighlightingTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testIsAttributeSupport() {
    doHighlightingTest()
  }

  @Test
  fun testKeyAttributeSupport() {
    doHighlightingTest()
  }

  @Test
  fun testPropsWithOptions() {
    doHighlightingTest()
  }

  @Test
  fun testFilters() {
    doHighlightingTest()
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
    doHighlightingTest()
  }

  @Test
  fun testPseudoSelectors() {
    doHighlightingTest(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testPrivateMembersHighlighting() {
    doHighlightingTest(
      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java),
    )
  }

  @Test
  fun testMultipleScriptTagsInHTML() {
    doHighlightingTest(configureFileName = "multipleScriptTagsInHTML.html")
  }

  @Test
  fun testMultipleScriptTagsInVue() {
    doHighlightingTest()
  }

  @Test
  fun testCompositionApiBasic_0_4_0() {
    doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_0_4_0,
      configureFileName = "compositionComponent1.vue",
    )
  }

  @Test
  fun testCompositionApiBasic_1_0_0() {
    doHighlightingTest(
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
    doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.VUEX_3_1_0,
    ) {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testComputedTypeTS() {
    doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testComputedTypeJS() {
    doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testDataTypeTS() {
    doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      // TEMP
      configurators = listOf(
        VueTsConfigFile(enabled = false)
      ),
    )
  }

  @Test
  fun testScssBuiltInModules() {
    // Tree access disabled
    //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
    disableAstLoadingFilter()

    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doHighlightingTest(
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
      doHighlightingTest(
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
    doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testAsyncSetup() {
    doHighlightingTest(VueTestModule.VUE_3_0_0)
  }

  @Test
  fun testScriptSetup() {
    doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testScriptSetupComplexImports() {
    doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testMissingLabelSuppressed() {
    doHighlightingTest(
      inspections = listOf(HtmlFormInputWithoutLabelInspection::class.java),
    )
  }

  @Test
  fun testSuperComponentMixin() {
    doHighlightingTest {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testCompositionPropsJS() {
    doHighlightingTest()
  }

  @Test
  fun testCssSelectors() {
    doHighlightingTest(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testCssUnusedPseudoSelector() {
    doHighlightingTest {
      enableInspections(CssUnusedSymbolInspection())
    }
  }

  @Test
  fun testScriptSetupScopePriority() {
    doHighlightingTest(
      // TEMP
      configurators = listOf(
        VueTsConfigFile(enabled = false)
      ),
    )
  }

  @Test
  fun testBindingToDataAttributes() {
    doHighlightingTest()
  }

  @Test
  fun testPropsValidation() {
    doHighlightingTest(
      additionalDependencies = mapOf("lib" to "*"),
    ) {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testScriptSetupRef() {
    doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testScriptSetupImportedDirective() {
    doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testTypedComponentsScriptSetup() {
    doHighlightingTest(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testTypedComponentsScriptSetup2() {
    doHighlightingTest(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testCssVBind() {
    doHighlightingTest(
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testCssVBindVue31() {
    doHighlightingTest(
      VueTestModule.VUE_3_1_0,
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testGlobalSymbols() {
    doHighlightingTest()
  }

  @Test
  fun testStandardBooleanAttributes() {
    doHighlightingTest()
  }

  @Test
  fun testRefUnwrap() {
    doHighlightingTest()
  }

  @Test
  fun testVModelWithMixin() {
    doHighlightingTest(configureFileName = "MyForm.vue")
  }

  @Test
  fun testScriptSetupSymbolsHighlighting() {
    doConfiguredTest {
      checkHighlightingWithSymbolNames(this, true, true, true)
    }
  }

  @Test
  fun testSlotTypes() {
    doHighlightingTest(
      VueTestModule.QUASAR_2_6_5,
      VueTestModule.VUE_3_5_0,
      configureFileName = "MyTable.vue",
    ) {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testGlobalScriptSetup() {
    doHighlightingTest(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testDynamicArguments() {
    doHighlightingTest(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testWithPropsFromFunctionCall() {
    doHighlightingTest()
  }

  @Test
  fun testWithPropsFromFunctionCall2() {
    doHighlightingTest()
  }

  @Test
  fun testInferPropType() {
    doHighlightingTest(
      VueTestModule.VUE_3_2_2,
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
    )
  }

  @Test
  fun testLocalWebTypes() {
    // Tree access disabled
    //  /Test2.vue
    disableAstLoadingFilter()

    doHighlightingTest(
      configureFileName = "main.vue",
    )
  }

  @Test
  fun testPropertyReferenceInLambda() {
    doHighlightingTest()
  }

  @Test
  fun testSourceScopedSlots() {
    doHighlightingTest(configureFileName = "Catalogue.vue") {
      disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testCustomEvents() {
    doHighlightingTest()
  }

  @Test
  fun testCustomEventsTypedComponent() {
    doHighlightingTest()
  }

  @Test
  fun testLifecycleEventsVue2ClassComponent() {
    doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue2VueExtend() {
    doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3Options() {
    doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3DefineComponent() {
    doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testIdIndexer() {
    doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testVueCreateApp() {
    doHighlightingTest(configureFileName = "test.html")
  }

  @Test
  fun testInstanceMountedOnElement() {
    doHighlightingTest(configureFileName = "test.html")
  }

  @Test
  fun testScriptCaseSensitivity() {
    doHighlightingTest()
  }

  @Test
  fun testVPre() {
    doHighlightingTest()
  }

  @Test
  fun testHtmlTagOmission() {
    doHighlightingTest(configureFileName = "htmlTagOmission.html")
  }

  @Test
  fun testVueNoTagOmission() {
    doHighlightingTest()
  }

  @Test
  fun testScriptSetupGeneric() {
    doHighlightingTest()
  }

  @Test
  fun testGenericComponentUsage() {
    doHighlightingTest()
  }

  @Test
  fun testComponentFromFunctionPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromFunctionPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromObjectPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromObjectPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromFunctionPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromFunctionPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromObjectPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromObjectPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle() {
    doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() {
    doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testStdTagsInspections() {
    doHighlightingTest(
      inspections = listOf(
        HtmlRequiredTitleElementInspection::class.java,
        HtmlRequiredAltAttributeInspection::class.java,
      ),
    )
  }

  @Test
  fun testPropTypeJsDoc() {
    doHighlightingTest(
      inspections = listOf(JSValidateTypesInspection::class.java),
    )
  }

  @Test
  fun testPropsWithDefaults() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    doHighlightingTest()
  }

  @Test
  fun testPropsWithDefaultsInTs() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    doHighlightingTest(configureFileName = "propsWithDefaultsInTs.ts")
  }

  @Test
  fun testVuetifyWebTypesWithTrailingNewLine() {
    doHighlightingTest(VueTestModule.VUETIFY_3_3_3)
  }

  @Test
  fun testBindShorthandAttribute() {
    doHighlightingTest()
  }

  @Test
  fun testWatchProperty() {
    doHighlightingTest(configureFileName = "watchProperty.js")
  }

  @Test
  fun testTypedMixins() {
    doHighlightingTest(configureFileName = "index.js")
  }

  @Test
  fun testVaporSimpleApplication() {
    doHighlightingTest(
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
