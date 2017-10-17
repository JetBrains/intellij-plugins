package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.codeInsight.VueJSSpecificHandlersFactory

/**
 * @author Irina.Chernushina on 7/28/2017.
 */
class VueResolveTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/testData/resolve/"

  fun testResolveInjectionToPropInObject() {
    myFixture.configureByText("ResolveToPropInObject.vue", """
<template>
    <div class="list">
        <ul>
            <li>
                {{ <caret>message25620 }}
            </li>
        </ul>
    </div>
</template>

<script>
  export default {
    name: 'list',
    props: {message25620: {}}
  }
  let message25620 = 111;
</script>""")
    val reference =  myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    TestCase.assertNotNull(reference)
    TestCase.assertTrue(reference is JSReferenceExpression)
    val resolver = VueJSSpecificHandlersFactory().createReferenceExpressionResolver(
      reference as JSReferenceExpressionImpl, true)
    val results = resolver.resolve(reference, false)
    TestCase.assertEquals(1, results.size)
    TestCase.assertTrue(results[0].element!! is JSProperty)
    TestCase.assertEquals("message25620", (results[0].element!! as JSProperty).name)
  }

  fun testResolveUsageInAttributeToPropInArray() {
    myFixture.configureByText("ResolveToPropInObject.vue", """
<template>
  <list25620 v-text="'prefix' + <caret>message25620Arr + 'postfix'">
  Text
  </list25620>
</template>

<script>
  export default {
    name: 'list25620',
    props: ['message25620Arr']
  }
</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    TestCase.assertTrue(reference is JSReferenceExpression)
    val resolver = VueJSSpecificHandlersFactory().createReferenceExpressionResolver(
      reference as JSReferenceExpressionImpl, true)
    val results = resolver.resolve(reference, false)
    TestCase.assertEquals(1, results.size)
    val literal = results[0].element!!
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue(literal.parent is JSArrayLiteralExpression)
    TestCase.assertEquals("'message25620Arr'", literal.text)
  }

  fun testResolveAttributeInPascalCaseUsageInPropsArray() {
    myFixture.configureByText("ResolveAttributeInPascalCaseUsageInPropsArray.vue", """
<template>
  <list25620 <caret>PascalCase">
  Text
  </list25620>
</template>

<script>
  export default {
    name: 'list25620',
    props: ['pascalCase']
  }
  let message25620 = 111;
</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val literal = reference!!.resolve()
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue(literal!!.parent is JSArrayLiteralExpression)
    TestCase.assertEquals("'pascalCase'", literal.text)
  }

  fun testResolveIntoComputedProperty() {
    myFixture.configureByText("ResolveIntoComputedProperty.vue", """
<template>
{{<caret>TestRight}}
</template>
<script>
export default {
  name: 'childComp',
  props: {'myMessage': {}},
  computed: {
    testWrong: 111,
    testRight: function() {}
  }
}
</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("testRight", (property as JSProperty).name)
  }

  fun testResolveIntoComputedES6FunctionProperty() {
    myFixture.configureByText("ResolveIntoComputedES6FunctionProperty.vue", """
<template>
{{<caret>TestRight}}
</template>
<script>
export default {
  name: 'childComp',
  props: {'myMessage': {}},
  computed: {
    testWrong: 111,
    testRight() {}
  }
}
</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("testRight", (property as JSProperty).name)
  }

  fun testResolveIntoMethodsFromBoundAttributes() {
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
    myFixture.configureByText("ResolveIntoMethodsFromBoundAttributes.vue", """
<template>
    <child-comp v-bind:my-message="me215t<caret>hod"></child-comp>
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
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("me215thod", (property as JSProperty).name)
  }

  fun testResolveLocallyInsideComponentPropsArray() {
    myFixture.configureByText("ResolveLocallyInsideComponentPropsArray.vue", """
<script>
export default {
  name: 'parent',
  props: ['parentMsg', 'parentSize'],
  computed: {
    normalizedSize: function () {
      return this.<caret>parentMsg.trim().toLowerCase()
    }
  }
}</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val literal = reference!!.resolve()
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    TestCase.assertEquals("'parentMsg'", literal.text)
  }

  fun testResolveLocallyInsideComponentPropsArrayRefVariant() {
    myFixture.configureByText("ResolveLocallyInsideComponentPropsArrayRefVariant.vue", """
<script>
let props = ['parentMsg', 'parentSize'];
export default {
  name: 'parent',
  props: props,
  computed: {
    normalizedSize: function () {
      return this.<caret>parentMsg.trim().toLowerCase()
    }
  }
}</script>""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val literal = reference!!.resolve()
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    TestCase.assertEquals("'parentMsg'", literal.text)
  }

  fun testResolveLocallyInsideComponentArrayFunctionInsideExport() {
    JSTestUtils.testES6<Exception>(myFixture.project, {
      myFixture.configureByText("ResolveLocallyInsideComponentArrayFunctionInsideExport.vue", """
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  methods: {
    oneMethod: () => {
      return this.<caret>parentMsg * 3;
    }
  }
}</script>""")
      val reference = myFixture.getReferenceAtCaretPosition()
      TestCase.assertNotNull(reference)
      val literal = reference!!.resolve()
      TestCase.assertTrue(literal is JSLiteralExpression)
      TestCase.assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
      TestCase.assertEquals("'parentMsg'", literal.text)
    })
  }

  fun testResolveLocallyInsideComponent() {

    doTestResolveLocallyInsideComponent("""
<script>
export default {
  name: 'parent',
  props: {parentMsg: {}, parentSize: {}},
  computed: {
    normalizedSize: function () {
      return this.<caret>parentMsg.trim().toLowerCase()
    }
  }
}</script>
""", "parentMsg")

    doTestResolveLocallyInsideComponent("""
<script>
let props = {parentMsg: {}, parentSize: {}};
export default {
  name: 'parent',
  props: props,
  computed: {
    normalizedSize: function () {
      return this.<caret>parentMsg.trim().toLowerCase()
    }
  }
}</script>
""", "parentMsg")

    doTestResolveLocallyInsideComponent("""
<script>
let props = {parentMsg: {}, parentSize: {}};
export default {
  name: 'parent',
  props: props,
  methods: {
    normalizedSize() {
      return this.<caret>parentMsg.trim().toLowerCase()
    }
  }
}</script>
""", "parentMsg")

    doTestResolveLocallyInsideComponent("""
<script>
let props = {parentMsg: {}, parentSize: {}};
let methods = {
    wise() {
      return this.<caret>normalizedSize() / 2;
    }
  };
let computedProps = {
    normalizedSize() {
      return this.parentMsg.trim().toLowerCase()
    }
  };
export default {
  name: 'parent',
  props: props,
  computedProps: computedProps,
  methods: methods
}</script>
""", "normalizedSize")

    doTestResolveLocallyInsideComponent("""
<script>
let props = {parentMsg: {}, parentSize: {}};

function wouldBeUsedLater() {
  return this.<caret>parentMsg * 3;
}

export default {
  name: 'parent',
  props: props,
}</script>
""", "parentMsg")

    doTestResolveLocallyInsideComponent("""
<script>
let props = ['parentMsg'];

let wouldBeUsedLater = () => {
  return this.<caret>parentMsg * 3;
}

export default {
  name: 'parent',
  props: props,
}</script>
""", null)

    doTestResolveLocallyInsideComponent("""
<template>{{<caret>groceryList}}</template>
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  data: {
    groceryList: {}
  }
}</script>
""", "groceryList")

    doTestResolveLocallyInsideComponent("""
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  methods: {
    callMum() {
      return this.<caret>groceryList;
    }
  }
  data: {
    groceryList: {}
  }
}</script>
""", "groceryList")

    doTestResolveLocallyInsideComponent("""
<template>{{<caret>groceryList}}</template>
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  data: () => ({
    groceryList: {}
  })
}</script>
""", "groceryList")

    doTestResolveLocallyInsideComponent("""
<template>{{<caret>groceryList}}</template>
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
}</script>
""", "groceryList")

    doTestResolveLocallyInsideComponent("""
