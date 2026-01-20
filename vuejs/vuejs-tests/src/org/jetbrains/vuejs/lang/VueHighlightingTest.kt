// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredAltAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlRequiredTitleElementInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection
import com.intellij.psi.PsiFile
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import com.intellij.xml.util.CheckTagEmptyBodyInspection
import junit.framework.TestCase
import org.jetbrains.plugins.scss.inspections.SassScssResolvedByNameOnlyInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
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
class VueHighlightingTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/highlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  private fun doTest(
    packageJsonDependencies: Map<String, String> = emptyMap(),
    addNodeModules: List<VueTestModule> = emptyList(),
    extension: String = "vue",
    vararg files: String,
  ) {
    configureTestProject(packageJsonDependencies, addNodeModules, extension, *files)
    myFixture.checkHighlighting()
  }

  private fun configureTestProject(
    packageJsonDependencies: Map<String, String> = emptyMap(),
    addNodeModules: List<VueTestModule> = emptyList(),
    extension: String = "vue",
    vararg files: String,
  ): PsiFile {
    myFixture.configureVueDependencies(*addNodeModules.toTypedArray(),
                                       additionalDependencies = packageJsonDependencies)
    myFixture.configureByFiles(*files)
    return myFixture.configureByFile(getTestName(true) + "." + extension)
  }

  private fun doDirTest(
    addNodeModules: List<VueTestModule> = emptyList(),
    additionalDependencies: Map<String, String> = emptyMap(),
    fileName: String? = null,
    vararg additionalFilesToCheck: String,
  ) {
    val testName = getTestName(true)
    if (addNodeModules.isNotEmpty()) {
      myFixture.configureVueDependencies(
        modules = addNodeModules.toTypedArray(),
        additionalDependencies = additionalDependencies,
      )
    }
    myFixture.copyDirectoryToProject(testName, ".")

    for (toCheck in sequenceOf(fileName ?: "$testName.vue").plus(additionalFilesToCheck)) {
      myFixture.configureFromTempProjectFile(toCheck)
        .virtualFile.putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, "$testDataPath/$testName/$toCheck")
      myFixture.checkHighlighting()
    }
  }

  @Test
  fun testDirectivesWithoutParameters() = doTest()

  @Test
  fun testVIfRequireParameter() = doTest()

  @Test
  fun testArrowFunctionsAndExpressionsInTemplate() = doTest()

  @Test
  fun testShorthandArrowFunctionInTemplate() = doTest()

  @Test
  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() = doTest()

  @Test
  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() = doTest()

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
    doTest()
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
    doTest()
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
    doTest()
  }

  @Test
  fun testCompRequiredAttributesTest() = doTest()

  @Test
  fun testCompRequiredAttributesTestTS() = doTest()

  @Test
  fun testRequiredAttributeWithModifierTest() = doDirTest()

  @Test
  fun testRequiredAttributeWithVModel() = doDirTest(listOf(VueTestModule.VUE_2_6_10))

  @Test
  fun testRequiredAttributeWithVModel3() = doDirTest(listOf(VueTestModule.VUE_3_5_0))

  @Test
  fun testVueAttributeInCustomTag() = doTest()

  @Test
  fun testVFor() = com.intellij.testFramework.runInInitMode { doTest() }

  @Test
  fun testVForInPug() = com.intellij.testFramework.runInInitMode { doTest() }

  @Test
  fun testTopLevelThisInInjection() = doTest()

  @Test
  fun testTextarea() = doTest()

  @Test
  fun testGlobalComponentLiteral() = doDirTest()

  @Test
  fun testExternalMixin() = doDirTest()

  @Test
  fun testTwoExternalMixins() = doDirTest()

  @Test
  fun testTwoGlobalMixins() = doDirTest()

  @Test
  fun testNotImportedComponentIsUnknown() = doDirTest()

  @Test
  fun testNoDoubleSpellCheckingInAttributesWithEmbeddedContents() {
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    doTest()
  }

  @Test
  fun testNoSpellcheckInEnumeratedAttributes() {
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    doTest()
  }

  @Test
  fun testSpellchecking() {
    myFixture.enableInspections(GrazieSpellCheckingInspection())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testTypeScriptTypesAreResolved() = doTest()

  @Test
  fun testVBindVOnHighlighting() = doTest()

  @Test
  fun testComponentNameAsStringTemplate() = doTest()

  @Test
  fun testTypeScriptTypesInVue() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    doTest()
  }

  @Test
  fun testCustomDirectives() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")
    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testGlobalItemsAugmentedFromCompilerOptionsTypes() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
      additionalDependencies = mapOf("my-vue-items-library" to "*"),
    )
  }

  @Test
  fun testDirectivesFromGlobalDirectives() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesWithModifiersFromGlobalDirectives() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testEmptyAttributeValue() = doTest()

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
  fun testEmptyTagsForVueAreAllowed() = doTest()

  @Test
  fun testBuiltinTagsHighlighting() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_5_3))

  @Test
  fun testNonPropsAttributesAreNotHighlighted() = doTest()

  @Test
  fun testVueAttributeWithoutValueWithFollowingAttribute() = doTest()

  @Test
  fun testTsxIsNormallyParsed() = doTest()

  @Test
  fun testJadeWithVueShortcutAttributes() = doTest()

  @Test
  fun testComponentsNamedLikeHtmlTags() = doTest()

  @Test
  fun testClassComponentAnnotationWithLocalComponent() {
    myFixture.configureVueDependencies()
    createTwoClassComponents(myFixture)
    doTest()
  }

  @Test
  fun testClassComponentAnnotationWithLocalComponentTs() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    doTest()
  }

  @Test
  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  @Test
  fun testLocalComponentExtendsInClassSyntax() = doDirTest()

  @Test
  fun testLocalComponentInClassSyntax() = doDirTest()

  @Test
  fun testLocalComponentInMixin() = doDirTest()

  @Test
  fun testLocalComponentInMixinRecursion() = doDirTest()

  @Test
  fun testBooleanProps() = doDirTest()

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
  fun testTopLevelTags() = doTest()

  @Test
  fun testEndTagNotForbidden() = doDirTest()

  @Test
  fun testColonInEventName() = doTest()

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
  fun testVSlotSyntax() = doTest()

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  @Test
  fun testSlotSyntax() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    doTest()
  }

  @Test
  fun testSlotName() = doTest()

  @Test
  fun testSlotNameBinding() = doTest()

  @Test
  fun testVueExtendSyntax() = doDirTest(addNodeModules = listOf(VueTestModule.VUE_2_5_3))

  @Test
  fun testBootstrapVue() = doTest(addNodeModules = listOf(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11))

  @Test
  fun testDestructuringPatternsInVFor() = doTest()

  @Test
  fun testDirectivesWithParameters() = doTest()

  @Test
  fun testDirectiveWithModifiers() = doTest(addNodeModules = listOf(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11))

  @Test
  fun testIsAttributeSupport() = doTest()

  @Test
  fun testKeyAttributeSupport() = doTest()

  @Test
  fun testPropsWithOptions() = doDirTest()

  @Test
  fun testFilters() = doTest()

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
  fun testComputedPropType() = doTest()

  @Test
  fun testPseudoSelectors() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection::class.java)
    doTest()
  }

  @Test
  fun testPrivateMembersHighlighting() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
    doTest()
  }

  @Test
  fun testMultipleScriptTagsInHTML() = doTest(extension = "html")

  @Test
  fun testMultipleScriptTagsInVue() = doTest()

  @Test
  fun testCompositionApiBasic_0_4_0() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.COMPOSITION_API_0_4_0)
    myFixture.configureByFile("compositionComponent1.vue")
    myFixture.checkHighlighting()
    myFixture.configureByFile("compositionComponent2.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testCompositionApiBasic_1_0_0() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.COMPOSITION_API_1_0_0)
    myFixture.configureByFile("compositionComponent1.vue")
    myFixture.checkHighlighting()
    myFixture.configureByFile("compositionComponent2.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testSimpleVueHtml() {
    for (suffix in listOf("cdn", "cdn2", "cdn3", "cdn.js", "cdn@", "js", "deep")) {
      myFixture.configureByFile("simple-vue/simple-vue-${suffix}.html")
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
  fun testCommonJSSupport() = doTest(mapOf("vuex" to "*"))

  @Test
  fun testComputedTypeTS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  @Test
  fun testComputedTypeJS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  @Test
  fun testDataTypeTS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  @Test
  fun testScssBuiltInModules() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java,
                                SassScssResolvedByNameOnlyInspection::class.java,
                                SassScssUnresolvedVariableInspection::class.java)
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doTest()
    }
  }

  @Test
  fun testSassBuiltInModules() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java,
                                SassScssResolvedByNameOnlyInspection::class.java,
                                SassScssUnresolvedVariableInspection::class.java)
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(project) {
      doTest()
    }
  }

  @Test
  fun testIndirectExport() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  @Test
  fun testAsyncSetup() = doTest(addNodeModules = listOf(VueTestModule.VUE_3_0_0))

  @Test
  fun testScriptSetup() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testScriptSetupComplexImports() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testMissingLabelSuppressed() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    myFixture.enableInspections(HtmlFormInputWithoutLabelInspection())
    myFixture.configureByText("Foo.vue", """<input>""")
    myFixture.checkHighlighting()
  }

  @Test
  fun testSuperComponentMixin() = doDirTest()

  @Test
  fun testCompositionPropsJS() = doTest()

  @Test
  fun testCssSelectors() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection())
    doTest()
  }

  @Test
  fun testCssUnusedPseudoSelector() {
    myFixture.enableInspections(CssUnusedSymbolInspection())
    doTest()
  }

  @Test
  fun testScriptSetupScopePriority() = doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))

  @Test
  fun testBindingToDataAttributes() = doTest()

  @Test
  fun testPropsValidation() = doDirTest()

  @Test
  fun testScriptSetupRef() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest()
  }

  @Test
  fun testScriptSetupImportedDirective() {
    myFixture.enableInspections(
      ES6UnusedImportsInspection(),
    )
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testTypedComponentsScriptSetup() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.NAIVE_UI_2_19_11, VueTestModule.HEADLESS_UI_1_4_1, VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testTypedComponentsScriptSetup2() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.NAIVE_UI_2_19_11, VueTestModule.HEADLESS_UI_1_4_1, VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testCssVBind() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testCssVBindVue31() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_1_0))
  }

  @Test
  fun testGlobalSymbols() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testStandardBooleanAttributes() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  @Test
  fun testRefUnwrap() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testVModelWithMixin() {
    doDirTest(fileName = "MyForm.vue")
  }

  @Test
  fun testScriptSetupSymbolsHighlighting() {
    configureTestProject()
    JSTestUtils.checkHighlightingWithSymbolNames(myFixture, true, true, true)
  }

  @Test
  fun testSlotTypes() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(
      fileName = "MyTable.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0, VueTestModule.QUASAR_2_6_5),
    )
  }

  @Test
  fun testGlobalScriptSetup() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(
      fileName = "HelloWorld.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDynamicArguments() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(
      fileName = "HelloWorld.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testWithPropsFromFunctionCall() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  @Test
  fun testWithPropsFromFunctionCall2() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  @Test
  fun testInferPropType() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2, VueTestModule.NAIVE_UI_2_33_2_PATCHED))
  }

  @Test
  fun testLocalWebTypes() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(
      fileName = "main.vue",
      additionalFilesToCheck = arrayOf("main2.vue"),
    )
  }

  @Test
  fun testPropertyReferenceInLambda() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  @Test
  fun testSourceScopedSlots() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(
      fileName = "Catalogue.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testCustomEvents() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testCustomEventsTypedComponent() {
    doDirTest(
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testLifecycleEventsVue2ClassComponent() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))
  }

  @Test
  fun testLifecycleEventsVue2VueExtend() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))
  }

  @Test
  fun testLifecycleEventsVue3Options() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testLifecycleEventsVue3DefineComponent() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testIdIndexer() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testVueCreateApp() {
    doDirTest(fileName = "test.html")
  }

  @Test
  fun testInstanceMountedOnElement() {
    doDirTest(fileName = "test.html")
  }

  @Test
  fun testScriptCaseSensitivity() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testVPre() {
    doTest()
  }

  @Test
  fun testHtmlTagOmission() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0), extension = "html")
  }

  @Test
  fun testVueNoTagOmission() {
    doTest()
  }

  @Test
  fun testScriptSetupGeneric() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testGenericComponentUsage() {
    doDirTest(
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromFunctionPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromFunctionPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testComponentFromObjectPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromObjectPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromFunctionPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromFunctionPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromNestedFunctionPluginWithCycle_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromObjectPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromObjectPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPlugin_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testDirectivesFromNestedObjectPluginWithCycle_vapor() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
    )
  }

  @Test
  fun testStdTagsInspections() {
    myFixture.enableInspections(HtmlRequiredTitleElementInspection::class.java, HtmlRequiredAltAttributeInspection::class.java)
    doTest()
  }

  @Test
  fun testPropTypeJsDoc() {
    myFixture.enableInspections(JSValidateTypesInspection())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testPropsWithDefaults() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testPropsWithDefaultsInTs() {
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    doTest(extension = "ts")
  }

  @Test
  fun testVuetifyWebTypesWithTrailingNewLine() {
    doTest(addNodeModules = listOf(VueTestModule.VUETIFY_3_3_3))
  }

  @Test
  fun testBindShorthandAttribute() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0))
  }

  @Test
  fun testWatchProperty() {
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_5_0), extension = "js")
  }

  @Test
  fun testTypedMixins() {
    doDirTest(
      fileName = "index.js",
      addNodeModules = listOf(VueTestModule.VUE_3_5_0),
    )
  }

  @Test
  fun testVaporSimpleApplication() {
    doDirTest(
      fileName = "App.vue",
      addNodeModules = listOf(VueTestModule.VUE_3_6_0),
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
