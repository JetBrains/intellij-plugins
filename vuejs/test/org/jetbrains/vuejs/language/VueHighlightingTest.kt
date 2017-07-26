package org.jetbrains.vuejs.language

import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspectionBase
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
class VueHighlightingTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testDirectivesWithoutParameters() {
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspectionBase())
    myFixture.configureByText("directivesWithoutHighlighting.vue", "<template>\n" +
                                                                   "  <div v-once></div>\n" +
                                                                   "  <div v-else></div>\n" +
                                                                   "  <div v-pre></div>\n" +
                                                                   "  <div v-cloak></div>\n" +
                                                                   "</template>")
    myFixture.checkHighlighting()
  }

  fun testVIfRequireParameter() {
    myFixture.enableInspections(HtmlUnknownBooleanAttributeInspectionBase())
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
    let todos = 1;
</script>
""")
    myFixture.checkHighlighting()
  }

  fun testShorthandArrowFunctionInTemplate() {
    myFixture.configureByText("ArrowFunctionsAndExpressionsInTemplate.vue", """
<template>
    <div id="app">
        <div @event="val => bar = val"></div>
        {{bar}}
    </div>
</template>
<script>
    let bar = {};
</script>
""")
    myFixture.checkHighlighting()
  }
}