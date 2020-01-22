// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency

class VuexCompletionTest : BasePlatformTestCase() {

  fun testVuexGettersCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_getter.js", "export const store = new Vuex.Store({\n" +
                                                "    getters: {\n" +
                                                "        getter1(state) {\n" +
                                                "            let data = {\n" +
                                                "                insideGetter1: \"uno\",\n" +
                                                "                insideGetter2: \"duos\"\n" +
                                                "            }\n" +
                                                "        },\n" +
                                                "        getter_2(state) {\n" +
                                                "        }\n" +
                                                "    }\n" +
                                                "\n" +
                                                "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapGetters([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "getter1", "getter_2")
  }

  fun testVuexMutationsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_mutations.js",
                              "export const store = new Vuex.Store({\n" +
                              "    mutations: {\n" +
                              "        mutation1(state, payload) {\n" +
                              "            let data = {\n" +
                              "                insideMutation1: \"uno\",\n" +
                              "                insideMutation2: \"duos\"\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapMutations([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testVuexMutations2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("index.js", "export const store = new Vuex.Store({\n" +
                                          "    mutations: {\n" +
                                          "        mutation1(state, payload) {\n" +
                                          "            let data = {\n" +
                                          "                mutation1_inside: \"uno\",\n" +
                                          "                mutation2_inside: \"duos\"\n" +
                                          "            }\n" +
                                          "        }\n," +
                                          "        mutation2(state, payload) {}" +
                                          "    },\n" +
                                          "    actions: {\n" +
                                          "        action1: function ({commit}, payload) {\n" +
                                          "            commit('m<caret>')\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "})")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testVuexActionsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_actions.js", "export const store = new Vuex.Store({\n" +
                                                 "    actions: {\n" +
                                                 "        action1: function ({commit}, payload) {\n" +
                                                 "            commit('mutation1')\n" +
                                                 "            let data = {\n" +
                                                 "                insideAction1: \"uno\",\n" +
                                                 "                insideAction2: \"duos\"\n" +
                                                 "            }\n" +
                                                 "        },\n" +
                                                 "        action_2: function ({commit}) {\n" +
                                                 "            commit('mutation_2')\n" +
                                                 "        },\n" +
                                                 "    },\n" +
                                                 "})")
    myFixture.configureByText("state.vue", "<script>\n" +
                                           "    export default {\n" +
                                           "        methods: {\n" +
                                           "            ...mapActions([\n" +
                                           "                '<caret>'\n" +
                                           "            ])\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVuexActions2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByText("vuex_actions.js", "export const store = new Vuex.Store({\n" +
                                                 "    actions: {\n" +
                                                 "        action1: function ({commit}, payload) {\n" +
                                                 "            commit('mutation1')\n" +
                                                 "            let data = {\n" +
                                                 "                insideAction1: \"uno\",\n" +
                                                 "                insideAction2: \"duos\"\n" +
                                                 "            }\n" +
                                                 "        },\n" +
                                                 "        action_2: function ({commit}) {\n" +
                                                 "            commit('mutation_2')\n" +
                                                 "        },\n" +
                                                 "    },\n" +
                                                 "})")
    myFixture.configureByText("state.vue", "\n" +
                                           "<script>\n" +
                                           "    export default {\n" +
                                           "        },\n" +
                                           "        computed: {\n" +
                                           "            dataData() {\n" +
                                           "                this.store.dispatch('<caret>')\n" +
                                           "            },\n" +
                                           "        }\n" +
                                           "    }\n" +
                                           "</script>")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }


}
