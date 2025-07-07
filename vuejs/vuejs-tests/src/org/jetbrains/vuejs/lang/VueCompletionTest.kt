// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.javascript.testFramework.web.filterOutStandardHtmlSymbols
import com.intellij.javascript.testFramework.web.forceReloadProjectRoots
import com.intellij.lang.javascript.BaseJSCompletionTestCase.assertStartsWith
import com.intellij.lang.javascript.BaseJSCompletionTestCase.checkJSStringCompletion
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.lang.javascript.typeWithWaitCoroutinesBlocking
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.RecursionManager
import com.intellij.polySymbols.testFramework.*
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import junit.framework.ComparisonFailure
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.libraries.VUE_CLASS_COMPONENT

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
      checkLookupItems(lookupFilter = filterOutAriaAttributes)

      type(":")
      assertLookupShown()
      checkLookupItems(lookupFilter = filterOutAriaAttributes)

      type("a")
      checkLookupItems(lookupFilter = filterOutAriaAttributes)
    }

  fun testBuefyCompletion() =
    doLookupTest(VueTestModule.BUEFY_0_6_2, fileContents = "<template><b-<caret></template>")

  fun testClassComponentCompletion() {
    createTwoClassComponents(myFixture)
    myFixture.configureByText("ClassComponentCompletion.vue",
                              """
<template>
  <<caret>
</template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!,
                                          listOf("ShortComponent", "LongVue", "short-component", "long-vue"))
  }

  fun testClassComponentCompletionTs() {
    createTwoClassComponents(myFixture, true)
    myFixture.configureByText("ClassComponentCompletionTs.vue",
                              """
<template>
  <<caret>
</template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!,
                                          listOf("ShortComponent", "LongVue", "short-component", "long-vue"))
  }

  fun testComponentInsertion() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10,
                                       additionalDependencies = mapOf(VUE_CLASS_COMPONENT to "*"))
    val data = listOf(
      Pair("""<template>
  <Sho<caret>
</template>
""", """<template>
  <ShortComponent
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertion extends Vue {
}
</script>"""),
      Pair("""<template>
  <Sho<caret>
</template>
<script></script>
""", """<template>
  <ShortComponent
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertion extends Vue {
}
</script>
"""),
      Pair("""<template>
  <Sho<caret>
</template>
<script>
import Vue from "vue";
</script>
""", """<template>
  <ShortComponent
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertion extends Vue {
}

</script>
"""),
      Pair("""<template>
  <Sho<caret>
</template>
<script>
import Component from "vue-class-component";
</script>
""", """<template>
  <ShortComponent
</template>
<script>
import Component from "vue-class-component";
import Vue from "vue";
import ShortComponent from "./ShortComponent.vue";

@Component({
  components: {ShortComponent}
})
export default class ComponentInsertion extends Vue {
}

</script>
"""),
      Pair("""<template>
  <Sho<caret>
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
@Component({
  name: "a123"
})
export default class ComponentInsertion extends Vue {
}
</script>
""", """<template>
  <ShortComponent
</template>
<script>
import Vue from "vue";
import Component from "vue-class-component";
import ShortComponent from "./ShortComponent.vue";
@Component({
  name: "a123",
  components: {ShortComponent}
})
export default class ComponentInsertion extends Vue {
}
</script>
""")
    )
    myFixture.configureByText("package.json", """{
          dependencies: {
            "vue-class-component" : "latest"
          }
        }""")
    createTwoClassComponents(myFixture, true)
    data.forEach {
      myFixture.configureByText("ComponentInsertion.vue", it.first)
      myFixture.completeBasic()
      myFixture.checkResult(it.second)
    }
  }

  fun testTypescriptVForItemCompletion() {
    myFixture.configureByText("TypescriptVForItemCompletion.vue", """
<template>
    <ul>
        <li v-for="item in goodTypes">{{item.<caret>}}</li>
    </ul>
</template>

<script lang="ts">
    import { Component, Prop, Vue } from 'vue-property-decorator';

    @Component
    export default class HelloWorld extends Vue {
        @Prop() private msg!: string;
        goodTypes: Array<string> = [];
    }
</script>
""")
    myFixture.completeBasic()

    checkJSStringCompletion(myFixture.lookupElements!!, false)
  }

  fun testTypescriptVForCompletionWebTypes() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("TypescriptVForCompletionWebTypes.vue",
                              "<template><div v-for=\"fooBar1 in goodTypes\">{{<caret>}}</li></template>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "fooBar1")
  }

  fun testLocalComponentsExtendsCompletion() {
    createLocalComponentsExtendsData(myFixture, false)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "prop-from-a")
  }

  fun testCompletionWithRecursiveMixins() {
    defineRecursiveMixedMixins(myFixture)
    myFixture.configureByText("CompletionWithRecursiveMixins.vue", """
        <template>
          <<caret>
        </template>
      """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("hidden-component", "HiddenComponent",
                                                                    "OneMoreComponent", "one-more-component"))
    myFixture.configureByText("CompletionWithRecursiveMixins2.vue", """
        <template>
          <HiddenComponent <caret>/>
        </template>
      """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("from-d", "from-hidden"))
  }

  fun testNoImportInsertedForRecursivelyLocalComponent() {
    noAutoComplete {
      defineRecursiveMixedMixins(myFixture)
      myFixture.configureByText("CompletionWithRecursiveMixins.vue", """
        <template>
          <HiddenComponen<caret>
        </template>
      """)
      myFixture.completeBasic()
      assertContainsElements(myFixture.lookupElementStrings!!, "HiddenComponent")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
        <template>
          <HiddenComponent from-hidden="" from-d=""<caret>
        </template>
      """)
    }

  }

  fun testCssClassInPug() {
    myFixture.configureByText("foo.vue", "<template lang='pug'>\n" +
                                         "    .<caret>\n" +
                                         "</template>\n" +
                                         "<style>\n" +
                                         "    .someClass {}\n" +
                                         "</style>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, "someClass")
  }

  fun testEventsAfterAt() {
    myFixture.configureVueDependencies(VueTestModule.BOOTSTRAP_VUE_2_0_0_RC_11)
    myFixture.configureByText("foo.vue", "<template> <BAlert @<caret> </template>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, // first 3 items come from the BAlert component
                                             "@dismiss-count-down", "@dismissCountDown", "@dismissed", "@input",
                                             "@abort", "@auxclick", "@beforeinput", "@beforematch", "@blur", "@cancel", "@canplay",
                                             "@canplaythrough", "@change", "@click", "@close", "@contextlost", "@contextmenu",
                                             "@contextrestored", "@copy", "@cuechange", "@cut", "@dblclick")

    myFixture.configureByText("foo.vue", "<template> <BAlert @inp<caret> </template>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, "@input", ".capture", ".once", ".passive", ".prevent")

    myFixture.configureByText("foo.vue", "<template> <div @c<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "@copy", "@cancel", "@click", "@canplaythrough", "@close",
                       "@change", "@canplay", "@cut", "@cuechange", "@contextlost", "@contextmenu", "@contextrestored", ".capture",
                       ".once", ".passive", ".prevent", ".self", ".stop")
  }

  fun testEventsAfterVOn() {
    myFixture.configureByText("foo.vue", "<template> <MyComponent v-on:cl<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "auxclick", "click", "close", "dblclick", ".capture", ".once", ".passive",
                       ".prevent", ".self", ".stop")

    myFixture.configureByText("foo.vue", "<template> <div v-on:<caret> </template>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, "abort", "auxclick", "beforeinput", "beforematch", "blur",
                                             "cancel", "canplay", "canplaythrough", "change", "click")
  }


  fun testEventModifiers() {
    // general modifiers only
    myFixture.configureByText("foo.vue", "<template> <MyComponent @click123.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, ".stop", ".prevent", ".capture", ".self", ".once", ".passive")

    // general modifiers (except already used) + key modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div v-on:keyup.stop.passive.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, ".prevent", ".capture", ".self", ".once",
                       ".enter", ".tab", ".delete", ".esc", ".space", ".up", ".down", ".left", ".right",
                       ".ctrl", ".alt", ".shift", ".meta", ".exact")

    // general modifiers (except already used) + mouse button modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div @click.capture.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, ".stop", ".prevent", ".self", ".once", ".passive",
                       ".left", ".right", ".middle",
                       ".ctrl", ".alt", ".shift", ".meta", ".exact")

    // general modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div @drop.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, ".stop", ".prevent", ".capture", ".self", ".once", ".passive",
                       ".ctrl", ".alt", ".shift", ".meta", ".exact")
  }

  fun testAutopopupAfterVOnSelection() {
    myFixture.configureByText("a.vue", "<div v-o<caret>>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, "v-on:", "v-on")
    (myFixture.lookup as LookupImpl).finishLookup(Lookup.NORMAL_SELECT_CHAR)
    // new completion must start
    myFixture.assertPreferredCompletionItems(0, "abort", "auxclick", "beforeinput", "beforematch", "blur", "cancel", "canplay")
    (myFixture.lookup as LookupImpl).finishLookup(Lookup.NORMAL_SELECT_CHAR)
    myFixture.checkResult("<div v-on:abort=\"<caret>\">")
  }

  fun testStyleAttributes() {
    myFixture.configureByText("foo.vue", "<style <caret>></style>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "scoped", "src", "module")
    assertDoesntContain(myFixture.lookupElementStrings!!, "functional")
  }

  fun testTemplateAttributes() {
    myFixture.configureByText("foo.vue", "<template <caret>></template>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "functional", "v-if", "v-else")
    assertDoesntContain(myFixture.lookupElementStrings!!, "scoped", "module")
  }

  fun testNoVueTagsWithNamespace() {
    myFixture.configureByText("foo.vue", """
      <template>
        <foo:tran<caret>/>
      </template>""")
    myFixture.completeBasic()
    assertNullOrEmpty(myFixture.lookupElementStrings)
  }

  fun testVueCompletionInsideScript() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        name: 'test',\n" +
                                          "        data() {\n" +
                                          "            return {testItem: 10}\n" +
                                          "        },\n" +
                                          "        props : {\n" +
                                          "          props1: true\n" +
                                          "        },\n" +
                                          "        methods: {\n" +
                                          "            method1() {}\n" +
                                          "        },\n" +
                                          "        computed: {\n" +
                                          "            dataData() {this.<caret> }\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "testItem", "props1", "method1")
  }

  fun testVueCompletionInsideScriptLifecycleHooks() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        computed: {\n" +
                                          "            dataData() {this.<caret> }\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "\$el", "\$options", "\$parent", "\$props")
  }

  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        this.<caret> " +
                                          "    }\n" +
                                          "</script>")
    assertDoesntContainVueLifecycleHooks()
  }

  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        methods: {name(){<caret>}} " +
                                          "    }\n" +
                                          "</script>")

    assertDoesntContainVueLifecycleHooks()
  }

  fun testVueCompletionWithExtend() {
    myFixture.configureByText("a-component.vue", """<script>export default Vue.extend({props:{msg: String}})</script>""")
    myFixture.configureByText("b-component.vue", """
      <template>
        <HW <caret>/>
      </template>
      <script>
        import Vue from "vue"
        import HW from './a-component.vue'
        
        export default Vue.extend({
            name: 'app',
            components: {
                HW
            },
        });
      </script>
    """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "msg")
  }

  fun testVueNoComponentNameAsStyleSelector() {
    myFixture.configureByText("A.vue", """<template><div/></template>""")
    myFixture.configureByText("B.vue", """
      <template>
        <div/>
      </template>
      <style lang="scss">
      <caret>
      </style>
    """)
    myFixture.completeBasic()
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "A")
  }

  fun testCompletionPriorityAndHints() =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, VueTestModule.SHARDS_VUE_1_0_5, dir = true, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testCompletionPriorityAndHintsBuiltInTags() =
    doLookupTest(VueTestModule.VUE_2_5_3, lookupItemFilter = filterOutStandardHtmlSymbols)

  fun testDirectiveCompletionOnComponent() =
    doLookupTest(VueTestModule.VUETIFY_1_3_7, renderPriority = false, renderTypeText = false, containsCheck = true)

  fun testBuiltInTagsAttributeCompletion() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("a-component.vue", """
      <template>
        <transition <caret>>
      </template>
    """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "appear-active-class", "css", "leave-class")
  }

  fun testBindProposalsPriority() =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, VueTestModule.VUE_2_6_10, renderTypeText = false, renderProximity = true) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  fun testBindProposalsStdTag() =
    doLookupTest(VueTestModule.VUE_2_6_10, renderPriority = false, renderTypeText = false, lookupItemFilter = filterOutAriaAttributes)

  private fun doAttributeNamePriorityTest(vueVersion: VueTestModule) =
    doLookupTest(VueTestModule.VUETIFY_1_2_10, vueVersion,
                 fileContents = """
         <template>
          <v-alert <caret>
        <template>
      """.trimIndent(),
                 renderTypeText = false,
                 renderProximity = true) {
      !it.lookupString.contains("aria-") && !it.lookupString.startsWith("on")
    }

  fun testAttributeNamePriorityVue26() {
    doAttributeNamePriorityTest(VueTestModule.VUE_2_6_10)
  }

  fun testAttributeNamePriorityVue30() {
    doAttributeNamePriorityTest(VueTestModule.VUE_3_0_0)
  }

  fun testAttributeNamePriorityVue31() {
    doAttributeNamePriorityTest(VueTestModule.VUE_3_1_0)
  }

  fun testAttributeNamePriorityVue32() {
    doAttributeNamePriorityTest(VueTestModule.VUE_3_2_2)
  }

  fun testComplexComponentDecoratorCompletion() {
    myFixture.copyDirectoryToProject("complexComponentDecorator", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "component-prop", "mixin-prop", "decorated-mixin-prop", "decorated-mixin-prop2")
    assertDoesntContain(myFixture.lookupElementStrings!!, "decorated-mixin-prop3")
  }

  fun testComplexComponentDecoratorCompletionTs() {
    myFixture.copyDirectoryToProject("complexComponentDecoratorTs", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "component-prop", "mixin-prop", "decorated-mixin-prop", "decorated-mixin-prop2")
    assertDoesntContain(myFixture.lookupElementStrings!!, "decorated-mixin-prop3")
  }

  fun testComplexComponentDecoratorCompletion8Ts() {
    myFixture.copyDirectoryToProject("complexComponentDecorator8Ts", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "component-prop", "mixin-prop", "decorated-mixin-prop", "decorated-mixin-prop2")
    assertDoesntContain(myFixture.lookupElementStrings!!, "decorated-mixin-prop3")
  }

  fun testComponentDecoratorCompletion8Name() {
    myFixture.copyDirectoryToProject("complexComponentDecorator8Ts", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.moveToOffsetBySignature("<<caret>HW")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "Empty", "empty", "HelloWorld", "hello-world")
    assertDoesntContain(myFixture.lookupElementStrings!!, "EmptyClass")
  }

  fun testDestructuringVariableTypeInVFor() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.completeBasic()
    assertStartsWith(myFixture.lookupElements!!, "first", "last")
  }

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

  fun testSlotNameCompletion() {
    myFixture.configureVueDependencies("some_lib" to "0.0.0")
    myFixture.copyDirectoryToProject("slotNames", ".")
    myFixture.configureByFile("test.vue")

    for ((tag, slots) in listOf(
      Pair("script-template-vue", listOf("scriptTemplateVue1", "scriptTemplateVue2", "default")),
      Pair("require-decorators", listOf("requireDecorators1", "requireDecorators2", "default")),
      Pair("x-template", listOf("xTemplate1", "xTemplate2", "default")),
      Pair("export-import", listOf("exportImport1", "exportImport2")),
      Pair("some-lib", listOf("someLib1", "someLib2")),
      Pair("no-script-section", listOf("noScriptSection1", "noScriptSection2", "default"))
    )) {
      for (signature in listOf("$tag><template v-slot:<caret></$tag", "$tag><div slot=\"<caret>\"</$tag")) {
        myFixture.moveToOffsetBySignature(signature)
        myFixture.completeBasic()
        assertEquals(signature, slots.sorted(), myFixture.lookupElementStrings!!.sorted())
      }
    }
  }

  fun testFilters() =
    doLookupTest(dir = true, renderTypeText = false, renderPriority = false)

  fun testComplexThisContext() =
    doLookupTest(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_5_3,
                 renderTypeText = false, renderPriority = false, dir = true)

  fun testTopLevelTagsNoI18n() {
    myFixture.configureVueDependencies()

    myFixture.configureByText("test.vue", "<<caret>")
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "i18n")
  }

  fun testTopLevelTags() {
    myFixture.configureVueDependencies("vue-i18n" to "*")
    val toTest = listOf(
      Pair("i18n", listOf("lang")),
      Pair("template", listOf("lang", "src", "functional")),
      Pair("script", listOf("lang", "id", "src")),
      Pair("style", listOf("lang", "src", "module", "scoped"))
    )
    myFixture.configureByText("test.vue", "<<caret>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, toTest.map { it.first })

    for ((tag, list) in toTest) {
      myFixture.configureByText("test.vue", "<$tag <caret>")
      myFixture.completeBasic()
      assertContainsElements(myFixture.lookupElementStrings!!, list)
    }
  }

  fun testScriptLangAttributeWithAlreadyPresentCode() {
    myFixture.configureByText("Test.vue", """
      <script la<caret>>
      export default {
        name: "Test"
      };
      </script>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.finishLookup('\t')
    myFixture.checkResult("""
      <script lang="<caret>">
      export default {
        name: "Test"
      };
      </script>
    """.trimIndent())
  }

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
    RecursionManager.disableAssertOnRecursionPrevention(myFixture.testRootDisposable)
    RecursionManager.disableMissedCacheAssertions(myFixture.testRootDisposable)

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

  fun testNoDuplicateCompletionProposals() {
    myFixture.configureByFile("noDupedAttrs.vue")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-dir2", "v-on:", "v-bind:")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "v-dir1", "v-slot", "v-slot:")
    myFixture.type("v-on:")
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "click", "dblclick")
    myFixture.type("\n\" :")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, ":dir", ":bar")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":foo", ":id")
  }

  fun testPropsDataOptionsJS() {
    myFixture.configureByFile("propsDataOptionsJS.vue")
    for ((tests, results) in listOf(
      Pair(listOf("{{this.\$props.<caret>", "{{\$props.<caret>", "this.\$props.<caret>foo"),
           listOf("mixinProp (typeText=null; priority=101.0; bold)", "aProp (typeText=null; priority=101.0; bold)")),
      Pair(listOf("{{this.\$data.<caret>", "{{\$data.<caret>", "this.\$data.<caret>foo"),
           listOf("mixinData (typeText='string'; priority=101.0; bold)", "foo (typeText='number'; priority=101.0; bold)",
                  "\$foo (typeText='number'; priority=101.0; bold)")),
      Pair(listOf("{{this.\$options.<caret>", "{{\$options.<caret>", "this.\$options.<caret>foo"),
           listOf("customOption (typeText='string'; priority=101.0; bold)", "customStuff (typeText='number'; priority=101.0; bold)",
                  "props (typeText=null; priority=101.0; bold)", "name (typeText='string'; priority=101.0; bold)"))
    )) {
      for (test in tests) {
        try {
          myFixture.moveToOffsetBySignature(test)
          myFixture.completeBasic()
          UsefulTestCase.assertContainsElements(myFixture.renderLookupItems(true, true), results)
          PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
        }
        catch (e: ComparisonFailure) {
          throw ComparisonFailure(test + ":" + e.message, e.expected, e.actual).initCause(e)
        }
        catch (e: AssertionError) {
          throw AssertionError(test + ":" + e.message, e)
        }
      }
    }
    myFixture.moveToOffsetBySignature("this.<caret>\$options.foo")
    myFixture.completeBasic()
    myFixture.renderLookupItems(false, true).let {
      assertContainsElements(it, "foo (typeText='number')")
      assertDoesntContain(it, "\$foo (typeText='number')")
    }
  }

  fun testSassGlobalFunctions() =
    WorkspaceEntityLifecycleSupporterUtils.withAllEntitiesInWorkspaceFromProvidersDefinedOnEdt(myFixture.project) {
      doLookupTest(renderTypeText = false)
    }

  fun testImportEmptyObjectInitializerComponent() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByText("FooBar.vue", "<script>export default {}</script>")
    myFixture.configureByText("check.vue", "<template><<caret></template>")
    myFixture.completeBasic()
    myFixture.type("foo-\n")
    myFixture.checkResult("""<template><foo-bar</template>
<script>
import FooBar from "./FooBar.vue";

export default {
  components: {FooBar}
}
</script>""")
  }

  fun testImportFunctionPropertyObjectInitializerComponent() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByText("FooBar.vue", "<script>export default {data(){}}</script>")
    myFixture.configureByText("check.vue", "<template><<caret></template>")
    myFixture.completeBasic()
    myFixture.type("foo-\n")
    myFixture.checkResult("""<template><foo-bar</template>
<script>
import FooBar from "./FooBar.vue";

export default {
  components: {FooBar}
}
</script>""")
  }

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

  fun testScriptSetupGlobalsTs()  =
    doLookupTest(VueTestModule.VUE_3_2_2, typeToFinishLookup = "Pro", dir = true)

  fun testTypedComponentsImportClassic() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0, VueTestModule.HEADLESS_UI_1_4_1)
    myFixture.configureByText("text.vue", "<template><Dial<caret></template>\n<script></script>")
    myFixture.completeBasic()
    myFixture.type('\n')
    myFixture.checkResultByFile("typedComponentsImportClassic.vue")
  }

  fun testTypedComponentsImportScriptSetup() {
    myFixture.configureVueDependencies(VueTestModule.HEADLESS_UI_1_4_1)
    myFixture.configureByText("text.vue", "<template><Dial<caret></template>")
    myFixture.completeBasic()
    myFixture.type('\n')
    myFixture.checkResultByFile("typedComponentsImportScriptSetup.vue")
  }

  fun testTypedComponentsImportScriptSetup2() {
    myFixture.configureVueDependencies(VueTestModule.NAIVE_UI_2_19_11_NO_WEB_TYPES)
    myFixture.configureByText("text.vue", "<template><n-a<caret></template>\n<script setup>\n</script>")
    myFixture.completeBasic()
    myFixture.type('\n')
    myFixture.checkResultByFile("typedComponentsImportScriptSetup2.vue")
  }

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

  fun testAutoImportInsertion() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.HEADLESS_UI_1_4_1)
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.completeBasic()
    myFixture.type("al\n")
    myFixture.checkResultByFile(getTestName(true) + ".after.vue")
  }

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

  fun testExternalSymbolsImport() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")

    fun doTest() {
      myFixture.moveToOffsetBySignature(":style=\"<caret>\"")
      myFixture.completeBasic()
      myFixture.typeWithWaitCoroutinesBlocking("Col\n.r")
      myFixture.completeBasic()
      myFixture.typeWithWaitCoroutinesBlocking("\n")

      myFixture.moveToOffsetBySignature(" {{ <caret> }}")
      myFixture.completeBasic()
      myFixture.typeWithWaitCoroutinesBlocking("getTe\n")

      myFixture.moveToOffsetBySignature("key) in i<caret>")
      myFixture.completeBasic()
      myFixture.typeWithWaitCoroutinesBlocking("tems\n")
    }

    myFixture.configureFromTempProjectFile("HelloWorld.vue")
    doTest()
    myFixture.checkResultByFile(getTestName(true) + "/HelloWorld.after.vue")

    myFixture.configureFromTempProjectFile("HelloWorldClassic.vue")
    doTest()
    myFixture.checkResultByFile(getTestName(true) + "/HelloWorldClassic.after.vue")
  }

  fun testImportComponentFromTextContext() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("Check.vue")
    myFixture.completeBasic()
    myFixture.checkResultByFile("${getTestName(true)}/Check.after.vue")
  }

  fun testImportNoScriptOrScriptSetupComponentInCode() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureByFile("test.ts")

    myFixture.moveToOffsetBySignature("foo: No<caret>")
    myFixture.completeBasic()
    myFixture.type("ScriptC\n")

    myFixture.moveToOffsetBySignature("bar: Sc<caret>")
    myFixture.completeBasic()
    myFixture.type("riptSet\n")

    myFixture.checkResultByFile("${getTestName(true)}/test.after.ts")

    myFixture.configureByFile("imports.ts")

    myFixture.moveToOffsetBySignature("import No<caret>")
    myFixture.completeBasic()
    myFixture.type("ScriptC\n")

    myFixture.moveToOffsetBySignature("import Sc<caret>")
    myFixture.completeBasic()
    myFixture.type("riptSet\n")

    myFixture.checkResultByFile("${getTestName(true)}/imports.after.ts")

  }

  fun testExternalScriptComponentEdit() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")

    myFixture.configureFromTempProjectFile("foo.vue")

    myFixture.completeBasic()
    myFixture.type("Col\n.r")
    myFixture.completeBasic()
    myFixture.type("\n")

    WriteAction.run<Throwable> {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    myFixture.checkResultByFile("${getTestName(true)}/foo.after.vue")

    myFixture.configureFromTempProjectFile("foo.js")
    myFixture.checkResultByFile("${getTestName(true)}/foo.after.js")
  }

  fun testExternalScriptComponentImport() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("test.vue")

    myFixture.type("<")
    myFixture.completeBasic()
    myFixture.type("fo\n :")
    myFixture.completeBasic()
    myFixture.type("ms\n")

    myFixture.checkResultByFile("${getTestName(true)}/test.after.vue")
  }

  fun testAliasedComponentImport() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("apps/vue-app/src/App.vue")

    myFixture.type("Co")
    myFixture.completeBasic()

    myFixture.checkResultByFile("${getTestName(true)}/apps/vue-app/src/App.after.vue")
  }

  fun testAliasedComponentImportKebabCase() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("apps/vue-app/src/App.vue")

    myFixture.type("Co")
    myFixture.completeBasic()

    myFixture.checkResultByFile("${getTestName(true)}/apps/vue-app/src/App.after.vue")
  }

  fun testAliasedComponentImportOptionsApi() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("apps/vue-app/src/App.vue")

    myFixture.type("Co")
    myFixture.completeBasic()

    myFixture.checkResultByFile("${getTestName(true)}/apps/vue-app/src/App.after.vue")
  }

  fun testImportComponentUsedInApp() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("src/components/CheckImportComponent.vue")

    myFixture.type("orl")
    myFixture.completeBasic()

    myFixture.checkResultByFile("${getTestName(true)}/src/components/CheckImportComponent.after.vue")
  }

  fun testVueTscComponent() =
    doLookupTest(VueTestModule.VUE_3_2_2, dir = true, renderPriority = true) {
      it.isItemTextBold
    }

  fun testVueTscComponentQualifiedComponentType() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4, additionalDependencies = mapOf("vee-validate" to "1.0.0"))

    val name = getTestName(true)
    myFixture.copyDirectoryToProject(name, "")
    forceReloadProjectRoots(project)
    myFixture.configureFromTempProjectFile("$name.vue")

    myFixture.completeBasic()
    myFixture.type('\n')

    myFixture.checkResultByFile("$name.after.vue")
  }

  fun testVueTscComponentWithSlots() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0, additionalDependencies = mapOf("vite-vue-testlib" to "0.0.2"))

    val name = getTestName(true)
    myFixture.copyDirectoryToProject(name, "")
    forceReloadProjectRoots(project)
    myFixture.configureFromTempProjectFile("$name.vue")

    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "prop1", "prop2")
  }

  fun testVueTscComponentAliasedExport() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0, additionalDependencies = mapOf("@inertiajs/vue3" to "1.0.14"))

    val name = getTestName(true)
    myFixture.copyDirectoryToProject(name, "")
    forceReloadProjectRoots(project)
    myFixture.configureFromTempProjectFile("$name.vue")

    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "as", "data", "method", "only", "on-cancel-token")
  }

  fun testWatchProperty() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByFile("watchProperty.vue")
    myFixture.completeBasic()
    myFixture.type("doubleA")
    myFixture.lookup.let { lookup ->
      lookup.currentItem = lookup.items.find { it.lookupString == "doubleAge." }
    }
    myFixture.type("\n")
    myFixture.type("\n")
    myFixture.checkResultByFile("watchProperty.after.vue")
  }

  fun testNamespacedComponents() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("scriptSetup.vue")
    myFixture.type("Forms.")
    myFixture.completeBasic()
    myFixture.type("Fo\n")
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("In\n :")
    myFixture.completeBasic()
    myFixture.type("fo\n")
    myFixture.checkResultByFile("${getTestName(true)}/scriptSetup_after.vue")
  }

  fun testNoFilterForOnProps() =
    doLookupTest()

  fun testScriptSetupGeneric() =
    doLookupTest(VueTestModule.VUE_3_3_4, locations = listOf(
      "clearable.<caret>", "value.<caret>", "Clearable).<caret>\">", "Clearable).<caret> }}", "foo.<caret>;"))

  fun testDefineModelAttribute() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByText("FooBar.vue", """
      <script setup lang="ts">
      let count = defineModel<number>('count', {default: 0})
      </script>
    """.trimIndent())
    myFixture.configureByText("app.vue", """
      <template>
        <FooBar v-model:<caret>></FooBar>
      </template>
    """.trimIndent())
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "count")
  }

  fun testInjectInLiterals() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "literal",
      "func",
      "computed",
      "funcData",
      "app",
      "scriptSetup",
      "scriptSetupRef",
      "globalProvide",
      "globalProvideRef"
    )
  }

  fun testInjectInLiteralsUnique() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertSize(3, myFixture.lookupElements!!)
    assertSameElements(
      myFixture.lookupElementStrings!!,
      "provideGlobal",
      "provideInApp",
      "provideLiteral",
    )
  }

  fun testInjectInLiteralsUnquoted() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "provideGlobal",
      "provideInApp",
      "provideLiteral",
    )
    assertDoesntContain(myFixture.lookupElementStrings!!, "\"provideGlobal\"")

    myFixture.type("provideIn")
    myFixture.completeBasic()
    myFixture.checkResultByFile("${getTestName(true)}/${getTestName(false)}.after.vue")
  }

  fun testInjectInScriptSetupLiteralsUnquoted() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "provideGlobal",
      "provideInApp",
      "provideLiteral",
    )

    myFixture.type("provideG")
    myFixture.completeBasic()
    myFixture.checkResultByFile("${getTestName(true)}/${getTestName(false)}.after.vue")
  }

  fun testInjectInProperties() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "literal",
      "func",
      "computed",
      "funcData",
      "app",
      "scriptSetup",
      "scriptSetupRef",
      "globalProvide",
      "globalProvideRef"
    )
    assertDoesntContain(myFixture.lookupElementStrings!!, "myInjectionKey")
  }

  fun testDefineSlotsSlotName() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "default", "header", "footer")
  }

  fun testComponentCustomProperties() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("componentCustomProperties", "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "attributeFromCustomProps")
  }

  fun testComponentCustomPropertiesWithFunctionOverloads() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("componentCustomPropertiesWithFunctionOverloads", "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "\$methodFromCustomProps")
  }

  fun testDefineSlotsProperties() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "year", "age", "companyName", "company-name")
    assertDoesntContain(myFixture.lookupElementStrings!!, "msg", "pageTitle", "page-title")
  }

  fun testDefineExpose() =
    doLookupTest(VueTestModule.VUE_3_3_4, dir = true)

  fun testCompleteComponentWithDefineOptions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByText("BestFeature.vue", """
      <script setup lang="ts">
      defineOptions({
        name: "BestComponentEver"
      })
      </script>
      <template></template>
    """.trimIndent())
    myFixture.configureByText("Component.vue", """
      <script setup lang="ts">
      import Best<caret>
      </script>
      <template></template>
    """.trimIndent())

    noAutoComplete {
      myFixture.completeBasic()
      assertContainsElements(myFixture.lookupElementStrings!!, "BestComponentEver")
      assertDoesntContain(myFixture.lookupElementStrings!!, "BestFeature")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
        <script setup lang="ts">
        import BestComponentEver from "./BestFeature.vue";
        </script>
        <template></template>
      """.trimIndent())
    }
  }

  private fun assertDoesntContainVueLifecycleHooks() {
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "\$el", "\$options", "\$parent")
  }

}

