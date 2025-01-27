// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.ml.JSMLTrackingCompletionProvider
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.LookupElementInfo
import com.intellij.webSymbols.testFramework.and
import com.intellij.webSymbols.testFramework.findOffsetBySignature
import com.intellij.webSymbols.testFramework.renderLookupItems
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.filterOutDollarPrefixedProperties
import org.jetbrains.vuejs.lang.filterOutJsKeywordsGlobalObjectsAndCommonProperties
import org.jetbrains.vuejs.lang.getVueTestDataPath
import java.io.File

class VuexCompletionTest : BasePlatformTestCase() {

  private val mappersAndStore = setOf("mapActions", "mapGetters", "mapMutations", "mapState", "rootStore")

  private val filterOutMappersAndStore: (item: LookupElementInfo) -> Boolean = {
    it.lookupString !in mappersAndStore
  }
  private val filterOutThisProperties: (item: LookupElementInfo) -> Boolean = {
    !it.lookupString.startsWith("this.")
  }

  private val filterOutJsTextReferences: (item: LookupElementInfo) -> Boolean = { info ->
    info.priority > JSLookupPriority.NESTING_LEVEL_1.priorityValue
    || info.lookupElement.getUserData(JSMLTrackingCompletionProvider.JS_PROVIDER_KEY) != JSMLTrackingCompletionProvider.Kind.TEXT_REFERENCE
  }

  private val filterOutNoise = (
    filterOutJsKeywordsGlobalObjectsAndCommonProperties
      and filterOutMappersAndStore
      and filterOutThisProperties
      and filterOutJsTextReferences
      and { !it.lookupString.contains("=>") }
      and { it.lookupString !in listOf("commit", "dispatch", "categoryModule") })

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuex/completion"

