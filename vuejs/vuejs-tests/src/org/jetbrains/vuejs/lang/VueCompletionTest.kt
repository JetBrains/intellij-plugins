// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.lang.javascript.BaseJSCompletionTestCase.*
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteAction.run
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.containers.ContainerUtil
import junit.framework.TestCase
import org.jetbrains.vuejs.codeInsight.toAsset

class VueCompletionTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/completion/"

  fun testCompleteCssClasses() {
    myFixture.configureByText("a.css", ".externalClass {}")
    myFixture.configureByText("a.vue", "<template><div class=\"<caret>\"></div></template><style>.internalClass</style>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "externalClass", "internalClass")
  }

  fun testCompleteAttributesWithVueInPackageJson() {
    try {
      run<Throwable> {
        @Suppress("DEPRECATION")
        val packageJson = myFixture.project.baseDir.createChildData(this, PackageJsonUtil.FILE_NAME)
        VfsUtil.saveText(packageJson, "{\"name\": \"id\", \"version\": \"1.0.0\", \"dependencies\": {\"vue\": \"2.4.1\"}}")
      }
      myFixture.configureByText("index.html", "<html <caret>></html>")
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-bind", "v-else")
    }
    finally {
      @Suppress("DEPRECATION")
      val packageJson = myFixture.project.baseDir.findChild(PackageJsonUtil.FILE_NAME)
      run<Throwable> { packageJson?.delete(this) }
    }
  }

  fun testCompleteAttributesWithVueFile() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("index.html", "<html <caret>></html>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-bind", "v-else")
  }

  fun testNoVueCompletionWithoutVue() {
    myFixture.configureByText("a.js", "")
    myFixture.configureByText("index.html", "<html <caret>></html>")
    myFixture.completeBasic()
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "v-bind", "v-else", ":class")
  }

  fun testCompleteImportedComponent() {
    myFixture.configureByText("compUI.vue", """
<template>{{ strangeCase }}</template>
<script>
  export default {
    props: ['strangeCase']
  }
</script>
""")
    myFixture.configureByText("CompleteImportedComponent.vue", """
<template>
<co<caret>
</template>
<script>
import compUI from 'compUI.vue'
</script>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "comp-u-i")
  }

  fun testCompleteWithImport() {
    configureTextsForCompleteLocalComponent()

    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "to-import")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
<template>
<to-import<caret>
</template>
<script>
  import ToImport from "./toImport";
  export default {
      components: {ToImport}
  }
</script>
""")
    })
  }

  private fun configureTextsForCompleteLocalComponent(tsLang: Boolean = false) {
    myFixture.configureByText("toImport.vue", """
<template>text here</template>
<script>
  export default {
    name: 'toImport',
    props: ['strangeCase']
  }
</script>
""")
    myFixture.configureByText(getTestName(false) + ".vue", """
<template>
<to<caret>
</template>
<script${if (tsLang) " lang=\"ts\"" else ""}>
  export default {
  }
</script>
""")
  }

  fun testCompleteNoImportIfSettingIsOffJs() {
    configureTextsForCompleteLocalComponent()
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseJavaScriptAutoImport
    jsApplicationSettings.isUseJavaScriptAutoImport = false
    try {
      noAutoComplete(Runnable {
        myFixture.completeBasic()
        UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "to-import")
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        myFixture.checkResult("""
<template>
<to-import<caret>
</template>
<script>
  export default {
  }
</script>
""")
      })
    }
    finally {
      jsApplicationSettings.isUseJavaScriptAutoImport = before
    }
  }

  fun testCompleteNoImportIfSettingIsOffTs() {
    configureTextsForCompleteLocalComponent(true)
    val jsApplicationSettings = JSApplicationSettings.getInstance()
    val before = jsApplicationSettings.isUseTypeScriptAutoImport
    jsApplicationSettings.isUseTypeScriptAutoImport = false
    try {
      noAutoComplete(Runnable {
        myFixture.completeBasic()
        UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "to-import")
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        myFixture.checkResult("""
<template>
<to-import<caret>
</template>
<script lang="ts">
  export default {
  }
</script>
""")
      })
    }
    finally {
      jsApplicationSettings.isUseTypeScriptAutoImport = before
    }
  }

  fun testCompleteWithImportCreateExport() {
    myFixture.configureByText("toImport.vue", """
<script>
  export default {
    name: 'toImport'
  }
</script>
""")
    myFixture.configureByText("CompleteWithImportCreateExport.vue", """
<template>
<To<caret>></To>
</template>
<script>
</script>
""")

    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "ToImport")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
<template>
<ToImport<caret>></ToImport>
</template>
<script>
    import ToImport from "./toImport";
    export default {
        components: {ToImport}
    }
</script>
""")
    })
  }

  fun testCompleteWithImportCreateScript() {
    myFixture.configureByText("toImport.vue", """
<script>
  export default {
    name: 'toImport'
  }
</script>
""")
    myFixture.configureByText("CompleteWithImportCreateScript.vue", """
