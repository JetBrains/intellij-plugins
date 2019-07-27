// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.htmlInspections.*
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection
import com.intellij.lang.javascript.JSBundle
import com.intellij.lang.javascript.JSTestUtils.testWithinLanguageLevel
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.openapi.application.PathManager
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.ThrowableRunnable
import com.intellij.xml.util.CheckEmptyTagInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.html.VueFileType

class VueHighlightingTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/highlighting"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(HtmlDeprecatedAttributeInspection(),
                                HtmlDeprecatedTagInspection(),
                                HtmlUnknownBooleanAttributeInspectionBase(),
                                HtmlUnknownAttributeInspection(),
                                HtmlUnknownTagInspection(),
                                RequiredAttributesInspection(),
                                JSUnusedLocalSymbolsInspection(),
                                JSAnnotatorInspection(),
                                JSUnresolvedVariableInspection(),
                                JSUnresolvedFunctionInspection(),
                                ThisExpressionReferencesGlobalObjectJSInspection(),
                                JSValidateTypesInspection(),
                                TypeScriptValidateTypesInspection(),
                                XmlUnboundNsPrefixInspection(),
                                CheckEmptyTagInspection())
  }

  fun testDirectivesWithoutParameters() {
    myFixture.configureByText("directivesWithoutHighlighting.vue", "<template>\n" +
                                                                   "  <div v-once></div>\n" +
                                                                   "  <div v-else></div>\n" +
                                                                   "  <div v-pre></div>\n" +
                                                                   "  <div v-cloak></div>\n" +
                                                                   "</template>")
    myFixture.checkHighlighting()
  }

  fun testVIfRequireParameter() {
    myFixture.configureByText("vIfRequireParameter.vue",
                              "<template><div <warning descr=\"Wrong attribute value\">v-if</warning>></div></template>")
    myFixture.checkHighlighting()
  }

  fun testArrowFunctionsAndExpressionsInTemplate() {
    myFixture.configureByText("ArrowFunctionsAndExpressionsInTemplate.vue", """
<template>
<p>Completed Tasks: {{ ((todo) => todo.done === true)({done: 111}) }}</p>
<p>Pending Tasks: {{ todos.filter((todo) => {return todo.done === false}).length }}</p>
<div class="map" v-bind:class="{ 'map--loading': 'test', aaa: 118 }">Additional...</div>
{{todos}}
</template>
<script>
    export default {
      data: () => ({todos: [{done: true}]})
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testShorthandArrowFunctionInTemplate() {
    testWithinLanguageLevel(JSLanguageLevel.JSX, myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("ShorthandArrowFunctionInTemplate.vue", """
<template>
    <div id="app">
        <div @event="val => bar = val"></div>
        {{bar}}
    </div>
</template>
<script>
    export default {
      data: () => ({bar: 'abc'}),
      render() {
        return <div>Hello!</div>
      }
    }
</script>
""")
      myFixture.checkHighlighting()
    })
  }

  fun testShorthandArrowFunctionParsedInECMAScript5InTemplate() {
    testWithinLanguageLevel(JSLanguageLevel.ES5, myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("ShorthandArrowFunctionInTemplate.vue", """
  <template>
      <div id="app">
          <div @event="val =<error descr="expression expected">></error> bar = val"></div>
          {{bar}}
      </div>
  </template>
  <script>
      export default {
        data: ()=>({bar: 'abc'})
      }
  </script>
  """)
      myFixture.checkHighlighting()
    })

  }

  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    myFixture.configureByText("LocalPropsInArrayInCompAttrsAndWithKebabCaseAlso.vue",
                              """
<template>
    <div id="app">
        <camelCase one-two="test" three-four=1></camelCase>
        <camelCase oneTwo="test" three-four=1></camelCase>
    </div>
</template>
<script>
    export default {
      name: 'camelCase',
      props: ['oneTwo']
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
    myFixture.configureByText("LocalPropsInObjectInCompAttrsAndWithKebabCaseAlso.vue",
                              """
<template>
    <div id="app">
        <camelCase one-two="test" three-four=1></camelCase>
        <camelCase oneTwo="test" three-four=1></camelCase>
    </div>
</template>
<script>
    export default {
      name: 'camelCase',
      props: {
        oneTwo: {}
      }
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testImportedComponentPropsInCompAttrsAsArray() {
    myFixture.configureByText("compUI.vue", """
<script>
    export default {
        name: 'compUI',
        props: ['seeMe']
    }
</script>
""")
    myFixture.configureByText("ImportedComponentPropsAsArray.vue", """
<template>
    <div id="app">
        <comp-u-i see-me="12345" butNotThis="112"></comp-u-i>
        <comp-u-i seeMe="12345" butNotThis="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
    export default {
      components: {CompUI}
    }
</script>
""")
    myFixture.checkHighlighting()
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
    myFixture.configureByText("ImportedComponentPropsAsObject.vue", """
<template>
    <div id="app">
        <comp-u-i see-me="12345" butNotThis="112"></comp-u-i>
        <comp-u-i seeMe="12345" butNotThis="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
    export default {
      components: {CompUI}
    }
</script>
""")
    myFixture.checkHighlighting()
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
    myFixture.configureByText("ImportedComponentPropsAsObjectRef.vue", """
<template>
    <div id="app">
        <comp-u-i see-me="12345" butNotThis="112"></comp-u-i>
        <comp-u-i seeMe="12345" butNotThis="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
    export default {
      components: {CompUI}
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testCompRequiredAttributesTest() {
    myFixture.configureByText("CompRequiredAttributesTest.vue", """
<template>
    <div id="app">
        <<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning> v-bind='<weak_warning descr="Unresolved variable or type incorrect">incorrect</weak_warning>'></<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning></warning>>
        <<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning> :one="5"></<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning></warning></warning>>
        <camelCase one="test" two="2" three=3 with-camel-case="1" four=1></camelCase>
        <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1"></camelCase>
        <<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning> v-bind:incorrect='0' v-bind='input'></<warning descr="Element camelCase doesn't have required attribute three">camelCase</warning>>
        <camelCase v-bind:three='3' v-bind='input'></camelCase>
        <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1" not-required=11></camelCase>
        <<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning> one="test" v-bind:two="2" :three=3></<warning descr="Element camelCase doesn't have required attribute with-camel-case">camelCase</warning>>
    </div>
</template>
<script>
    export default {
      name: 'camelCase',
      props: {
        one: {required:true},
        two: {required:true},
        three: {required:true},
        withCamelCase: {required:true},
        notRequired: {required:false}
      },
      data () {
        return {
          input: {
            one: '',
            two: '',
            withCamelCase: ''
          }
        }
      }
    }
</script>""")
    myFixture.checkHighlighting()
  }

  fun testRequiredAttributeWithModifierTest() {
    myFixture.configureByText("Definition.vue", """
<script>
  export default {
    props: {
      propC: { type: String, required: true }
    }
  }
</script>""")
    myFixture.configureByText("RequiredAttributeWithModifierTest.vue", """
<template>
  <<warning descr="Element Definition doesn't have required attribute prop-c">Definition</warning>/>
  <Definition :propC.sync="smtg"/>
  <Definition propC.sync="smtg"/>
</template>
<script>
  import Definition from './Definition';
  export default {
    components: { Definition },
    data: function() {
      return {
        smtg() {}
      };
    }
  }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testVueAttributeInCustomTag() {
    myFixture.configureByText("VueAttributeInCustomTag.vue", """
<template>
  <custom v-for="<warning descr="Unused local variable item">item</warning> in items">
    Hello!
  </custom>
</template>
<script>
export default {
  props: ['distract'],
  name: 'custom',
  data: function() {
    return {items:[]};
  }
}
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testVFor() {
    myFixture.configureByText("VFor.vue", """
<template>
  <ul>
    <li v-for="(item, <warning descr="Unused local variable key">key</warning>) in items">
      {{ item.message }}
    </li>
  </ul>
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      items: [
        { message: 'Foo' },
        { message: 'Bar' }
      ]
    }
  }
</script>
    """)
    com.intellij.testFramework.runInInitMode { myFixture.checkHighlighting() }
  }

  fun testVForInPug() {
    myFixture.configureByText("VForInPug.vue", """
<template lang="pug">
  ul
    li(v-for="itemP in itemsP") {{ itemP.message }}
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      itemsP: [
        { message: 'Foo' },
        { message: 'Bar' }
      ]
    }
  }
</script>
    """)
    com.intellij.testFramework.runInInitMode { myFixture.checkHighlighting() }
  }

  fun testTopLevelThisInInjection() {
    myFixture.configureByText("TopLevelThisInInjection.vue", """
<template>
{{ this.topLevelProp }}
</template>
<script>
  export default {
    props: ['topLevelProp']
  }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testGlobalComponentLiteral() {
    myFixture.configureByText("index.js", """
Vue.component('global-comp-literal', {
  props: {
    insideGlobalCompLiteral: {}
  }
});
""")
    myFixture.configureByText("GlobalComponentLiteral.vue", """
<template>
  <global-comp-literal inside-global-comp-literal=222></global-comp-literal>
</template>
""")
    myFixture.doHighlighting()
  }

  fun testExternalMixin() {
    myFixture.configureByText("MixinWithProp.vue", """
<script>
    export default {
        props: {
            mixinProp:  {
                type: String
            },
            requiredMixinProp: {
              required: true
            }
        },
        methods: {
            helloFromMixin: function () {
                console.log('hello from mixin!')
            }
        }
    }
</script>
""")
    myFixture.configureByText("CompWithMixin.vue", """
<template>
    <div>
        <div>{{ mixinProp }}</div>
    </div>
</template>
<script>
    import Mixin from "./MixinWithProp"

    export default {
        mixins: [Mixin]
    }
</script>
""")
    myFixture.configureByText("ParentComp.vue", """
<template>
  <<warning descr="Element comp-with-mixin doesn't have required attribute required-mixin-prop">comp-with-mixin</warning> mixin-prop=123>
  1</<warning descr="Element comp-with-mixin doesn't have required attribute required-mixin-prop">comp-with-mixin</warning>>
</template>
<script>
  import CompWithMixin from './CompWithMixin'

  export default {
    components: { CompWithMixin }
  }
</script>
""")

    myFixture.checkHighlighting(true, false, true)
  }

  fun testTwoExternalMixins() {
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
  <comp-with-two-mixins first-mixin-prop=1 second-mixin-prop=2 third-mixin-prop=3></comp-with-two-mixins>
</template>
<script>
  import FirstMixin from './FirstMixin';
  import SecondMixin from './SecondMixin';

  export default {
    name: 'CompWithTwoMixins',
    mixins: [FirstMixin, SecondMixin]
  }
</script>
""")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testTwoGlobalMixins() {
    myFixture.configureByText("GlobalMixins.js", globalMixinText())
    myFixture.configureByText("CompWithGlobalMixins.vue", """
<template>
    <<warning descr="Element local-comp doesn't have required attribute required-mixin-prop">local-comp</warning> hi2dden="found" interesting-prop="777"
    not-existing=5></<warning descr="Element local-comp doesn't have required attribute required-mixin-prop">local-comp</warning>>
</template>

<script>
    export default {
        name: "local-comp"
    }
</script>
""")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testNotImportedComponentIsUnknown() {
    myFixture.configureByText("ExternalComp.vue", """
<script>
    export default {
        name: 'ExternalComp',
        props: {
          seeMe: {}
        }
    }
</script>
""")
    myFixture.configureByText("notImportedComponentIsUnknown.vue", """
<template>
    <div id="app">
      <<warning descr="Unknown html tag ExternalComp">ExternalComp</warning>>22</<warning descr="Unknown html tag ExternalComp">ExternalComp</warning>>
    </div>
</template>
""")
    myFixture.checkHighlighting()
  }

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

  fun testTypeScriptTypesAreResolved() {
    myFixture.configureByText("TypeScriptTypesAreResolved.vue", """
<script lang="ts">
    function ds(a : string) {
      encodeURI(a);
    }
    ds('a');
    export default {
        name: "some-name"
    }
</script>
""")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testTypeScriptTypesAreNotResolvedIfECMA5Script() {
    testWithinLanguageLevel(JSLanguageLevel.ES5, myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("TypeScriptTypesAreNotResolvedIfECMA5Script.vue", """
<script>
    function ds(a : <error descr="Types are not supported by current JavaScript version"><weak_warning descr="Unresolved type string">string</weak_warning></error>) {
      encodeURI(a);
    }
    ds('a');
</script>
""")
      myFixture.checkHighlighting(true, false, true)
    })
  }

  fun testVBindVOnHighlighting() {
    myFixture.configureByText("VBindHighlighting.vue", """
<template>
    <for-v-bind :class="2" v-bind:style="" :test-prop.camel="1" v-on:click="callMe" @copy="onCopy" ></for-v-bind>
    <for-v-bind class="" style="" v-on:submit.prevent></for-v-bind>
</template>

<script>
    export default {
        name: "for-v-bind",
        props: {
            testProp: {}
        },
        methods: {
            callMe() {},
            onCopy() {}
        }
    }
</script>
""")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testComponentNameAsStringTemplate() {
    myFixture.configureByText("ComponentNameAsStringTemplate.vue", """
<template>
    <open1 @click="test"></open1>
</template>
<script>
    export default {
        name: `open1`,
        methods: {
            test() {}
        }
    }
</script>
""")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testTypeScriptTypesInVue() {
    myFixture.configureByText("TypeScriptTypesInVue.vue", """
<script lang="ts">
    interface Test {
        hello: string
    }

    const <warning descr="Unused constant test">test</warning>: Test = {
        hello: "not working",
    }
</script>""")
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection())
    myFixture.checkHighlighting(true, false, true)
  }

  fun testCustomDirectives() {
    directivesTestCase(myFixture)
    myFixture.checkHighlighting(true, false, true)
  }

  fun testEmptyAttributeValue() {
    myFixture.configureByText("EmptyAttributeValue.vue", """
<template>
    <div v-for=></div>
</template>""")
    myFixture.doHighlighting()
  }

  fun testNoCreateVarQuickFix() {
    myFixture.configureByText("NoCreateVarQuickFix.vue", """
<template>
{{ <caret>someNonExistingReference2389 }}
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(
      JSBundle.message("javascript.create.variable.intention.name", "someNonExistingReference2389"))
    TestCase.assertTrue(intentions.isEmpty())
  }

  fun testNoCreateFunctionQuickFix() {
    myFixture.configureByText("NoCreateFunctionQuickFix.vue", """
<template>
<div @click="<caret>notExistingF()"></div>
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(JSBundle.message("javascript.create.function.intention.name", "notExistingF"))
    TestCase.assertTrue(intentions.isEmpty())
  }

  fun testNoCreateClassQuickFix() {
    myFixture.configureByText("NoCreateClassQuickFix.vue", """
<template>
<div @click="new <caret>NotExistingClass().a()"></div>
</template>
""")
    val intentions = myFixture.filterAvailableIntentions(JSBundle.message("javascript.create.class.intention.name", "NotExistingClass"))
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

  fun testEmptyTagsForVueAreAllowed() {
    myFixture.configureByText("EmptyTagsForVueAreAllowed.vue",
                              """
<template>
  <test-empty-tags/>
  <test-empty-tags></test-empty-tags>

  <warning descr="Empty tag doesn't work in some browsers"><div/></warning>
  <warning descr="Empty tag doesn't work in some browsers"><h1/></warning>
  <img src="aaa.jpg"/>
</template>

<script>
  export default {
    name: 'test-empty-tags'
  }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testBuiltinTagsHighlighting() {
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.copyDirectoryToProject("../types/node_modules", "./node_modules")
    myFixture.configureByText("BuiltinTagsHighlighting.vue", """
<template>
    <transition-group>
        <transition>
            Text
        </transition>
    </transition-group>
    <keep-alive>
        333
    </keep-alive>
    <component>
        ddd
    </component>
    <slot>
        fff
    </slot>
</template>""")
    myFixture.checkHighlighting()
  }

  fun testNonPropsAttributesAreNotHighlighted() {
    myFixture.configureByText("EmptyTagsForVueAreAllowed.vue",
                              """
<template>
  <non-props-component aaa="e" bbb/>
  <div <warning descr="Attribute aaa is not allowed here">aaa</warning>="1"></div>
</template>

<script>
  export default {
    name: 'non-props-component'
  }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testVueAttributeWithoutValueWithFollowingAttrubute() {
    myFixture.configureByText("VueAttributeWithoutValueWithFollowingAttrubute.vue", """
<template>
 <div v-else class="one two three four">5</div>
</template>
""")
    myFixture.doHighlighting()
  }

  fun testTsxIsNormallyParsed() {
    myFixture.configureByText("TsxIsNormallyParsed.vue",
                              """
<script lang="tsx">
    export default {
        name: "with-tsx",
        render() {
            return <div></div>
        }
    }
</script>
""")
    myFixture.checkHighlighting(true, false, true, false)
  }

  fun testJadeWithVueShortcutAttributes() {
    myFixture.configureByText("JadeWithVueShortcutAttributes.vue", """
<template lang="pug">
    div(v-if="items" @fff="4" :click="<weak_warning descr="Unresolved variable or type onClick">onClick</weak_warning>" class="someName")
</template>
""")
    myFixture.checkHighlighting(true, false, true, false)
  }

  fun testComponentsNamedLikeHtmlTags() {
    myFixture.configureByText("ColVueComponent.vue", """
<template>
    <Input><span slot="prepend"></span></Input>
    <col>
    <Col></Col>
    <Col>
<error descr="Element Col is not closed"><</error>/template>
<script lang="es6">
  export default {
    components: {
      Col: {},
      Input: {}
    }
  }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testClassComponentAnnotationWithLocalComponent() {
    createTwoClassComponents(myFixture)
    myFixture.configureByText("ClassComponentAnnotationWithLocalComponent.vue",
                              """
<template>
  <LongVue/>
  <ShortComponent/>
  <warning descr="Empty tag doesn't work in some browsers"><<warning descr="Unknown html tag UnknownComponent">UnknownComponent</warning>/></warning>
  <UsageComponent/>
</template>
<script>
import { Component, Vue } from 'vue-property-decorator';
import LongComponent from './LongComponent';
import ShortComponent from './ShortComponent';
@Component({
  name: "UsageComponent",
  components: {
    "LongVue": LongComponent,
    ShortComponent
  }
})
export default class UsageComponent extends Vue {
}
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testClassComponentAnnotationWithLocalComponentTs() {
    myFixture.configureByText("vue.d.ts", "export interface Vue {};export class Vue {}")
    createTwoClassComponents(myFixture, true)
    myFixture.configureByText("ClassComponentAnnotationWithLocalComponentTs.vue",
                              """
  <template>
    <LongVue/>
    <ShortComponent/>
    <warning descr="Empty tag doesn't work in some browsers"><<warning descr="Unknown html tag UnknownComponent">UnknownComponent</warning>/></warning>
    <UsageComponent/>
  </template>
  <script lang="ts">
  import { Component } from 'vue-property-decorator';
  // just for test
  import { Vue } from 'vue.d.ts';
  import LongComponent from './LongComponent';
  import ShortComponent from './ShortComponent';
  @Component({
    name: "UsageComponent",
    components: {
      "LongVue": LongComponent,
      ShortComponent
    }
  })
  export default class UsageComponent extends Vue {
  }
  </script>
  """)
    myFixture.checkHighlighting()
  }

  fun testLocalComponentExtends() {
    createLocalComponentsExtendsData(myFixture)
    myFixture.checkHighlighting()
  }

  fun testLocalComponentExtendsInClassSyntax() {
    myFixture.configureByText("CompAForClass.vue", """
<template>
    <div>{{ propFromA1 }}</div>
</template>

<script>
    export default {
        name: "CompAForClass",
        props: {
            propFromA1: {
                required: true
            }
        }
    }
</script>
""")
    myFixture.configureByText("LocalComponentExtendsInClassSyntax.vue", """
<template>
    <<warning descr="Element ClassA doesn't have required attribute prop-from-a1">ClassA</warning> />
</template>

<script>
    import { Vue, Component } from 'vue-property-decorator'
    import CompAForClass from './CompAForClass'

    @Component({
        name: "ClassA",
        extends: CompAForClass
    })
    export default class ClassA extends Vue {
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalComponentInClassSyntax() {
    myFixture.configureByText("CompForClass.vue", """
<template>
    <div>{{ propFromA2 }}</div>
</template>

<script>
    export default {
        name: "CompForClass",
        props: {
            propFromA2: {
                required: true
            }
        }
    }
</script>
""")
    myFixture.configureByText("OtherCompForClass.vue", """
<script>
    export default {
        name: "OtherCompForClass"
    }
</script>
""")
    myFixture.configureByText("LocalComponentExtendsInClassSyntax.vue", """
<template>
    <<warning descr="Element CompForClass doesn't have required attribute prop-from-a2">CompForClass</warning> />
    <warning descr="Empty tag doesn't work in some browsers"><<warning descr="Unknown html tag OtherCompForClass">OtherCompForClass</warning>/></warning>
</template>

<script>
    import { Vue, Component } from 'vue-property-decorator'
    import CompForClass from './CompForClass'

    @Component({
        name: "ClassA",
        components: { CompForClass }
    })
    export default class ClassAB extends Vue {
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalComponentInMixin() {
    myFixture.configureByText("b-component.vue", """
<template>
    <div>Hello</div>
</template>

<script>
    export default {
        name: 'b-component',
        props: {
            fromB: {
                required: true
            }
        }
    }
</script>
""")
    myFixture.configureByText("a-component.js", """
import B from 'b-component'

export default {
    name: 'a-component',
    components: { 'b-comp': B }
}
""")
    myFixture.configureByText("c-component.vue", """
<template>
    <<warning descr="Element b-comp doesn't have required attribute from-b">b-comp</warning>>
    </<warning descr="Element b-comp doesn't have required attribute from-b">b-comp</warning>>
</template>

<script>
    import A from 'a-component'
    export default {
        mixins: [ A ]
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalComponentInMixinRecursion() {
    myFixture.configureByText("hidden-component.vue", """
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
    myFixture.configureByText("d-component.vue", """
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
    myFixture.configureByText("b-component.vue", """
<template>
    <div>Hello</div>
</template>

<script>
    import DComponent from 'd-component'
    export default {
        name: 'b-component',
        props: {
            fromB: {
                required: true
            }
        },
        mixins: [ DComponent ]
    }
</script>""")
    myFixture.configureByText("e-component.js", """
import BComponent from 'b-component'
    export default {
        name: "e-component",
        mixins: [ BComponent ]
    }
""")
    myFixture.configureByText("c-component.vue", """
<template>
    <<warning descr="Unknown html tag b-comp">b-comp</warning>>
    </<warning descr="Unknown html tag b-comp">b-comp</warning>>
    <<warning descr="Element hidden-component doesn't have required attribute from-hidden">hidden-component</warning>/>
</template>

<script>
    import E from 'e-component'
    export default {
        mixins: [ E ]
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testBooleanProps() {
    myFixture.configureByText("a-component.vue", """
      <script>
         export default {
            props: {
              foo: Boolean,
              bar: String
            }
         }
      </script>
    """)
    myFixture.configureByText("b-component.vue", """
      <template>
        <div>
          <A foo <warning descr="Wrong attribute value">bar</warning> unknown></A>
        </div>
      </template>
      
      <script>
          import A from 'a-component'
          export default {
              components: {
                A
              }
          }
      </script>
    """)
    myFixture.checkHighlighting()

  }

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

  fun testTopLevelTags() {
    myFixture.configureByText("foo.vue",
                              "<template functional v-if='' v-else='' <warning descr=\"Attribute scoped is not allowed here\">scoped</warning>></template>\n" +
                              "<style scoped <warning descr=\"Wrong attribute value\">src</warning> module <warning descr=\"Attribute functional is not allowed here\">functional</warning>></style>")
    myFixture.checkHighlighting()
  }

  fun testEndTagNotForbidden() {
    myFixture.enableInspections(HtmlExtraClosingTagInspection::class.java)
    myFixture.addFileToProject("input.vue", "<script>export default {name: 'Input'}</script>")
    myFixture.configureByText("foo.vue", """<template> <Input> </Input> </template>
      <script>
        import Input from 'input'
        export default { components: {Input}}
      </script>""")
    myFixture.checkHighlighting()
  }

  fun testColonInEventName() {
    myFixture.enableInspections(XmlUnboundNsPrefixInspection::class.java)
    myFixture.configureByText("foo.vue", """
      |<template>
      |  <div @update:property=''></div>
      |  <div <warning descr="Attribute update:property is not allowed here"><error descr="Namespace 'update' is not bound">update</error>:property</warning>=''></div>
      |</template>""".trimMargin())
    myFixture.checkHighlighting()
  }

  fun testNoVueTagErrorsInPlainXml() {
    myFixture.addFileToProject("any.vue", "") // to make sure that Vue support works for the project
    myFixture.configureByText("foo.xml", "<component><foo/></component>".trimMargin())
    myFixture.checkHighlighting()
  }

  fun testSemanticHighlighting() {
    myFixture.configureByText("c-component.vue", """
<script lang="ts">
namespace <info descr="moduleName">space</info> {
    export class <info descr="exported class">SpaceInterface</info> {
    }
    var <info descr="static field">i</info>:<info descr="exported class">SpaceInterface</info>;
}
import <info descr="exported class">SpaceInterface</info> = <info descr="moduleName">space</info>.<info descr="exported class">SpaceInterface</info>;
var <info descr="local variable">i</info>:<info descr="exported class">SpaceInterface</info>;
</script>
""")
    myFixture.checkHighlighting(false, true, true)
  }

  fun testVSlotSyntax() {
    myFixture.configureByText("c-component.vue", """
<template>
  <div>
    <div v-slot:name="propName"></div>
    <div v-slot:name></div>
    <div v-slot="propName"></div>
    <div v-slot></div>
    
    <div <warning descr="Attribute v-slots:name is not allowed here">v-slots:name</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div <warning descr="Attribute v-slots:name is not allowed here">v-slots:name</warning>></div>
    <div <warning descr="Attribute v-slots is not allowed here">v-slots</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div <warning descr="Attribute v-slots is not allowed here">v-slots</warning>></div>
  </div>
</template>
    """)
    myFixture.checkHighlighting()
  }

  fun testVueExtendSyntax() {
    myFixture.copyDirectoryToProject("../types/node_modules", "./node_modules")
    myFixture.configureByText("a-component.vue", """<script>export default Vue.extend({props:{msg: String}})</script>""")
    myFixture.configureByText("b-component.vue", """
      <template>
        <HW msg="foo"/>
        <warning descr="Empty tag doesn't work in some browsers"><<warning descr="Unknown html tag HW2">HW2</warning> msg="foo"/></warning>
      </template>
      <script>
        import HW from './a-component.vue'
        import Vue from 'vue'
        
        export default Vue.extend({
            name: 'app',
            components: {
                HW
            },
        });
      </script>
    """)
    myFixture.checkHighlighting()
  }

  fun testBootstrapVue() {
    createPackageJsonWithVueDependency(myFixture, "\"bootstrap-vue\": \"latest\"")
    myFixture.copyDirectoryToProject("../libs/bootstrap-vue/node_modules", "./node_modules")
    myFixture.configureByText("b-component.vue", """
      <template>
        <b-alert show>Foo</b-alert>
        <b-container>
          <b-jumbotron header="BootstrapVue" lead="Bootstrap v4 Components for Vue.js 2">
            <p>For more information visit our website</p>
            <b-btn variant="primary" href="https://bootstrap-vue.js.org/">More Info</b-btn>
          </b-jumbotron>
  
          <b-form-group
            horizontal
            :label-cols="4"
            description="Let us know your name."
            label="Enter your name"
          >
            <b-form-input v-model.trim="name"></b-form-input>
          </b-form-group>
  
          <b-alert variant="success" :show="<weak_warning descr="Unresolved variable or type showAlert">showAlert</weak_warning>">Hello {{ name }}</b-alert>
        </b-container>
      </template>
      <script>
        export default {
            name: 'app',
            components: {
            },
        };
      </script>
    """)
    myFixture.checkHighlighting()
  }

  fun testDestructuringPatternsInVFor() {
    myFixture.configureByText("HelloWorld.vue", """
      <template>
          <div class="hello">
              <h1>{{ msg }}</h1>
              <ul>
                  <li v-for="({name, price}, i) in list"> {{ i }}:{{ name }} - {{ price }}</li>
                  <li v-for="{name, price} in list"> {{ 111 }}:{{ name }} - {{ price }}</li>
                  <li v-for="(x, k, i) in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
                  <li v-for="(x, k, i<error descr="closing parenthesis expected"> </error>in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
                  <li v-for="(x, k, i<error descr="closing parenthesis expected">,</error> j) in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
              </ul>
          </div>
      </template> 
      <script>
        export default {
          name: 'HelloWorld',
          data: function () {
            return {
              list: [
                {name: 'Product 1', price: 300},
                {name: 'Product 2', price: 100},
                {name: 'Product 3', price: 200}
              ]
            }
          },
          props: {
            msg: String
          }
        };
      </script>
    """)
    myFixture.checkHighlighting()
  }

  fun testDirectivesWithParameters() {
    myFixture.configureByText("a-component.vue", """
      <template>
          <div>
              <a href="#" 
                 v-clipboard:copy='code'
                 <warning descr="Attribute v-unknown:foo is not allowed here">v-unknown:foo</warning>='<weak_warning descr="Unresolved variable or type bar">bar</weak_warning>'
                 <warning descr="Attribute foo:bar is not allowed here"><error descr="Namespace 'foo' is not bound">foo</error>:bar</warning>="test">
              </a>
          </div>
      </template>
      
      <script>
          export default {
            directives: {
              clipboard: function() {}
            }
          }
      </script>
    """)
    myFixture.checkHighlighting()
  }

  fun testDirectiveWithModifiers() {
    createPackageJsonWithVueDependency(myFixture, "\"bootstrap-vue\": \"latest\"")
    myFixture.copyDirectoryToProject("../libs/bootstrap-vue/node_modules", "./node_modules")
    myFixture.configureByText("a-component.vue", """
      <template>
        <div>
          <b-button v-b-modal></b-button>
          <b-button v-b-modal.myModal></b-button>
          <b-button v-b-modal="<weak_warning descr="Unresolved variable or type myModal">myModal</weak_warning>"></b-button>
        </div>
      </template>
    """)
    myFixture.checkHighlighting()
  }

  fun testIsAttributeSupport() {
    myFixture.configureByText("a-component.vue", """
      <table>
        <tr is="blog-post-row"></tr>
        <tr is></tr>
        <tr :is="<weak_warning descr="Unresolved variable or type foo">foo</weak_warning>"></tr>
      </table>
    """)
    myFixture.checkHighlighting()
  }

  fun testPropsWithOptions() {
    myFixture.configureByFiles("propsWithOptions/usage.vue", "propsWithOptions/component.vue")
    myFixture.checkHighlighting()
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
