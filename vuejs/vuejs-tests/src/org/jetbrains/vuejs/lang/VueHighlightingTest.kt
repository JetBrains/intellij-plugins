// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.lang.javascript.JSTestUtils.checkHighlightingWithSymbolNames
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.TrackFailedTestExtension
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.openapi.application.WriteAction
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInInitMode
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import com.intellij.xml.util.CheckTagEmptyBodyInspection
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.plugins.scss.inspections.SassScssResolvedByNameOnlyInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueLspConfigurator
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.libraries.nuxt.NuxtHighlightingTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File


/**
 * @see VueComponentTest
 * @see VueControlFlowTest
 * @see NuxtHighlightingTest
 */
@TestApplication
class VueHighlightingTest :
  VueHighlightingWithPluginTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    VueHighlightingTestBase(
      testMode = VueTestMode.TS_GO_PROXY,
      disableTypeScriptServiceWarnings = true,
      adjustDefaultDirName = { name ->
        "${name}_tsgo_proxy"
          .takeIf { File("$testDataPath/${it}").exists() }
        ?: name
      },
    ) {

    @RegisterExtension
    @JvmField
    val rule: TrackFailedTestExtension = TrackFailedTestExtension(
      "testCompRequiredAttributesTest",
      "testCompositionApiBasic_0_4_0",
      "testCompositionApiBasic_1_0_0",
      "testComputedTypeJS",
      "testCssSelectors",
      "testDynamicArguments",
      "testEmptyAttributeValue",
      "testGlobalSymbols",
      "testLocalWebTypes",
      "testRequiredAttributeWithVModel3",
      "testSlotNameBinding",
      "testVBindVOnHighlighting",
      "testVForInPug",
      "testVSlotSyntax",
      "testVueAttributeWithoutValueWithFollowingAttribute",
    )
  }

  @TestApplication
  class WithoutServiceTest :
    VueHighlightingTestBase(
      testMode = VueTestMode.NO_PLUGIN,
      adjustDefaultDirName = { name -> name + "_legacy" },
    )
}

abstract class VueHighlightingWithPluginTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueHighlightingTestBase(testMode, { VueHighlightingWithPluginFixture(it, testMode) }) {

  @Test
  @Disabled
  // flaky
  override fun testInferPropType() {
  }

  @Test
  @Disabled
  // flaky
  override fun testSourceScopedSlots() {
  }

  @Test
  @Disabled
  // flaky
  override fun testClassComponentAnnotationWithLocalComponent() {
  }

  @Test
  @Disabled
  // flaky
  override fun testClassComponentAnnotationWithLocalComponentTs() {
  }

  @Test
  @Disabled
  // flaky
  override fun testCompRequiredAttributesTest() {
  }

  @Test
  @Disabled
  // flaky
  override fun testCompRequiredAttributesTestTS() {
  }

  @Test
  @Disabled
  // flaky
  override fun testDirectivesWithoutParameters() {
  }
}

/**
 * [disableTypeScriptServiceWarnings] hides the highlighting of the TypeScript service.
 * [adjustDefaultDirName] gets the default directory name of a test and returns the directory name for the test.
 */
