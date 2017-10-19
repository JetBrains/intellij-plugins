package org.jetbrains.vuejs.language

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspectionBase
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.inspections.JSAnnotatorInspection
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.ThrowableRunnable
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection
import junit.framework.TestCase
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
class VueHighlightingTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspectionBase())
    myFixture.enableInspections(HtmlUnknownAttributeInspection())
    myFixture.enableInspections(HtmlUnknownTagInspection())
    myFixture.enableInspections(RequiredAttributesInspection())
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection())
    myFixture.enableInspections(JSAnnotatorInspection())
    myFixture.enableInspections(JSUnresolvedVariableInspection())
    myFixture.enableInspections(ThisExpressionReferencesGlobalObjectJSInspection())
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
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
    })
  }

  fun testShorthandArrowFunctionInTemplate() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JSX, myFixture.project, ThrowableRunnable<Exception> {
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

  fun testShorthandArrowFunctionNotParsedInECMAScript5InTemplate() {
      myFixture.configureByText("ShorthandArrowFunctionInTemplate.vue", """
<template>
    <div id="app">
        <div @event="val =<error descr="expression expected">></error> bar = val"></div>
        {{bar}}
    </div>
</template>
<script>
    export default {
      data: (<error descr="expression expected">)</error> =<error descr="expression expected">></error> ({bar: 'abc'})<EOLError descr="statement expected"></EOLError>
    }
</script>
""")
      myFixture.checkHighlighting()
  }

  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("LocalPropsInArrayInCompAttrsAndWithKebabCaseAlso.vue",
                                """
<template>
    <div id="app">
        <camelCase one-two="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
        <camelCase oneTwo="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
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
    })
  }

  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("LocalPropsInObjectInCompAttrsAndWithKebabCaseAlso.vue",
                                """
<template>
    <div id="app">
        <camelCase one-two="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
        <camelCase oneTwo="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
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
    })
  }

  fun testImportedComponentPropsInCompAttrsAsArray() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
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
    })
  }

  fun testImportedComponentPropsInCompAttrsAsObject() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
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
    })
  }

  fun testImportedComponentPropsInCompAttrsObjectRef() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
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
    })
  }

  fun testLocalPropsInArrayInCompAttrsRef() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("LocalPropsInArrayInCompAttrsRef.vue",
                                """
<template>
    <div id="app">
        <camelCase one-two="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
        <camelCase oneTwo="test" <warning descr="Attribute three-four is not allowed here">three-four</warning>=1></camelCase>
    </div>
</template>
<script>
const props = ['oneTwo']
    export default {
      name: 'camelCase',
      props: props
    }
</script>
""")
      myFixture.checkHighlighting()
    })
  }

  fun testCompRequiredAttributesTest() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("CompRequiredAttributesTest.vue", """
<template>
    <div id="app">
        <<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning></warning></warning></warning>></<warning descr="Element camelCase doesn't have required attribute one"><warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning></warning></warning></warning>>
        <<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning></warning></warning> :one="5"></<warning descr="Element camelCase doesn't have required attribute three"><warning descr="Element camelCase doesn't have required attribute two"><warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning></warning></warning>>
        <camelCase one="test" two="2" three=3 with-camel-case="1" <warning descr="Attribute four is not allowed here">four</warning>=1></camelCase>
        <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1"></camelCase>
        <camelCase one="test" v-bind:two="2" :three=3 withCamelCase="1" not-required=11></camelCase>
        <<warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning> one="test" v-bind:two="2" :three=3></<warning descr="Element camelCase doesn't have required attribute withCamelCase">camelCase</warning>>
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
      }
    }
</script>""")
      myFixture.checkHighlighting()
    })
  }

  fun testVueAttributeInCustomTag() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
    })
  }

  fun testVFor() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
    })
  }

  fun testVForInPug() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
    com.intellij.testFramework.runInInitMode{ myFixture.checkHighlighting() }
    })
  }

  fun testTopLevelThisInInjection() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
  })
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
  <<warning descr="Element comp-with-mixin doesn't have required attribute requiredMixinProp">comp-with-mixin</warning> mixin-prop=123>
  1</<warning descr="Element comp-with-mixin doesn't have required attribute requiredMixinProp">comp-with-mixin</warning>>
</template>
<script>
  import CompWithMixin from './CompWithMixin'

  export default {
    components: { CompWithMixin }
  }
</script>
""")

    JSTestUtils.testES6<Exception>(project, { myFixture.checkHighlighting(true, false, true) })
  }

  fun testNotImportedComponentIsUnknown() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
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
    })
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
    val list = myFixture.doHighlighting().filter { it.severity.name == "TYPO"}
    val typoRanges : MutableSet<Pair<Int, Int>> = mutableSetOf()
    for (info in list) {
      val pair = Pair(info.startOffset, info.endOffset)
      if (!typoRanges.add(pair)) TestCase.assertTrue("Duplicate $pair", false)
    }
  }

  // todo make look like master after other commits are merged
  fun testComponentNameAsStringTemplate() {
    myFixture.configureByText("ComponentNameAsStringTemplate.vue", """
<template>
    <open1 ></open1>
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
    JSTestUtils.testES6<Exception>(project, { myFixture.checkHighlighting(true, false, true) })
  }
}