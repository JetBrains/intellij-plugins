// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.Trinity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.testFramework.*
import com.intellij.polySymbols.utils.asSingleSymbol
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.VueJSSpecificHandlersFactory
import org.jetbrains.vuejs.lang.VueTestModule.VUE_2_6_10
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueSymbol
import org.jetbrains.vuejs.web.scopes.VueBindingShorthandSymbol
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VueResolveTest :
  BasePlatformTestCase() {

  override fun getTestDataPath(): String =
    getVueTestDataPath() + "/resolve/"

  @Test
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
    assertNotNull(reference)
    assertTrue(reference is JSReferenceExpression)
    val resolver = VueJSSpecificHandlersFactory().createReferenceExpressionResolver(
      reference as JSReferenceExpressionImpl, true)
    val results = resolver.resolve(reference, false)
    assertEquals(1, results.size)
    assertTrue(results[0].element!!.parent!! is JSProperty)
    assertEquals("message25620", (results[0].element!!.parent!! as JSProperty).name)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertInstanceOf(literal!!.parent, JSArrayLiteralExpression::class.java)
    assertEquals("'message25620Arr'", literal.text)
  }

  @Test
  fun testResolveAttributeInPascalCaseUsageInPropsArray() {
    myFixture.configureByText("ResolveAttributeInPascalCaseUsageInPropsArray.vue", """
<template>
  <list25620 <caret>pascalCase">
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertInstanceOf(literal!!.parent, JSArrayLiteralExpression::class.java)
    assertEquals("'pascalCase'", literal.text)
  }

  @Test
  fun testResolveIntoComputedProperty() {
    myFixture.configureByText("ResolveIntoComputedProperty.vue", """
<template>
{{<caret>testRight}}
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
    val property = myFixture.polySymbolSourceAtCaret()
    assertTrue((property as JSImplicitElement).context is JSFunctionItem)
    assertEquals("testRight", (property.context as JSFunctionItem).name)
  }

  @Test
  fun testResolveIntoComputedES6FunctionProperty() {
    myFixture.configureByText("ResolveIntoComputedES6FunctionProperty.vue", """
<template>
{{<caret>testRight}}
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
    assertNotNull(reference)
    val property = reference!!.resolve()
    assertTrue((property as JSImplicitElement).context is JSFunctionItem)
    assertEquals("testRight", (property.context as JSFunctionItem).name)
  }

  @Test
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
    assertNotNull(reference)
    val property = reference!!.resolve()?.let { if (it is JSImplicitElement) it.context else it }
    assertTrue(property is JSProperty)
    assertEquals("me215thod", (property as JSProperty).name)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertTrue(literal is JSLiteralExpression)
    assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    assertEquals("'parentMsg'", literal.text)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertTrue(literal is JSLiteralExpression)
    assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    assertEquals("'parentMsg'", literal.text)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertTrue(literal is JSLiteralExpression)
    assertTrue((literal as JSLiteralExpression).isQuotedLiteral)
    assertEquals("'parentMsg'", literal.text)
  }

  @Test
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
    assertNotNull(reference)
    val target = reference!!.resolve()
    if (expectedPropertyName == null) {
      assertNull(target)
    }
    else {
      val property = if (target is JSImplicitElement) target.parent else target
      assertTrue(property is JSProperty)
      assertEquals(expectedPropertyName, (property as JSProperty).name)
    }
  }

  @Test
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
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent.parent is VueJSVForExpression)
  }

  @Test
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
    assertNotNull(reference)
    val part = reference!!.resolve()
    assertNotNull(part)
    assertTrue(part is JSProperty)
    assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  @Test
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
    assertNotNull(reference)
    val part = reference!!.resolve()?.let { if (it is JSImplicitElement) it.context else it }
    assertNotNull(part)
    assertTrue(part is JSProperty)
    assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  @Test
  fun testIntoVForVarInPug() {
    myFixture.configureByText("IntoVForVarInPug.vue", """
<template lang="pug">
  ul
    li(v-for="item in items") {{ <caret>item.message }}
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent.parent is VueJSVForExpression)
  }

  @Test
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
    assertNotNull(reference)
    val part = reference!!.resolve()
    assertNotNull(part)
    assertTrue(part is JSProperty)
    assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  @Test
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
    assertNotNull(reference)
    val part = reference!!.resolve()?.let { if (it is JSImplicitElement) it.context else it }
    assertNotNull(part)
    assertTrue(part is JSProperty)
    assertTrue(part!!.parent is JSObjectLiteralExpression)
  }

  @Test
  fun testIntoVForVarInHtml() {
    myFixture.configureVueDependencies()
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
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent.parent is VueJSVForExpression)
  }

  @Test
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
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent is JSVarStatement)
    assertTrue(variable.parent.parent is JSParenthesizedExpression)
    assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  @Test
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
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent is JSVarStatement)
    assertTrue(variable.parent.parent is JSParenthesizedExpression)
    assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  @Test
  fun testKeyIntoForResolveHtml() {
    myFixture.configureVueDependencies()
    myFixture.configureByText("KeyIntoForResolveHtml.html", """
<html>
  <li id="id123" v-for="(item1, index1) in items1" :key="<caret>item1" v-if="item1 > 0">
    {{ parentMessage1 }} - {{ index1 }} - {{ item1.message1 }}
  </li>
</html>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent is JSVarStatement)
    assertTrue(variable.parent.parent is JSParenthesizedExpression)
    assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  @Test
  fun testResolveByMountedVueInstanceInData() {
    myFixture.configureVueDependencies()
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
    assertNotNull(reference)
    val property = reference!!.resolve()?.let { if (it is JSImplicitElement) it.context else it }
    assertNotNull(property)
    assertTrue(property is JSProperty)
    assertTrue(property!!.parent.parent is JSProperty)
    assertEquals("data", (property.parent.parent as JSProperty).name)
  }

  @Test
  fun testResolveByMountedVueInstanceInProps() {
    myFixture.configureVueDependencies()
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertNotNull(literal)
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertInstanceOf(literal!!.parent.parent, JSProperty::class.java)
    assertEquals("props", (literal.parent.parent as JSProperty).name)
  }

  @Test
  fun testResolveVForIterableByMountedVueInstance() {
    myFixture.configureVueDependencies()
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
    assertNotNull(reference)
    val property = reference!!.resolve()?.let { if (it is JSImplicitElement) it.context else it }
    assertNotNull(property)
    assertTrue(property is JSProperty)
    assertTrue(property!!.parent.parent is JSProperty)
    assertEquals("data", (property.parent.parent as JSProperty).name)
  }

  @Test
  fun testKeyIntoForResolvePug() {
    myFixture.configureByText("KeyIntoForResolvePug.vue", """
<template lang="pug">
  ul
    li(id="id123" v-for="(item123, index1) in items1", :key="<caret>item123") {{ parentMessage1 }}
</template>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    assertNotNull(reference)
    val variable = reference!!.resolve()
    assertNotNull(variable)
    assertTrue(variable!!.parent is JSVarStatement)
    assertTrue(variable.parent.parent is JSParenthesizedExpression)
    assertTrue(variable.parent.parent.parent is VueJSVForExpression)
  }

  @Test
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

    val literal = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertEquals("'libComponentProp'", literal!!.text)
    assertInstanceOf(literal.parent, JSArrayLiteralExpression::class.java)
    assertEquals("props", (literal.parent.parent as JSProperty).name)
  }

  @Test
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

    val property = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(property, JSProperty::class.java)
    assertEquals("kuku", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  @Test
  fun testResolveVueRouterComponents() {
    myFixture.configureByFile("vue-router.js")
    myFixture.configureByText("ResolveVueRouterComponents.vue", """
      <template>
        <router-link <caret>to="/post"></router-link>
      </template>
    """.trimIndent())

    val property = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(property, JSProperty::class.java)
    assertEquals("to", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  @Test
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

    val property = myFixture.polySymbolSourceAtCaret()
    assertNotNull(property)
    assertTrue(property is JSProperty)
    assertEquals("from", (property as JSProperty).name)
    assertTrue(property.parent.parent is JSProperty)
    assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  @Test
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
    val target = myFixture.polySymbolSourceAtCaret()
    assertNotNull(target)
    assertEquals(fileName, target!!.containingFile.name)
    assertInstanceOf(target.parent, JSProperty::class.java)
    assertEquals(compName, (target as JSLiteralExpression).value)
  }

  @Test
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
    val property = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(property, JSProperty::class.java)
    assertEquals("insideGlobalCompLiteral", (property as JSProperty).name)
    assertTrue(property.parent.parent is JSProperty)
    assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertEquals("oneTwo", (literal as JSLiteralExpression).stringValue)
    assertTrue(literal.parent is JSArrayLiteralExpression)
  }

  @Test
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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertNotNull(literal)
    assertTrue(literal is JSLiteralExpression)
    assertEquals("oneTwo", (literal as JSLiteralExpression).stringValue)
    assertTrue(literal.parent is JSArrayLiteralExpression)
  }


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
    val literal = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(literal, JSLiteralExpression::class.java)
    assertEquals("seeMe", (literal as JSLiteralExpression).stringValue)
    assertEquals("compUI.vue", literal.containingFile.name)
    assertInstanceOf(literal.parent, JSArrayLiteralExpression::class.java)
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
    val property = myFixture.polySymbolSourceAtCaret()
    assertNotNull(property)
    assertTrue(property is JSProperty)
    assertEquals("seeMe", (property as JSProperty).name)
    assertEquals("compUI.vue", property.containingFile.name)
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
    val property = myFixture.polySymbolSourceAtCaret()
    assertNotNull(property)
    assertTrue(property is JSProperty)
    assertTrue(property!!.parent.parent is JSProperty)
    assertEquals("props", (property.parent.parent as JSProperty).name)
    assertEquals("seeMe", (property as JSProperty).name)
    assertEquals("compUI.vue", property.containingFile.name)
  }

  @Test
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

    val property = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(property, JSProperty::class.java)
    assertEquals("mixinProp", (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    assertEquals("props", (property.parent.parent as JSProperty).name)
    assertEquals("MixinWithProp.vue", property.containingFile.name)
  }


  @Test
  fun testTwoExternalMixins() {
    myFixture.configureByText("FirstMixin.vue", """
<script>
  export default {
    props: ['firstMixinProp']
  }
</script>
""")
    myFixture.configureByText("SecondMixin.vue", """
<script>
  export default {
    props: ['secondMixinProp']
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
      val literal = myFixture.polySymbolSourceAtCaret()
      assertInstanceOf(literal, JSLiteralExpression::class.java)
      assertEquals(propName, (literal as JSLiteralExpression).stringValue)
      assertInstanceOf(literal.parent.parent, JSProperty::class.java)
      assertEquals("props", (literal.parent.parent as JSProperty).name)
      assertEquals(file, literal.containingFile.name)
    }
    checkResolve("firstMixinProp", "FirstMixin.vue")

    val attribute = myFixture.findElementByText("second-mixin-prop", XmlAttribute::class.java)
    assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset)
    checkResolve("secondMixinProp", "SecondMixin.vue")
  }

  @Test
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

  @Test
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
    val property = myFixture.polySymbolSourceAtCaret()
    assertInstanceOf(property, JSProperty::class.java)
    assertEquals(name, (property as JSProperty).name)
    assertInstanceOf(property.parent.parent, JSProperty::class.java)
    assertEquals("props", (property.parent.parent as JSProperty).name)
  }

  @Test
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

  @Test
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

  @Test
  fun testTypeScriptResolve() {
    myFixture.configureByText("TypeScriptResolve.vue", """
<script lang="ts"><caret>encodeURI('a')</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    assertNotNull(reference)
    val function = reference!!.resolve()
    assertNotNull(function)
    assertTrue(function is TypeScriptFunction)
    assertEquals("lib.es5.d.ts", function!!.containingFile.name)
  }

  @Test
  fun testECMA5Resolve() {
    myFixture.configureByText("TypeScriptResolve.vue", """
<script><caret>encodeURI('a')</script>
""")
    val reference = myFixture.getReferenceAtCaretPosition()
    assertNotNull(reference)
    val function = reference!!.resolve()
    assertNotNull(function)
    assertTrue(function is TypeScriptFunction)
    assertEquals("lib.es5.d.ts", function!!.containingFile.name)
  }

  @Test
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

  @Test
  fun testResolveGlobalCustomDirective() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")
    val attribute = myFixture.findElementByText("v-focus", XmlAttribute::class.java)
    assertNotNull(attribute)
    myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)

    val callExpression = myFixture.polySymbolSourceAtCaret()
    assertNotNull(callExpression)
    // unstub for test
    assertNotNull(callExpression!!.text)
    assertEquals("focus", ((callExpression as JSCallExpression).arguments[0] as JSLiteralExpression).stringValue)
    assertEquals("CustomDirectives.js", callExpression.containingFile.name)
  }

  @Test
  fun testResolveLocalCustomDirective() {
    myFixture.copyDirectoryToProject("../common/customDirectives", ".")
    myFixture.configureFromTempProjectFile("CustomDirectives.vue")

    arrayOf(Trinity("v-local-directive", "localDirective", "CustomDirectives.vue"),
            Trinity("v-some-other-directive", "someOtherDirective", "CustomDirectives.vue"),
            Trinity("v-click-outside", "click-outside", "CustomDirectives.js"),
            Trinity("v-imported-directive", "importedDirective", "importedDirective.js"))
      .forEach {
        val attribute = myFixture.findElementByText(it.first, XmlAttribute::class.java)
        assertNotNull(attribute)
        myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)
        doTestResolveIntoDirective(it.second, it.third)
      }
  }

  @Test
  fun testResolveLocalCustomDirectiveLinkedFiles() {
    myFixture.copyDirectoryToProject("../common/customDirectivesLinkedFiles", ".")
    myFixture.configureVueDependencies()
    myFixture.configureFromTempProjectFile("CustomDirectives.html")

    arrayOf(Trinity("v-local-directive", "localDirective", "CustomDirectives.js"),
            Trinity("v-some-other-directive", "someOtherDirective", "CustomDirectives.js"),
            Trinity("v-click-outside", "click-outside", "GlobalCustomDirectives.js"),
            Trinity("v-imported-directive", "importedDirective", "importedDirective.js"))
      .forEach {
        val attribute = myFixture.findElementByText(it.first, XmlAttribute::class.java)
        assertNotNull(attribute)
        myFixture.editor.caretModel.moveToOffset(attribute.textOffset + 2)
        doTestResolveIntoDirective(it.second, it.third)
      }
  }

  private fun doTestResolveIntoDirective(directive: String, fileName: String) {
    val property = myFixture.polySymbolSourceAtCaret()
    assertNotNull(directive, property)
    when (property) {
      is JSProperty -> {
        assertEquals(directive, directive, property.name)
        assertEquals(directive, fileName, property.containingFile.name)
      }
      is JSCallExpression -> {
        assertNotNull(directive, property.text)
        assertEquals(directive, directive, (property.arguments[0] as JSLiteralExpression).stringValue)
        assertEquals(directive, fileName, property.containingFile.name)
      }
      is JSObjectLiteralExpression -> {
        assertNotNull(directive, property.text)
        assertEquals(directive, fileName, property.containingFile.name)
      }
      else -> assertTrue("$directive class: ${property?.javaClass?.name}", false)
    }
  }

  @Test
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
    assertNotNull(reference)
    val target = reference!!.resolve()
    assertNotNull(target)
    assertEquals("options.d.ts", target!!.containingFile.name)
    assertTrue(target is TypeScriptPropertySignature)
  }

  @Test
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

  @Test
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
        assertNotNull(reference)
        val target = reference!!.resolve()
        assertNotNull(target)
        assertEquals(it.third, target!!.containingFile.name)
        assertTrue(target.parent is JSCallExpression)
      }
      else {
        doResolveIntoLibraryComponent(it.second, it.third)
      }
    }
  }

  @Test
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

  @Test
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

  @Test
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

    val target = myFixture.polySymbolSourceAtCaret()
    assertNotNull(target)
    assertEquals("lib-comp-for-alias.es6", target!!.containingFile.name)
    assertTrue(target.parent is JSProperty)
  }

  @Test
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

  @Test
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
    val target = myFixture.polySymbolSourceAtCaret()
    assertNotNull(target)
    assertEquals("lib-spread.es6", target!!.containingFile.name)
    assertTrue(target.parent is JSProperty)
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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
    myFixture.checkGotoDeclaration("<Short<caret>Vue", "default class <caret>ShortComponent", "ShortComponent.vue")
  }

  @Test
  fun testResolveWithClassComponentTs() {
    myFixture.configureVueDependencies()
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
    val target = myFixture.resolveToPolySymbolSource("<<caret>LongComponent/>")
    assertEquals("ResolveWithClassComponentTs.vue", target.containingFile.name)
    assertInstanceOf(target, JSProperty::class.java)
    myFixture.checkGotoDeclaration("<<caret>LongComponent/>", "export default class <caret>LongComponent", "LongComponent.vue")
  }

  @Test
  fun testLocalComponentsExtendsResolve() {
    createLocalComponentsExtendsData(myFixture, false)
    myFixture.type("prop-from-a=\"\"")
    myFixture.editor.caretModel.moveToOffset(myFixture.editor.caretModel.offset - 5)
    doTestResolveIntoProperty("propFromA")
  }

  private fun doResolveIntoLibraryComponent(compName: String, fileName: String) {
    val target = myFixture.polySymbolSourceAtCaret()
    assertEquals(fileName, target!!.containingFile.name)
    assertTrue(target.parent is JSProperty)
    assertEquals(compName, StringUtil.unquoteString((target.parent as JSProperty).value!!.text))
  }

  @Test
  fun testResolveWithRecursiveMixins() {
    defineRecursiveMixedMixins(myFixture)
    myFixture.configureByText("ResolveWithRecursiveMixins.vue", """
        <template>
          <<caret>HiddenComponent/>
        </template>
      """)
    myFixture.checkGotoDeclaration("<Hidden<caret>Component/>", "export default <caret>{", "hidden-component.vue")
    myFixture.configureByText("ResolveWithRecursiveMixins2.vue", """
        <template>
          <<caret>OneMoreComponent/>
        </template>
      """)
    myFixture.checkGotoDeclaration("<One<caret>MoreComponent/>", "default class <caret>Kuku extends", "OneMoreComponent.vue")
  }

  @Test
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

  @Test
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
    myFixture.checkGotoDeclaration("<Hello<caret>World", "HelloWorld = <caret>{ name:",
                                   "HelloWorld.vue")
  }

  @Test
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
    val component = VueModelManager.findEnclosingContainer(file) as VueComponent
    assertEquals(null, component.model?.prop)
    assertEquals("foo", component.model?.event)
  }

  @Test
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
    myFixture.resolveToPolySymbolSource("<F<caret>oo>")
      .parent.text
      .let { assertEquals("{ Foo }", it) }
  }

  @Test
  fun testAtComponentResolution() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    val file = myFixture.configureByFile("at_component.vue")
    val component = VueModelManager.findEnclosingContainer(file) as VueComponent

    val getNames = { list: Collection<VueSymbol> -> list.map { it.name }.sorted() }

    assertSameElements(getNames(component.props), "bar", "foo_prop", "name", "checked")
    assertSameElements(getNames(component.data), "foo", "foo_prop", "foo_data")
    assertSameElements(getNames(component.computed), "computedBar", "computedSetter", "syncedName")
    assertSameElements(getNames(component.methods), "addToCount", "getBar", "resetCount")
    assertSameElements(getNames(component.emits), "add-to-count", "reset", "update:name")
    assertEquals("checked", component.model?.prop)
    assertEquals("change", component.model?.event)
  }

  @Test
  fun testAtComponentResolutionTs() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    val file = myFixture.configureByFile("at_component_ts.vue")
    val component = VueModelManager.findEnclosingContainer(file) as VueComponent

    val getNames = { list: Collection<VueSymbol> -> list.map { it.name }.sorted() }

    assertSameElements(getNames(component.props), "bar", "foo_prop", "name", "checked")
    assertSameElements(getNames(component.data), "foo", "foo_prop", "foo_data")
    assertSameElements(getNames(component.computed), "computedBar", "computedSetter", "syncedName")
    assertSameElements(getNames(component.methods), "addToCount", "getBar", "resetCount")
    assertSameElements(getNames(component.emits), "add-to-count", "reset", "update:name")
    assertEquals("checked", component.model?.prop)
    assertEquals("change", component.model?.event)
  }

  @Test
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
        assertEquals(
          testCase.value,
          myFixture.resolveToPolySymbolSource(testCase.key)
            .let {
              (it as? JSImplicitElement)?.context ?: it
            }.text)
      }
  }

  @Test
  fun testVueDefaultSymbols() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    myFixture.configureByFile("vueDefaultSymbols.vue")
    assertEquals("vue.d.ts",
                 myFixture.resolveReference("\$<caret>slots").containingFile.name)
    assertEquals("vue.d.ts",
                 myFixture.resolveReference("\$<caret>emit()").containingFile.name)
  }

  @Test
  fun testResolveVueLoaderStyleReference() {
    myFixture.copyDirectoryToProject("resolve-vue-loader-url", ".")
    myFixture.configureFromTempProjectFile("App.vue")
    assertEquals("vue-multiselect.min.css",
                 myFixture.resolveReference("vue-multiselect.<caret>min.css")
                   .containingFile.name)
  }

  @Test
  fun testSlotName() {
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
        val element = myFixture.resolveToPolySymbolSource(signature)
        assertEquals(signature, slotDeclText, element.text)
      }
    }
  }

  @Test
  fun testFilters() {
    myFixture.configureVueDependencies("some_lib" to "0.0.0")
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
      assertEquals(filterName, resolvedItemText, text)
    }
    myFixture.assertUnresolvedReference("<caret>wrongFilter")
  }

  @Test
  fun testImportedProps() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("props-import-resolve", ".")
    myFixture.configureFromTempProjectFile("main.vue")
    val element = myFixture.resolveReference("\"user<caret>Id\"")
    assertEquals("props.js", element.containingFile.name)
    myFixture.assertUnresolvedReference("\"user<caret>Id2\"")
  }

  @Test
  fun testMixinExtend() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    myFixture.copyDirectoryToProject("vue-sfc-extend-mixin", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    assertEquals(
      "test.vue",
      myFixture.resolveReference("\"sty<caret>le\"").containingFile.name)
    myFixture.moveToOffsetBySignature("\"<caret>classes\"")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "classes (tailText=' (mixin.ts)'; typeText=null; priority=101.0; bold)",
                           "style (tailText=' (test.vue)'; typeText=null; priority=101.0; bold)"
    )
    assertEquals(
      "mixin.ts",
      myFixture.resolveReference("{{ class<caret>es }}").containingFile.name)
    assertEquals(
      "mixin.ts",
      myFixture.resolveReference("\"class<caret>es\"").containingFile.name)
  }

  @Test
  fun testTypedMixins() {
    myFixture.configureVueDependencies(VUE_2_6_10)
    myFixture.copyDirectoryToProject("vue-sfc-typed-mixins", ".")
    myFixture.configureFromTempProjectFile("component.vue")
    assertEquals(
      "mixin.ts",
      myFixture.resolveReference("\"show<caret>1\"").containingFile.name)
    myFixture.moveToOffsetBySignature("\"show<caret>1\"")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "show1 (tailText='() (mixin.ts)'; typeText='void'; priority=101.0; bold)",
                           "show2 (tailText='() (component.vue)'; typeText='void'; priority=101.0; bold)",
                           "show5 (tailText='() (mixin2.ts)'; typeText='void'; priority=101.0; bold)")
    myFixture.moveToOffsetBySignature("this.<caret>show2()")
    myFixture.completeBasic()
    assertContainsElements(myFixture.renderLookupItems(true, true, true),
                           "show1 (tailText='() (mixin.ts)'; typeText='void'; priority=99.0; bold)",
                           "show2 (tailText='() (component.vue)'; typeText='void'; priority=99.0; bold)",
                           "show5 (tailText='() (mixin2.ts)'; typeText='void'; priority=99.0; bold)")

  }

  @Test
  fun testGotoDeclarationDirectives() {
    myFixture.configureByFile("gotoDeclarationDirectives.vue")
    myFixture.performEditorAction("GotoDeclaration")
    assertEquals(104, myFixture.caretOffset)
  }

  @Test
  fun testGotoDeclarationTS() {
    myFixture.configureByFile("gotoDeclarationTS.vue")
    for (check in listOf("base", "watch", "computed", "methods")) {
      myFixture.checkGotoDeclaration("fetch<caret>Tracks/*$check*/()", "async <caret>fetchTracks()")
    }
  }

  @Test
  fun testNoScriptSection() {
    myFixture.copyDirectoryToProject("noScriptSection", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkGotoDeclaration("<no-script<caret>-section>", "<caret><template>", "noScriptSection.vue")
  }

  @Test
  fun testLazyLoaded() {
    myFixture.configureByFiles("lazyLoaded/main.vue", "lazyLoaded/index.vue")
    myFixture.checkGotoDeclaration("<Hello<caret>World", "export default <caret>{", "index.vue")
  }

  @Test
  fun testScriptSetupTagNavigation() {
    myFixture.copyDirectoryToProject("scriptSetupTagNavigation", ".")
    myFixture.configureFromTempProjectFile("HelloWorld.vue")
    myFixture.checkGotoDeclaration("<Sam<caret>ple/>", "<caret><template>", "Sample.vue")
  }

  @Test
  fun testScriptSetupRef() {
    myFixture.configureByFiles("scriptSetupRef.vue")
    for (check in listOf(
      Pair("ref='f<caret>oo2'", "const <caret>foo2 = ref("),
      Pair("\$refs.fo<caret>o2 ", "const <caret>foo2 = ref("),
      Pair("\$refs.fo<caret>o ", "<div ref='<caret>foo'>"))) {

      myFixture.checkGotoDeclaration(check.first, check.second)
    }
  }

  @Test
  fun testScriptSetupPropShadowing() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFiles("scriptSetupPropShadowing.vue")
    myFixture.checkGotoDeclaration("{{<caret>foo}}", "const <caret>foo = 2")
  }

  @Test
  fun testCreateApp() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    sequenceOf(
      Triple("<B<caret>oo>", null, null),
      Triple("<B<caret>ar>", "export default <caret>{", "foo.vue"),
      Triple("<C<caret>ar>", "<caret><template>", "TheComponent.vue"),
      Triple("v-f<caret>oo", ".directive(<caret>\"foo\"", "main.ts"),
      Triple("<NonCha<caret>in>", "(\"NonChain\", <caret>{})", "main.ts"),
      Triple("<B<caret>oo>", null, null),
      Triple("w<B<caret>ar>", null, null),
      Triple("w<C<caret>ar>", null, null),
      Triple("w<div v-f<caret>oo", null, null),
      Triple("w<NonCha<caret>in>", null, null),
    ).forEach { (signature, offset, expectedFileName) ->
      myFixture.configureFromTempProjectFile("index.html")
      if (offset == null) {
        assertEmpty("Expected empty for $signature", myFixture.multiResolvePolySymbolReference(signature))
      }
      else {
        myFixture.checkGotoDeclaration(signature, offset, expectedFileName)
      }
    }
  }

  @Test
  fun testGlobalComponentCompositionApiFromUnlinkedTemplate() { // WEB-55665
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByText("AppUnlinked.vue", "<template>\n<Bar/>\n</template>")
    myFixture.checkGotoDeclaration("<B<caret>ar/>", "export default <caret>{", "foo.vue")
  }

  @Test
  fun testSameMixinsViaStubsAndViaPsi() {
    val dir = myFixture.copyDirectoryToProject("../sameMixinsViaStubsAndViaPsi", ".")
    val mixinJsFile = dir.findChild("mixin.spec.js")
    val mixinJsPsiFile = mixinJsFile?.let { PsiManager.getInstance(project).findFile(mixinJsFile) }
    if (mixinJsPsiFile == null) {
      fail("broken test data")
      return
    }

    assertNull((mixinJsPsiFile as PsiFileImpl).treeElement)
    val mixinsViaStubs = VueModelManager.getGlobal(mixinJsPsiFile).mixins
    assertNull(mixinJsPsiFile.treeElement)
    PsiManager.getInstance(project).dropPsiCaches()

    myFixture.openFileInEditor(mixinJsFile)
    assertNotNull(mixinJsPsiFile.calcTreeElement())
    assertNotNull(mixinJsPsiFile.treeElement)
    val mixinsViaPsi = VueModelManager.getGlobal(mixinJsPsiFile).mixins

    assertSameElements(mixinsViaStubs, mixinsViaPsi)
  }

  @Test
  fun testMixinQualifiedReference() {
    myFixture.copyDirectoryToProject("mixinQualifiedReference", ".")
    myFixture.configureFromTempProjectFile("Test.vue")
    assertEquals("clickMixin.js",
                 myFixture.resolveReference("cl<caret>icked(").containingFile.name)
  }

  @Test
  fun testScriptSetupCustomEmitInObjectLiteral() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("${getTestName(true)}.vue")
    sequenceOf(
      "@<caret>add" to "<caret>add,\n",
      "@ch<caret>ange" to "<caret>change(ctx) {\n",
      "v-on:re<caret>move" to "<caret>remove: (ctx)"
    ).forEach { (signature, offset) ->
      myFixture.checkGotoDeclaration(signature, offset)
    }
  }

  @Test
  fun testPropsConstructorsAndGenerics() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile("${getTestName(true)}.vue")
    sequenceOf(
      "m<caret>sg=\"You did it!\"" to "<caret>msg: {type:",
      "auto<caret>focus :value" to "<caret>autofocus: Boolean",
      "autofocus :va<caret>lue" to "<caret>value: {} as",
    ).forEach { (signature, offset) ->
      myFixture.checkGotoDeclaration(signature, offset)
    }
  }

  @Test
  fun testInjectLiteralLocal() {
    myFixture.configureByText("InjectLiteral.vue", """
      <script>
      export default {
        inject: ['message'],
      }
      </script>
      
      <template>
        {{message}}
      </template>
    """.trimIndent())
    myFixture.checkGotoDeclaration("{{me<caret>ssage}}", "inject: [<caret>'message']")
  }

  @Test
  fun testInjectPropertyLocal() {
    myFixture.configureByText("InjectProperty.vue", """
      <script>
      export default {
        inject: {
          message: {
          }
        }
      }
      </script>
      
      <template>
        {{message}}
      </template>
    """.trimIndent())
    myFixture.checkGotoDeclaration("{{mes<caret>sage}}", "<caret>message: {")
  }

  @Test
  fun testInjectAliasedLocal() {
    myFixture.configureByText("InjectAliased.vue", """
      <script>
      export default {
        inject: {
          localMessage: {
            from: 'message'
          }
        }
      }
      </script>
      
      <template>
        {{localMessage}}
      </template>
    """.trimIndent())
    myFixture.checkGotoDeclaration("{{loc<caret>alMessage}}", "<caret>localMessage: {")
  }

  @Test
  fun testInjectLiteral() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'me<caret>ssage'", "<caret>message: 'hello", "Provide.vue")
  }

  @Test
  fun testInjectLiteralProvidedInApp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'me<caret>ssage'", "<caret>message: 'hello", "App.vue")
  }

  @Test
  fun testInjectAlias() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'me<caret>ssage'", "<caret>message: 'msg'", "Provide.vue")
  }

  @Test
  fun testInjectAliasDuplicatedName() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'me<caret>ssage'", "<caret>message: 'msg'", "Provide.vue")
  }

  @Test
  fun testInjectProperty() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("m<caret>essage", "<caret>message: 'msg'", "Provide.vue")
  }

  @Test
  fun testInjectDeepNested() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'provide<caret>Deep'", "<caret>provideDeep: 12", "ProvideB.vue")
  }

  @Test
  fun testInjectScriptSetup() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.checkHighlighting()
    myFixture.checkGotoDeclaration("'provided<caret>InCall'", "provide(<caret>'providedInCall", "Provide.vue")
  }

  @Test
  fun testInjectAppGlobal() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'global<caret>Provide'", "app.provide(<caret>'globalProvide'", "main.js")
  }

  @Test
  fun testInjectSetup() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("'inject<caret>Setup'", "provide(<caret>'injectSetup'", "Provide.vue")
  }

  @Test
  fun testImportFromContextScriptScope() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("items: IT<caret>est[]", "export interface <caret>ITest", "Button.vue")
  }

  @Test
  fun testDefineSlots() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByText("DefineSlots.vue", """
      <script setup lang="ts">
      defineSlots<{
        default: (props: { msg: string }) => any
        header?: (props: { pageTitle?: string }) => any
        footer: (props: { year?: number, dayOfWeek: number }) => any
      }>()
      
      const dynamicSlotPart = "ault";
      </script>
      
      <template>
        <div class="container">
          <header>
            <slot name="header" pageTitle="Hello!"></slot>
            <slot :name="'default'" msg="hello"></slot>
            <slot name="footer" day-of-week="2"></slot>
            <slot :name="`def${"$"}{dynamicSlotPart}`" msg="template"></slot>
          </header>
        </div>
      </template>
    """.trimIndent())
    myFixture.checkGotoDeclaration("name=\"hea<caret>der\"",
                                   "<caret>header?: (props: { pageTitle?: string }) => any")
    myFixture.checkGotoDeclaration("pageT<caret>itle=\"Hello!\"",
                                   "<caret>pageTitle?: string }")
    myFixture.checkGotoDeclaration("day-o<caret>f-week=\"2\"",
                                   "<caret>dayOfWeek: number }")
    myFixture.checkGotoDeclaration("m<caret>sg=\"hello\"",
                                   "<caret>msg: string }")
    myFixture.checkGotoDeclaration("ms<caret>g=\"template\"",
                                   "<caret>msg: string }")
  }

  @Test
  fun testDefineSlotDefault() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject("defineSlotDefault", "")
    myFixture.configureByText("Component.vue", """
      <script setup lang='ts'>
      import TextField from './TextField.vue'
      </script>

      <template>
        <TextField v-slot='{ field }'>
          <span v-bind='field.class'></span>
        </TextField>
      </template>
    """.trimIndent())

    assertEquals(
      "default?: (props: { field: FieldSlotPropText }) => any",
      myFixture.multiResolvePolySymbolReference("v-sl<caret>ot='{ field }'").asSingleSymbol()
        ?.asSafely<PsiSourcedPolySymbol>()?.source?.text
    )
  }

  @Test
  fun testDefineSlotDefaultTemplate() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject("defineSlotDefault", "")
    myFixture.configureByText("Component.vue", """
      <script setup lang='ts'>
      import TextField from './TextField.vue'
      </script>

      <template>
        <TextField>
          <template v-slot='{ field }'>
            {{ field.class }}
          </template>
        </TextField>
      </template>
    """.trimIndent())

    assertEquals(
      "default?: (props: { field: FieldSlotPropText }) => any",
      myFixture.multiResolvePolySymbolReference("v-sl<caret>ot='{ field }'").asSingleSymbol()
        ?.asSafely<PsiSourcedPolySymbol>()?.source?.text
    )
  }

  @Test
  fun testDefineSlotDefaultNamed() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.copyDirectoryToProject("defineSlotDefault", "")
    myFixture.configureByText("Component.vue", """
      <script setup lang='ts'>
      import TextField from './TextField.vue'
      </script>

      <template>
        <TextField>
          <template v-slot:default='{ field }'>
            {{ field.style }}
          </template>
        </TextField>
      </template>
    """.trimIndent())

    assertEquals(
      "default?: (props: { field: FieldSlotPropText }) => any",
      myFixture.multiResolvePolySymbolReference("v-slot:def<caret>ault='{ field }'").asSingleSymbol()
        ?.asSafely<PsiSourcedPolySymbol>()?.source?.text
    )
  }

  @Test
  fun testResolvePropFromComponentWithDefineOptionsAndRegularScript() {
    myFixture.configureByText("HelloWorld.vue", """
      <script lang="ts">
      export const exportedFromScript = 123;
      </script>

      <script setup lang="ts">
      defineProps<{ customProperty: string }>()

      defineOptions({
        name: "BestComponentOfMyLife"
      })
      </script>
      
      <template></template>
    """.trimIndent())
    myFixture.configureByText("ComponentUsage.vue", """
      <template>
        <best-component-of-my-life customProperty="Hello!"/>
      </template>

      <script setup lang="ts">
      import BestComponentOfMyLife from "./HelloWorld.vue";

      defineOptions({ name: 'SuperComp' });
      </script>
    """.trimIndent())

    myFixture.checkGotoDeclaration("customPro<caret>perty=\"Hello!\"",
                                   "defineProps<{ <caret>customProperty: string }>",
                                   "HelloWorld.vue")
  }

  @Test
  fun testBindShorthand() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    val declarations = myFixture
      .multiResolvePolySymbolReference("v-bind:input<caret>Prop")
      .asSequence()
      .filterIsInstance<VueBindingShorthandSymbol>()
      .flatMap { it.nameSegments }
      .flatMap { it.symbols }
      .filterIsInstance<PsiSourcedPolySymbol>()
      .mapNotNull { if (it.source is JSImplicitElement) it.source?.context else it.source }
      .map { it.text }
      .toList()
    assertSameElements(declarations, "inputProp = 'abc'", "inputProp?: string")
  }

  @Test
  fun testNavigateThroughTypeofReferenceToImport() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("globalComponentsWithTypeofImport", "")
    myFixture.configureFromTempProjectFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("ButtonS<caret>FC", "eComponent(<caret>{\n  props: {\n", "ButtonSFC.vue")
  }

  @Test
  fun testResolveGlobalAppComponent() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("resolveGlobalAppComponent", "")
    myFixture.configureFromTempProjectFile("ForComponent.vue")
    myFixture.checkGotoDeclaration("<Global<caret>Component></GlobalComponent>", "<caret>name: \"GlobalComponent\"", "GlobalComponent.vue")
  }

  @Test
  fun testComponentCustomProperties() {
    myFixture.configureVueDependencies()
    myFixture.copyDirectoryToProject("componentCustomProperties", "")
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkGotoDeclaration("{{\$te<caret>st}}", "<caret>\$test: string", "index.ts")
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