<template>{{groceryList.<caret>carrot}}</template>
<script>
let props = ['parentMsg'];

export default {
  name: 'parent',
  props: props,
  data:
    function () {
            return {
              groceryList: {
                carrot: {}
              }
            }
          }
}</script>
""", "carrot")
}

  fun doTestResolveLocallyInsideComponent(text: String, expectedPropertyName: String?) {
    myFixture.configureByText("ResolveLocallyInsideComponent.vue", text)
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    if (expectedPropertyName == null) {
      TestCase.assertNull(property)
    } else {
      TestCase.assertTrue(property is JSProperty)
      TestCase.assertEquals(expectedPropertyName, (property as JSProperty).name)
    }
  }

  fun testIntoVForVar() {
    myFixture.configureByText("IntoVForVar.vue", """
<template>
  <ul>
    <li v-for="item in items">
      {{ <caret>item.message }}
    </li>
  </ul>
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent.parent is VueVForExpression)
  }

  fun testVForDetailsResolve() {
    myFixture.configureByText("IntoVForDetailsResolve.vue", """
<template>
  <ul>
    <li v-for="item in items">
      {{ item.<caret>message }}
    </li>
  </ul>
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      items: [
        { message: 'Foo' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val part = reference!!.resolve()
    TestCase.assertNotNull(part)
    TestCase.assertTrue(part is JSProperty)
    TestCase.assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  fun testVForIteratedExpressionResolve() {
    myFixture.configureByText("VForIteratedExpressionResolve.vue", """
<template>
  <ul>
    <li v-for="item in <caret>items">
      {{ item.message }}
    </li>
  </ul>
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      items: [
        { message: 'Foo' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val part = reference!!.resolve()
    TestCase.assertNotNull(part)
    TestCase.assertTrue(part is JSProperty)
    TestCase.assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  fun testIntoVForVarInPug() {
    myFixture.configureByText("IntoVForVarInPug.vue", """
<template lang="pug">
  ul
    li(v-for="item in items") {{ <caret>item.message }}
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent.parent is VueVForExpression)
  }

  fun testVForDetailsResolveInPug() {
    myFixture.configureByText("IntoVForDetailsResolveInPug.vue", """
<template lang="pug">
  ul
    li(v-for="item in items") {{ item.<caret>message }}
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      items: [
        { message: 'Foo' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val part = reference!!.resolve()
    TestCase.assertNotNull(part)
    TestCase.assertTrue(part is JSProperty)
    TestCase.assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  fun testVForIteratedExpressionResolveInPug() {
    myFixture.configureByText("VForIteratedExpressionResolveInPug.vue", """
<template lang="pug">
  ul
    li(v-for="item in <caret>itemsPP") {{ item.message }}
</template>
<script>
  export default {
    name: 'v-for-test',
    data: {
      itemsPP: [
        { message: 'Foo' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val part = reference!!.resolve()
    TestCase.assertNotNull(part)
    TestCase.assertTrue(part is JSProperty)
    TestCase.assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  fun testIntoVForVarInHtml() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("IntoVForVarInHtml.html", """
<html>
  <ul>
    <li v-for="itemHtml in itemsHtml">
      {{ <caret>itemHtml.message }}
    </li>
  </ul>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent.parent is VueVForExpression)
  }

  fun testKeyIntoForResolve() {
    myFixture.configureByText("KeyIntoForResolve.vue", """
<template>
  <li v-for="(item1, index1) of items1" :key="<caret>item1" v-if="item1 > 0">
    {{ parentMessage1 }} - {{ index1 }} - {{ item1.message1 }}
  </li>
</template>
<script>
  export default {
    data: {
      parentMessage1: 'Parent',
      items1: [
        { message1: 'Foo' },
        { message1: 'Bar' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent is JSVarStatement)
    TestCase.assertTrue(variable.parent.parent is JSParenthesizedExpression)
    TestCase.assertTrue(variable.parent.parent.parent is VueVForExpression)
  }

  fun testVIfIntoForResolve() {
    myFixture.configureByText("VIfIntoForResolve.vue", """
<template>
  <li v-for="(item1, index1) in items1" :key="item1" v-if="<caret>item1 > 0">
    {{ parentMessage1 }} - {{ index1 }} - {{ item1.message1 }}
  </li>
</template>
<script>
  export default {
    data: {
      parentMessage1: 'Parent',
      items1: [
        { message1: 'Foo' },
        { message1: 'Bar' }
      ]
    }
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent is JSVarStatement)
    TestCase.assertTrue(variable.parent.parent is JSParenthesizedExpression)
    TestCase.assertTrue(variable.parent.parent.parent is VueVForExpression)
  }

  fun testKeyIntoForResolveHtml() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("KeyIntoForResolveHtml.html", """
<html>
  <li id="id123" v-for="(item1, index1) in items1" :key="<caret>item1" v-if="item1 > 0">
    {{ parentMessage1 }} - {{ index1 }} - {{ item1.message1 }}
  </li>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent is JSVarStatement)
    TestCase.assertTrue(variable.parent.parent is JSParenthesizedExpression)
    TestCase.assertTrue(variable.parent.parent.parent is VueVForExpression)
  }

  fun testResolveByMountedVueInstanceInData() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("ResolveByMountedVueInstanceInData.js", """
new Vue({
  el: '#ResolveByMountedVueInstanceInData',
  data: {
    messageToFind: 'Parent'
  }
})
""")
    myFixture.configureByText("ResolveByMountedVueInstanceInData.html", """
<!DOCTYPE html>
<html lang="en">
<body>
<ul id="ResolveByMountedVueInstanceInData">
  {{ <caret>messageToFind }}
</ul>
</body>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertTrue(property!!.parent.parent is JSProperty)
    TestCase.assertEquals("data", (property.parent.parent as JSProperty).name)
  }

  fun testResolveByMountedVueInstanceInProps() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("ResolveByMountedVueInstanceInProps.js", """
new Vue({
  el: '#ResolveByMountedVueInstanceInProps',
  props: ['compProp']
})
""")
    myFixture.configureByText("ResolveByMountedVueInstanceInProps.html", """
<!DOCTYPE html>
<html lang="en">
<body>
<ul id="ResolveByMountedVueInstanceInProps">
  {{ <caret>compProp }}
</ul>
</body>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val arrayItem = reference!!.resolve()
    TestCase.assertNotNull(arrayItem)
    TestCase.assertTrue(arrayItem is JSLiteralExpression)
    TestCase.assertTrue(arrayItem!!.parent.parent is JSProperty)
    TestCase.assertEquals("props", (arrayItem.parent.parent as JSProperty).name)
  }

  fun testResolveVForIterableByMountedVueInstance() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("ResolveVForIterableByMountedVueInstance.js", """
new Vue({
  el: '#ResolveVForIterableByMountedVueInstance',
  data: {
    parentMessage: 'Parent',
    mountedItems: [
      { message2233: 'Foo' },
      { message2233: 'Bar' }
    ]
  }
})
""")
    myFixture.configureByText("ResolveVForIterableByMountedVueInstance.html", """
<!DOCTYPE html>
<html lang="en">
<body>
<ul id="ResolveVForIterableByMountedVueInstance">
  <li v-for="(item, index) in <caret>mountedItems">
    {{parentMessage }} - {{ index }} - {{item.message2233 }}
  </li>
</ul>
</body>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertTrue(property!!.parent.parent is JSProperty)
    TestCase.assertEquals("data", (property.parent.parent as JSProperty).name)
  }

  fun testKeyIntoForResolvePug() {
    myFixture.configureByText("KeyIntoForResolvePug.vue", """
<template lang="pug">
  ul
    li(id="id123" v-for="(item123, index1) in items1", :key="<caret>item123") {{ parentMessage1 }}
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val variable = reference!!.resolve()
    TestCase.assertNotNull(variable)
    TestCase.assertTrue(variable!!.parent is JSVarStatement)
    TestCase.assertTrue(variable.parent.parent is JSParenthesizedExpression)
    TestCase.assertTrue(variable.parent.parent.parent is VueVForExpression)
  }

  fun testResolveForRenamedGlobalComponent() {
    myFixture.configureByText("libComponent.vue", """
<template>text here</template>
<script>
  export default {
    name: 'libComponent',
    props: ['libComponentProp']
  }
</script>
""")
    myFixture.configureByText("main.js", """
import LibComponent from "./libComponent"
Vue.component('renamed-component', LibComponent)
""")
    myFixture.configureByText("CompleteWithoutImportForRenamedGlobalComponent.vue", """
<template>
<renamed-component <caret>lib-component-prop=1></renamed-component>
</template>
<script>
export default {
}
</script>
""")

    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val literal = reference!!.resolve()
    TestCase.assertNotNull(literal)
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertEquals("'libComponentProp'", literal!!.text)
    TestCase.assertTrue(literal.parent is JSArrayLiteralExpression)
    TestCase.assertEquals("props", (literal.parent.parent as JSProperty).name)
  }

  fun testLocalQualifiedNameOfGlobalComponent() {
    myFixture.configureByText("LocalQualifiedNameOfGlobalComponent.js", """
      let CompDef = {
        props: {
          kuku: {}
        }
      };

      Vue.component('complex-ref', CompDef);
    """.trimIndent())
    myFixture.configureByText("LocalQualifiedNameOfGlobalComponent.vue", """
      <template>
        <complex-ref <caret>kuku="e23"></complex-ref>
      </template>
    """.trimIndent())

    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("kuku", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testResolveVueRouterComponents() {
    myFixture.configureByFile("vue-router.js")
    myFixture.configureByText("ResolveVueRouterComponents.vue", """
      <template>
        <router-link <caret>to="/post"></router-link>
      </template>
    """.trimIndent())

    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("to", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testResolveIntoGlobalComponentInLocalVar() {
    myFixture.configureByText("ResolveIntoGlobalComponentInLocalVarComponent.js", """
(function(a, b, c) {/* ... */}
(
  a,b,
  function() {
      let CompDefIFFE = {
        props: {
          from: {}
        }
      };

      function install() {
        Vue.component('iffe-comp', CompDefIFFE);
      }
  }
))
""")
    myFixture.configureByText("ResolveIntoGlobalComponentInLocalVarComponent.vue", """
      <template>
        <iffe-comp <caret>from="e23"></complex-ref>
      </template>
""")

    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("from", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
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
  <global-comp-literal <caret>inside-global-comp-literal=222></global-comp-literal>
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("insideGlobalCompLiteral", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testResolveMixinProp() {
    myFixture.configureByText("MixinWithProp.vue", """
<script>
    export default {
        props: {
            mixinProp:  {
                type: String
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
  <comp-with-mixin <caret>mixin-prop=123>1</comp-with-mixin>
</template>
<script>
  import CompWithMixin from './CompWithMixin'
  export default {
    components: { CompWithMixin }
  }
</script>
""")

    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("mixinProp", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
    TestCase.assertEquals("MixinWithProp.vue", property.containingFile.name)
  }

  fun testResolveIntoLocalMixin() {
    myFixture.configureByText("ResolveIntoLocalMixin.vue", """
<template>
    <local-mixin <caret>local-mixin-prop="1" local-prop="1"></local-mixin>
</template>

<script>
    let LocalMixin = {
        props: {
            localMixinProp: {
                required: true
            }
        }
    };

    export default {
        name: "local-mixin",
        mixins: [LocalMixin],
        props: {
            localProp: {}
        }
    }
</script>
""")

    doTestResolveIntoProperty("localMixinProp")
  }

  fun testResolveInMixinLiteral() {
    myFixture.configureByText("ResolveInMixinLiteral.vue", """
<template>
    <local-mixin <caret>prop-in-mixin-literal="11" local-prop="1"></local-mixin>
</template>

<script>
    export default {
        name: "local-mixin",
        mixins: [{
            props: {
                propInMixinLiteral: {}
            }
        }],
        props: {
            localProp: {}
        }
    }
</script>
""")

    doTestResolveIntoProperty("propInMixinLiteral")
  }

  private fun doTestResolveIntoProperty(name: String) {
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val property = reference!!.resolve()
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals(name, (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testResolveIntoGlobalMixin1() {
    myFixture.configureByText("GlobalMixins.js", globalMixinText())
    myFixture.configureByText("ResolveIntoGlobalMixin1.vue", """
<template>
    <local-comp <caret>hi2dden="found" interesting-prop="777"</local-comp>
</template>

<script>
    export default {
        name: "local-comp"
    }
</script>
""")
    doTestResolveIntoProperty("hi2dden")
  }

  fun testResolveIntoGlobalMixin2() {
    myFixture.configureByText("GlobalMixins.js", globalMixinText())
    myFixture.configureByText("ResolveIntoGlobalMixin2.vue", """
<template>
    <local-comp hi2dden="found" <caret>interesting-prop="777"</local-comp>
</template>

<script>
    export default {
        name: "local-comp"
    }
</script>
""")
    doTestResolveIntoProperty("interestingProp")
  }
}

fun globalMixinText(): String {
  return """
  let mixin = {
      props: {
          hi2dden: {}
      }
  };

  Vue.mixin(mixin);

  Vue.mixin({
      props: {
          interestingProp: {},
          requiredMixinProp: {
            required: true
          }
      }
  });
  """
}