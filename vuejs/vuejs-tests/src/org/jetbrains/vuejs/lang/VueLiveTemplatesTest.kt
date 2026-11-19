package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.liveTemplate.VueComponentDescriptorLiveTemplateContextType
import org.jetbrains.vuejs.liveTemplate.VueInsideTagLiveTemplateContextType
import org.jetbrains.vuejs.liveTemplate.VueScriptLiveTemplateContextType
import org.jetbrains.vuejs.liveTemplate.VueTemplateLiveTemplateContextType
import org.jetbrains.vuejs.liveTemplate.VueTopLevelLiveTemplateContextType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueLiveTemplatesTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testTopLevelVueApplicable(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val cases = listOf(
      Triple(true, "", ".vue"),
      Triple(false, "", ".js"),
      Triple(false, "", ".html"),
      Triple(false, "<template><caret></template>", ".vue"),
      Triple(false, "<caret><template></template>", ".vue")
    )
    val context = VueTopLevelLiveTemplateContextType()
    cases.forEach {
      myFixture.configureByText("TopLevelVueApplicable" + it.third, it.second)
      doTestIsApplicable(context, it.first)
    }
  }

  @Test
  fun testTemplateApplicable(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val cases = listOf(
      Triple(false, "", ".vue"),
      Triple(false, "<div><caret></div>", ".html"),
      Triple(true, "<template><caret></template>", ".vue"),
      Triple(false, "<caret><template></template>", ".vue"),
      Triple(false, "<template><div <caret>></div></template>", ".vue"),
      Triple(true, "<template><div><caret></div></template>", ".vue"),
      Triple(false, "<script><caret></script>", ".vue")
    )
    val context = VueTemplateLiveTemplateContextType()
    cases.forEach {
      myFixture.configureByText("TopLevelVueApplicable" + it.third, it.second)
      doTestIsApplicable(context, it.first)
    }
  }

  @Test
  fun testInsideTagApplicable(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val cases = listOf(
      Triple(false, "", ".vue"),
      Triple(false, "<template><caret></template>", ".vue"),
      Triple(false, "<caret><template></template>", ".vue"),
      Triple(false, "<template></template><caret>", ".vue"),
      Triple(false, "<script><caret></script>", ".vue"),
      Triple(true, "<script <caret>></script>", ".vue"),
      Triple(true, "<template><div <caret>></div></template>", ".vue"),
      Triple(true, "<template><div<caret>></div></template>", ".vue"),
      Triple(false, "<template><div></div><caret></template>", ".vue"),
      Triple(false, "<template><div><caret></div></template>", ".vue")
    )
    val context = VueInsideTagLiveTemplateContextType()
    cases.forEach {
      myFixture.configureByText("TopLevelVueApplicable" + it.third, it.second)
      doTestIsApplicable(context, it.first)
    }
  }

  @Test
  fun testInsideScriptApplicable(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val cases = listOf(
      Triple(false, "", ".vue"),
      Triple(false, "<template><caret></template>", ".vue"),
      Triple(true, "<script><caret></script>", ".vue"),
      Triple(true, "<script>let t = 1;<caret></script>", ".vue"),
      Triple(true, "<script>let t = <caret></script>", ".vue"), // hard to distinguish here
      Triple(true, "<script>export default <caret></script>", ".vue"),  // hard to distinguish here
      Triple(false, "<script>export default {<caret>}</script>", ".vue")
    )
    val context = VueScriptLiveTemplateContextType()
    cases.forEach {
      myFixture.configureByText("TopLevelVueApplicable" + it.third, it.second)
      doTestIsApplicable(context, it.first)
    }
  }

  @Test
  fun testInsideComponentDescriptorApplicable(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies()
    val cases = listOf(
      Triple(false, "", ".vue"),
      Triple(false, "<template><caret></template>", ".vue"),
      Triple(false, "<script><caret></script>", ".vue"),
      Triple(false, "<script <caret>></script>", ".vue"),
      Triple(false, "<script>let t = 1;<caret></script>", ".vue"),
      Triple(false, "<script>let t = <caret></script>", ".vue"), // hard to distinguish here
      Triple(false, "<script>export default <caret></script>", ".vue"),  // hard to distinguish here
      Triple(true, "<script>export default {<caret>}</script>", ".vue"),
      Triple(true, "<script>export default { a: {}, <caret>}</script>", ".vue"),
      Triple(true, "<script>export default { <caret>a: {}}</script>", ".vue"),
      Triple(true, "export default { <caret>a: {}}", ".js"),
      Triple(true, "let Comp = { <caret>a: {}}", ".js")
    )
    val context = VueComponentDescriptorLiveTemplateContextType()
    cases.forEach {
      myFixture.configureByText("TopLevelVueApplicable" + it.third, it.second)
      doTestIsApplicable(context, it.first)
    }
  }

  @Test
  fun testVAction(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("VAction.vue", """
<script>
export default {
  vaction<caret>
}
</script>
""")
    val editor = myFixture.editor
    ListTemplatesAction().actionPerformedImpl(project, editor)
    val lookup = LookupManager.getActiveLookup(editor) as LookupImpl?
    assertNotNull(lookup)
    lookup!!.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    myFixture.checkResult("""
<script>
export default {
  actions: {
    updateValue({commit}, payload) {
      commit('updateValue', payload);
    }
  }
}
</script>
""")
  }

  private fun doTestIsApplicable(context: TemplateContextType, value: Boolean) {
    assertEquals(value,
                 context.isInContext(TemplateActionContext.expanding(myFixture.file, myFixture.caretOffset)),
                 "Wrong in: ${myFixture.file.name}, text: \"${myFixture.file.text}\"")
  }
}
