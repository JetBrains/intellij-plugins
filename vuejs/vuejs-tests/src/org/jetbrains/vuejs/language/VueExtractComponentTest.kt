package org.jetbrains.vuejs.language

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.codeInsight.VueExtractComponentIntention
import org.jetbrains.vuejs.codeInsight.VueExtractComponentRefactoring

/**
 * @author Irina.Chernushina on 12/19/2017.
 */
class VueExtractComponentTest: LightPlatformCodeInsightFixtureTestCase() {
  fun testExtractSingleTag() = doExtractTest(
"""<template>
<selection><p>Paragraph!</p></selection>
</template>""",

"""<template>
    <new-component/>
</template>
<script>
    import NewComponent from "./NewComponent";
    export default {
        components: {NewComponent}
    }
</script>""",

"""<template>
    <p>Paragraph!</p>
</template>
<script>
    export default {
        name: 'new-component'
    }
</script>""")

  fun testExtractTwoTagsWithProp() = doExtractTest(
    """<template>
<selection><p>Paragraph! {{ one + 1 }}</p>
<div>And div {{ unresolved }} </div></selection>
</template>
<script>
export default {
    name: 'existing',
    props: {
        one: {}
    }
}
</script>""",

    """<template>
    <new-component :one="one"/>
</template>
<script>
import NewComponent from "./NewComponent";
export default {
    name: 'existing',
    components: {NewComponent},
    props: {
        one: {}
    }
}
</script>""",

    """<template>
    <p>Paragraph! {{ one + 1 }}</p>
    <div>And div {{ unresolved }}</div>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            one: {}
        }
    }
</script>""", 2)

  private fun doExtractTest(existing: String, modified: String, newText: String, numTags: Int = 1) {
    myFixture.configureByText(getTestName(false) + ".vue", existing)

    val context = VueExtractComponentIntention.getContext(myFixture.editor, myFixture.elementAtCaret)
    TestCase.assertNotNull(context)
    TestCase.assertEquals(numTags, context!!.size)

    VueExtractComponentRefactoring(myFixture.project, context, myFixture.editor).perform("new-component")

    myFixture.checkResult(modified)

    FileDocumentManager.getInstance().saveAllDocuments()
    val created = myFixture.file.parent!!.findFile("NewComponent.vue")
    TestCase.assertNotNull(created)
    myFixture.configureByText("NewComponent2.vue", VfsUtil.loadText(created!!.viewProvider.virtualFile))
    myFixture.checkResult(newText)
  }
}