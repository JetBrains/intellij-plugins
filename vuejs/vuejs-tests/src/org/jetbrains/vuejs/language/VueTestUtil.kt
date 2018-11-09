package org.jetbrains.vuejs.language

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

/**
 * @author Irina.Chernushina on 10/23/2017.
 */
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

fun getVueTestDataPath() = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData"