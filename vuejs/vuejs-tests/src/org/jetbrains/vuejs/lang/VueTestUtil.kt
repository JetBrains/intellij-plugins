package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import com.intellij.util.containers.ContainerUtil

fun directivesTestCase(myFixture: CodeInsightTestFixture) {
  myFixture.configureByText("CustomDirectives.js", """
Vue.directive('focus', {
    inserted: function (el) {
        el.focus()
    }
});
Vue.directive('click-outside', {
inserted: function (el) {
        el.focus()
    }
});
""")
  myFixture.configureByText("importedDirective.js", """
export default {
    inserted: {}
}
""")
  myFixture.configureByText("CustomDirectives.vue", """
<template>
    <label>
        <input v-focus v-local-directive v-some-other-directive v-click-outside/>
    </label>
    <client-comp v-imported-directive></client-comp>
    <div    style=""></div>
</template>

<script>
    import importedDirective from './importedDirective'
    let someOtherDirective = {

    };
    export default {
        name: "client-comp",
        directives: {
            localDirective: {
                // directive definition
                inserted: function (el) {
                    el.focus()
                }
            },
            someOtherDirective,
            importedDirective
        }
    }
</script>
""")
}

fun getVueTestDataPath() = PathManager.getHomePath() + vueRelativeTestDataPath()

fun vueRelativeTestDataPath() = "/contrib/vuejs/vuejs-tests/testData"

// TODO remove duplication with AngularTestUtil
fun renderLookupItems(fixture: CodeInsightTestFixture, renderPriority: Boolean, renderTypeText: Boolean): List<String> {
  return ContainerUtil.mapNotNull<LookupElement, String>(fixture.lookupElements!!) { el ->
    val result = StringBuilder()
    val presentation = TestLookupElementPresentation.renderReal(el)
    if (renderPriority && presentation.isItemTextBold) {
      result.append('!')
    }
    result.append(el.lookupString)
    if (renderTypeText) {
      result.append('#')
      result.append(presentation.typeText)
    }
    if (renderPriority) {
      result.append('#')
      var priority = 0.0
      if (el is PrioritizedLookupElement<*>) {
        priority = el.priority
      }
      result.append(priority.toInt())
    }
    result.toString()
  }
}
