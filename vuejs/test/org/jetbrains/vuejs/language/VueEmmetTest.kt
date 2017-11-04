package org.jetbrains.vuejs.language

import com.intellij.codeInsight.template.emmet.EmmetPreviewUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase

class VueEmmetTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testImplicitTag() {
    myFixture.configureByText("a.vue", "<template>.cls<caret></template>")
    myFixture.type("\t")
    myFixture.checkResult("<template><div class=\"cls\"></div></template>")
  }

  fun testPreviewTooltip() {
    myFixture.configureByText("PreviewTooltip.vue", "<template>div>vfor<caret></template>")
    TestCase.assertEquals(
"""<div>
    <div v-for="item in items" :key="item.id">
        {{ item }}
    </div>
</div>""", EmmetPreviewUtil.calculateTemplateText(myFixture.editor, myFixture.file, true))
  }
}