  fun testBasicGettersCompletion() {
    myFixture.configureVueDependencies("vuex" to "^3.0.1")
    myFixture.configureByFiles("basicGetter.vue", "basicGetter.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "getter1", "getter_2")
  }

  fun testBasicMutationsCompletion() {
    myFixture.configureVueDependencies("vuex" to "^3.0.1")
    myFixture.configureByFiles("basicMutations.vue", "basicMutations.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicMutations2Completion() {
    myFixture.configureVueDependencies("vuex" to "^3.0.1")
    myFixture.configureByFiles("basicMutations2.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicActionsCompletion() {
    myFixture.configureVueDependencies("vuex" to "^3.0.1")
    myFixture.configureByFiles("basicActions.vue", "basicActions.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVuexActions2Completion() {
    myFixture.configureVueDependencies("vuex" to "^3.0.1")
    myFixture.configureByFiles("basicActions2.vue", "basicActions2.ts")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testStorefrontDirectGetter() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { return this.\$store.getters['<caret>']", section = "computed")
    doItemsTest(0, "foo() { return this.store.getters['<caret>']", section = "computed")
    doItemsTest(1, "foo() { return this.\$store.getters['cart/<caret>getCoupon']", section = "computed",
                lookupFilter = filterOutNoise)
    doItemsTest(2, "foo() { return this.\$store.getters.<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "foo() { return this.\$store.getters[<caret>]", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)

    doTypingTest("foo() { return this.\$store.getters['<caret>foo/bar']",
                 "cartCoupon\t", "this.\$store.getters['cart/getCoupon']", section = "computed")
  }

  fun testStorefrontDirectState() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { return this.\$store.state.<caret>", section = "computed", renderPriority = true,
                lookupFilter = filterOutNoise)
    doItemsTest(1, "foo() { return this.\$store.state.category.<caret>", section = "computed", renderPriority = true,
                lookupFilter = filterOutNoise)
  }

  fun testStorefrontDirectDispatch() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>cart/applyCoupon', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch('<caret>cart/applyCoupon', code) }", section = "methods",
                lookupFilter = filterOutNoise)
    doItemsTest(1, "foo() { this.\$store.dispatch('cart/a<caret>', code) }", section = "methods")
    doItemsTest(1, "foo() { this.store.dispatch('cart/a<caret>', code) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.dispatch(<caret>, code) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(2, "foo() { this.store.dispatch(<caret>, code) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)

    doItemsTest(0, "foo() { this.\$store.dispatch({type:'<caret>'}) }", section = "methods")
    doItemsTest(0, "foo() { this.store.dispatch({type:'<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.dispatch({type:'cart/a<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.store.dispatch({type:'cart/a<caret>'}) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.dispatch({type:<caret>}) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(2, "foo() { this.store.dispatch({type:<caret>}) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)

    doTypingTest("foo() { return this.\$store.dispatch('<caret>foo/bar', code)",
                 "cartCoupon\t", "('cart/applyCoupon', code)", section = "methods")
  }

  fun testStorefrontDirectCommit() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "foo() { this.\$store.commit('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.store.commit('<caret>', code) }", section = "methods")
    doItemsTest(0, "foo() { this.\$store.commit('<caret>cart/breadcrumbs/set', code) }", section = "methods",
                lookupFilter = filterOutNoise)
    doItemsTest(0, "foo() { this.store.commit('<caret>cart/breadcrumbs/set', code) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.commit('cart/<caret>', code) }", section = "methods")
    doItemsTest(1, "foo() { this.store.commit('cart/<caret>', code) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.commit(<caret>, code) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(2, "foo() { this.store.commit(<caret>, code) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)


    doItemsTest(0, "foo() { this.\$store.commit({type:'<caret>'}) }", section = "methods")
    doItemsTest(0, "foo() { this.store.commit({type:'<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.\$store.commit({type:'cart/<caret>'}) }", section = "methods")
    doItemsTest(1, "foo() { this.store.commit({type:'cart/<caret>'}) }", section = "methods")
    doItemsTest(2, "foo() { this.\$store.commit({type:<caret>}) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(2, "foo() { this.store.commit({type:<caret>}) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)

    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)",
                 "cartset\n", "('cart/breadcrumbs/setfoo/bar', code)", section = "methods")
    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)",
                 "cartset\t", "('cart/breadcrumbs/set', code)", section = "methods")
  }

  fun testStorefrontMapGetters() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapGetters(<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(1, "...mapGetters('<caret>'", section = "computed")

    doItemsTest(2, "...mapGetters([<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapGetters(['<caret>'", section = "computed")
    doItemsTest(4, "...mapGetters('cart',[<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapGetters('cart',['<caret>'", section = "computed")

    doItemsTest(2, "...mapGetters({foo: <caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapGetters({foo: '<caret>'", section = "computed")
    doItemsTest(4, "...mapGetters('cart',{foo: <caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapGetters('cart',{foo: '<caret>'", section = "computed")

    doItemsTest(4, "...mapGetters([<caret>",
                section = "computed", renderPriority = true, additionalContent = namespacedHandlersCode,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapGetters(['<caret>'",
                section = "computed", additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(4, "...mapGetters({foo: <caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapGetters({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapGetters([<caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapGetters(['<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapGetters({foo: <caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapGetters({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Getter(<caret>)", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "@Getter('<caret>')")
    doItemsTest(5, "@Getter('cart/<caret>')")

    doItemsTest(4, "@cartModule.Getter(<caret>)",
                renderPriority = true, additionalContent = namespacedDecoratorsCode,
                lookupFilter = filterOutNoise and { it.lookupString != "cartModule" })
    doItemsTest(5, "@cartModule.Getter('<caret>')",
                additionalContent = namespacedDecoratorsCode)

    doItemsTest(8, "...mapState({foo(state, getters) { return getters.<caret> }", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
  }

  fun testStorefrontMapState() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapState(<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(1, "...mapState('<caret>'", section = "computed")

    doItemsTest(2, "...mapState([<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapState(['<caret>'", section = "computed")
    doItemsTest(4, "...mapState('cart',[<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapState('cart',['<caret>'", section = "computed")

    doItemsTest(2, "...mapState({foo: <caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapState({foo: '<caret>'", section = "computed")
    doItemsTest(4, "...mapState('cart',{foo: <caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapState('cart',{foo: '<caret>'", section = "computed")

    doItemsTest(6, "...mapState({foo: state => state.<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(7, "...mapState({foo(state) { return state.cart.<caret> }", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)

    doItemsTest(7, "...mapState('cart',{foo: state => state.<caret>", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(8, "...mapState('cart',{foo(state) { return state.breadcrumbs.<caret> }", section = "computed",
                renderPriority = true, lookupFilter = filterOutNoise)

    doItemsTest(4, "...mapState([<caret>",
                section = "computed", renderPriority = true, additionalContent = namespacedHandlersCode,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapState(['<caret>'",
                section = "computed", additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(4, "...mapState({foo: <caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapState({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(7, "...mapState({foo: state => state.<caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(8, "...mapState({foo(state) { return state.breadcrumbs.<caret> }", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(9, "...categoryModule.mapState([<caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(10, "...categoryModule.mapState(['<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(9, "...categoryModule.mapState({foo: <caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(10, "...categoryModule.mapState({foo: '<caret>'", section = "computed",
                additionalContent = namespacedHandlersCode)

    doItemsTest(11, "...categoryModule.mapState({foo: state => state.<caret>", section = "computed",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(2, "@State(<caret>)", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "@State('<caret>')")
    doItemsTest(5, "@State('cart/<caret>')")
    doItemsTest(6, "@State(state => state.<caret>", renderPriority = true, lookupFilter = filterOutNoise)

    doItemsTest(4, "@cartModule.State(<caret>)",
                renderPriority = true, additionalContent = namespacedDecoratorsCode,
                lookupFilter = filterOutNoise and { it.lookupString != "cartModule" })
    doItemsTest(5, "@cartModule.State('<caret>')",
                additionalContent = namespacedDecoratorsCode)
    doItemsTest(7, "@cartModule.State(state => state.<caret>",
                renderPriority = true, additionalContent = namespacedDecoratorsCode, lookupFilter = filterOutNoise)

  }

  fun testStorefrontMapActions() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapActions(<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(1, "...mapActions('<caret>'", section = "methods")

    doItemsTest(2, "...mapActions([<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapActions(['<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',[<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions('cart',['<caret>'", section = "methods")

    doItemsTest(2, "...mapActions({foo(dispatch) { dispatch(<caret>) }", section = "methods",
                renderPriority = true,
                lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapActions({foo: dispatch => dispatch('<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',{foo(dispatch) { dispatch(<caret> } }",
                section = "methods", renderPriority = true,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions('cart',{foo: dispatch => dispatch('<caret>'",
                section = "methods")

    doItemsTest(2, "...mapActions({foo(dispatch) { dispatch({type: <caret>}) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapActions({foo: dispatch => dispatch({type: '<caret>'", section = "methods")
    doItemsTest(4, "...mapActions('cart',{foo(dispatch) { dispatch({type: <caret> } }",
                section = "methods", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions('cart',{foo: dispatch => dispatch({type: '<caret>'",
                section = "methods")

    doItemsTest(4, "...mapActions([<caret>",
                section = "methods", renderPriority = true, additionalContent = namespacedHandlersCode,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions(['<caret>'",
                section = "methods", additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(4, "...mapActions({foo(dispatch) { dispatch(<caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions({foo: dispatch => dispatch('<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(4, "...mapActions({foo(dispatch) { dispatch({type:<caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapActions({foo: dispatch => dispatch({type: '<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions([<caret>", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapActions(['<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions({foo(dispatch) { dispatch(<caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapActions({foo: dispatch => dispatch('<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapActions({foo(dispatch) { dispatch({type: <caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapActions({foo: dispatch => dispatch({type: '<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Action(<caret>)", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "@Action('<caret>')")
    doItemsTest(5, "@Action('cart/<caret>')")

    doItemsTest(4, "@cartModule.Action(<caret>)",
                renderPriority = true, additionalContent = namespacedDecoratorsCode,
                lookupFilter = filterOutNoise and { it.lookupString != "cartModule" })
    doItemsTest(5, "@cartModule.Action('<caret>')",
                additionalContent = namespacedDecoratorsCode)
  }

  fun testStorefrontMapMutations() {
    myFixture.configureStore(VuexTestStore.Storefront)
    doItemsTest(0, "...mapMutations(<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise and filterOutMappersAndStore)
    doItemsTest(1, "...mapMutations('<caret>'", section = "methods")

    doItemsTest(2, "...mapMutations([<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapMutations(['<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',[<caret>", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations('cart',['<caret>'", section = "methods")

    doItemsTest(2, "...mapMutations({foo(commit) { commit(<caret>) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapMutations({foo: commit => commit('<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',{foo(commit) { commit(<caret> } }",
                section = "methods", renderPriority = true,
                lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations('cart',{foo: commit => commit('<caret>'",
                section = "methods")

    doItemsTest(2, "...mapMutations({foo(commit) { commit({type: <caret>) }", section = "methods",
                renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "...mapMutations({foo: commit => commit({type: '<caret>'", section = "methods")
    doItemsTest(4, "...mapMutations('cart',{foo(commit) { commit({type: <caret> } }",
                section = "methods", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations('cart',{foo: commit => commit({type: '<caret>'",
                section = "methods")

    doItemsTest(4, "...mapMutations([<caret>", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations(['<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)

    doItemsTest(4, "...mapMutations({foo(commit) { commit(<caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations({foo: commit => commit('<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(4, "...mapMutations({foo(commit) { commit({type: <caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(5, "...mapMutations({foo: commit => commit({type: '<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations([<caret>", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapMutations(['<caret>'", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations({foo(commit) { commit(<caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapMutations({foo: commit => commit('<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(6, "...categoryModule.mapMutations({foo(commit) { commit({type: <caret> }", section = "methods",
                renderPriority = true, additionalContent = namespacedHandlersCode, lookupFilter = filterOutNoise)
    doItemsTest(7, "...categoryModule.mapMutations({foo: commit => commit({type: '<caret>", section = "methods",
                additionalContent = namespacedHandlersCode)

    doItemsTest(2, "@Mutation(<caret>)", renderPriority = true, lookupFilter = filterOutNoise)
    doItemsTest(3, "@Mutation('<caret>')")
    doItemsTest(5, "@Mutation('cart/<caret>')")

    doItemsTest(4, "@cartModule.Mutation(<caret>)",
                renderPriority = true, additionalContent = namespacedDecoratorsCode,
                lookupFilter = filterOutNoise and { it.lookupString != "cartModule" })
    doItemsTest(5, "@cartModule.Mutation('<caret>')",
                additionalContent = namespacedDecoratorsCode)
  }

  fun testStorefrontStoreActionContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val categoryActions = "store/category/actions.ts"
    // context
    doStoreItemsTest(0, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.<caret> ", lookupFilter = filterOutJsKeywordsGlobalObjectsAndCommonProperties)

    // { state }
    doStoreItemsTest(1, categoryActions, "<caret>async loadCategoryProducts",
                     additionalContent = "test({state}){ state.<caret>}", lookupFilter = filterOutNoise)
    doStoreItemsTest(2, categoryActions, "<caret>async loadCategoryProducts",
                     additionalContent = "test({state}){ state['<caret>']}")

    // { rootState }
    doStoreItemsTest(1, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.category.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(2, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.category['<caret>']",
                     lookupFilter = filterOutNoise)
    doStoreItemsTest(3, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(16, categoryActions, "<caret>const searchCategory",
                     additionalContent = "rootState['<caret>']")

    // context.state
    doStoreItemsTest(1, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.state.<caret> ", lookupFilter = filterOutNoise)
    doStoreItemsTest(2, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.state['<caret>'] ")

    // context.rootState
    doStoreItemsTest(1, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.category.<caret> ", lookupFilter = filterOutNoise)
    doStoreItemsTest(2, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.category['<caret>'] ", lookupFilter = filterOutNoise)

    doStoreItemsTest(3, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState.<caret> ", lookupFilter = filterOutNoise)
    doStoreItemsTest(16, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootState['<caret>'] ")


    // { getters }
    doStoreItemsTest(4, categoryActions, "category || getters.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(5, categoryActions, "<caret>const searchCategory",
                     additionalContent = "getters['<caret>'] ")

    // { rootGetters }
    doStoreItemsTest(6, categoryActions, "rootGetters.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(7, categoryActions, "rootGetters['<caret>")

    // context.getters
    doStoreItemsTest(4, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.getters.<caret> ", lookupFilter = filterOutNoise)
    doStoreItemsTest(5, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.getters['<caret>'] ")

    // context.rootGetters
    doStoreItemsTest(6, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootGetters.<caret> ", lookupFilter = filterOutNoise)
    doStoreItemsTest(7, categoryActions, "<caret>return CategoryService",
                     additionalContent = "context.rootGetters['<caret>'] ")

    // { dispatch }
    doStoreItemsTest(8, categoryActions, "await dispatch('<caret>loadCategoryFilters'")
    doStoreItemsTest(8, categoryActions, "await dispatch('<caret>changeRouterFilterParameters', {foo:12})")
    doStoreItemsTest(9, categoryActions, "await dispatch('<caret>tax/calculateTaxes'") // {root:true}
    doStoreItemsTest(10, categoryActions, "await dispatch(<caret>'loadCategoryFilters'",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })
    doStoreItemsTest(11, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "<caret>,{},{root:true})(", // {root:true}
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    doStoreItemsTest(8, categoryActions, "await dispatch(<caret>'loadCategoryFilters'",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(8, categoryActions, "await dispatch(<caret>'changeRouterFilterParameters', {foo:12})",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(9, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(10, categoryActions, "await dispatch(<caret>'loadCategoryFilters'",
                     additionalContent = "{type: <caret>}", lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })
    doStoreItemsTest(11, categoryActions, "await dispatch(<caret>'tax/calculateTaxes'",
                     additionalContent = "{type: <caret>},{root:true}",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    // context.dispatch
    doStoreItemsTest(8, categoryActions, "context.dispatch('<caret>changeRouterFilterParameters', currentQuery)")
    doStoreItemsTest(9, categoryActions, "context.dispatch('<caret>changeRouterFilterParameters', {}, {root: true})")
    doStoreItemsTest(10, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })
    doStoreItemsTest(11, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "<caret>,{},{root:true})(",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    doStoreItemsTest(8, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)",
                     additionalContent = "{type: '<caret>'}")
    doStoreItemsTest(9, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(10, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', currentQuery)",
                     additionalContent = "{type: <caret>}", lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })
    doStoreItemsTest(11, categoryActions, "context.dispatch(<caret>'changeRouterFilterParameters', {}, {root: true})",
                     additionalContent = "{type: <caret>},{root:true}",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    // { commit }
    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "commit('<caret>set'")
    doStoreItemsTest(13, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "'<caret>',{},{root:true})")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'",
                     lookupFilter = filterOutNoise and { it.lookupString != "payload" })
    doStoreItemsTest(15, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "<caret>,{},{root:true})",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'",
                     additionalContent = "{type: '<caret>'},")
    doStoreItemsTest(13, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "{type: '<caret>'},{root:true},")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "commit(<caret>'set'",
                     additionalContent = "{type: <caret>},",
                     lookupFilter = filterOutNoise and { it.lookupString != "payload" })
    doStoreItemsTest(15, categoryActions, "commit(<caret>'cart/breadcrumbs/set', {foo:12})",
                     additionalContent = "{type: <caret>},{root:true},",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    // context.commit
    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit('<caret>'}")
    doStoreItemsTest(13, categoryActions, "context.commit('<caret>cart/breadcrumbs/set', {}, {root: true})")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit(<caret>}",
                     lookupFilter = filterOutNoise and { it.lookupString != "context" })
    doStoreItemsTest(15, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "<caret>,{},{root:true})",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })

    doStoreItemsTest(12, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit({type: '<caret>'})}")
    doStoreItemsTest(13, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "{type: '<caret>'},{root:true}")
    doStoreItemsTest(14, "store/cart/breadcrumbs/index.ts", "<caret>set ({ commit }",
                     additionalContent = "test(context){context.commit({type: <caret>})}",
                     lookupFilter = filterOutNoise and { it.lookupString != "context" })
    doStoreItemsTest(15, categoryActions, "context.commit(<caret>'cart/breadcrumbs/set', {}, {root: true})",
                     additionalContent = "{type: <caret>}, {root:true})",
                     lookupFilter = filterOutNoise and { it.lookupString.startsWith("\"") })
  }

  fun testStorefrontStoreGettersContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val cartGetters = "store/cart/getters.ts"

    // state
    doStoreItemsTest(0, cartGetters, "state.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(1, cartGetters, "state.<caret>", additionalContent = "breadcrumbs.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(2, cartGetters, "state<caret>.", additionalContent = "['<caret>']")

    // getters
    doStoreItemsTest(3, cartGetters, "getters.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(4, cartGetters, "getters<caret>.", additionalContent = "[<caret>]", lookupFilter = filterOutNoise)
    doStoreItemsTest(5, cartGetters, "getters<caret>.", additionalContent = "['<caret>']")

    // rootState
    doStoreItemsTest(6, cartGetters, "rootState.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(0, cartGetters, "rootState.<caret>", additionalContent = "cart.<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(7, cartGetters, "rootState<caret>.", additionalContent = "['<caret>']")

    // rootGetters
    doStoreItemsTest(8, cartGetters, "rootGetters<caret>[", additionalContent = ".<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(9, cartGetters, "rootGetters[<caret>", lookupFilter = filterOutNoise)
    doStoreItemsTest(10, cartGetters, "rootGetters['<caret>")
    doStoreItemsTest(5, cartGetters, "rootGetters['cart/<caret>")

  }

  fun testStorefrontStoreMutationsContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    val breadcrumbs = "store/cart/breadcrumbs/index.ts"

    // state
    doStoreItemsTest(0, breadcrumbs, "state.<caret>routes",
                     lookupFilter = filterOutNoise)
    doStoreItemsTest(1, breadcrumbs, "state<caret>.routes", additionalContent = "[<caret>]",
                     lookupFilter = filterOutNoise)
    doStoreItemsTest(2, breadcrumbs, "state<caret>.routes", additionalContent = "['<caret>']",
                     lookupFilter = filterOutNoise)
  }

  fun testFunctionInit() {
    myFixture.configureStore(VuexTestStore.FunctionInit)

    val module = "store/myVuexModule.js"

    doStoreItemsTest(0, module, "'set<caret>Prop1'")
    doStoreItemsTest(1, module, "state.<caret>prop1", lookupFilter = filterOutNoise)

    doItemsTest(2, "...mapMutations(['<caret>'", section = "methods")
    doItemsTest(3, "...mapActions(['<caret>'", section = "methods")
  }

  fun testStorefrontThisCompletion() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.configureByFile("storefront-mappers-JS.vue")
    prepareFiles(project)
    checkItems(0, true, true, true, lookupFilter = filterOutDollarPrefixedProperties)

    myFixture.configureByFile("storefront-mappers-TS.ts")
    prepareFiles(project)
    checkItems(1, true, true, true, lookupFilter = filterOutDollarPrefixedProperties)
  }

  fun testStarImportCompletion() {
    myFixture.configureStore(VuexTestStore.StarImport)
    doItemsTest(0, "...mapGetters({foo:'<caret>'", section = "computed", renderPriority = true)
    doItemsTest(1, "...mapState({foo:'<caret>'", section = "computed", renderPriority = true)
    doItemsTest(2, "...mapActions(['<caret>'", section = "methods", renderPriority = true)
  }

  fun testCompositionShoppingCart() {
    myFixture.configureStore(VuexTestStore.CompositionShoppingCart)
    val fileName = "test.vue"
    myFixture.copyFileToProject("composition-shopping-cart.vue", fileName)

    doStoreItemsTest(0, fileName, "store.state.<caret>", renderType = true, renderPriority = true,
                     lookupFilter = filterOutNoise)
    doStoreItemsTest(1, fileName, "store.dispatch('cart/<caret>addProductToCart'", renderType = true, renderPriority = true)
    doStoreItemsTest(2, fileName, "store.dispatch('<caret>products/getAllProducts')", renderType = true, renderPriority = true)
    doStoreItemsTest(3, fileName, "store.dispatch('products/<caret>getAllProducts')", renderType = true, renderPriority = true)
    doStoreItemsTest(4, fileName, "store.state.products.<caret>", renderType = true, renderPriority = true,
                     lookupFilter = filterOutNoise)
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

  private fun doStoreItemsTest(id: Int,
                               file: String,
                               location: String,
                               additionalContent: String = "<caret>",
                               renderType: Boolean = true,
                               renderPriority: Boolean = true,
                               lookupFilter: (item: LookupElementInfo) -> Boolean = { true }) {
    doTestInFile(file, location, additionalContent) {
      checkItems(id, renderType, renderPriority, lookupFilter = lookupFilter)
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

  private fun doItemsTest(id: Int, content: String,
                          section: String? = null,
                          renderType: Boolean = true,
                          renderPriority: Boolean = false,
                          checkJS: Boolean = true,
                          additionalContent: String = "",
                          lookupFilter: (item: LookupElementInfo) -> Boolean = { true }) {
    createFile(content, false, section, additionalContent)
    checkItems(id, renderType, renderPriority, lookupFilter = lookupFilter)
    if (checkJS) {
      createFile(content, true, section, additionalContent)
      checkItems(id, renderType, renderPriority, lookupFilter = lookupFilter)
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

  private fun checkItems(id: Int,
                         renderType: Boolean,
                         renderPriority: Boolean,
                         renderTailText: Boolean = false,
                         lookupFilter: (item: LookupElementInfo) -> Boolean = { true }) {
    myFixture.completeBasic()
    var checkFileName: String
    checkFileName = "gold/${myFixture.file.name}.${id}.txt"
    if (!File("$testDataPath/$checkFileName").exists()) {
      checkFileName = "gold/${getTestName(true)}.${id}.txt"
      FileUtil.createIfDoesntExist(File("$testDataPath/$checkFileName"))
    }
    myFixture.renderLookupItems(renderPriority, renderType, renderTailText, lookupFilter = lookupFilter)
      .let { list ->
        myFixture.configureByText("out.txt", list.joinToString("\n") + "\n")
        myFixture.checkResultByFile(checkFileName, true)
      }
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
  }
}