abstract class VueHighlightingTestBase protected constructor(
  testMode: VueTestMode,
  createFixture: (context: HybridFixtureContext) -> VueHighlightingFixture,
) : VueTestCase(testMode, createFixture) {

  constructor(
    testMode: VueTestMode = VueTestMode.DEFAULT,
    disableTypeScriptServiceWarnings: Boolean = false,
    adjustDefaultDirName: VueFixture.(name: String) -> String = { it },
  ) : this(testMode, { VueHighlightingFixture(it, testMode, disableTypeScriptServiceWarnings, adjustDefaultDirName) })

  @Test
  open fun testDirectivesWithoutParameters() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVIfRequireParameter() {
    vue.doHighlightingTest()
  }

  @Test
  fun testArrowFunctionsAndExpressionsInTemplate() {
    vue.doHighlightingTest()
  }

  @Test
  fun testShorthandArrowFunctionInTemplate() {
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
    vue.doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsArray() {
    vue.doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsObject() {
    vue.doHighlightingTest()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsObjectRef() {
    vue.doHighlightingTest()
  }

  @Test
  open fun testCompRequiredAttributesTest() {
    vue.doHighlightingTest()
  }

  @Test
  open fun testCompRequiredAttributesTestTS() {
    vue.doHighlightingTest()
  }

  @Test
  fun testRequiredAttributeWithModifierTest() {
    vue.doHighlightingTest()
  }

  @Test
  fun testRequiredAttributeWithVModel() {
    vue.doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testRequiredAttributeWithVModel3() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVueAttributeInCustomTag() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVFor() {
    runInInitMode {
      vue.doHighlightingTest()
    }
  }

  @Test
  fun testVForInPug() {
    runInInitMode {
      vue.doHighlightingTest()
    }
  }

  @Test
  fun testTopLevelThisInInjection() {
    vue.doHighlightingTest()
  }

  @Test
  fun testTextarea() {
    vue.doHighlightingTest()
  }

  @Test
  fun testGlobalComponentLiteral() {
    vue.doHighlightingTest {
      // Tree access disabled
      //  /index.js
      vue.disableAstLoadingFilter()
    }
  }

  @Test
  fun testExternalMixin() {
    vue.doHighlightingTest()
  }

  @Test
  fun testTwoExternalMixins() {
    vue.doHighlightingTest()
  }

  @Test
  fun testTwoGlobalMixins() {
    vue.doHighlightingTest()
  }

  @Test
  fun testNotImportedComponentIsUnknown() {
    vue.doHighlightingTest()
  }

  @Test
  fun testNoDoubleSpellCheckingInAttributesWithEmbeddedContents() {
    vue.doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testNoSpellcheckInEnumeratedAttributes() {
    vue.doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testSpellchecking() {
    vue.doHighlightingTest(
      inspections = listOf(GrazieSpellCheckingInspection::class.java),
    )
  }

  @Test
  fun testTypeScriptTypesAreResolved() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVBindVOnHighlighting() {
    vue.doHighlightingTest()
  }

  @Test
  fun testComponentNameAsStringTemplate() {
    vue.doHighlightingTest()
  }

  @Test
  fun testTypeScriptTypesInVue() {
    vue.doHighlightingTest(
      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java),
    )
  }

  @Test
  fun testCustomDirectives() {
    vue.doHighlightingTest(
      configureFileName = "${vue.getTestName(false)}.vue",
      dirName = "../common/customDirectives",
    )
  }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    vue.doHighlightingTest(
      configureFileName = "App.vue",
      additionalDependencies = mapOf("my-vue-items-library" to "*"),
      configurators = listOf(
        VueTsConfigFile(types = listOf("my-vue-items-library/dist/volar")),
      ),
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesWithModifiersFromGlobalDirectives() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testEmptyAttributeValue() {
    vue.doHighlightingTest()
  }

  @Test
  fun testNoCreateVarQuickFix() {
    vue.doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.variable.intention.name", "someNonExistingReference2389"),
      )

      assertThat(intentions).isEmpty()
    }
  }

  @Test
  fun testNoCreateFunctionQuickFix() {
    vue.doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.function.intention.name", "notExistingF"),
      )

      assertThat(intentions).isEmpty()
    }
  }

  @Test
  fun testNoCreateClassQuickFix() {
    vue.doConfiguredTest {
      val intentions = filterAvailableIntentions(
        JavaScriptBundle.message("javascript.create.class.intention.name", "NotExistingClass"),
      )

      assertThat(intentions).isEmpty()
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
    assertThat(intentions).isEmpty()

    //but near
    myFixture.configureByText("NoSplitTagInsideInjection2.vue", """
<template>
{{ injection }} here <caret>we can split
</template>
""")
    intentions = myFixture.filterAvailableIntentions("Split current tag")
    assertThat(intentions).isNotEmpty()
  }

  @Test
  fun testEmptyTagsForVueAreAllowed() {
    vue.doHighlightingTest()
  }

  @Test
  fun testBuiltinTagsHighlighting() {
    vue.doHighlightingTest(VueTestModule.VUE_2_5_3)
  }

  @Test
  fun testNonPropsAttributesAreNotHighlighted() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVueAttributeWithoutValueWithFollowingAttribute() {
    vue.doHighlightingTest()
  }

  @Test
  fun testTsxIsNormallyParsed() {
    vue.doHighlightingTest()
  }

  @Test
  fun testJadeWithVueShortcutAttributes() {
    vue.doHighlightingTest()
  }

  @Test
  fun testComponentsNamedLikeHtmlTags() {
    vue.doHighlightingTest()
  }

  @Test
  open /* temp */
  fun testClassComponentAnnotationWithLocalComponent() {
    myFixture.configureVueDependencies()
    createTwoClassComponents(myFixture)
    vue.doHighlightingTest()
  }

  @Test
  open /* temp */
  fun testClassComponentAnnotationWithLocalComponentTs() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  @Test
  fun testLocalComponentExtendsInClassSyntax() {
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalComponentInClassSyntax() {
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalComponentInMixin() {
    vue.doHighlightingTest()
  }

  @Test
  fun testLocalComponentInMixinRecursion() {
    vue.doHighlightingTest()
  }

  @Test
  fun testBooleanProps() {
    vue.doHighlightingTest()
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
    vue.doHighlightingTest()
  }

  @Test
  fun testEndTagNotForbidden() {
    vue.doHighlightingTest()
  }

  @Test
  fun testColonInEventName() {
    vue.doHighlightingTest()
  }

  @Test
  fun testNoVueTagErrorsInPlainXml() {
    myFixture.addFileToProject("any.vue", "") // to make sure that Vue support works for the project
    myFixture.configureByText("foo.xml", "<component><foo/></component>".trimMargin())
    myFixture.checkHighlighting()
  }

  @Test
  fun testSemanticHighlighting() {
    vue.doConfiguredTest {
      checkHighlightingWithSymbolNames(this, false, false, true)
    }
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  fun testVSlotSyntax() {
    vue.doHighlightingTest()
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  open /* temp */
  fun testSlotSyntax() {
    vue.doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testSlotName() {
    vue.doHighlightingTest()
  }

  @Test
  fun testSlotNameBinding() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVueExtendSyntax() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_5_3,
    ) {
      // Tree access disabled
      //  /a-component.vue
      vue.disableAstLoadingFilter()
    }
  }

  @Test
  fun testBootstrapVue() {
    vue.doHighlightingTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testDestructuringPatternsInVFor() {
    vue.doHighlightingTest()
  }

  @Test
  fun testDirectivesWithParameters() {
    vue.doHighlightingTest()
  }

  @Test
  fun testDirectiveWithModifiers() {
    vue.doHighlightingTest(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
  }

  @Test
  fun testIsAttributeSupport() {
    vue.doHighlightingTest()
  }

  @Test
  fun testKeyAttributeSupport() {
    vue.doHighlightingTest()
  }

  @Test
  fun testPropsWithOptions() {
    vue.doHighlightingTest()
  }

  @Test
  fun testFilters() {
    vue.doHighlightingTest()
  }

  @Test
  fun testEmptyTags() {
    vue.doConfiguredTest(
      configureFile = false,
    ) {
      enableInspections(CheckTagEmptyBodyInspection())

      for (file in listOf("test.vue", "test-html.html", "test-reg.html")) {
        configureFromTempProjectFile(file)
        checkHighlighting()
      }
    }
  }

  @Test
  fun testComputedPropType() {
    vue.doHighlightingTest()
  }

  @Test
  fun testPseudoSelectors() {
    vue.doHighlightingTest(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testPrivateMembersHighlighting() {
    vue.doHighlightingTest(
      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java),
    )
  }

  @Test
  fun testMultipleScriptTagsInHTML() {
    vue.doHighlightingTest(configureFileName = "multipleScriptTagsInHTML.html")
  }

  @Test
  fun testMultipleScriptTagsInVue() {
    vue.doHighlightingTest()
  }

  @Test
  open /* temp */
  fun testCompositionApiBasic_0_4_0() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_0_4_0,
      configureFileName = "compositionComponent1.vue",
    )
  }

  @Test
  fun testCompositionApiBasic_1_0_0() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.COMPOSITION_API_1_0_0,
      configureFileName = "compositionComponent1.vue",
    )
  }

  @Test
  fun testSimpleVueHtml() {
    // Tree access disabled
    //  /xml/xml-psi-impl/psi-impl.jar!/standardSchemas/xml.xsd
    vue.disableAstLoadingFilter()

    for (suffix in listOf("cdn", "cdn2", "cdn3", "cdn.js", "cdn@", "js", "deep")) {
      myFixture.configureByFile("simple-vue/simple-vue-${suffix}.html")
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
  fun testCommonJSSupport() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      VueTestModule.VUEX_3_1_0,
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testComputedTypeTS() {
    vue.doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testComputedTypeJS() {
    vue.doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testDataTypeTS() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      // TEMP
      configurators = listOf(
        VueTsConfigFile(enabled = false)
      ),
    )
  }

  @Test
  fun testScssBuiltInModules() {
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      vue.doHighlightingTest(
        inspections = listOf(
          CssInvalidFunctionInspection::class.java,
          SassScssResolvedByNameOnlyInspection::class.java,
          SassScssUnresolvedVariableInspection::class.java,
        ),
      ) {
        // Tree access disabled
        //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
        vue.disableAstLoadingFilter()
      }
    }
  }

  @Test
  fun testSassBuiltInModules() {
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      vue.doHighlightingTest(
        inspections = listOf(
          CssInvalidFunctionInspection::class.java,
          SassScssResolvedByNameOnlyInspection::class.java,
          SassScssUnresolvedVariableInspection::class.java,
        ),
      ) {
        // Tree access disabled
        //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
        vue.disableAstLoadingFilter()
      }
    }
  }

  @Test
  fun testIndirectExport() {
    vue.doHighlightingTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testAsyncSetup() {
    vue.doHighlightingTest(VueTestModule.VUE_3_0_0)
  }

  @Test
  fun testScriptSetup() {
    vue.doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testScriptSetupComplexImports() {
    vue.doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testMissingLabelSuppressed() {
    vue.doHighlightingTest(
      inspections = listOf(HtmlFormInputWithoutLabelInspection::class.java),
    )
  }

  @Test
  fun testSuperComponentMixin() {
    vue.doHighlightingTest {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testCompositionPropsJS() {
    vue.doHighlightingTest()
  }

  @Test
  fun testCssSelectors() {
    vue.doHighlightingTest(
      inspections = listOf(CssInvalidPseudoSelectorInspection::class.java),
    )
  }

  @Test
  fun testCssUnusedPseudoSelector() {
    vue.doHighlightingTest {
      enableInspections(CssUnusedSymbolInspection())
    }
  }

  @Test
  fun testScriptSetupScopePriority() {
    vue.doHighlightingTest(
      // TEMP
      configurators = listOf(
        VueTsConfigFile(enabled = false)
      ),
    )
  }

  @Test
  fun testBindingToDataAttributes() {
    vue.doHighlightingTest()
  }

  @Test
  fun testPropsValidation() {
    vue.doHighlightingTest(
      additionalDependencies = mapOf("lib" to "*"),
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testScriptSetupRef() {
    vue.doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
      configurators = listOf(
        VueTsConfigFile(
          vueCompilerOptions = VueCompilerOptions(
            inferTemplateDollarRefs = true,
          ),
        ),
      ),
    )
  }

  @Test
  fun testScriptSetupRefTS() {
    vue.doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
      configurators = listOf(
        VueTsConfigFile(
          vueCompilerOptions = VueCompilerOptions(
            inferTemplateDollarRefs = true,
          ),
        ),
      ),
    )
  }

  @Test
  fun testScriptSetupImportedDirective() {
    vue.doHighlightingTest(
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    )
  }

  @Test
  fun testTypedComponentsScriptSetup() {
    vue.doHighlightingTest(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testTypedComponentsScriptSetup2() {
    vue.doHighlightingTest(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      inspections = listOf(ES6UnusedImportsInspection::class.java),
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testCssVBind() {
    vue.doHighlightingTest(
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testCssVBindVue31() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_1_0,
      inspections = listOf(CssInvalidFunctionInspection::class.java),
    )
  }

  @Test
  fun testGlobalSymbols() {
    vue.doHighlightingTest()
  }

  @Test
  fun testStandardBooleanAttributes() {
    vue.doHighlightingTest()
  }

  @Test
  fun testRefUnwrap() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVModelWithMixin() {
    vue.doHighlightingTest(configureFileName = "MyForm.vue")
  }

  @Test
  fun testScriptSetupSymbolsHighlighting() {
    vue.doConfiguredTest {
      checkHighlightingWithSymbolNames(this, true, true, true)
    }
  }

  @Test
  fun testSlotTypes() {
    vue.doHighlightingTest(
      VueTestModule.QUASAR_2_6_5,
      configureFileName = "MyTable.vue",
    ) {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testGlobalScriptSetup() {
    vue.doHighlightingTest(configureFileName = "HelloWorld.vue")
  }

  @Test
  open /* temp */
  fun testDynamicArguments() {
    vue.doHighlightingTest(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testWithPropsFromFunctionCall() {
    vue.doHighlightingTest()
  }

  @Test
  fun testWithPropsFromFunctionCall2() {
    vue.doHighlightingTest()
  }

  @Test
  open /* temp */
  fun testInferPropType() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_2_2,
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
    )
  }

  @Test
  fun testLocalWebTypes() {
    vue.doHighlightingTest(
      configureFileName = "main.vue",
    ) {
      // Tree access disabled
      //  /Test2.vue
      vue.disableAstLoadingFilter()
    }
  }

  @Test
  fun testPropertyReferenceInLambda() {
    vue.doHighlightingTest()
  }

  @Test
  open /* temp */
  fun testSourceScopedSlots() {
    vue.doHighlightingTest(configureFileName = "Catalogue.vue") {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testCustomEvents() {
    vue.doHighlightingTest()
  }

  @Test
  fun testCustomEventsTypedComponent() {
    vue.doHighlightingTest {
      vue.disableAstLoadingFilterWhenPluginUsed()
    }
  }

  @Test
  fun testLifecycleEventsVue2ClassComponent() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue2VueExtend() {
    vue.doHighlightingTest(
      VueTestModule.VUE_2_6_10,
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3Options() {
    vue.doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testLifecycleEventsVue3DefineComponent() {
    vue.doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testIdIndexer() {
    vue.doHighlightingTest(
      inspections = listOf(
        JSUnusedLocalSymbolsInspection::class.java,
        JSUnusedGlobalSymbolsInspection::class.java,
      ),
    )
  }

  @Test
  fun testVueCreateApp() {
    vue.doHighlightingTest(configureFileName = "test.html")
  }

  @Test
  fun testInstanceMountedOnElement() {
    vue.doHighlightingTest(configureFileName = "test.html")
  }

  @Test
  fun testScriptCaseSensitivity() {
    vue.doHighlightingTest()
  }

  @Test
  fun testVPre() {
    vue.doHighlightingTest()
  }

  @Test
  fun testHtmlTagOmission() {
    vue.doHighlightingTest(configureFileName = "htmlTagOmission.html")
  }

  @Test
  open /* temp */
  fun testVueNoTagOmission() {
    vue.doHighlightingTest()
  }

  @Test
  fun testScriptSetupGeneric() {
    vue.doHighlightingTest()
  }

  @Test
  fun testGenericComponentUsage() {
    vue.doHighlightingTest()
  }

  @Test
  fun testComponentFromFunctionPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromFunctionPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromObjectPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromObjectPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromFunctionPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromFunctionPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromObjectPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromObjectPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle() {
    vue.doHighlightingTest(configureFileName = "App.vue")
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }

  @Test
  fun testStdTagsInspections() {
    vue.doHighlightingTest(
      inspections = listOf(
        HtmlRequiredTitleElementInspection::class.java,
        HtmlRequiredAltAttributeInspection::class.java,
      ),
    )
  }

  @Test
  fun testPropTypeJsDoc() {
    vue.doHighlightingTest(
      inspections = listOf(JSValidateTypesInspection::class.java),
    )
  }

  @Test
  fun testPropsWithDefaults() {
    vue.doHighlightingTest()
  }

  @Test
  fun testPropsWithDefaultsInTs() {
    vue.doHighlightingTest(configureFileName = "propsWithDefaultsInTs.ts")
  }

  @Test
  fun testVuetifyWebTypesWithTrailingNewLine() {
    vue.doHighlightingTest(VueTestModule.VUETIFY_3_3_3)
  }

  @Test
  fun testBindShorthandAttribute() {
    vue.doHighlightingTest()
  }

  @Test
  fun testWatchProperty() {
    vue.doHighlightingTest(configureFileName = "watchProperty.js")
  }

  @Test
  fun testTypedMixins() {
    vue.doHighlightingTest(configureFileName = "index.js")
  }

  @Test
  fun testGlobalComponentFileReplacement() {
    vue.doConfiguredTest(checkResult = false, configureFileName = "index.vue") {
      checkHighlighting()
      val file = tempDirFixture.getFile("components.d.ts")!!
      WriteAction.run<Exception> {
        file.setBinaryContent(("""
        interface _GlobalComponents {
          AppLogo: typeof import("./app-logo.vue")['default']
        }
        declare module 'vue' {
          export interface GlobalComponents extends _GlobalComponents { }
        }
        export {}
      """).toByteArray())
      }
      checkHighlighting()
    }
  }

  @Test
  fun testGlobalComponentFromVueFileIndexedAccess() {
    vue.doHighlightingTest(configureFileName = "src/App.vue")
  }

  @Test
  fun testGlobalComponentFromVueFileDefaultProperty() {
    vue.doHighlightingTest(configureFileName = "src/App.vue")
  }

  @Test
  fun testVaporSimpleApplication() {
    vue.doHighlightingTest(
      VueTestModule.VUE_3_6_0,
      configureFileName = "App.vue",
    )
  }
}

/**
 * The state of [VueHighlightingTestBase]: a [VueFixture] in the dir mode that enables the [VueInspectionsProvider] inspections.
 */
open class VueHighlightingFixture(
  context: HybridFixtureContext,
  testMode: VueTestMode,
  private val disableServiceWarnings: Boolean = false,
  private val adjustDefaultDirName: VueFixture.(name: String) -> String = { it },
) : VueFixture(context, "highlighting", testMode, dirModeByDefault = true) {

  override val defaultDirName: String
    get() = adjustDefaultDirName(super.defaultDirName)

  override fun setUp() {
    super.setUp()
    fixture.enableInspections(VueInspectionsProvider())
    if (disableServiceWarnings) {
      disableTypeScriptServiceWarnings()
    }
  }
}

/**
 * The state of [VueHighlightingWithPluginTestBase]: a [VueHighlightingFixture] that hides the highlighting of the TypeScript service,
 * loads the trees of other files, and waits for the Vue language server.
 */
private class VueHighlightingWithPluginFixture(
  context: HybridFixtureContext,
  testMode: VueTestMode,
) : VueHighlightingFixture(context, testMode) {

  override fun setUp() {
    super.setUp()

    disableTypeScriptServiceWarnings()
    // The Vue plugin modes load the tree of other files
    disableAstLoadingFilter()
  }

  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    super.adjustConfigurators(configurators)
      .plus(VueLspConfigurator())
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
