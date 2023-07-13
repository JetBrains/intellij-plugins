package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.liveTemplate.*

class VueLiveTemplatesTest : BasePlatformTestCase() {
  fun testTopLevelVueApplicable() {
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

  fun testTemplateApplicable() {
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

  fun testInsideTagApplicable() {
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

  fun testInsideScriptApplicable() {
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

  fun testInsideComponentDescriptorApplicable() {
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

  fun testVAction() {
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
    TestCase.assertNotNull(lookup)
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
    TestCase.assertEquals("Wrong in: ${myFixture.file.name}, text: \"${myFixture.file.text}\"",
                          value, context.isInContext(TemplateActionContext.expanding(myFixture.file, myFixture.caretOffset)))
  }
}
