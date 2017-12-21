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

  fun testExtractTagWithAttributeAndMethodCall() = doExtractTest(
    """<template>
<caret><p v-if="one">Paragraph! {{ compMethod() }}</p>
</template>
<script>
export default {
    props: {
        one: {}
    },
    computed: {
      compMethod() {}
    }
}
</script>""",

    """<template>
    <new-component :comp-method="compMethod()" :one="one"/>
</template>
<script>
import NewComponent from "./NewComponent";
export default {
    components: {NewComponent},
    props: {
        one: {}
    },
    computed: {
      compMethod() {}
    }
}
</script>""",

    """<template>
    <p v-if="one">Paragraph! {{ compMethod }}</p>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            compMethod: {},
            one: {}
        }
    }
</script>""", 1)

  fun testExtractComponentWithOtherComponentInside() {
    myFixture.configureByText("OtherComp.vue", "<script>export default { name: 'other-comp' }</script>")
    doExtractTest(
"""<template>
  <caret><div>
    <other-comp>123 -> {{ prop }}</other-comp>
  </div>
</template>
<script>
    import OtherComp from './OtherComp'
    export default {
        name: 'current-comp',
        components: { OtherComp },
        props: ['prop']
    }
</script>
""".trimMargin(),

"""<template>
    <new-component :prop="prop"/>
</template>
<script>
    import OtherComp from './OtherComp'
    import NewComponent from "./NewComponent";
    export default {
        name: 'current-comp',
        components: {NewComponent},
        props: ['prop']
    }
</script>""",

"""<template>
    <div>
        <other-comp>123 -> {{ prop }}</other-comp>
    </div>
</template>
<script>
    import OtherComp from './OtherComp'
    export default {
        name: 'new-component',
        components: {OtherComp},
        props: {
            prop: {}
        }
    }
</script>""", 1)
  }

  fun testExtractComponentWithOtherComponentInsideTS() {
    myFixture.configureByText("OtherComp.vue", "<script>export default { name: 'other-comp' }</script>")
    doExtractTest(
"""<template>
  <caret><div>
    <other-comp>123 -> {{ prop }}</other-comp>
    <unknown-comp/>
  </div>
</template>
<script lang="ts">
    import OtherComp from './OtherComp'
    export default {
        name: 'current-comp',
        components: { OtherComp },
        props: ['prop']
    }
</script>
""".trimMargin(),

"""<template>
    <new-component :prop="prop"/>
</template>
<script lang="ts">
    import NewComponent from "./NewComponent";

    export default {
        name: 'current-comp',
        components: {NewComponent},
        props: ['prop']
    }
</script>""",

"""<template>
    <div>
        <other-comp>123 -> {{ prop }}</other-comp>
        <unknown-comp/>
    </div>
</template>
<script lang="ts">
    import OtherComp from './OtherComp'
    export default {
        name: 'new-component',
        components: {OtherComp},
        props: {
            prop: {}
        }
    }
</script>""", 1)
  }

  fun testExtractWithVFor() = doExtractTest(
"""<template>
    <div v-for="item in items">
        <caret><span>Text: {{ item }}</span>
    </div>
</template>

<script>
    export default {
        name: "test-v-for",
        data() {
            return {
                items: [1,2,3,4,5]
            }
        }
    };
</script>""",

"""<template>
    <div v-for="item in items">
        <new-component :item="item"/>
    </div>
</template>

<script>
    import NewComponent from "./NewComponent";
    export default {
        name: "test-v-for",
        components: {NewComponent},
        data() {
            return {
                items: [1,2,3,4,5]
            }
        }
    };
</script>""",

"""<template>
    <span>Text: {{ item }}</span>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            item: {}
        }
    }
</script>"""
  )

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