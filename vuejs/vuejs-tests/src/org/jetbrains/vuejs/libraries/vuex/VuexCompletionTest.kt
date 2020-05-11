// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency
import org.jetbrains.vuejs.lang.findOffsetBySignature
import org.jetbrains.vuejs.lang.renderLookupItems
import java.io.File

class VuexCompletionTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/vuex/completion"

  fun testBasicGettersCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicGetter.vue", "basicGetter.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "getter1", "getter_2")
  }

  fun testBasicMutationsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicMutations.vue", "basicMutations.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicMutations2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicMutations2.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicActionsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicActions.vue", "basicActions.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVuexActions2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicActions2.vue", "basicActions2.ts")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testStorefrontDirectGetter() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { return this.\$store.getters['<caret>']", section = "computed")
    doItemsTest(0, "foo() { return this.store.getters['<caret>']", section = "computed")
    doItemsTest(1, "foo() { return this.\$store.getters['cart/<caret>getCoupon']", section = "computed")
    doItemsTest(2, "foo() { return this.\$store.getters.<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(3, "foo() { return this.\$store.getters[<caret>]", section = "computed", strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.getters['<caret>foo/bar']",
                 "cartCoupon\t", "this.\$store.getters['cart/getCoupon']", section = "computed")
  }

  fun testStorefrontDirectState() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { return this.\$store.state.<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(1, "foo() { return this.\$store.state.category.<caret>", section = "computed", strict = false, renderPriority = true)
  }

  fun testStorefrontDirectDispatch() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>cart/applyCoupon', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch('<caret>cart/applyCoupon', code) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.dispatch('cart/a<caret>', code) }", section = "methods")
    doItemsTest(1, "foo() { this.store.dispatch('cart/a<caret>', code) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.dispatch(<caret>, code) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.dispatch(<caret>, code) }", section = "methods", strict = false, renderPriority = true)

    doItemsTest(0, "foo() { this.\$store.dispatch({type:'<caret>'}) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch({type:'<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.dispatch({type:'cart/a<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.store.dispatch({type:'cart/a<caret>'}) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.dispatch({type:<caret>}) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.dispatch({type:<caret>}) }", section = "methods", strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.dispatch('<caret>foo/bar', code)",
                 "cartCoupon\t", "('cart/applyCoupon', code)", section = "methods")
  }

  fun testStorefrontDirectCommit() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { this.\$store.commit('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.commit('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.\$store.commit('<caret>cart/breadcrumbs/set', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.commit('<caret>cart/breadcrumbs/set', code) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.commit('cart/<caret>', code) }", section = "methods")
    doItemsTest(1, "foo() { this.store.commit('cart/<caret>', code) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.commit(<caret>, code) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.commit(<caret>, code) }", section = "methods", strict = false, renderPriority = true)


    doItemsTest(0, "foo() { this.\$store.commit({type:'<caret>'}) }", section = "methods")
    doItemsTest(0, "foo() { this.store.commit({type:'<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.commit({type:'cart/<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.store.commit({type:'cart/<caret>'}) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.commit({type:<caret>}) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.commit({type:<caret>}) }", section = "methods", strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)",
                 "cartset\n", "('cart/breadcrumbs/setfoo/bar', code)", section = "methods")
    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)",
                 "cartset\t", "('cart/breadcrumbs/set', code)", section = "methods")
  }

  fun testStorefrontMapGetters() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapGetters(<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(1, "...mapGetters('<caret>'", section = "computed")

    doItemsTest(2, "...mapGetters([<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(3, "...mapGetters(['<caret>'", section = "computed")
    doItemsTest(4, "...mapGetters('cart',[<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapGetters('cart',['<caret>'", section = "computed")

    doItemsTest(2, "...mapGetters({foo: <caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(3, "...mapGetters({foo: '<caret>'", section = "computed")
    doItemsTest(4, "...mapGetters('cart',{foo: <caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapGetters('cart',{foo: '<caret>'", section = "computed")

    doItemsTest(4, "...mapGetters([<caret>",
                additionalContent = namespacedHandlersCode, section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapGetters(['<caret>'",
                additionalContent = namespacedHandlersCode, section = "computed")

    doItemsTest(4, "...mapGetters({foo: <caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapGetters({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapGetters([<caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapGetters(['<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapGetters({foo: <caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapGetters({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Getter(<caret>)", strict = false, renderPriority = true)
    doItemsTest(3, "@Getter('<caret>')")
    doItemsTest(5, "@Getter('cart/<caret>')")

    doItemsTest(4, "@cartModule.Getter(<caret>)",
                additionalContent = namespacedDecoratorsCode, strict = false, renderPriority = true)
    doItemsTest(5, "@cartModule.Getter('<caret>')",
                additionalContent = namespacedDecoratorsCode)

    doItemsTest(8, "...mapState({foo(state, getters) { return getters.<caret> }", section = "computed", strict = false,
                renderPriority = true)
  }

  fun testStorefrontMapState() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapState(<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(1, "...mapState('<caret>'", section = "computed")

    doItemsTest(2, "...mapState([<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(3, "...mapState(['<caret>'", section = "computed")
    doItemsTest(4, "...mapState('cart',[<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapState('cart',['<caret>'", section = "computed")

    doItemsTest(2, "...mapState({foo: <caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(3, "...mapState({foo: '<caret>'", section = "computed")
    doItemsTest(4, "...mapState('cart',{foo: <caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapState('cart',{foo: '<caret>'", section = "computed")

    doItemsTest(6, "...mapState({foo: state => state.<caret>", section = "computed", strict = false, renderPriority = true)
    doItemsTest(7, "...mapState({foo(state) { return state.cart.<caret> }", section = "computed", strict = false, renderPriority = true)

    doItemsTest(7, "...mapState('cart',{foo: state => state.<caret>",
                section = "computed", strict = false, renderPriority = true)
    doItemsTest(8, "...mapState('cart',{foo(state) { return state.breadcrumbs.<caret> }",
                section = "computed", strict = false, renderPriority = true)

    doItemsTest(4, "...mapState([<caret>",
                additionalContent = namespacedHandlersCode, section = "computed", strict = false, renderPriority = true)
    doItemsTest(5, "...mapState(['<caret>'",
                additionalContent = namespacedHandlersCode, section = "computed")

    doItemsTest(4, "...mapState({foo: <caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapState({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(7, "...mapState({foo: state => state.<caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(8, "...mapState({foo(state) { return state.breadcrumbs.<caret> }", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)

    doItemsTest(9, "...categoryModule.mapState([<caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(10, "...categoryModule.mapState(['<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(9, "...categoryModule.mapState({foo: <caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(10, "...categoryModule.mapState({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(11, "...categoryModule.mapState({foo: state => state.<caret>", section = "computed",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)

    doItemsTest(2, "@State(<caret>)", strict = false, renderPriority = true)
    doItemsTest(3, "@State('<caret>')")
    doItemsTest(5, "@State('cart/<caret>')")
    doItemsTest(6, "@State(state => state.<caret>", strict = false, renderPriority = true)

    doItemsTest(4, "@cartModule.State(<caret>)",
                additionalContent = namespacedDecoratorsCode, strict = false, renderPriority = true)
    doItemsTest(5, "@cartModule.State('<caret>')",
                additionalContent = namespacedDecoratorsCode)
    doItemsTest(7, "@cartModule.State(state => state.<caret>",
                additionalContent = namespacedDecoratorsCode, strict = false, renderPriority = true)

  }

  fun testStorefrontMapActions() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapActions(<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(1, "...mapActions('<caret>'", section = "methods")

    doItemsTest(2, "...mapActions([<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapActions(['<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',[<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions('cart',['<caret>'", section = "methods")

    doItemsTest(2, "...mapActions({foo(dispatch) { dispatch(<caret>) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapActions({foo: dispatch => dispatch('<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',{foo(dispatch) { dispatch(<caret> } }",
                section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions('cart',{foo: dispatch => dispatch('<caret>'",
                section = "methods")

    doItemsTest(2, "...mapActions({foo(dispatch) { dispatch({type: <caret>}) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapActions({foo: dispatch => dispatch({type: '<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',{foo(dispatch) { dispatch({type: <caret> } }",
                section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions('cart',{foo: dispatch => dispatch({type: '<caret>'",
                section = "methods")

    doItemsTest(4, "...mapActions([<caret>",
                additionalContent = namespacedHandlersCode, section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions(['<caret>'",
                additionalContent = namespacedHandlersCode, section = "methods")

    doItemsTest(4, "...mapActions({foo(dispatch) { dispatch(<caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions({foo: dispatch => dispatch('<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(4, "...mapActions({foo(dispatch) { dispatch({type:<caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapActions({foo: dispatch => dispatch({type: '<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions([<caret>", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapActions(['<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions({foo(dispatch) { dispatch(<caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapActions({foo: dispatch => dispatch('<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions({foo(dispatch) { dispatch({type: <caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapActions({foo: dispatch => dispatch({type: '<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Action(<caret>)", strict = false, renderPriority = true)
    doItemsTest(3, "@Action('<caret>')")
    doItemsTest(5, "@Action('cart/<caret>')")

    doItemsTest(4, "@cartModule.Action(<caret>)",
                additionalContent = namespacedDecoratorsCode, strict = false, renderPriority = true)
    doItemsTest(5, "@cartModule.Action('<caret>')",
                additionalContent = namespacedDecoratorsCode)
  }

  fun testStorefrontMapMutations() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapMutations(<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(1, "...mapMutations('<caret>'", section = "methods")

    doItemsTest(2, "...mapMutations([<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapMutations(['<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',[<caret>", section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations('cart',['<caret>'", section = "methods")

    doItemsTest(2, "...mapMutations({foo(commit) { commit(<caret>) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapMutations({foo: commit => commit('<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',{foo(commit) { commit(<caret> } }",
                section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations('cart',{foo: commit => commit('<caret>'",
                section = "methods")

    doItemsTest(2, "...mapMutations({foo(commit) { commit({type: <caret>) }", section = "methods", strict = false, renderPriority = true)
    doItemsTest(3, "...mapMutations({foo: commit => commit({type: '<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',{foo(commit) { commit({type: <caret> } }",
                section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations('cart',{foo: commit => commit({type: '<caret>'",
                section = "methods")

    doItemsTest(4, "...mapMutations([<caret>",
                additionalContent = namespacedHandlersCode, section = "methods", strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations(['<caret>'",
                additionalContent = namespacedHandlersCode, section = "methods")

    doItemsTest(4, "...mapMutations({foo(commit) { commit(<caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations({foo: commit => commit('<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(4, "...mapMutations({foo(commit) { commit({type: <caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(5, "...mapMutations({foo: commit => commit({type: '<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations([<caret>", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapMutations(['<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations({foo(commit) { commit(<caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapMutations({foo: commit => commit('<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations({foo(commit) { commit({type: <caret> }", section = "methods",
                additionalContent = namespacedHandlersCode, strict = false, renderPriority = true)
    doItemsTest(7, "...categoryModule.mapMutations({foo: commit => commit({type: '<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Mutation(<caret>)", strict = false, renderPriority = true)
    doItemsTest(3, "@Mutation('<caret>')")
    doItemsTest(5, "@Mutation('cart/<caret>')")

    doItemsTest(4, "@cartModule.Mutation(<caret>)",
                additionalContent = namespacedDecoratorsCode, strict = false, renderPriority = true)
    doItemsTest(5, "@cartModule.Mutation('<caret>')",
                additionalContent = namespacedDecoratorsCode)
  }

  fun testStorefrontStoreActionContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val categoryActions = "store/category/actions.ts"
    //context
    doStoreItemsTest(0, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.<caret> ", strict = false)

    // { state }
    doStoreItemsTest(1, categoryActions, "<caret>async loadCategoryProducts",
                     additionalContent = "test({state}){ state.<caret>}", strict = false)
    doStoreItemsTest(2, categoryActions, "<caret>async loadCategoryProducts",
                     additionalContent = "test({state}){ state['<caret>']}")

    // { rootState }
    doStoreItemsTest(1, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.category.<caret>", strict = false)
    doStoreItemsTest(2, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.category['<caret>']", strict = false, renderPriority = false)
    doStoreItemsTest(3, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.<caret>", strict = false, renderPriority = false)
    doStoreItemsTest(3, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState['<caret>']")

    // context.state
    doStoreItemsTest(1, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.state.<caret> ", strict = false)
    doStoreItemsTest(2, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.state['<caret>'] ")

    // context.rootState
    doStoreItemsTest(1, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.category.<caret> ", strict = false)
    doStoreItemsTest(2, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.category['<caret>'] ", strict = false, renderPriority = false)

    doStoreItemsTest(3, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.<caret> ", strict = false, renderPriority = false)
    doStoreItemsTest(3, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState['<caret>'] ")


    // { getters }
    doStoreItemsTest(4, categoryActions, "category || getters.<caret>", strict = false)
    doStoreItemsTest(5, categoryActions, "<caret>const searchCategory",
                     additionalContent = "getters['<caret>'] ")

    // { rootGetters }
    doStoreItemsTest(6, categoryActions, "rootGetters.<caret>", strict = false)
    doStoreItemsTest(7, categoryActions, "rootGetters['<caret>")

    // context.getters
    doStoreItemsTest(4, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.getters.<caret> ", strict = false)
    doStoreItemsTest(5, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.getters['<caret>'] ")

    // context.rootGetters
    doStoreItemsTest(6, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootGetters.<caret> ", strict = false)
    doStoreItemsTest(7, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootGetters['<caret>'] ")

    // { dispatch }
    doStoreItemsTest(8, categoryActions, "await dispatch('<caret>loadCategoryFilters'")
    doStoreItemsTest(8, categoryActions, "await dispatch('<caret>changeRouterFilterParameters', {foo:12})")
    doStoreItemsTest(9, categoryActions, "await dispatch('<caret>tax/calculateTaxes'") // {root:true}
    doStoreItemsTest(10, categoryActions, "await dispatch(<caret>'loadCategoryFilters'", strict = false)
    doStoreItemsTest(11, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "<caret>,{},{root:true})", strict = false) // {root:true}

    doStoreItemsTest(8, categoryActions, "await dispatch(<caret>'loadCategoryFilters'",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(8, categoryActions, "await dispatch(<caret>'changeRouterFilterParameters', {foo:12})",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(9, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(10, categoryActions, "await dispatch(<caret>'loadCategoryFilters'",
                     additionalContent = "{type: <caret>}", strict = false)
    doStoreItemsTest(11, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "{type: <caret>},{root:true}", strict = false)

    // context.dispatch
    doStoreItemsTest(8, categoryActions, "context.dispatch('<caret>changeRouterFilterParameters', currentQuery)")
    doStoreItemsTest(9, categoryActions, "context.dispatch('<caret>changeRouterFilterParameters', {}, {root: true})")
    doStoreItemsTest(10, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)", strict = false)
    doStoreItemsTest(11, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "<caret>,{},{root:true})", strict = false)

    doStoreItemsTest(8, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(9, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(10, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)",
                     additionalContent = "{type: <caret>}", strict = false)
    doStoreItemsTest(11, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "{type: <caret>},{root:true}", strict = false)

    // { commit }
    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "commit('<caret>set'")
    doStoreItemsTest(13, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "'<caret>',{},{root:true})")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'", strict = false)
    doStoreItemsTest(15, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "<caret>,{},{root:true})", strict = false)

    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'",
                     additionalContent = "{type: '<caret>'},")
    doStoreItemsTest(13, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "{type: '<caret>'},{root:true},")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'",
                     additionalContent = "{type: <caret>},", strict = false)
    doStoreItemsTest(15, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "{type: <caret>},{root:true},", strict = false)

    // context.commit
    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit('<caret>'}")
    doStoreItemsTest(13, categoryActions, "context.commit('<caret>cart/breadcrumbs/set', {}, {root: true})")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit(<caret>}", strict = false)
    doStoreItemsTest(15, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "<caret>,{},{root:true})", strict = false)

    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit({type: '<caret>'})}")
    doStoreItemsTest(13, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit({type: <caret>})}", strict = false)
    doStoreItemsTest(15, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "{type: <caret>}, {root:true})", strict = false)
  }

  fun testStorefrontStoreGettersContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val cartGetters = "store/cart/getters.ts"

    // state
    doStoreItemsTest(0, cartGetters, "state.<caret>", strict = false)
    doStoreItemsTest(1, cartGetters, "state.<caret>", additionalContent = "breadcrumbs.<caret>", strict = false)
    doStoreItemsTest(2, cartGetters, "state<caret>.", additionalContent = "['<caret>']")

    // getters
    doStoreItemsTest(3, cartGetters, "getters.<caret>", strict = false)
    doStoreItemsTest(4, cartGetters, "getters<caret>.", additionalContent = "[<caret>]", strict = false)
    doStoreItemsTest(5, cartGetters, "getters<caret>.", additionalContent = "['<caret>']")

    // rootState
    doStoreItemsTest(6, cartGetters, "rootState.<caret>", strict = false)
    doStoreItemsTest(0, cartGetters, "rootState.<caret>", additionalContent = "cart.<caret>", strict = false)
    doStoreItemsTest(7, cartGetters, "rootState<caret>.", additionalContent = "['<caret>']")

    // rootGetters
    doStoreItemsTest(8, cartGetters, "rootGetters<caret>[", additionalContent = ".<caret>", strict = false)
    doStoreItemsTest(9, cartGetters, "rootGetters[<caret>", strict = false)
    doStoreItemsTest(10, cartGetters, "rootGetters['<caret>")
    doStoreItemsTest(5, cartGetters, "rootGetters['cart/<caret>")

  }

  fun testStorefrontStoreMutationsContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val breadcrumbs = "store/cart/breadcrumbs/index.ts"

    // state
    doStoreItemsTest(0, breadcrumbs, "state.<caret>routes", strict = false)
    doStoreItemsTest(1, breadcrumbs, "state<caret>.routes", additionalContent = "[<caret>]", strict = false)
    doStoreItemsTest(2, breadcrumbs, "state<caret>.routes", additionalContent = "['<caret>']")
  }

  fun testFunctionInit() {
    myFixture.configureStore(VuexTestStore.FunctionInit)

    val module = "store/myVuexModule.js"

    doStoreItemsTest(0, module, "'set<caret>Prop1'", strict = true)
    doStoreItemsTest(1, module, "state.<caret>prop1", strict = false)

    doItemsTest(2, "...mapMutations(['<caret>'", section = "methods")
    doItemsTest(3, "...mapActions(['<caret>'", section = "methods")
  }

  fun testStorefrontThisCompletion() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.configureByFile("storefront-mappers-JS.vue")
    checkItems(0, false, true, true, true)

    myFixture.configureByFile("storefront-mappers-TS.ts")
    checkItems(1, false, true, true, true)
  }

  private val namespacedHandlersCode = """
    const {mapState, mapActions, mapGetters, mapMutations} = createNamespacedHelpers('cart')
    const categoryModule = createNamespacedHelpers('category')
  """

  private val namespacedDecoratorsCode = "const cartModule = namespace('cart')"

  private fun doTypingTest(content: String, toType: String, expectedContents: String, section: String? = null, checkJS: Boolean = true,
                           additionalContent: String = "") {
    createFile(content, false, section, additionalContent)
    checkTyping(toType, expectedContents)
    if (checkJS) {
      createFile(content, true, section, additionalContent)
      checkTyping(toType, expectedContents)
    }
  }

  private fun checkTyping(toType: String, expectedContents: String) {
    myFixture.completeBasic()
    myFixture.type(toType)
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    TestCase.assertEquals(expectedContents, myFixture.file.findElementAt(myFixture.caretOffset)?.context?.context?.text)
  }

  private fun doStoreItemsTest(id: Int, file: String, location: String, additionalContent: String = "<caret>",
                               strict: Boolean = true, renderType: Boolean = true, renderPriority: Boolean = !strict) {
    doTestInFile(file, location, additionalContent) {
      checkItems(id, strict, renderType, renderPriority)
    }
  }

  private fun doTestInFile(file: String, location: String, additionalContent: String, test: () -> Unit) {
    myFixture.configureFromTempProjectFile(file)
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    val insertionOffset = myFixture.file.findOffsetBySignature(location)
    val document = myFixture.getDocument(myFixture.file)
    val contentOffset = additionalContent.indexOf("<caret>")
    assert(contentOffset >= 0) { additionalContent }
    WriteCommandAction.runWriteCommandAction(project) {
      document.insertString(insertionOffset, additionalContent.replace("<caret>", ""))
    }
    PsiDocumentManager.getInstance(project).commitDocument(document)
    myFixture.editor.caretModel.moveToOffset(insertionOffset + contentOffset)
    try {
      test()
    }
    finally {
      FileDocumentManager.getInstance().reloadFromDisk(document)
    }
  }

  private fun doItemsTest(id: Int, content: String, section: String? = null, strict: Boolean = true, renderType: Boolean = true,
                          renderPriority: Boolean = false, checkJS: Boolean = true, additionalContent: String = "") {
    createFile(content, false, section, additionalContent)
    checkItems(id, strict, renderType, renderPriority)
    if (checkJS) {
      createFile(content, true, section, additionalContent)
      checkItems(id, strict, renderType, renderPriority)
    }
  }

  private fun createFile(content: String, js: Boolean, section: String?, additionalContent: String) {
    val fileContents = """
      import {mapActions, mapGetters, mapMutations, mapState} from 'vuex'
      import {rootStore} from "aaa"
      
      ${additionalContent}
      
      export default {
        ${if (section != null) "$section: {" else ""}
           ${content}
        ${if (section != null) "}" else ""}
      }""".trimIndent()
    myFixture.configureByText("${getTestName(true)}.${if (js) "js" else "ts"}", fileContents)
  }

  private fun checkItems(id: Int, strict: Boolean, renderType: Boolean, renderPriority: Boolean, renderTailText: Boolean = false) {
    myFixture.completeBasic()
    var checkFileName: String
    checkFileName = "gold/${myFixture.file.name}.${id}.txt"
    if (!File("$testDataPath/$checkFileName").exists()) {
      checkFileName = "gold/${getTestName(true)}.${id}.txt"
      FileUtil.createIfDoesntExist(File("$testDataPath/$checkFileName"))
    }
    myFixture.renderLookupItems(renderPriority, renderType, renderTailText)
      .let { list ->
        if (strict) {
          myFixture.configureByText("out.txt", list.sorted().joinToString("\n") + "\n")
          myFixture.checkResultByFile(checkFileName, true)
        }
        else {
          val items = myFixture.configureByFile(checkFileName).text.split('\n').filter { !it.isEmpty() }
          UsefulTestCase.assertContainsElements(list, items)
        }
      }
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
  }
}