<template>
<to<caret>
</template>
""")

    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "to-import")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
<template>
<to-import
</template>
<script>
    import ToImport from "./toImport";
    export default {
        components: {ToImport}
    }
</script>""")
    })
  }

  fun testCompleteWithoutImportForRenamedGlobalComponent() {
    myFixture.configureByText("libComponent.vue", """
<template>text here</template>
<script>
  export default {
    name: 'libComponent',
    props: ['strangeCase']
  }
</script>
""")
    myFixture.configureByText("main.js", """
import LibComponent from "./libComponent"
Vue.component('renamed-component', LibComponent)
""")
    myFixture.configureByText("CompleteWithoutImportForRenamedGlobalComponent.vue", """
<template>
<ren<caret>
</template>
<script>
export default {
}
</script>
""")

    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "renamed-component")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
<template>
<renamed-component<caret>
</template>
<script>
export default {
}
</script>
""")
    })
  }

  fun testCompleteWithoutImportForGlobalComponent() {
    myFixture.configureByText("lib2Component.vue", """
<template>text here</template>
<script>
  export default {
    name: 'lib2Component',
    props: ['strangeCase']
  }
</script>
""")
    myFixture.configureByText("main.js", """
import Lib2Component from "./lib2Component"
Vue.component('lib2-component', LibComponent)
""")
    myFixture.configureByText("CompleteWithoutImportForGlobalComponent.vue", """
