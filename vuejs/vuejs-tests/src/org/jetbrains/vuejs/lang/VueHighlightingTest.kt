// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JSTestUtils.testWithinLanguageLevel
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.inspections.ES6UnusedImportsInspection
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.css.inspections.invalid.CssInvalidFunctionInspection
import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.ThrowableRunnable
import com.intellij.xml.util.CheckTagEmptyBodyInspection
import junit.framework.TestCase
import org.jetbrains.plugins.scss.inspections.SassScssResolvedByNameOnlyInspection
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedVariableInspection
import org.jetbrains.vuejs.lang.html.VueFileType

/**
 * @see VueComponentTest
 * @see VueControlFlowTest
 */
class VueHighlightingTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/highlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  private fun doTest(packageJsonDependencies: String? = null,
                     addNodeModules: List<VueTestModule> = emptyList(),
                     extension: String = "vue",
                     vararg files: String) {
    configureTestProject(packageJsonDependencies, addNodeModules, extension, *files)
    myFixture.checkHighlighting()
  }

  private fun configureTestProject(packageJsonDependencies: String? = null,
                                   addNodeModules: List<VueTestModule> = emptyList(),
                                   extension: String = "vue",
                                   vararg files: String): PsiFile {
    if (packageJsonDependencies != null) {
      createPackageJsonWithVueDependency(myFixture, packageJsonDependencies)
    }
    if (addNodeModules.isNotEmpty()) {
      myFixture.configureVueDependencies(*addNodeModules.toTypedArray())
    }
    myFixture.configureByFiles(*files)
    return myFixture.configureByFile(getTestName(true) + "." + extension)
  }

  private fun doDirTest(addNodeModules: List<VueTestModule> = emptyList(), fileName: String? = null) {
    val testName = getTestName(true)
    if (addNodeModules.isNotEmpty()) {
      myFixture.configureVueDependencies(*addNodeModules.toTypedArray())
    }
    myFixture.copyDirectoryToProject(testName, ".")
    val actualFileName = fileName ?: "$testName.vue"
    myFixture.configureFromTempProjectFile(actualFileName)
      .virtualFile.putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, "$testDataPath/$testName/$actualFileName")
    myFixture.checkHighlighting()
  }

  fun testDirectivesWithoutParameters() = doTest()

  fun testVIfRequireParameter() = doTest()

  fun testArrowFunctionsAndExpressionsInTemplate() = doTest()

  fun testShorthandArrowFunctionInTemplate() = doTest()

  fun testShorthandArrowFunctionInTemplateES5() {
    testWithinLanguageLevel(JSLanguageLevel.ES5, myFixture.project, ThrowableRunnable {
      doTest()
    })
  }

  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() = doTest()

  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() = doTest()

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

  fun testCompRequiredAttributesTest() = doTest("")

  fun testCompRequiredAttributesTestTS() = doTest("")

  fun testRequiredAttributeWithModifierTest() = doDirTest()

  fun testRequiredAttributeWithVModel() = doDirTest(listOf(VueTestModule.VUE_2_6_10))

  fun testRequiredAttributeWithVModel3() = doDirTest(listOf(VueTestModule.VUE_3_0_0))

  fun testVueAttributeInCustomTag() = doTest()

  fun testVFor() = com.intellij.testFramework.runInInitMode { doTest() }

  fun testVForInPug() = com.intellij.testFramework.runInInitMode { doTest() }

  fun testTopLevelThisInInjection() = doTest()

  fun testTextarea() = doTest()

  fun testGlobalComponentLiteral() = doDirTest()

  fun testExternalMixin() = doDirTest()

  fun testTwoExternalMixins() = doDirTest()

  fun testTwoGlobalMixins() = doDirTest()

  fun testNotImportedComponentIsUnknown() = doDirTest()

  fun testNoDoubleSpellCheckingInAttributesWithEmbeddedContents() {
    myFixture.enableInspections(SpellCheckingInspection())
    myFixture.configureByText(VueFileType.INSTANCE, """
<template>
    <div>
        <ul>
            <li v-for="somewordd in someObject">{{ somewordd }}
        </ul>
    </div>
</template>
<script>
    var someObject = []
</script>
""")
    val list = myFixture.doHighlighting().filter { it.severity.name == "TYPO" }
    val typoRanges: MutableSet<Pair<Int, Int>> = mutableSetOf()
    for (info in list) {
      val pair = Pair(info.startOffset, info.endOffset)
      if (!typoRanges.add(pair)) TestCase.assertTrue("Duplicate $pair", false)
    }
  }

  fun testTypeScriptTypesAreResolved() = doTest()

  fun testTypeScriptTypesAreNotResolvedIfECMA5Script() {
    testWithinLanguageLevel(JSLanguageLevel.ES5, myFixture.project, ThrowableRunnable {
      doTest()
    })
  }

  fun testVBindVOnHighlighting() = doTest()

  fun testComponentNameAsStringTemplate() = doTest()

  fun testTypeScriptTypesInVue() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    doTest()
  }

  fun testCustomDirectives() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testEmptyAttributeValue() = doTest()

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

  fun testEmptyTagsForVueAreAllowed() = doTest()

  fun testBuiltinTagsHighlighting() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_5_3))

  fun testNonPropsAttributesAreNotHighlighted() = doTest()

  fun testVueAttributeWithoutValueWithFollowingAttribute() = doTest()

  fun testTsxIsNormallyParsed() = doTest()

  fun testJadeWithVueShortcutAttributes() = doTest()

  fun testComponentsNamedLikeHtmlTags() = doTest()

  fun testClassComponentAnnotationWithLocalComponent() {
    createPackageJsonWithVueDependency(myFixture)
    createTwoClassComponents(myFixture)
    doTest()
  }

  fun testClassComponentAnnotationWithLocalComponentTs() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    doTest()
  }

  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  fun testLocalComponentExtendsInClassSyntax() = doDirTest()

  fun testLocalComponentInClassSyntax() = doDirTest()

  fun testLocalComponentInMixin() = doDirTest()

  fun testLocalComponentInMixinRecursion() = doDirTest()

  fun testBooleanProps() = doDirTest()

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

  fun testFlowJSEmbeddedContent() {
    // Flow is not used unless there is associated .flowconfig. Instead of it to have 'console' resolved we may enable HTML library.
    JSTestUtils.setDependencyOnPredefinedJsLibraries(project, testRootDisposable, JSCorePredefinedLibrariesProvider.LIB_HTML)
    testWithinLanguageLevel<Exception>(JSLanguageLevel.FLOW, project) {
      myFixture.configureByText("FlowJSEmbeddedContent.vue", """
<script>
    type Foo = { a: number }
    const foo: Foo = { a: 1 }
    console.log(foo);
</script>
""")
      myFixture.checkHighlighting()
    }
  }

  fun testTopLevelTags() = doTest()

  fun testEndTagNotForbidden() = doDirTest()

  fun testColonInEventName() = doTest()

  fun testNoVueTagErrorsInPlainXml() {
    myFixture.addFileToProject("any.vue", "") // to make sure that Vue support works for the project
    myFixture.configureByText("foo.xml", "<component><foo/></component>".trimMargin())
    myFixture.checkHighlighting()
  }

  fun testSemanticHighlighting() {
    configureTestProject()
    myFixture.checkHighlighting( /* checkWarnings = */ false,   /* checkInfos = */ true,  /* checkWeakWarnings = */ false)
  }

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  fun testVSlotSyntax() = doTest()

  // TODO add special inspection for unused slot scope parameters - WEB-43893
  fun testSlotSyntax() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    doTest()
  }

  fun testSlotName() = doTest()

  fun testSlotNameBinding() = doTest()

  fun testVueExtendSyntax() = doDirTest(addNodeModules = listOf(VueTestModule.VUE_2_5_3))

  fun testBootstrapVue() = doTest(addNodeModules = listOf(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11))

  fun testDestructuringPatternsInVFor() = doTest()

  fun testDirectivesWithParameters() = doTest()

  fun testDirectiveWithModifiers() = doTest(addNodeModules = listOf(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11))

  fun testIsAttributeSupport() = doTest()

  fun testKeyAttributeSupport() = doTest()

  fun testPropsWithOptions() = doDirTest()

  fun testFilters() = doTest("")

  fun testEmptyTags() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.enableInspections(CheckTagEmptyBodyInspection())
    myFixture.copyDirectoryToProject("emptyTags", ".")
    for (file in listOf("test.vue", "test-html.html", "test-reg.html")) {
      myFixture.configureFromTempProjectFile(file)
      myFixture.checkHighlighting()
    }
  }

  fun testComputedPropType() = doTest("")

  fun testPseudoSelectors() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection::class.java)
    doTest()
  }

  fun testPrivateMembersHighlighting() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
    doTest()
  }

  fun testMultipleScriptTagsInHTML() = doTest("", extension = "html")

  fun testMultipleScriptTagsInVue() = doTest("")

  fun testCompositionApiBasic_0_4_0() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.COMPOSITION_API_0_4_0)
    myFixture.configureByFile("compositionComponent1.vue")
    myFixture.checkHighlighting()
    myFixture.configureByFile("compositionComponent2.vue")
    myFixture.checkHighlighting()
  }

  fun testCompositionApiBasic_1_0_0() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.COMPOSITION_API_1_0_0)
    myFixture.configureByFile("compositionComponent1.vue")
    myFixture.checkHighlighting()
    myFixture.configureByFile("compositionComponent2.vue")
    myFixture.checkHighlighting()
  }

  fun testSimpleVueHtml() {
    for (suffix in listOf("cdn", "cdn2", "cdn3", "cdn.js", "cdn@", "js", "deep")) {
      myFixture.configureByFile("simple-vue/simple-vue-${suffix}.html")
      myFixture.checkHighlighting(true, false, true)
    }
  }

  fun testCommonJSSupport() = doTest("")

  fun testComputedTypeTS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  fun testComputedTypeJS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  fun testDataTypeTS() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  fun testScssBuiltInModules() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java,
                                SassScssResolvedByNameOnlyInspection::class.java,
                                SassScssUnresolvedVariableInspection::class.java)
    doTest()
  }

  fun testSassBuiltInModules() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java,
                                SassScssResolvedByNameOnlyInspection::class.java,
                                SassScssUnresolvedVariableInspection::class.java)
    doTest()
  }

  fun testIndirectExport() = doTest(addNodeModules = listOf(VueTestModule.VUE_2_6_10))

  fun testAsyncSetup() = doTest(addNodeModules = listOf(VueTestModule.VUE_3_0_0))

  fun testScriptSetup() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_0_0))
  }

  fun testScriptSetupComplexImports() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2))
  }

  fun testMissingLabelSuppressed() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.enableInspections(HtmlFormInputWithoutLabelInspection())
    myFixture.configureByText("Foo.vue", """<input>""")
    myFixture.checkHighlighting()
  }

  fun testSuperComponentMixin() = doDirTest()

  fun testCompositionPropsJS() = doTest()

  fun testCssSelectors() {
    myFixture.enableInspections(CssInvalidPseudoSelectorInspection())
    doTest()
  }

  fun testScriptSetupScopePriority() = doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2))

  fun testBindingToDataAttributes() = doTest()

  fun testPropsValidation() = doDirTest()

  fun testScriptSetupRef() {
    myFixture.enableInspections(
      JSUnusedLocalSymbolsInspection(),
      JSUnusedGlobalSymbolsInspection()
    )
    doTest()
  }

  fun testTypedComponentsScriptSetup() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.NAIVE_UI_2_19_11, VueTestModule.HEADLESS_UI_1_4_1, VueTestModule.VUE_3_2_2))
  }

  fun testTypedComponentsScriptSetup2() {
    myFixture.enableInspections(ES6UnusedImportsInspection())
    doTest(addNodeModules = listOf(VueTestModule.NAIVE_UI_2_19_11, VueTestModule.HEADLESS_UI_1_4_1, VueTestModule.VUE_3_2_2))
  }

  fun testCssVBind() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2))
  }

  fun testCssVBindVue31() {
    myFixture.enableInspections(CssInvalidFunctionInspection::class.java)
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_1_0))
  }

  fun testGlobalSymbols() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_1_0))
  }

  fun testStandardBooleanAttributes() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  fun testRefUnwrap() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest(addNodeModules = listOf(VueTestModule.VUE_3_0_0))
  }

  fun testVModelWithMixin() {
    doDirTest(fileName = "MyForm.vue")
  }

  fun testScriptSetupSymbolsHighlighting() {
    configureTestProject()
    myFixture.checkHighlighting(/* checkWarnings = */ true, /* checkInfos = */ true, /* checkWeakWarnings = */ true)
  }

  fun testSlotTypes() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2, VueTestModule.QUASAR_2_6_5), "MyTable.vue")
  }

  fun testGlobalScriptSetup() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2), "HelloWorld.vue")
  }

  fun testDynamicArguments() {
    myFixture.enableInspections(VueInspectionsProvider())
    doDirTest(addNodeModules = listOf(VueTestModule.VUE_3_2_2), "HelloWorld.vue")
  }

  fun testWithPropsFromFunctionCall() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
  }

  fun testWithPropsFromFunctionCall2() {
    myFixture.enableInspections(VueInspectionsProvider())
    doTest()
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
  val nameWithMarkup = if (withMarkup) "<warning descr=\"Element CompB doesn't have required attribute prop-from-a\">CompB</warning>" else "CompB"
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
