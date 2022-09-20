// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.*
import junit.framework.TestCase
import org.jetbrains.vuejs.codeInsight.VueJSSpecificHandlersFactory
import org.jetbrains.vuejs.lang.VueTestModule.VUE_2_6_10
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueNamedSymbol
import org.jetbrains.vuejs.model.VueRegularComponent

class VueResolveTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/resolve/"

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
    val reference = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    TestCase.assertNotNull(reference)
    TestCase.assertTrue(reference is JSReferenceExpression)
    val resolver = VueJSSpecificHandlersFactory().createReferenceExpressionResolver(
      reference as JSReferenceExpressionImpl, true)
    val results = resolver.resolve(reference, false)
    TestCase.assertEquals(1, results.size)
    TestCase.assertTrue(results[0].element!!.parent!! is JSProperty)
    TestCase.assertEquals("message25620", (results[0].element!!.parent!! as JSProperty).name)
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
    val element = results[0].element!!
    assertInstanceOf(element, JSImplicitElement::class.java)
    val literal = element.parent
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertInstanceOf(literal.parent, JSArrayLiteralExpression::class.java)
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
    val literal = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertInstanceOf(literal!!.parent, JSArrayLiteralExpression::class.java)
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
    TestCase.assertTrue((property as JSImplicitElement).context is JSFunctionItem)
    TestCase.assertEquals("testRight", (property.context as JSFunctionItem).name)
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
    TestCase.assertTrue((property as JSImplicitElement).context is JSFunctionItem)
    TestCase.assertEquals("testRight", (property.context as JSFunctionItem).name)
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
    val literal = reference!!.resolve()!!.parent
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
    val literal = reference!!.resolve()!!.parent
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    TestCase.assertEquals("'parentMsg'", literal.text)
  }

  fun testResolveLocallyInsideComponentArrayFunctionInsideExport() {
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
    val literal = reference!!.resolve()!!.parent
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    TestCase.assertEquals("'parentMsg'", literal.text)
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

  private fun doTestResolveLocallyInsideComponent(text: String, expectedPropertyName: String?) {
    myFixture.configureByText("ResolveLocallyInsideComponent.vue", text)
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val target = reference!!.resolve()
    if (expectedPropertyName == null) {
      TestCase.assertNull(target)
    }
    else {
      val property = if (target is JSImplicitElement) target.parent else target
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
    TestCase.assertTrue(variable!!.parent.parent is VueJSVForExpression)
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
    TestCase.assertTrue(variable!!.parent.parent is VueJSVForExpression)
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
    createPackageJsonWithVueDependency(myFixture)
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
    TestCase.assertTrue(variable!!.parent.parent is VueJSVForExpression)
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
    TestCase.assertTrue(variable.parent.parent.parent is VueJSVForExpression)
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
    TestCase.assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  fun testKeyIntoForResolveHtml() {
    createPackageJsonWithVueDependency(myFixture)
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
    TestCase.assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  fun testResolveByMountedVueInstanceInData() {
    createPackageJsonWithVueDependency(myFixture)
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
    createPackageJsonWithVueDependency(myFixture)
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
    UsefulTestCase.assertInstanceOf(arrayItem, JSImplicitElement::class.java)
    UsefulTestCase.assertInstanceOf(arrayItem!!.parent, JSLiteralExpression::class.java)
    UsefulTestCase.assertInstanceOf(arrayItem.parent.parent.parent, JSProperty::class.java)
    TestCase.assertEquals("props", (arrayItem.parent.parent.parent as JSProperty).name)
  }

  fun testResolveVForIterableByMountedVueInstance() {
    createPackageJsonWithVueDependency(myFixture)
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
    TestCase.assertTrue(variable.parent.parent.parent is VueJSVForExpression)
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

    val literal = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    TestCase.assertEquals("'libComponentProp'", literal!!.text)
    assertInstanceOf(literal.parent, JSArrayLiteralExpression::class.java)
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

    val property = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(property, JSProperty::class.java)
    TestCase.assertEquals("kuku", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testResolveVueRouterComponents() {
    myFixture.configureByFile("vue-router.js")
    myFixture.configureByText("ResolveVueRouterComponents.vue", """
      <template>
        <router-link <caret>to="/post"></router-link>
      </template>
    """.trimIndent())

    val property = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(property, JSProperty::class.java)
    TestCase.assertEquals("to", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
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

    val property = myFixture.webSymbolSourceAtCaret()!!.parent
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("from", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testGlobalComponentNameInReference() {
    myFixture.configureByText("WiseComp.vue",
                              """
<script>export default { name: 'wise-comp', props: {} }</script>
""")
    myFixture.configureByText("register.es6",
                              """
import WiseComp from 'WiseComp'
const alias = 'wise-comp-alias'
Vue.component(alias, WiseComp)
""")
    myFixture.configureByText("use.vue",
                              """
<template><<caret>wise-comp-alias</template>
""")
    doResolveAliasIntoLibraryComponent("wise-comp", "WiseComp.vue")
  }

  private fun doResolveAliasIntoLibraryComponent(compName: String, fileName: String) {
    val target = myFixture.webSymbolSourceAtCaret()
    TestCase.assertNotNull(target)
    TestCase.assertEquals(fileName, target!!.containingFile.name)
    assertInstanceOf(target.parent, JSProperty::class.java)
    TestCase.assertEquals(compName, (target as JSLiteralExpression).value)
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
    val element = myFixture.webSymbolSourceAtCaret()
    val property = element!!.parent
    assertInstanceOf(property, JSProperty::class.java)
    TestCase.assertEquals("insideGlobalCompLiteral", (property as JSProperty).name)
    TestCase.assertTrue(property.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  fun testLocalPropsInArrayInCompAttrsAndWithKebabCaseAlso() {
    myFixture.configureByText("LocalPropsInArrayInCompAttrsAndWithKebabCaseAlso.vue",
                              """
<template>
    <div id="app">
        <camelCase <caret>one-two="test" three-four=1></camelCase>
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
    myFixture.doHighlighting()
    val literal = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    TestCase.assertEquals("oneTwo", (literal as JSLiteralExpression).stringValue)
    TestCase.assertTrue(literal.parent is JSArrayLiteralExpression)
  }

  fun testLocalPropsInArrayInCompAttrsRef() {
    myFixture.configureByText("LocalPropsInArrayInCompAttrsRef.vue",
                              """
<template>
    <div id="app">
        <camelCase <caret>one-two="test" three-four=1></camelCase>
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
    myFixture.doHighlighting()
    val literal = myFixture.webSymbolSourceAtCaret()!!.parent
    TestCase.assertNotNull(literal)
    TestCase.assertTrue(literal is JSLiteralExpression)
    TestCase.assertEquals("oneTwo", (literal as JSLiteralExpression).stringValue)
    TestCase.assertTrue(literal.parent is JSArrayLiteralExpression)
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
        <comp-u-i <caret>see-me="12345" butNotThis="112"></comp-u-i>
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
    myFixture.doHighlighting()
    val literal = myFixture.webSymbolSourceAtCaret()!!.parent
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    TestCase.assertEquals("seeMe", (literal as JSLiteralExpression).stringValue)
    TestCase.assertEquals("compUI.vue", literal.containingFile.name)
    assertInstanceOf(literal.parent, JSArrayLiteralExpression::class.java)
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
        <comp-u-i <caret>see-me="12345" ></comp-u-i>
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
    val property = myFixture.webSymbolSourceAtCaret()!!.parent
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertEquals("seeMe", (property as JSProperty).name)
    TestCase.assertEquals("compUI.vue", property.containingFile.name)
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
        <comp-u-i <caret>see-me="12345" butNotThis="112"></comp-u-i>
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
    val property = myFixture.webSymbolSourceAtCaret()!!.parent
    TestCase.assertNotNull(property)
    TestCase.assertTrue(property is JSProperty)
    TestCase.assertTrue(property!!.parent.parent is JSProperty)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
    TestCase.assertEquals("seeMe", (property as JSProperty).name)
    TestCase.assertEquals("compUI.vue", property.containingFile.name)
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

    val property = myFixture.webSymbolSourceAtCaret()?.parent
    assertInstanceOf(property, JSProperty::class.java)
    TestCase.assertEquals("mixinProp", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    TestCase.assertEquals("props", (property.parent.parent as JSProperty).name)
    TestCase.assertEquals("MixinWithProp.vue", property.containingFile.name)
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
  <comp-with-two-mixins <caret>first-mixin-prop=1 second-mixin-prop=2 />
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

    val checkResolve = { propName: String, file: String ->
      val literal = myFixture.webSymbolSourceAtCaret()!!.parent
      assertInstanceOf(literal, JSLiteralExpression::class.java)
      TestCase.assertEquals(propName, (literal as JSLiteralExpression).stringValue)
      assertInstanceOf(literal.parent.parent, JSProperty::class.java)
      TestCase.assertEquals("props", (literal.parent.parent as JSProperty).name)
      TestCase.assertEquals(file, literal.containingFile.name)
    }
    checkResolve("FirstMixinProp", "FirstMixin.vue")

    val attribute = myFixture.findElementByText("second-mixin-prop", XmlAttribute::class.java)
    TestCase.assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset)
    checkResolve("SecondMixinProp", "SecondMixin.vue")
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
    val property = myFixture.webSymbolSourceAtCaret()?.parent
    assertInstanceOf(property, JSProperty::class.java)
    TestCase.assertEquals(name, (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
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

  fun testTypeScriptResolve() {
    myFixture.configureByText("TypeScriptResolve.vue", """
<script lang="ts"><caret>encodeURI('a')</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val function = reference!!.resolve()
    TestCase.assertNotNull(function)
    TestCase.assertTrue(function is TypeScriptFunction)
    TestCase.assertEquals("lib.es5.d.ts", function!!.containingFile.name)
  }

  fun testECMA5Resolve() {
    myFixture.configureByText("TypeScriptResolve.vue", """
<script><caret>encodeURI('a')</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val function = reference!!.resolve()
    TestCase.assertNotNull(function)
    TestCase.assertTrue(function is TypeScriptFunction)
    TestCase.assertEquals("lib.es5.d.ts", function!!.containingFile.name)
  }

  fun testVBindResolve() {
    myFixture.configureByText("VBindCommonResolve.vue", """
<template>
    <for-v-bind :<caret>test-prop.camel="1"></for-v-bind>
</template>
<script>
    export default {
        name: "for-v-bind",
        props: {
            testProp: {}
        }
    }
</script>
""")
    doTestResolveIntoProperty("testProp")
  }

  fun testResolveGlobalCustomDirective() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")
    val attribute = myFixture.findElementByText("v-focus", XmlAttribute::class.java)
    TestCase.assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)

    val callExpression = myFixture.webSymbolSourceAtCaret()
    TestCase.assertNotNull(callExpression)
    // unstub for test
    TestCase.assertNotNull(callExpression!!.text)
    TestCase.assertEquals("focus", ((callExpression as JSCallExpression).arguments[0] as JSLiteralExpression).stringValue)
    TestCase.assertEquals("CustomDirectives.js", callExpression.containingFile.name)
  }

  fun testResolveLocalCustomDirective() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")

    arrayOf(Trinity("v-local-directive", "localDirective", "CustomDirectives.vue"),
            Trinity("v-some-other-directive", "someOtherDirective", "CustomDirectives.vue"),
            Trinity("v-click-outside", "click-outside", "CustomDirectives.js"),
            Trinity("v-imported-directive", "importedDirective", "importedDirective.js"))
      .forEach {
        val attribute = myFixture.findElementByText(it.first, XmlAttribute::class.java)
        TestCase.assertNotNull(attribute)
        myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)
        doTestResolveIntoDirective(it.second, it.third)
      }
  }

  fun testResolveLocalCustomDirectiveLinkedFiles() {
    myFixture.copyDirectoryToProject("../common/customDirectivesLinkedFiles", ".")
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.configureFromTempProjectFile("CustomDirectives.html")

    arrayOf(Trinity("v-local-directive", "localDirective", "CustomDirectives.js"),
            Trinity("v-some-other-directive", "someOtherDirective", "CustomDirectives.js"),
            Trinity("v-click-outside", "click-outside", "GlobalCustomDirectives.js"),
            Trinity("v-imported-directive", "importedDirective", "importedDirective.js"))
      .forEach {
        val attribute = myFixture.findElementByText(it.first, XmlAttribute::class.java)
        TestCase.assertNotNull(attribute)
        myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)
        doTestResolveIntoDirective(it.second, it.third)
      }
  }

  private fun doTestResolveIntoDirective(directive: String, fileName: String) {
    val property = myFixture.webSymbolSourceAtCaret()
    TestCase.assertNotNull(directive, property)
    when (property) {
      is JSProperty -> {
        TestCase.assertEquals(directive, directive, property.name)
        TestCase.assertEquals(directive, fileName, property.containingFile.name)
      }
      is JSCallExpression -> {
        TestCase.assertNotNull(directive, property.text)
        TestCase.assertEquals(directive, directive, (property.arguments[0] as JSLiteralExpression).stringValue)
        TestCase.assertEquals(directive, fileName, property.containingFile.name)
      }
      is JSObjectLiteralExpression -> {
        TestCase.assertNotNull(directive, property.text)
        TestCase.assertEquals(directive, fileName, property.containingFile.name)
      }
      else -> TestCase.assertTrue("$directive class: ${property?.javaClass?.name}", false)
    }
  }

  fun testResolveIntoVueDefinitions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByText("ResolveIntoVueDefinitions.vue", """
<script>
  export default {
    <caret>mixins: []
  }
</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    TestCase.assertNotNull(reference)
    val target = reference!!.resolve()
    TestCase.assertNotNull(target)
    TestCase.assertEquals("options.d.ts", target!!.containingFile.name)
    TestCase.assertTrue(target.parent is TypeScriptPropertySignature)
  }

  fun testResolveElementUiComponent() {
    myFixture.configureVueDependencies(VueTestModule.ELEMENT_UI_2_0_5)
    val testData = arrayOf(
      Trinity("el-col", "ElCol", "col.js"),
      Trinity("el-button", "ElButton", "button.vue"),
      Trinity("el-button-group", "ElButtonGroup", "button-group.vue")
    )
    testData.forEach {
      myFixture.configureByText("ResolveElementUiComponent.vue", "<template><<caret>${it.first}></${it.first}></template>")
      doResolveIntoLibraryComponent(it.second, it.third)
    }
  }

  fun testResolveMintUiComponent() {
    myFixture.configureVueDependencies(VueTestModule.MINT_UI_2_2_3)
    val testData = arrayOf(
      Trinity("mt-field", "mt-field", "field.vue"),
      Trinity("mt-swipe", "mt-swipe", "swipe.vue"),
      Trinity("mt-swipe-item", "mt-swipe-item", "swipe-item.vue")
    )
    testData.forEach {
      myFixture.configureByText("ResolveMintUiComponent.vue", "<template><<caret>${it.first}></${it.first}></template>")
      doResolveIntoLibraryComponent(it.second, it.third)
    }
  }

  // Resolve into web-types libraries not supported for now.
  @Suppress("TestFunctionName", "unused")
  fun _testResolveVuetifyComponent() {
    myFixture.configureVueDependencies(VueTestModule.VUETIFY_0_17_2)
    val testData = arrayOf(
      Trinity("v-list", "v-list", "VList.js"),
      Trinity("v-list-tile-content", "v-list-tile-content", "index.js")
    )
    testData.forEach {
      myFixture.configureByText("ResolveVuetifyComponent.vue", "<template><<caret>${it.first}></${it.first}></template>")
      if (it.first == "v-list-tile-content") {
        val reference = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(reference)
        val target = reference!!.resolve()
        TestCase.assertNotNull(target)
        TestCase.assertEquals(it.third, target!!.containingFile.name)
        TestCase.assertTrue(target.parent is JSCallExpression)
      }
      else {
        doResolveIntoLibraryComponent(it.second, it.third)
      }
    }
  }

  fun testElementUiDatePickerLikeComponent() {
    myFixture.configureByText("date-picker.js", """
export default {
    name: 'ElDatePicker',
    mixins: []
}
""")
    myFixture.configureByText("index.js", """
import DatePicker from './date-picker';

/* istanbul ignore next */
DatePicker.install = function install(Vue) {
  Vue.component(DatePicker.name, DatePicker);
};

export default DatePicker;
""")
    myFixture.configureByText("usage.vue", """
<template>
<<caret>el-date-picker />
</template>
""")
    doResolveIntoLibraryComponent("ElDatePicker", "date-picker.js")
  }

  fun testResolveSimpleObjectMemberComponent() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("lib-comp.es6",
                              """
export default {
  name: 'lib-comp',
  template: '',
  render() {}
}
""")
    myFixture.configureByText("lib.es6",
                              """
import LibComp from './lib-comp';
const obj = { LibComp };

Object.keys(obj).forEach(key => {
        Vue.component(key, obj[key]);
    });
""")
    myFixture.configureByText("ResolveSimpleObjectMemberComponent.vue",
                              """<template><<caret>lib-comp/></template>""")
    doResolveIntoLibraryComponent("lib-comp", "lib-comp.es6")
  }

  fun testResolveAliasedObjectMemberComponent() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("lib-comp-for-alias.es6",
                              """
export default {
  name: 'lib-comp',
  template: '',
  render() {}
}
""")
    myFixture.configureByText("libAlias.es6",
                              """
import Alias from './lib-comp-for-alias';
const obj = { Alias };

Object.keys(obj).forEach(key => {
        Vue.component(key, obj[key]);
    });
""")
    myFixture.configureByText("ResolveAliasedObjectMemberComponent.vue",
                              """<template><<caret>alias/></template>""")

    val target = myFixture.webSymbolSourceAtCaret()
    TestCase.assertNotNull(target)
    TestCase.assertEquals("lib-comp-for-alias.es6", target!!.containingFile.name)
    TestCase.assertTrue(target.parent is JSProperty)
  }

  fun testResolveObjectWithSpreadComponent() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("lib-spread.es6",
                              """
export default {
  name: 'lib-spread',
  template: '',
  render() {}
}
""")
    myFixture.configureByText("lib-register-spread.es6",
                              """
import LibSpread from './lib-spread';
const obj = { LibSpread };
const other = {...obj};

Object.keys(other).forEach(key => {
        Vue.component(key, other[key]);
    });
""")
    myFixture.configureByText("ResolveObjectWithSpreadComponent.vue",
                              """<template><<caret>lib-spread/></template>""")
    doResolveIntoLibraryComponent("lib-spread", "lib-spread.es6")
  }

  fun testResolveObjectWithSpreadComponentAliased() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("lib-spread.es6",
                              """
export default {
  name: 'lib-spread',
  template: '',
  render() {}
}
""")
    myFixture.configureByText("lib-register-spread.es6",
                              """
import LibSpreadAlias from './lib-spread';
const obj = { LibSpreadAlias };
const other = {...obj};

Object.keys(other).forEach(key => {
        Vue.component(key, other[key]);
    });
""")
    myFixture.configureByText("ResolveObjectWithSpreadComponentAliased.vue",
                              """<template><<caret>lib-spread-alias/></template>""")
    val target = myFixture.webSymbolSourceAtCaret()
    TestCase.assertNotNull(target)
    TestCase.assertEquals("lib-spread.es6", target!!.containingFile.name)
    TestCase.assertTrue(target.parent is JSProperty)
  }

  fun testResolveObjectWithSpreadLiteralComponent() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("lib-spread.es6",
                              """
export default {
  name: 'lib-spread',
  template: '',
  render() {}
}
""")
    myFixture.configureByText("lib-register-spread.es6",
                              """
import LibSpread from './lib-spread';
const other = {...{ LibSpread }};

Object.keys(other).forEach(key => {
        Vue.component(key, other[key]);
    });
""")
    myFixture.configureByText("ResolveObjectWithSpreadLiteralComponent.vue",
                              """<template><<caret>lib-spread/></template>""")
    doResolveIntoLibraryComponent("lib-spread", "lib-spread.es6")
  }

  fun testResolveWithExplicitForInComponentsBindingEs6() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("CompForForIn.es6",
                              """export default {
name: 'compForForIn',
template: '',
render() {}""")
    myFixture.configureByText("register.es6", """
import CompForForIn from './CompForForIn';

      const components = {
        CompForForIn
      }

components.install = (Vue, options = {}) => {
    for (const componentName in components) {
        const component = components[componentName]

        if (component && componentName !== 'install') {
            Vue.component(component.name, component)
        }
    }
}
""")
    myFixture.configureByText("ResolveWithExplicitForInComponentsBinding.vue",
                              """<template><<caret>CompForForIn/></template>""")
    doResolveIntoLibraryComponent("compForForIn", "CompForForIn.es6")
  }

  fun testResolveWithExplicitForInComponentsBinding() {
    myFixture.configureByText("a.vue", "")
    myFixture.configureByText("CompForForIn.vue",
                              """<script>export default {
name: 'compForForIn',
template: '',
render() {}</script>""")
    myFixture.configureByText("register.es6", """
import CompForForIn from './CompForForIn';

      const components = {
        CompForForIn
      }

components.install = (Vue, options = {}) => {
    for (const componentName in components) {
        const component = components[componentName]

        if (component && componentName !== 'install') {
            Vue.component(component.name, component)
        }
    }
}
""")
    myFixture.configureByText("ResolveWithExplicitForInComponentsBinding.vue",
                              """<template><<caret>CompForForIn/></template>""")
    doResolveIntoLibraryComponent("compForForIn", "CompForForIn.vue")
  }

  fun testResolveWithClassComponent() {
    createTwoClassComponents(myFixture)
    myFixture.configureByText("ResolveWithClassComponent.vue",
                              """
<template>
  <<caret>ShortVue/>
  <LongComponent/>
</template>
<script>
import { Component, Vue } from 'vue-property-decorator';
import ShortComponent from './ShortComponent';
import LongComponent from './LongComponent';

@Component({
  components: {
    shortVue: ShortComponent,
    LongComponent
  }
})
export default class UsageComponent extends Vue {
}
</script>
""")
    myFixture.checkGotoDeclaration("<Short<caret>Vue", 107, "ShortComponent.vue")
  }

  fun testResolveWithClassComponentTs() {
    createPackageJsonWithVueDependency(myFixture)
    createTwoClassComponents(myFixture, true)
    myFixture.configureByText("ResolveWithClassComponentTs.vue",
                              """
<template>
  <ShortVue/>
  <<caret>LongComponent/>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import ShortComponent from './ShortComponent';
import LongComponent from './LongComponent';

@Component({
  components: {
    shortVue: ShortComponent,
    LongComponent
  }
})
export default class UsageComponent extends Vue {
}
</script>
""")
    val target = myFixture.resolveToWebSymbolSource("<<caret>LongComponent/>")
    TestCase.assertEquals("ResolveWithClassComponentTs.vue", target.containingFile.name)
    assertInstanceOf(target, ES6Property::class.java)
    myFixture.checkGotoDeclaration("<<caret>LongComponent/>", 145, "LongComponent.vue")
  }

  fun testLocalComponentsExtendsResolve() {
    createLocalComponentsExtendsData(myFixture, false)
    myFixture.type("prop-from-a=\"\"")
    myFixture.editor.caretModel.moveToOffset(myFixture.editor.caretModel.offset - 5)
    doTestResolveIntoProperty("propFromA")
  }

  private fun doResolveIntoLibraryComponent(compName: String, fileName: String) {
    val target = myFixture.webSymbolSourceAtCaret()
    TestCase.assertEquals(fileName, target!!.containingFile.name)
    TestCase.assertTrue(target.parent is JSProperty)
    TestCase.assertEquals(compName, StringUtil.unquoteString((target.parent as JSProperty).value!!.text))
  }

  fun testResolveWithRecursiveMixins() {
    defineRecursiveMixedMixins(myFixture)
    myFixture.configureByText("ResolveWithRecursiveMixins.vue", """
        <template>
          <<caret>HiddenComponent/>
        </template>
      """)
    myFixture.checkGotoDeclaration("<Hidden<caret>Component/>", 33, "hidden-component.vue")
    myFixture.configureByText("ResolveWithRecursiveMixins2.vue", """
        <template>
          <<caret>OneMoreComponent/>
        </template>
      """)
    myFixture.checkGotoDeclaration("<One<caret>MoreComponent/>", 214, "OneMoreComponent.vue")
  }

  fun testCssClassInPug() {
    myFixture.configureByText("foo.vue", "<template lang='pug'>\n" +
                                         "    .someClass\n" +
                                         "</template>\n" +
                                         "<style>\n" +
                                         "    .someClass<caret> {}\n" +
                                         "</style>")
    val usages = myFixture.findUsages(myFixture.elementAtCaret)
    assertEquals(2, usages.size)
  }

  fun testComponentExportDefault() {
    myFixture.addFileToProject("HelloWorld.vue", """
      <script>
        const HelloWorld = { name: 'HelloWorld' };
        export default HelloWorld;
      </script>""")
    myFixture.configureByText("App.vue", """
      <template>
        <HelloWorld<caret> msg="foo"></HelloWorld>
      </template>
      <script>
        import HelloWorld from './HelloWorld.vue';
        export default app;
        const app = { name: 'app', components: { HelloWorld } };
      </script>""")
    myFixture.checkGotoDeclaration("<Hello<caret>World", 43,
                                   "HelloWorld.vue")
  }

  fun testComponentModelProperty() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    val file = myFixture.configureByText("a-component.vue", """
      <script>
        export default {
          model: {
            event: "foo"
          }
        }
      </script>
    """)
    val component = VueModelManager.findEnclosingContainer(file) as VueRegularComponent
    assertEquals(null, component.model?.prop)
    assertEquals("foo", component.model?.event)
  }

  fun testResolveToUnresolvedComponent() {
    myFixture.configureByText("a.vue", """
       <script>
        import Foo from "foo.vue"
        export default {
          components: { Foo }
        }
      </script>
      <template><Foo></Foo></template>
    """.trimIndent())
    myFixture.resolveToWebSymbolSource("<F<caret>oo>")
      .parent.text
      .let { TestCase.assertEquals("{ Foo }", it) }
  }

  fun testAtComponentResolution() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    val file = myFixture.configureByFile("at_component.vue")
    val component = VueModelManager.findEnclosingContainer(file) as VueRegularComponent

    val getNames = { list: Collection<VueNamedSymbol> -> list.map { it.name }.sorted() }

    assertSameElements(getNames(component.props), "bar", "foo_prop", "name", "checked")
    assertSameElements(getNames(component.data), "foo", "foo_prop", "foo_data")
    assertSameElements(getNames(component.computed), "computedBar", "computedSetter", "syncedName")
    assertSameElements(getNames(component.methods), "addToCount", "getBar", "resetCount")
    assertSameElements(getNames(component.emits), "add-to-count", "reset", "update:name")
    assertEquals("checked", component.model?.prop)
    assertEquals("change", component.model?.event)
  }

  fun testAtComponentResolutionTs() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    val file = myFixture.configureByFile("at_component_ts.vue")
    val component = VueModelManager.findEnclosingContainer(file) as VueRegularComponent

    val getNames = { list: Collection<VueNamedSymbol> -> list.map { it.name }.sorted() }

    assertSameElements(getNames(component.props), "bar", "foo_prop", "name", "checked")
    assertSameElements(getNames(component.data), "foo", "foo_prop", "foo_data")
    assertSameElements(getNames(component.computed), "computedBar", "computedSetter", "syncedName")
    assertSameElements(getNames(component.methods), "addToCount", "getBar", "resetCount")
    assertSameElements(getNames(component.emits), "add-to-count", "reset", "update:name")
    assertEquals("checked", component.model?.prop)
    assertEquals("change", component.model?.event)
  }

  fun testWebTypesSource() {
    myFixture.copyDirectoryToProject("web-types-source", ".")
    myFixture.configureFromTempProjectFile("src/App.vue")

    mapOf(
      Pair("<relative<caret>-module-ref-local>", "export class RelativeModuleRefLocal {\n\n}"),
      Pair("<file<caret>-offset-local>", "export class FileOffsetLocal {\n\n}"),
      Pair("<relative<caret>-module-default-local>", "export default class {\n  def: string\n}"),
      Pair("<absolute<caret>-module-ref>", "export class AbsoluteModuleRef {\n\n}"),
      Pair("<implied<caret>-module-ref>", "export class ImpliedModuleRef {\n\n}"),
      Pair("<file<caret>-offset>", "export class FileOffset {\n\n}"),
      Pair("<relative-module<caret>-default>", "export default class {\n  def: string\n}")
    )
      .forEach { testCase ->
        TestCase.assertEquals(
          testCase.value,
          myFixture.resolveToWebSymbolSource(testCase.key)
            .let {
              (it as? JSImplicitElement)?.context ?: it
            }.text)
      }
  }

  fun testVueDefaultSymbols() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByFile("vueDefaultSymbols.vue")
    assertEquals("vue.d.ts",
                 myFixture.resolveReference("\$<caret>slots").containingFile.name)
    assertEquals("vue.d.ts",
                 myFixture.resolveReference("\$<caret>emit()").containingFile.name)
  }

  fun testResolveVueLoaderStyleReference() {
    myFixture.copyDirectoryToProject("resolve-vue-loader-url", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    TestCase.assertEquals("vue-multiselect.min.css",
                          myFixture.resolveReference("vue-multiselect.<caret>min.css")
                            .containingFile.name)
  }

  fun testSlotName() {
    createPackageJsonWithVueDependency(myFixture, "\"some_lib\":\"0.0.0\"")
    myFixture.copyDirectoryToProject("../completion/slotNames", ".")
    myFixture.copyFileToProject("slotNames/test2.vue", "test2.vue")
    myFixture.configureFromTempProjectFile("test2.vue")

    for ((tag, slotName, slotDeclText) in listOf(
      Triple("script-template-vue", "scriptTemplateVue1", "<slot name=\"scriptTemplateVue1\"></slot>"),
      Triple("require-decorators", "default", "<slot></slot>"),
      Triple("x-template", "xTemplate1", "<slot name=\"xTemplate1\"></slot>"),
      Triple("export-import", "exportImport1", "<slot name=\"exportImport1\"></slot>"),
      Triple("no-script-section", "noScriptSection1", "<slot name=\"noScriptSection1\"></slot>")
    )) {
      val slotWithCaret = slotName.replaceRange(1, 1, "<caret>")
      for (signature in listOf("<$tag><template v-slot:$slotWithCaret",
                               "<$tag><div slot=\"$slotWithCaret\"")) {
        val element = myFixture.resolveToWebSymbolSource(signature)
        assertEquals(signature, slotDeclText, element.text)
      }
    }
  }

  fun testFilters() {
    createPackageJsonWithVueDependency(myFixture, "\"some_lib\":\"0.0.0\"")
    myFixture.copyDirectoryToProject("filters/", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    for ((filterName, resolvedItemText) in listOf(
      Pair("localFilter", "localFilter: function (arg1, arg2, arg3) { return true }"),
      Pair("globalFilter", "function (value) { return 12 }"),
      Pair("globalReferencedFilter", "filterDefinition = function (value) { return 42 }"),
      Pair("globalQualifiedReferencedFilter", """Vue.filter("globalQualifiedReferencedFilter", danger.filterDefinition)"""),
      Pair("appFilter", """appFilter: function (value, param) { return "" }"""),
    )) {
      val element = myFixture.resolveReference("<caret>${filterName}")
      val text = if (element is JSImplicitElement) element.parent.text else element.text
      TestCase.assertEquals(filterName, resolvedItemText, text)
    }
    myFixture.assertUnresolvedReference("<caret>wrongFilter")
  }

  fun testImportedProps() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.copyDirectoryToProject("props-import-resolve", ".")
    myFixture.configureFromTempProjectFile("main.vue")
    val element = myFixture.resolveReference("\"user<caret>Id\"")
    assertEquals("props.js", element.containingFile.name)
    myFixture.assertUnresolvedReference("\"user<caret>Id2\"")
  }

  fun testMixinExtend() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    myFixture.copyDirectoryToProject("vue-sfc-extend-mixin", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    TestCase.assertEquals(
      "test.vue",
      myFixture.resolveReference("\"sty<caret>le\"").containingFile.name)
    myFixture.moveToOffsetBySignature("\"<caret>classes\"")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "!classes% (mixin.ts)#null#101", "!style% (test.vue)#null#101")
    TestCase.assertEquals(
      "mixin.ts",
      myFixture.resolveReference("{{ class<caret>es }}").containingFile.name)
    TestCase.assertEquals(
      "mixin.ts",
      myFixture.resolveReference("\"class<caret>es\"").containingFile.name)
  }

  fun testTypedMixins() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    myFixture.copyDirectoryToProject("vue-sfc-typed-mixins", ".")
    myFixture.configureFromTempProjectFile("component.vue")
    TestCase.assertEquals(
      "mixin.ts",
      myFixture.resolveReference("\"show<caret>1\"").containingFile.name)
    myFixture.moveToOffsetBySignature("\"show<caret>1\"")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "!show2%() (methods, component.vue)#void#101", "!show1%() (test.methods, mixin.ts)#void#101",
                           "!show5%() (methods, mixin2.ts)#void#101")
    myFixture.moveToOffsetBySignature("this.<caret>show2()")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "!show2%() (methods, component.vue)#void#99", "!show1%() (test.methods, mixin.ts)#void#99",
                           "!show5%() (methods, mixin2.ts)#void#99")

  }

  fun testGotoDeclarationDirectives() {
    myFixture.configureByFile("gotoDeclarationDirectives.vue")
    myFixture.performEditorAction("GotoDeclaration")
    TestCase.assertEquals(104, myFixture.caretOffset)
  }

  fun testGotoDeclarationTS() {
    myFixture.configureByFile("gotoDeclarationTS.vue")
    for (check in listOf("base", "watch", "computed", "methods")) {
      myFixture.checkGotoDeclaration("fetch<caret>Tracks/*$check*/()", 554)
    }
  }

  fun testNoScriptSection() {
    myFixture.copyDirectoryToProject("noScriptSection", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkGotoDeclaration("<no-script<caret>-section>", 0, "noScriptSection.vue")
  }

  fun testLazyLoaded() {
    myFixture.configureByFiles("lazyLoaded/main.vue", "lazyLoaded/index.vue")
    myFixture.checkGotoDeclaration("<Hello<caret>World", 24, "index.vue")
  }

  fun testScriptSetupTagNavigation() {
    myFixture.copyDirectoryToProject("scriptSetupTagNavigation", ".")
    myFixture.configureFromTempProjectFile("HelloWorld.vue")
    myFixture.checkGotoDeclaration("<Sam<caret>ple/>", 0, "Sample.vue")
  }

  fun testScriptSetupRef() {
    myFixture.configureByFiles("scriptSetupRef.vue")
    for (check in listOf(
      Pair("ref='f<caret>oo2'", 167),
      Pair("\$refs.fo<caret>o2 ", 167),
      Pair("\$refs.fo<caret>o ", 48))) {

      myFixture.checkGotoDeclaration(check.first, check.second)
    }
  }

  fun testCreateApp() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    sequenceOf(
      Triple("<B<caret>oo>", null, null),
      Triple("<B<caret>ar>", 128, "foo.vue"),
      Triple("<C<caret>ar>", 0, "TheComponent.vue"),
      Triple("v-f<caret>oo", 175, "main.ts"),
      Triple("<NonCha<caret>in>", 379, "main.ts"),
      Triple("<B<caret>oo>", null, null),
      Triple("w<B<caret>ar>", null, null),
      Triple("w<C<caret>ar>", null, null),
      Triple("w<div v-f<caret>oo", null, null),
      Triple("w<NonCha<caret>in>", null, null),
    ).forEach { (signature, offset, expectedFileName) ->
      myFixture.configureFromTempProjectFile("index.html")
      if (offset == null) {
        assertEmpty("Expected empty for $signature", myFixture.multiResolveWebSymbolReference(signature))
      }
      else {
        myFixture.checkGotoDeclaration(signature, offset, expectedFileName)
      }
    }
  }

  fun testGlobalComponentCompositionApiFromUnlinkedTemplate() { // WEB-55665
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByText("AppUnlinked.vue", "<template>\n<Bar/>\n</template>")
    myFixture.checkGotoDeclaration("<B<caret>ar/>", 128, "foo.vue")
  }

  fun testSameMixinsViaStubsAndViaPsi() {
    val dir = myFixture.copyDirectoryToProject("../sameMixinsViaStubsAndViaPsi", ".")
    val mixinJsFile = dir.findChild("mixin.spec.js")
    val mixinJsPsiFile = mixinJsFile?.let { PsiManager.getInstance(project).findFile(mixinJsFile) }
    if (mixinJsPsiFile == null) {
      fail("broken test data")
      return
    }

    TestCase.assertNull((mixinJsPsiFile as PsiFileImpl).treeElement)
    val mixinsViaStubs = VueModelManager.getGlobal(mixinJsPsiFile).mixins
    TestCase.assertNull(mixinJsPsiFile.treeElement)
    PsiManager.getInstance(project).dropPsiCaches()

    myFixture.openFileInEditor(mixinJsFile)
    TestCase.assertNotNull(mixinJsPsiFile.calcTreeElement())
    TestCase.assertNotNull(mixinJsPsiFile.treeElement)
    val mixinsViaPsi = VueModelManager.getGlobal(mixinJsPsiFile).mixins

    assertSameElements(mixinsViaStubs, mixinsViaPsi)
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