<template>
<lib<caret>
</template>
<script>
export default {
}
</script>
""")

    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "lib2-component")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
<template>
<lib2-component<caret>
</template>
<script>
export default {
}
</script>
""")
    })
  }

  fun testCompleteAttributesFromProps() {
    myFixture.configureByText("compUI.vue", """
<template>{{ strangeCase }}</template>
<script>
  export default {
    props: ['strangeCase']
  }
</script>
""")
    myFixture.configureByText("CompleteAttributesFromProps.vue", """
<template>
<compUI <caret>></compUI>
</template>
<script>
import compUI from 'compUI.vue'

export default {
  components: {compUI}
}
</script>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "strange-case")
  }

  fun testCompletePropsInInterpolation() {
    myFixture.configureByText("CompletePropsInInterpolation.vue", """
<template>
{{<caret>}}
</template>
<script>
export default {
  name: 'childComp',
  props: {'myMessage': {}}
}
</script>""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "myMessage")
  }

  fun testCompleteComputedPropsInInterpolation() {
    myFixture.configureByText("CompleteComputedPropsInInterpolation.vue", """
<template>
{{<caret>}}
</template>
<script>
export default {
  name: 'childComp',
  props: {'MyMessage': {}},
  computed: {
    testWrong: 111,
    testRight: function() {}
  }
}
</script>""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "MyMessage", "testRight")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "testWrong")
  }

  fun testCompleteMethodsInBoundAttributes() {
    myFixture.configureByText("child.vue", """
<template>
</template>
<script>
export default {
  name: 'childComp',
  props: {'myMessage': {}},
  methods: {
    reverseMessage() {
      return this.myMessage.reverse()
    }
  }
}
</script>""")
    myFixture.configureByText("CompleteMethodsInBoundAttributes.vue", """
<template>
    <child-comp v-bind:my-message="m<caret>"></child-comp>
</template>
<script>
import ChildComp from 'child.vue'
export default {
  components: {ChildComp},
  name: 'parent',
  methods: {
    me215thod: function () {
      return 'something!'
    }
  }
}
</script>""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "me215thod")
  }

  fun testCompleteElementsFromLocalData() {
    configureVueDefinitions()
    myFixture.configureByText("CompleteElementsFromLocalData.vue", """
  <template>{{<caret>}}</template>
  <script>
  let props = ['parentMsg'];

  export default {
    name: 'parent',
    props: props,
    data: {
      groceryList: {}
    }
  }</script>""")
    assertDoesntContainVueLifecycleHooks()
    assertContainsElements(myFixture.lookupElementStrings!!, "groceryList", "parentMsg")
  }

  fun testCompleteElementsFromLocalData2() {
    configureVueDefinitions()
    myFixture.configureByText("CompleteElementsFromLocalData2.vue", """
    <template>{{<caret>}}</template>
    <script>
    let props = ['parentMsg'];

    export default {
      name: 'parent',
      props: props,
      data: () => {
                return {groceryList: 12}
              }
    }</script>""")
    assertDoesntContainVueLifecycleHooks()
    assertContainsElements(myFixture.lookupElementStrings!!, "groceryList", "parentMsg")
  }

  fun testScrInStyleCompletion() {
    val excluded = arrayOf("exclude.xml", "exclude.txt")
    val included = arrayOf("foo/bar.xml", "a.pcss", "b.styl", "c.less", "d.sass", "e.scss", "f.css")

    excluded.forEach { myFixture.addFileToProject(it, "") }
    included.forEach { myFixture.addFileToProject(it, "") }

    val file = myFixture.addFileToProject("./ScrInStyleCompletion.vue", """<style src="./<caret>"></style>""")
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    myFixture.completeBasic()

    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, included.map { it.substringBefore('/', it) })
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, excluded)
  }

  fun testScrInStyleCompletionWithLang() {
    val excluded = arrayOf("exclude.xml", "exclude.txt", "a.pcss", "c.less", "d.sass", "e.scss", "f.css")
    val included = arrayOf("foo/bar.xml", "b.styl")

    excluded.forEach { myFixture.addFileToProject(it, "") }
    included.forEach { myFixture.addFileToProject(it, "") }

    val file = myFixture.addFileToProject("./ScrInStyleCompletion.vue", """<style src="./<caret>" lang="stylus"></style>""")
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    myFixture.completeBasic()

    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, included.map { it.substringBefore('/', it) })
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, excluded)
  }

  fun testInsertAttributeWithoutValue() {
    noAutoComplete(Runnable {
      myFixture.configureByText("InsertAttributeWithoutValue.vue", "<template v-onc<caret>></template>")
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-once")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("<template v-once<caret>></template>")
    })
  }

  fun testInsertAttributeWithValue() {
    noAutoComplete(Runnable {
      myFixture.configureByText("InsertAttributeWithValue.vue", "<template v-tex<caret>></template>")
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-text")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("<template v-text=\"<caret>\"></template>")
    })
  }

  private fun noAutoComplete(callback: Runnable) {
    val old = CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = false
    try {
      callback.run()
    }
    finally {
      CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = old
    }
  }

  fun testMixinsInCompletion() {
    myFixture.configureByText("index.js", globalMixinText())
    myFixture.configureByText("FirstMixin.vue", """
<script>
  export default {
    props: ['FirstMixinProp']
  }
</script>
""")
    myFixture.configureByText("SecondMixin.vue", """
<script>
  export default {
    props: ['SecondMixinProp']
  }
</script>
""")
    myFixture.configureByText("CompWithTwoMixins.vue", """
<template>
  <mixins-in-completion <caret>></mixins-in-completion>
</template>
<script>
  import FirstMixin from './FirstMixin';
  import SecondMixin from './SecondMixin';

  export default {
    name: 'mixins-in-completion',
    mixins: [FirstMixin, SecondMixin]
  }
</script>
""")
    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!,
                                            "first-mixin-prop", "second-mixin-prop", "hi2dden",
                                            "interesting-prop")
      UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                         "FirstMixinProp", "firstMixinProp",
                                         "SecondMixinProp", "secondMixinProp", "Hi2dden",
                                         "InterestingProp", "interestingProp")
    })
  }

  fun testNoNotImportedMixinsInCompletion() {
    myFixture.configureByText("index.js", globalMixinText())
    myFixture.configureByText("FirstMixin.vue", """
<script>
  export default {
    props: ['FirstMixinProp']
  }
</script>
""")
    myFixture.configureByText("SecondMixin.vue", """
<script>
  export default {
    props: ['SecondMixinProp']
  }
</script>
""")
    myFixture.configureByText("NoNotImportedMixinsInCompletion.vue", """
<template>
  <local-comp <caret>></local-comp>
</template>
<script>
  export default {
    name: "local-comp"
  }
</script>
""")
    noAutoComplete(Runnable {
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "hi2dden", "interesting-prop")
      UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                         "Hi2dden", "interestingProp", "InterestingProp",
                                         "FirstMixinProp", "firstMixinProp", "first-mixin-prop",
                                         "SecondMixinProp", "secondMixinProp", "second-mixin-prop")
    })
  }

  fun testNoCompletionInVueAttributes() {
    JSTestUtils.withNoLibraries(project) {
      myFixture.configureByText("NoCompletionInVueAttributes.vue", """
<template>
    <ul v-show="open<caret>">
    </ul>
</template>
""")
      myFixture.completeBasic()
      assertSameElements(myFixture.lookupElementStrings!!)
    }
  }

  fun testTypeScriptCompletionFromPredefined() {
    JSTestUtils.withNoLibraries(project) {
      myFixture.configureByText("TypeScriptCompletionFromPredefined.vue", """
<script lang="ts">
    open<caret>
</script>
""")
      myFixture.completeBasic()
      assertSameElements(myFixture.lookupElementStrings!!, "open", "opener", "openDatabase")
    }
  }

  fun testCustomDirectivesInCompletion() {
    directivesTestCase(myFixture)
    val attribute = myFixture.findElementByText("style", XmlAttribute::class.java)
    TestCase.assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset - 1)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "v-focus", "v-local-directive", "v-some-other-directive",
                           "v-imported-directive")
  }

  fun testPrettyLookup() {
    myFixture.configureByText("PrettyLookup.vue", """
<template>
{{ call<caret> }}
</template>
<script>
  export default {
    methods: {
      callMe(aaa, bbb) {
        return 5;
      }
    }
  }
</script>
""")
    noAutoComplete(Runnable {
      myFixture.completeBasic()
      TestCase.assertNotNull(myFixture.lookupElements)
      val item: LookupElement? = myFixture.lookupElements?.firstOrNull { "callMe" == it.lookupString }
      TestCase.assertNotNull(item)
      val presentation = LookupElementPresentation()
      item!!.renderElement(presentation)
      TestCase.assertEquals("number", presentation.typeText)
      TestCase.assertEquals("(aaa, bbb)" + getLocationPresentation("default.methods", "PrettyLookup.vue"), presentation.tailText)
    })
  }

  fun testCompleteVBind() {
    val script =
      """
<script>
  export default {
    name: 'childComp',
    props: {
      twoWords: {
      }
    },
    methods: {
      useMe(){}
    }
  }
</script>
"""
    myFixture.configureByText("CompleteVBind.vue", """
<template>
<child-comp :<caret>></child-comp>
</template>
$script""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, ":two-words", ":hidden")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":use-me", ":onclick", ":onchange")

    myFixture.configureByText("CompleteVBind.vue", """
<template>
<a v-bind:<caret>></a>
</template>
$script""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "hidden", "key", "is")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                       ":use-me", ":two-words", "use-me", "two-words", "onclick", "onchange")

    myFixture.configureByText("User.vue", """
<template>
<child :<caret>></child>
</template>
<script>
  import Child from './CompleteVBind';
  export default {
    components: { Child }
  }
</script>
""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, ":two-words", ":about")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":use-me", ":v-for", ":v-bind", ":onclick", ":onchange")
  }

  fun testVueOutObjectLiteralCompletion() {
    configureVueDefinitions()
    myFixture.configureByText("VueOutObjectLiteralCompletion.vue", """
    <script>
      export default {
        <caret>
      }
    </script>
""")
    myFixture.completeBasic()
    assertVueExportedObjectCompletionVariants()
  }

  private fun assertVueExportedObjectCompletionVariants() {
    assertSameElements(myFixture.lookupElementStrings!!, listOf("activated", "beforeCreate", "beforeDestroy", "beforeMount",
                                                                "beforeUpdate", "comments", "compile", "component", "components",
                                                                "computed", "config", "created", "data", "deactivated", "delete",
                                                                "delimiters", "destroyed", "directive", "directives", "el",
                                                                "errorCaptured", "extend", "extends", "filter", "filters",
                                                                "functional", "inheritAttrs", "inject", "methods", "mixin",
                                                                "mixins", "model", "mounted", "name", "nextTick", "parent",
                                                                "props", "propsData", "provide", "render", "renderError",
                                                                "set", "staticRenderFns", "template", "transitions", "updated",
                                                                "use", "watch"))
  }

  fun testVueOutObjectLiteralCompletionTs() {
    configureVueDefinitions()
    myFixture.configureByText("VueOutObjectLiteralCompletionTs.vue", """
    <script lang="ts">
      export default {
        <caret>
      }
    </script>
""")
    myFixture.completeBasic()
    assertVueExportedObjectCompletionVariants()
  }

  fun testVueOutObjectLiteralCompletionJsx() {
    configureVueDefinitions()
    myFixture.configureByText("VueOutObjectLiteralCompletionJsx.vue", """
    <script lang="jsx">
      export default {
        before<caret>
      }
    </script>
""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "beforeCreate", "beforeDestroy", "beforeUpdate", "beforeMount")
  }

  private fun configureVueDefinitions() {
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.copyDirectoryToProject("../types/node_modules", "./node_modules")
  }

  fun testNoDoubleCompletionForLocalComponent() {
    myFixture.configureByText("AnotherPanel.vue", """
<script>
  import Comp from "./Comp";

  export default {
    components: {Comp},
    name: "another-panel",
    methods: {
      displayDetails() {

      }
    }
</script>""")
    myFixture.configureByText("NoDoubleCompletionForLocalComponent.vue", """
<template>
<<caret>
</template>

<script>
  import AnotherPanel from "./AnotherPanel";

  export default {
    name: 'comp',
    components: {
      AnotherPanel
    },
  }
</script>
""")
    myFixture.completeBasic()
    val cnt = myFixture.lookupElementStrings!!.filter { "another-panel" == it }.count()
    TestCase.assertEquals(1, cnt)
  }

  fun testElementUiCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"element-ui\": \"2.0.5\"")
    myFixture.copyDirectoryToProject("../libs/element-ui/node_modules", "./node_modules")
    myFixture.configureByText("ElementUiCompletion.vue",
                              """
<template><el-<caret></template>
""")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, listOf("el-col", "el-button", "el-button-group"))
  }

  fun testMintUiCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"mint-ui\": \"^2.2.3\"")
    myFixture.copyDirectoryToProject("../libs/mint-ui/node_modules", "./node_modules")
    myFixture.configureByText("MintUiCompletion.vue",
                              """
<template><mt-<caret></template>
""")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, listOf("mt-field", "mt-swipe", "mt-swipe-item"))
  }

  fun testVuetifyCompletion_017() {
    createPackageJsonWithVueDependency(myFixture, "\"vuetify\": \"0.17.2\"")
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_017/node_modules", "./node_modules")
    myFixture.configureByText("VuetifyCompletion.vue",
                              """
<template><<caret></template>
""")
    myFixture.completeBasic()
    val vuetifyComponents = VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE.toMutableList()
    vuetifyComponents.removeAll(listOf("v-breadcrumbs-divider", "VBreadcrumbsDivider",
                                       "v-autocomplete", "VAutocomplete",
                                       "v-scroll-x-transition", "VScrollXTransition",
                                       "v-scroll-y-transition", "VScrollYTransition",
                                       "v-scroll-x-reverse-transition", "VScrollXReverseTransition",
                                       "v-scroll-y-reverse-transition", "VScrollYReverseTransition",
                                       "v-tab-item", "VTabItem"))
    assertContainsElements(myFixture.lookupElementStrings!!, listOf("v-list", "v-list-group", "v-list-tile", "v-list-tile-action"))
    assertContainsElements(myFixture.lookupElementStrings!!, vuetifyComponents)
  }

  fun testVuetifyCompletion_137() {
    createPackageJsonWithVueDependency(myFixture, "\"vuetify\": \"1.3.7\"")
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_137/node_modules", "./node_modules")
    myFixture.configureByText("VuetifyCompletion.vue",
                              """
<template><<caret></template>
""")
    myFixture.completeBasic()
    val vuetifyComponents = VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE.toList()
    assertContainsElements(myFixture.lookupElementStrings!!, ContainerUtil.concat(vuetifyComponents, listOf("v-alert")))
  }

  fun testVuetifyCompletion_1210() {
    createPackageJsonWithVueDependency(myFixture, "\"vuetify\": \"1.2.10\"")
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_1210/node_modules", "./node_modules")
    myFixture.configureByText("VuetifyCompletion.vue",
                              """
<template><<caret></template>
""")
    myFixture.completeBasic()
    val vuetifyComponents = VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE.toMutableList()
    vuetifyComponents.removeAll(listOf("v-breadcrumbs-divider", "VBreadcrumbsDivider"))
    assertContainsElements(myFixture.lookupElementStrings!!, ContainerUtil.concat(vuetifyComponents, listOf("v-btn")))
  }

  fun testIviewCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"iview\": \"2.8.0\"")
    myFixture.copyDirectoryToProject("../libs/iview/node_modules", "./node_modules")
    myFixture.configureByText("IviewCompletion.vue",
                              """
<template><a<caret></template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, listOf("affix", "alert", "auto-complete", "avatar"))
  }

  fun testBootstrapVueCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"bootstrap-vue\": \"latest\"")
    myFixture.copyDirectoryToProject("../libs/bootstrap-vue/node_modules", "./node_modules")
    myFixture.configureByText("BoostrapVue.vue",
                              """
<template><<caret></template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!,
                                          listOf("b-form", "b-form-row", "b-form-text", "b-form-invalid-feedback"))
  }

  fun testShardsVueCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"shards-vue\": \"latest\"")
    myFixture.copyDirectoryToProject("../libs/shards-vue/node_modules", "./node_modules")
    myFixture.configureByText("ShardsVue.vue",
                              """
<template><<caret></template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, listOf("d-alert", "DAlert"))
  }

  fun testWrongPropsNotInCompletion() {
    myFixture.configureByText("WrongPropsNotInCompletion.vue", """
<template>
    <test-comp <caret>></test-comp>
</template>

<script>
    export default {
        name: "test-comp",
        props: ["aaa", `sss`, 'ddd', true, 123]
    };
</script>
""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "aaa", "v-for", "ddd", "sss",
                           "class", "about", "onclick", "v-bind", "v-bind:", "v-on:")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":aaa", ":ddd", ":sss", ":about", ":onclick", ":", "123", "true")

    myFixture.type(":")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, ":aaa", ":ddd", ":sss", ":about")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                       "aaa", "v-for", "ddd", "sss", "v-bind", "v-bind:", "v-on:", ":onclick")

    myFixture.type("a")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, ":aaa", ":about")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "aaa", "v-for", "ddd", "sss", "v-bind",
                                       ":ddd", ":sss", ":onclick", "v-bind:", "v-on:")
  }

  fun testBuefyCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"buefy\": \"0.6.2\"")
    myFixture.copyDirectoryToProject("../libs/buefy/node_modules", "./node_modules")
    myFixture.configureByText("BuefyCompletion.vue",
                              """
<template><b-<caret></template>
""")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.lookupElementStrings!!, listOf("b-autocomplete", "b-checkbox",
                                                                               "b-checkbox-button", "b-radio", "b-radio-button"))
  }

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
    val data = listOf(
      Pair("""<template>
  <Sho<caret>
</template>
""", """<template>
  <ShortComponent
</template>
<script>
    import Vue from "vue";
    import {Component} from "vue-class-component";
    import ShortComponent from "./ShortComponent";
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
    import {Component} from "vue-class-component";
    import ShortComponent from "./ShortComponent";
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
    import {Component} from "vue-class-component";
    import ShortComponent from "./ShortComponent";
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
    import {Component} from "vue-class-component";
</script>
""", """<template>
  <ShortComponent
</template>
<script>
    import {Component} from "vue-class-component";
    import Vue from "vue";
    import ShortComponent from "./ShortComponent";
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
    import {Component} from "vue-class-component";
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
    import {Component} from "vue-class-component";
    import ShortComponent from "./ShortComponent";
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

    function getArr() : Promise<Array<string>> {
        return new Promise<Array<string>>(resolve => {
            return resolve(['1','2','3','4']);
        })
    }

    @Component
    export default class HelloWorld extends Vue {
        @Prop() private msg!: string;
        goodTypes: Array<string> = [];
        async created (){
            this.goodTypes = await getArr()
        }
    }
</script>
""")
    myFixture.completeBasic()

    checkJSStringCompletion(myFixture.lookupElements!!, true)
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
    noAutoComplete(Runnable {
      defineRecursiveMixedMixins(myFixture)
      myFixture.configureByText("CompletionWithRecursiveMixins.vue", """
        <template>
          <HiddenComponen<caret>
        </template>
      """)
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "HiddenComponent")
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
      myFixture.checkResult("""
        <template>
          <HiddenComponent<caret>
        </template>
      """)
    })

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
    myFixture.copyDirectoryToProject("../libs/bootstrap-vue/node_modules", "./node_modules")
    myFixture.configureByText("foo.vue", "<template> <BAlert @<caret> </template>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, // first 3 items come from the BAlert component
                                             "@dismiss-count-down", "@dismissed", "@input",
                                             "@abort", "@autocomplete", "@autocompleteerror", "@blur", "@cancel", "@canplay",
                                             "@canplaythrough", "@change", "@click", "@close", "@contextmenu", "@cuechange", "@dblclick")

    myFixture.configureByText("foo.vue", "<template> <div @c<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "@cancel", "@click", "@canplaythrough", "@close", "@change", "@canplay",
                       "@cuechange", "@contextmenu")
  }

  fun testEventsAfterVOn() {
    myFixture.configureByText("foo.vue", "<template> <MyComponent v-on:cl<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "click", "close", "dblclick")

    myFixture.configureByText("foo.vue", "<template> <div v-on:<caret> </template>")
    myFixture.completeBasic()
    myFixture.assertPreferredCompletionItems(0, "abort", "autocomplete", "autocompleteerror", "blur", "cancel", "canplay",
                                             "canplaythrough", "change", "click")
  }


  fun testEventModifiers() {
    // general modifiers only
    myFixture.configureByText("foo.vue", "<template> <MyComponent @click123.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "stop", "prevent", "capture", "self", "once", "passive", "native")

    // general modifiers (except already used) + key modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div v-on:keyup.stop.passive.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "prevent", "capture", "self", "once",
                       "enter", "tab", "delete", "esc", "space", "up", "down", "left", "right",
                       "ctrl", "alt", "shift", "meta", "exact", "native")

    // general modifiers (except already used) + mouse button modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div @click.capture.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "stop", "prevent", "self", "once", "passive",
                       "left", "right", "middle",
                       "ctrl", "alt", "shift", "meta", "exact", "native")

    // general modifiers + system modifiers
    myFixture.configureByText("foo.vue", "<template> <div @drop.<caret> </template>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "stop", "prevent", "capture", "self", "once", "passive",
                       "ctrl", "alt", "shift", "meta", "exact", "native")
  }

  fun testAutopopupAfterVOnSelection() {
    myFixture.configureByText("a.vue", "<div v-o<caret>>")
    myFixture.completeBasic()
    (myFixture.lookup as LookupImpl).finishLookup(Lookup.NORMAL_SELECT_CHAR)
    // new completion must start
    myFixture.assertPreferredCompletionItems(0, "abort", "autocomplete", "autocompleteerror", "blur", "cancel", "canplay")
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
    assertNull(myFixture.lookup)
  }

  fun testVuexGettersCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_getter.js", "export const store = new Vuex.Store({\n" +
                                                "    getters: {\n" +
                                                "        getter1(state) {\n" +
                                                "            let data = {\n" +
                                                "                insideGetter1: \"uno\",\n" +
                                                "                insideGetter2: \"duos\"\n" +
                                                "            }\n" +
                                                "        },\n" +
                                                "        getter_2(state) {\n" +
                                                "        }\n" +
                                                "    }\n" +
                                                "\n" +
                                                "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapGetters([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "getter1", "getter_2")
  }

  fun testVuexMutationsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_mutations.js",
                              "export const store = new Vuex.Store({\n" +
                              "    mutations: {\n" +
                              "        mutation1(state, payload) {\n" +
                              "            let data = {\n" +
                              "                insideMutation1: \"uno\",\n" +
                              "                insideMutation2: \"duos\"\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapMutations([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testVuexMutations2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("index.js", "export const store = new Vuex.Store({\n" +
                                          "    mutations: {\n" +
                                          "        mutation1(state, payload) {\n" +
                                          "            let data = {\n" +
                                          "                mutation1_inside: \"uno\",\n" +
                                          "                mutation2_inside: \"duos\"\n" +
                                          "            }\n" +
                                          "        }\n" +
                                          "    },\n" +
                                          "    actions: {\n" +
                                          "        action1: function ({commit}, payload) {\n" +
                                          "            commit('m<caret>')\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "})")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testVuexActionsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_actions.js", "export const store = new Vuex.Store({\n" +
                                                 "    actions: {\n" +
                                                 "        action1: function ({commit}, payload) {\n" +
                                                 "            commit('mutation1')\n" +
                                                 "            let data = {\n" +
                                                 "                insideAction1: \"uno\",\n" +
                                                 "                insideAction2: \"duos\"\n" +
                                                 "            }\n" +
                                                 "        },\n" +
                                                 "        action_2: function ({commit}) {\n" +
                                                 "            commit('mutation_2')\n" +
                                                 "        },\n" +
                                                 "    },\n" +
                                                 "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapActions([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVuexActions2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_actions.js", "export const store = new Vuex.Store({\n" +
                                                 "    actions: {\n" +
                                                 "        action1: function ({commit}, payload) {\n" +
                                                 "            commit('mutation1')\n" +
                                                 "            let data = {\n" +
                                                 "                insideAction1: \"uno\",\n" +
                                                 "                insideAction2: \"duos\"\n" +
                                                 "            }\n" +
                                                 "        },\n" +
                                                 "        action_2: function ({commit}) {\n" +
                                                 "            commit('mutation_2')\n" +
                                                 "        },\n" +
                                                 "    },\n" +
                                                 "})")
    myFixture.configureByText("state.vue", "\n" +
                                           "<script>\n" +
                                           "    export default {\n" +
                                           "        },\n" +
                                           "        computed: {\n" +
                                           "            dataData() {\n" +
                                           "                this.store.dispatch('<caret>')\n" +
                                           "            },\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVueCompletionInsideScript() {
    configureVueDefinitions()
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
    configureVueDefinitions()
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        computed: {\n" +
                                          "            dataData() {this.<caret> }\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "\$el", "\$options", "\$parent")
  }

  fun testVueCompletionInsideScriptNoLifecycleHooks() {
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        computed: {\n" +
                                          "            dataData() {this.<caret> }\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "</script>")
    assertDoesntContainVueLifecycleHooks()
  }

  fun testVueCompletionInsideScriptNoLifecycleHooksTopLevel() {
    configureVueDefinitions()
    myFixture.configureByText("test.vue", "<script>\n" +
                                          "    export default {\n" +
                                          "        this.<caret> " +
                                          "    }\n" +
                                          "</script>")
    assertDoesntContainVueLifecycleHooks()
  }

  fun testVueCompletionInsideScriptNoLifecycleHooksWithoutThis() {
    configureVueDefinitions()
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

  fun testCompletionPriorityAndHints() {
    myFixture.copyDirectoryToProject("hierarchy", ".")
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_1210/node_modules", "./node_modules")
    myFixture.copyDirectoryToProject("../libs/shards-vue/node_modules/shards-vue", "./node_modules/@shards/vue")
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.completeBasic()
    assertEquals(listOf("!HW#null#100", "DCardHeader#@shards/vue#80", "HelloApp#null#90", "HelloWorld#null#50", "HeyWorld#null#80",
                        "VBottomSheet#vuetify#80", "VBottomSheetTransition#vuetify#80", "VCheckbox#vuetify#80", "VChip#vuetify#80",
                        "VDatePickerHeader#vuetify#80", "VDatePickerMonthTable#vuetify#80", "VHover#vuetify#80",
                        "VStepperHeader#vuetify#80", "VSubheader#vuetify#80", "VSwitch#vuetify#80"),
                 renderLookupItems(myFixture, renderPriority = true, renderTypeText = true)
                   .filter { !it.contains("html") }
                   .sorted())
  }

  fun testCompletionPriorityAndHintsBuiltInTags() {
    myFixture.copyDirectoryToProject("../types/node_modules", "./node_modules")
    myFixture.configureByText("b-component.vue", """
      <template>
        <<caret>
      <template>
    """)
    myFixture.completeBasic()
    assertEquals(listOf("KeepAlive#vue#80", "Transition#vue#80", "TransitionGroup#vue#80", "component#vue#0",
                        "keep-alive#vue#80", "slot#vue#0", "transition#vue#80", "transition-group#vue#80"),
                 renderLookupItems(myFixture, renderPriority = true, renderTypeText = true)
                   .filter { !it.contains("http://www.w3.org") }
                   .sorted())
  }

  fun testDirectiveCompletionOnComponent() {
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_137/node_modules", "./node_modules")
    myFixture.configureByText("a-component.vue", """
      <template>
        <v-list>
            <v-list-tile
                    v-for="color in ['primary', 'secondary', 'info', 'success', 'warning', 'error']"
                    :key="color"
                    v<caret>
            >
                <v-list-tile-title>Item with "{{ color }}" class</v-list-tile-title>
            </v-list-tile>
        </v-list>
      </template>
    """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "v-ripple", "v-resize", "v-scroll")
  }

  fun testBuiltInTagsAttributeCompletion() {
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.copyDirectoryToProject("../types/node_modules", "./node_modules")
    myFixture.configureByText("a-component.vue", """
      <template>
        <transition <caret>>
      </template>
    """)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "appear-active-class", "css", "leave-class")
  }

  fun testBindProposalsPriority() {
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_1210/node_modules", "./node_modules")
    myFixture.configureByText("b-component.vue", """
      <template>
        <v-alert v-bind:<caret>
      <template>
    """)
    myFixture.completeBasic()
    assertEquals(
      listOf("!color#100", "!dismissible#100", "!icon#100", "!mode#100", "!origin#100", "!outline#100", "!transition#100", "!type#100",
             "!value#100", "about#25", "accesskey#25", "align#25", "autocapitalize#25", "base#25", "class#25", "content#25",
             "contenteditable#25", "datafld#25", "dataformatas#25", "datasrc#25", "datatype#25", "dir#25", "draggable#25", "hidden#25",
             "id#25", "inlist#25", "is#25", "itemid#25", "itemprop#25", "itemref#25", "itemscope#25", "itemtype#25", "key#25", "lang#25",
             "prefix#25", "property#25", "rel#25", "resource#25", "rev#25", "role#25", "slot#25", "space#25", "spellcheck#25", "style#25",
             "tabindex#25", "title#25", "translate#25", "typeof#25", "vocab#25"),
      renderLookupItems(myFixture, renderPriority = true, renderTypeText = false)
        .filter { !it.contains("aria-") }
        .sorted())
  }

  fun testAttributeNamePriority() {
    myFixture.copyDirectoryToProject("../libs/vuetify/vuetify_1210/node_modules", "./node_modules")
    myFixture.configureByText("b-component.vue", """
      <template>
        <v-alert <caret>
      <template>
    """)
    myFixture.completeBasic()
    assertEquals(
      listOf("!color#100", "!dismissible#100", "!icon#100", "!mode#100", "!origin#100", "!outline#100", "!transition#100", "!type#100",
             "!value#100", "about#0", "accesskey#0", "align#0", "autocapitalize#0", "class#0", "content#0", "contenteditable#0",
             "datafld#0", "dataformatas#0", "datasrc#0", "datatype#0", "dir#0", "draggable#0", "hidden#0", "id#0", "inlist#0", "is#25",
             "itemid#0", "itemprop#0", "itemref#0", "itemscope#0", "itemtype#0", "prefix#0", "property#0", "ref#25", "rel#0",
             "resource#0", "rev#0", "role#0", "slot#0", "slot-scope#0", "spellcheck#0", "style#0", "tabindex#0", "title#0", "translate#0",
             "typeof#0", "v-bind#25", "v-bind:#25", "v-cloak#25", "v-else#25", "v-else-if#25", "v-for#25", "v-html#25", "v-if#25",
             "v-model#25", "v-on:#25", "v-once#25", "v-pre#25", "v-show#25", "v-slot#25", "v-text#25", "vocab#0", "xml:base#0",
             "xml:lang#0", "xml:space#0"),
      renderLookupItems(myFixture, renderPriority = true, renderTypeText = false)
        .filter { !it.contains("aria-") && !it.startsWith("on") }
        .sorted())
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

  fun testDestructuringVariableTypeInVFor() {
    configureVueDefinitions()
    myFixture.configureByFile(getTestName(false) + ".vue")
    myFixture.completeBasic()
    assertStartsWith(myFixture.lookupElements!!, "first", "last")
  }

  private fun assertDoesntContainVueLifecycleHooks() {
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "\$el", "\$options", "\$parent")
  }

}

fun createPackageJsonWithVueDependency(fixture: CodeInsightTestFixture,
                                       additionalDependencies: String) {
  fixture.configureByText(PackageJsonUtil.FILE_NAME, """
  {
    "name": "test",
    "version": "0.0.1",
    "dependencies": {
      "vue": "2.5.3" ${if (additionalDependencies.isBlank()) "" else ", $additionalDependencies"}
    },
    "typings": "types/index.d.ts"
  }
  """)
}

private val VUETIFY_UNRESOLVED_COMPONENTS = setOf(
  //grid components
  "v-flex",
  "v-spacer",
  "v-container",
  "v-layout",
  //functional components
  "v-autocomplete",
  "v-bottom-sheet-transition",
  "v-breadcrumbs-divider",
  "v-carousel-reverse-transition",
  "v-carousel-transition",
  "v-dialog-bottom-transition",
  "v-dialog-transition",
  "v-expand-transition",
  "v-fab-transition",
  "v-fade-transition",
  "v-menu",
  "v-menu-transition",
  "v-row-expand-transition",
  "v-select",
  "v-scale-transition",
  "v-scroll-x-reverse-transition",
  "v-scroll-x-transition",
  "v-scroll-y-reverse-transition",
  "v-scroll-y-transition",
  "v-slide-x-reverse-transition",
  "v-slide-x-transition",
  "v-slide-y-reverse-transition",
  "v-slide-y-transition",
  "v-stepper-items",
  "v-tab-item",
  "v-tab-reverse-transition",
  "v-tab-transition",
  "v-table-overflow",
  "v-tabs",
  "v-tabs-items",
  "v-text-field",
  "v-card-actions",
  "v-card-text",
  "v-list-tile-action",
  "v-list-tile-action-text",
  "v-list-tile-content",
  "v-list-tile-sub-title",
  "v-list-tile-title",
  "v-stepper-header",
  "v-toolbar-items",
  "v-toolbar-title"
)
private val VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE: MutableIterable<String> = ContainerUtil.concat(VUETIFY_UNRESOLVED_COMPONENTS,
                                                                                                           VUETIFY_UNRESOLVED_COMPONENTS.map {
                                                                                                             toAsset(it).capitalize()
                                                                                                           })
