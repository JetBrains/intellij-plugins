// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.JSTestUtils
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
import org.jetbrains.vuejs.libraries.nuxt.NuxtHighlightingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/**
 * @see VueComponentTest
 * @see VueControlFlowTest
 * @see NuxtHighlightingTest
 */
@RunWith(JUnit4::class)
class VueHighlightingTest :
  VueTestCase("highlighting", testMode = VueTestMode.NO_PLUGIN) {

  override val dirModeByDefault: Boolean = true

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

  private fun configureTestProject() {
    myFixture.configureByFile("$testName/$testName.vue")
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
    myFixture.configureByText("compUI.vue", """
<script>
    export default {
        name: 'compUI',
        props: ['seeMe']
    }
</script>
""")
    checkHighlighting()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsAsObject() {
    myFixture.configureByText("compUI.vue", """
<script>
    export default {
        name: 'compUI',
        props: {
          seeMe: {}
        }
    }
</script>
""")
    checkHighlighting()
  }

  @Test
  fun testImportedComponentPropsInCompAttrsObjectRef() {
    myFixture.configureByText("compUI.vue", """
<script>
const props = {seeMe: {}}
    export default {
        name: 'compUI',
        props: props
    }
</script>
""")
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
    // Tree access disabled
    //  /index.js
    disableAstLoadingFilter()

    checkHighlighting()
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
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    checkHighlighting()
  }

  @Test
  fun testNoSpellcheckInEnumeratedAttributes() {
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    checkHighlighting()
  }

  @Test
  fun testSpellchecking() {
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    checkHighlighting()
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
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    checkHighlighting()
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
    myFixture.configureByText("NoCreateVarQuickFix.vue", """
<template>
{{ <caret>someNonExistingReference2389 }}
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(
      JavaScriptBundle.message("javascript.create.variable.intention.name", "someNonExistingReference2389"))
    TestCase.assertTrue(intentions.isEmpty())
  }

  @Test
  fun testNoCreateFunctionQuickFix() {
    myFixture.configureByText("NoCreateFunctionQuickFix.vue", """
<template>
<div @click="<caret>notExistingF()"></div>
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(
      JavaScriptBundle.message("javascript.create.function.intention.name", "notExistingF"))
    TestCase.assertTrue(intentions.isEmpty())
  }

  @Test
  fun testNoCreateClassQuickFix() {
    myFixture.configureByText("NoCreateClassQuickFix.vue", """
<template>
<div @click="new <caret>NotExistingClass().a()"></div>
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(
      JavaScriptBundle.message("javascript.create.class.intention.name", "NotExistingClass"))
    TestCase.assertTrue(intentions.isEmpty())
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

  /*
  @Test
  fun testFlowJSEmbeddedContent() {
    // Flow is not used unless there is associated .flowconfig. Instead of it to have 'console' resolved we may enable HTML library.
    JSTestUtils.setDependencyOnPredefinedJsLibraries(project, testRootDisposable, JSCorePredefinedLibrariesConstants.LIB_HTML)
    testWithinLanguageLevel<Exception>(JSLanguageLevel.FLOW, project) {
      myFixture.configureByText("FlowJSEmbeddedContent.vue", """
<script>
    // @flow
    type Foo = { a: number }
    const foo: Foo = { a: 1 }
    console.log(foo);
</script>
""")
      myFixture.checkHighlighting()
    }
  }
  */

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
    configureTestProject()
    JSTestUtils.checkHighlightingWithSymbolNames(myFixture, false, false, true)
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
    // Tree access disabled
    //  /a-component.vue
    disableAstLoadingFilter()

    checkHighlighting(VueTestModule.VUE_2_5_3)
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
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection::class.java)
    checkHighlighting()
  }

  @Test
  fun testPrivateMembersHighlighting() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
    checkHighlighting()
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

    myFixture.enableInspections(
      CssInvalidFunctionInspection::class.java,
      SassScssResolvedByNameOnlyInspection::class.java,
      SassScssUnresolvedVariableInspection::class.java,
    )

    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      checkHighlighting()
    }
  }

  @Test
  fun testSassBuiltInModules() {
    // Tree access disabled
    //  /plugins/sass/sass.jar!/org/jetbrains/plugins/sass/stdlib/sass_math.scss
    disableAstLoadingFilter()

    myFixture.enableInspections(
      CssInvalidFunctionInspection::class.java,
      SassScssResolvedByNameOnlyInspection::class.java,
      SassScssUnresolvedVariableInspection::class.java,
    )

    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      checkHighlighting()
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
    myFixture.enableInspections(ES6UnusedImportsInspection())
    checkHighlighting()
  }

  @Test
  fun testScriptSetupComplexImports() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    checkHighlighting()
  }

  @Test
  fun testMissingLabelSuppressed() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    myFixture.enableInspections(HtmlFormInputWithoutLabelInspection())
    myFixture.configureByText("Foo.vue", """<input>""")
    myFixture.checkHighlighting()
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
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection())
    checkHighlighting()
  }

  @Test
  fun testCssUnusedPseudoSelector() {
    myFixture.enableInspections(CssUnusedSymbolInspection())
    checkHighlighting()
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
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting()
  }

  @Test
  fun testScriptSetupImportedDirective() {
    myFixture.enableInspections(
      ES6UnusedImportsInspection(),
    )
    checkHighlighting()
  }

  @Test
  fun testTypedComponentsScriptSetup() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    checkHighlighting(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
    )
  }

  @Test
  fun testTypedComponentsScriptSetup2() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    checkHighlighting(
      VueTestModule.NAIVE_UI_2_19_11,
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.VUE_3_5_0,
    )
  }

  @Test
  fun testCssVBind() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    checkHighlighting()
  }

  @Test
  fun testCssVBindVue31() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    checkHighlighting(VueTestModule.VUE_3_1_0)
  }

  @Test
  fun testGlobalSymbols() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testStandardBooleanAttributes() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testRefUnwrap() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testVModelWithMixin() {
    checkHighlighting(configureFileName = "MyForm.vue")
  }

  @Test
  fun testScriptSetupSymbolsHighlighting() {
    configureTestProject()
    JSTestUtils.checkHighlightingWithSymbolNames(myFixture, true, true, true)
  }

  @Test
  fun testSlotTypes() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting(
      VueTestModule.QUASAR_2_6_5,
      VueTestModule.VUE_3_5_0,
      configureFileName = "MyTable.vue",
    )
  }

  @Test
  fun testGlobalScriptSetup() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testDynamicArguments() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting(configureFileName = "HelloWorld.vue")
  }

  @Test
  fun testWithPropsFromFunctionCall() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testWithPropsFromFunctionCall2() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testInferPropType() {
    myFixture.enableInspections(VueInspectionsProvider())
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

    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting(
      configureFileName = "main.vue",
    )
  }

  @Test
  fun testPropertyReferenceInLambda() {
    myFixture.enableInspections(VueInspectionsProvider())
    checkHighlighting()
  }

  @Test
  fun testSourceScopedSlots() {
    myFixture.enableInspections(VueInspectionsProvider())
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
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testLifecycleEventsVue2VueExtend() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testLifecycleEventsVue3Options() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting()
  }

  @Test
  fun testLifecycleEventsVue3DefineComponent() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting()
  }

  @Test
  fun testIdIndexer() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    checkHighlighting()
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
    myFixture.enableInspections(
      HtmlRequiredTitleElementInspection::class.java,
      HtmlRequiredAltAttributeInspection::class.java,
    )

    checkHighlighting()
  }

  @Test
  fun testPropTypeJsDoc() {
    myFixture.enableInspections(JSValidateTypesInspection())
    checkHighlighting()
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
