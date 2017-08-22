package org.jetbrains.vuejs.language

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspectionBase
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.ThrowableRunnable

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
    let todos = 1;
</script>
""")
      myFixture.checkHighlighting()
    })
  }

  fun testShorthandArrowFunctionInTemplate() {
    JSTestUtils.testES6(myFixture.project, ThrowableRunnable<Exception> {
      myFixture.configureByText("ShorthandArrowFunctionInTemplate.vue", """
<template>
    <div id="app">
        <div @event="val => bar = val"></div>
        {{bar}}
    </div>
</template>
<script>
    export default {
      data: () => ({bar: 'abc'})
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
      data: () => ({bar: 'abc'})
    }
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
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
  }

  fun testLocalPropsInObjectInCompAttrsAndWithKebabCaseAlso() {
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
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
        <comp-u-i see-me="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
        <comp-u-i seeMe="12345" <warning descr="Attribute butNotThis is not allowed here">butNotThis</warning>="112"></comp-u-i>
    </div>
</template>
<script>
    import CompUI from 'compUI.vue'
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testLocalPropsInArrayInCompAttrsRef() {
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
  }

  fun testCompRequiredAttributesTest() {
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
  }

  fun testVueAttributeInCustomTag() {
    myFixture.configureByText("VueAttributeInCustomTag.vue", """
<template>
  <custom v-for="item in items">
    Hello!
  </custom>
</template>
<script>
export default {
  name: 'custom',
  data: function() {
    return {items:[]};
  }
}
</script>
""")
    myFixture.checkHighlighting()
  }
}