// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.language

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteAction.run
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase

class VueCompletionTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/types/"

  fun testCompleteCssClasses() {
    myFixture.configureByText("a.css", ".externalClass {}")
    myFixture.configureByText("a.vue", "<template><div class=\"<caret>\"></div></template><style>.internalClass</style>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "externalClass", "internalClass")
  }

  fun testCompleteAttributesWithVueInPackageJson() {
    try {
      run<Throwable> {
        val packageJson = myFixture.project.baseDir.createChildData(this, PackageJsonUtil.FILE_NAME)
        VfsUtil.saveText(packageJson, "{\"name\": \"id\", \"version\": \"1.0.0\", \"dependencies\": {\"vue\": \"2.4.1\"}}")
      }
      myFixture.configureByText("index.html", "<html <caret>></html>")
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "v-bind", "v-else")
    } finally {
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
    } finally {
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
    } finally {
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
<To<caret>
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
<ToImport<caret>
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
    JSTestUtils.testES6<Exception>(myFixture.project, {
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
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "groceryList", "parentMsg")

      myFixture.configureByText("CompleteElementsFromLocalData.vue", """
<template>{{<caret>}}</template>
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  data:
    () => {
            return {
              groceryList: {}
            }
          }
}</script>""")
      myFixture.completeBasic()
      UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "groceryList", "parentMsg")
      UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "grocery-list", "parent-msg",
                                         "GroceryList", "ParentMsg")
    })
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
                     JSTestUtils.testES6<Exception>(project, {
                       myFixture.completeBasic()
                       UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!,
                                                             "first-mixin-prop", "second-mixin-prop", "hi2dden",
                                                             "interesting-prop")
                       UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                                             "FirstMixinProp", "firstMixinProp",
                                                             "SecondMixinProp", "secondMixinProp", "Hi2dden",
                                                             "InterestingProp", "interestingProp")
                     })
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
                     JSTestUtils.testES6<Exception>(project, {
                       myFixture.completeBasic()
                       UsefulTestCase.assertContainsElements(myFixture.lookupElementStrings!!, "hi2dden", "interesting-prop")
                       UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!,
                                                          "Hi2dden", "interestingProp", "InterestingProp",
                                                          "FirstMixinProp", "firstMixinProp", "first-mixin-prop",
                                                          "SecondMixinProp", "secondMixinProp", "second-mixin-prop")
                     })
                   })
  }

  fun testNoCompletionInVueAttributes() {
    JSTestUtils.withNoLibraries(project, {
      myFixture.configureByText("NoCompletionInVueAttributes.vue", """
<template>
    <ul v-show="open<caret>">
    </ul>
</template>
""")
      myFixture.completeBasic()
      assertSameElements(myFixture.lookupElementStrings!!)
    })
  }

  fun testTypeScriptCompletionFromPredefined() {
    JSTestUtils.withNoLibraries(project, {
      myFixture.configureByText("TypeScriptCompletionFromPredefined.vue", """
<script lang="ts">
    open<caret>
</script>
""")
      myFixture.completeBasic()
      assertSameElements(myFixture.lookupElementStrings!!, "open", "opener", "onopen", "openDatabase")
    })
  }

  fun testCustomDirectivesInCompletion() {
    directivesTestCase(myFixture)
    val attribute = myFixture.findElementByText("style", XmlAttribute::class.java)
    TestCase.assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset - 1)
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "v-focus", "v-local-directive", "v-some-other-directive", "v-imported-directive")
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
    noAutoComplete( Runnable {
      myFixture.completeBasic()
      TestCase.assertNotNull(myFixture.lookupElements)
      val item : LookupElement? = myFixture.lookupElements?.firstOrNull { "callMe" == it.lookupString }
      TestCase.assertNotNull(item)
      val presentation = LookupElementPresentation()
      item!!.renderElement(presentation)
      TestCase.assertEquals("number", presentation.typeText)
      TestCase.assertEquals("(aaa, bbb)", presentation.tailText)
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
    assertContainsElements(myFixture.lookupElementStrings!!, ":two-words", ":hidden", ":onclick", ":onchange")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":use-me")

    myFixture.configureByText("CompleteVBind.vue", """
<template>
<a v-bind:<caret>></a>
</template>
$script""")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "v-bind:href", "v-bind:hidden", "v-bind:onclick", "v-bind:onchange")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":use-me", ":two-words")

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
    assertContainsElements(myFixture.lookupElementStrings!!, ":two-words", ":onclick", ":onchange")
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, ":use-me", ":v-for", ":v-bind")
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
    myFixture.copyDirectoryToProject("node_modules", "./node_modules")
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

  fun testVuetifyCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuetify\": \"0.17.2\"")
    myFixture.copyDirectoryToProject("../libs/vuetify/node_modules", "./node_modules")
    myFixture.configureByText("VuetifyCompletion.vue",
"""
<template><v-<caret></template>
""")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, listOf("v-app", "v-list", "v-list-group", "v-list-tile",
                                                                "v-list-tile-action", "v-list-tile-action-text",
                                                                "v-list-tile-avatar", "v-list-tile-content",
                                                                "v-list-tile-sub-title", "v-list-tile-title"))
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
    assertContainsElements(myFixture.lookupElementStrings!!, "aaa", ":aaa", "v-for", "ddd", "sss")
    // actually the test is against exception, which occurred on completion
    UsefulTestCase.assertDoesntContain(myFixture.lookupElementStrings!!, "123", "true")
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
    JSTestUtils.testES6<Exception>(myFixture.project, {
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
    })
  }

  fun testClassComponentCompletionTs() {
    JSTestUtils.testES6<Exception>(myFixture.project, {
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
    })
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
    JSTestUtils.testES6<Exception>(myFixture.project, {
      myFixture.configureByText("package.json", """{
          dependencies: {
            "vue-class-component" : "latest"
          }
        }""")
      createTwoClassComponents(myFixture, true)
      data.forEach {
        println("*")
        myFixture.configureByText("ComponentInsertion.vue", it.first)
        myFixture.completeBasic()
        myFixture.checkResult(it.second)
      }
    })
  }

  fun testTypescriptVForItemCompletion() {
    JSTestUtils.testES6<Exception>(myFixture.project, {
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

      assertSameElements(myFixture.lookupElementStrings!!, listOf("[Symbol.iterator]",
                                                                  "anchor",
                                                                  "big",
                                                                  "blink",
                                                                  "bold",
                                                                  "charAt",
                                                                  "charCodeAt",
                                                                  "codePointAt",
                                                                  "concat",
                                                                  "constructor",
                                                                  "endsWith",
                                                                  "fixed",
                                                                  "fontcolor",
                                                                  "fontsize",
                                                                  "hasOwnProperty",
                                                                  "includes",
                                                                  "indexOf",
                                                                  "isPrototypeOf",
                                                                  "italics",
                                                                  "lastIndexOf",
                                                                  "length",
                                                                  "link",
                                                                  "localeCompare",
                                                                  "match",
                                                                  "normalize",
                                                                  "padEnd",
                                                                  "padStart",
                                                                  "propertyIsEnumerable",
                                                                  "repeat",
                                                                  "replace",
                                                                  "search",
                                                                  "slice",
                                                                  "small",
                                                                  "split",
                                                                  "startsWith",
                                                                  "strike",
                                                                  "sub",
                                                                  "substr",
                                                                  "substring",
                                                                  "sup",
                                                                  "toLocaleLowerCase",
                                                                  "toLocaleString",
                                                                  "toLocaleUpperCase",
                                                                  "toLowerCase",
                                                                  "toString",
                                                                  "toUpperCase",
                                                                  "trim",
                                                                  "valueOf"))
    })
  }

  fun testLocalComponentsExtendsCompletion() {
    JSTestUtils.testES6<Exception>(myFixture.project, {
      createLocalComponentsExtendsData(myFixture, false)
      myFixture.completeBasic()
      assertContainsElements(myFixture.lookupElementStrings!!, "prop-from-a")
    })
  }

  fun testCompletionWithRecursiveMixins() {
    JSTestUtils.testES6<Exception>(myFixture.project, {
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
    })
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
}

fun createPackageJsonWithVueDependency(fixture: CodeInsightTestFixture,
                                       additionalDependencies: String) {
  fixture.configureByText(PackageJsonUtil.FILE_NAME, """
  {
    "name": "test",
    "version": "0.0.1",
    "dependencies": {
      "vue": "2.5.3" ${if (additionalDependencies.isBlank()) "" else ", " + additionalDependencies}
    },
    "typings": "types/index.d.ts"
  }
  """)
}
