package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JSTestUtils
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

  fun testExtractSingleTagWithName() = doExtractTest(
"""<template>
<selection><p>Paragraph!</p></selection>
</template>
<script>
    export default {
        name: 'Hello'
    }
</script>
""",

"""<template>
    <new-component/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
        name: 'Hello',
        components: {NewComponent}
    }
</script>
""",

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

  fun testExtractForPug() = doExtractTest(
"""<template lang ="pug">
    div(v-for="item in items")
        <selection>span Text: {{ item + data1 }}</selection>
</template>

<script>
    export default {
        name: "test-v-for",
        data() {
            return {
                items: [1,2,3,4,5],
                data1: 2
            }
        }
    };
</script>""",

"""<template lang ="pug">
    div(v-for="item in items")
        new-component(:data1="data1" :item="item")
</template>

<script>
    import NewComponent from "./NewComponent";

    export default {
        name: "test-v-for",
        components: {NewComponent},
        data() {
            return {
                items: [1,2,3,4,5],
                data1: 2
            }
        }
    };
</script>""",

"""<template lang="pug">
    span Text: {{ item + data1 }}
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            data1: {},
            item: {}
        }
    }
</script>"""
  )

  fun testSameNamedFunctionCalls() = doExtractTest(
"""<template>
    <selection><p>Very first paragraph {{oneMore}}</p>
    <p>Second paragraph {{oneMore}}</p>
    <p>Third paragraph {{oneMore()}}</p>
    <p>Fourth paragraph {{oneMore()}}</p></selection>
</template>
<script>
    export default {
        methods: {
            oneMore() {}
        }
    }
</script>
""",

"""<template>
    <new-component :one-more="oneMore"/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
        components: {NewComponent},
        methods: {
            oneMore() {}
        }
    }
</script>
""",

"""<template>
    <p>Very first paragraph {{oneMore}}</p>
    <p>Second paragraph {{oneMore}}</p>
    <p>Third paragraph {{oneMore()}}</p>
    <p>Fourth paragraph {{oneMore()}}</p>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            oneMore: {type: Function}
        }
    }
</script>""", 4)

  fun testSameNamedProps() = doExtractTest(
"""<template>
    <selection><p>Very first paragraph {{propWithCamel}}</p>
    <p>Second paragraph {{propWithCamel}}</p></selection>
</template>
<script>
    export default {
        props: {
            propWithCamel: {}
        }
    }
</script>
""",

"""<template>
    <new-component :prop-with-camel="propWithCamel"/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
        components: {NewComponent},
        props: {
            propWithCamel: {}
        }
    }
</script>
""",

"""<template>
    <p>Very first paragraph {{propWithCamel}}</p>
    <p>Second paragraph {{propWithCamel}}</p>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            propWithCamel: {}
        }
    }
</script>""", 2)

  fun testCleanupIfNameIsUsed() {
    doExtractTest(
      """<template>
    <caret><p>Very first paragraph {{propWithCamel}}</p>
    <p>Second paragraph {{propWithCamel}}</p>
</template>
<script>
    export default {
        props: {
            propWithCamel: {}
        }
    }
</script>
""",
      """<template>
    <caret><p>Very first paragraph {{propWithCamel}}</p>
    <p>Second paragraph {{propWithCamel}}</p>
</template>
<script>
    export default {
        props: {
            propWithCamel: {}
        }
    }
</script>
""", null, 1, "dd")
  }

  fun testCleanupPugIfNameIsUsed() {
    doExtractTest(
      """<template lang="pug">
    <caret>div(v-if="items()")
        div(v-for="item in items()")
            p Id: {{ item.id }}, name: {{ item.name }}
</template>
<script>
    export default {
        props: {
            items: {}
        }
    }
</script>
""",
      """<template lang="pug">
    <caret>div(v-if="items()")
        div(v-for="item in items()")
            p Id: {{ item.id }}, name: {{ item.name }}
</template>
<script>
    export default {
        props: {
            items: {}
        }
    }
</script>
""", null, 1, "dd")
  }

  fun testFindImport() {
    myFixture.configureByText("OtherName.vue",
"""
<script>export default { name: "cool-stuck" }</script>
""")
    doExtractTest(
"""
<template>
  <caret><cool-stuck>
    <p>
    Inner Text
    </p>
  </cool-stuck>
</template>
<script>
    import CoolStuck from './OtherName'
    export default {
      components: { CoolStuck }
    }
</script>
"""
,
"""
<template>
    <new-component/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
      components: {NewComponent}
    }
</script>
""",
"""<template>
    <cool-stuck>
        <p>
            Inner Text
        </p>
    </cool-stuck>
</template>
<script>
    import CoolStuck from './OtherName'
    export default {
        name: 'new-component',
        components: {CoolStuck}
    }
</script>""")
  }

  fun testFindNonExistingImport() {
    doExtractTest(
      """
<template>
  <caret><cool-stuck>
    <p>
    Inner Text
    </p>
  </cool-stuck>
</template>
<script>
    import CoolStuck from './OtherName'
    export default {
      components: { CoolStuck }
    }
</script>
"""
      ,
      """
<template>
    <new-component/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
      components: {NewComponent}
    }
</script>
""",
"""<template>
    <cool-stuck>
        <p>
            Inner Text
        </p>
    </cool-stuck>
</template>
<script>
    import CoolStuck from './OtherName'
    export default {
        name: 'new-component',
        components: {CoolStuck}
    }
</script>""")
  }

  fun testExtractWithMemberAccess() {
    doExtractTest(
"""
<template>
    <caret><div v-if="item">
        <p>
            {{ item.kids ? item.descendants + ' comments' : 'No comments yet.' }}
        </p>
    </div>
</template>
<script>
    export default {
        props: { item: {} }
    }
</script>
""",
"""
<template>
    <new-component :item="item"/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
        components: {NewComponent},
        props: { item: {} }
    }
</script>
""",
"""<template>
    <div v-if="item">
        <p>
            {{ item.kids ? item.descendants + ' comments' : 'No comments yet.' }}
        </p>
    </div>
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
  }

  fun testExtractWithStyle() {
    doExtractTest(
"""
<style scoped>
    .example {
        color: red;
    }
</style>
<style>
    .other {}
</style>

<template>
    <caret><div class="example">hi</div>
</template>

<script>
    export default {
        name: "styled"
    }
</script>
""",
"""<template>
    <new-component/>
</template>

<script>
    import NewComponent from "./NewComponent";

    export default {
        name: "styled",
        components: {NewComponent}
    }
</script>
""",
"""<template>
    <div class="example">hi</div>
</template>
<script>
    export default {
        name: 'new-component'
    }
</script>
<style scoped>
    .example {
        color: red;
    }
</style>
"""
    )
  }

  fun testExtractWithStylusStyle() {
    doExtractTest(
"""
<template>
  <transition>
    <caret><svg class="spinner" :class="{ show: show }" v-show="show" width="44px" height="44px" viewBox="0 0 44 44">
      <circle class="path" fill="none" stroke-width="4" stroke-linecap="round" cx="22" cy="22" r="20"></circle>
    </svg>
  </transition>
</template>

<script>
    export default {
        name: 'spinner',
        props: ['show'],
        serverCacheKey: props => props.show
    }
</script>

<style lang="stylus">
${'$'}offset = 126
${'$'}duration = 1.4s

.notUsed
  transition opacity .15s ease

.spinner
  transition opacity .15s ease
  animation rotator ${'$'}duration linear infinite
  animation-play-state paused
  &.show
    animation-play-state running
  &.v-enter, &.v-leave-active
    opacity 0
  &.v-enter-active, &.v-leave
    opacity 1

@keyframes rotator
  0%
    transform scale(0.5) rotate(0deg)
  100%
    transform scale(0.5) rotate(270deg)

.spinner .path
  stroke #ff6600
  stroke-dasharray ${'$'}offset
  stroke-dashoffset 0
  transform-origin center
  animation dash ${'$'}duration ease-in-out infinite

@keyframes dash
  0%
    stroke-dashoffset ${'$'}offset
  50%
    stroke-dashoffset (${'$'}offset/2)
    transform rotate(135deg)
  100%
    stroke-dashoffset ${'$'}offset
    transform rotate(450deg)
</style>
""",
"""
<template>
  <transition>
      <new-component :show="show"/>
  </transition>
</template>

<script>
    import NewComponent from "./NewComponent";

    export default {
        name: 'spinner',
        components: {NewComponent},
        props: ['show'],
        serverCacheKey: props => props.show
    }
</script>

<style lang="stylus">
${'$'}offset = 126
${'$'}duration = 1.4s

@keyframes rotator
  0%
    transform scale(0.5) rotate(0deg)
  100%
    transform scale(0.5) rotate(270deg)

@keyframes dash
  0%
    stroke-dashoffset ${'$'}offset
  50%
    stroke-dashoffset (${'$'}offset/2)
    transform rotate(135deg)
  100%
    stroke-dashoffset ${'$'}offset
    transform rotate(450deg)
</style>
""",
"""<template>
    <svg class="spinner" :class="{ show: show }" v-show="show" width="44px" height="44px" viewBox="0 0 44 44">
        <circle class="path" fill="none" stroke-width="4" stroke-linecap="round" cx="22" cy="22" r="20"></circle>
    </svg>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            show: {}
        }
    }
</script>
<style lang="stylus">
    ${'$'}offset = 126
    ${'$'}duration = 1.4s

    .spinner
        transition opacity .15s ease
        animation rotator ${'$'}duration linear infinite
        animation-play-state paused

    @keyframes rotator
        0%
            transform scale(0.5) rotate(0deg)
        100%
            transform scale(0.5) rotate(270deg)

    .spinner .path
        stroke #ff6600
        stroke-dasharray ${'$'}offset
        stroke-dashoffset 0
        transform-origin center
        animation dash ${'$'}duration ease-in-out infinite

    @keyframes dash
        0%
            stroke-dashoffset ${'$'}offset
        50%
            stroke-dashoffset (${'$'}offset/ 2)
            transform rotate(135deg)
        100%
            stroke-dashoffset ${'$'}offset
            transform rotate(450deg)
</style>""")
  }

  fun testExtractWithScss() {
    doExtractTest(
"""
<template>
    <caret><header class="">
        <h1>TEXT <a class="t-btn" @click="showTools"><span></span></a></h1>
    </header>
</template>
<script>
    export default {
        methods: {
            showTools(){
                this.${'$'}emit('tools');
            }
        }
    }
</script>

<style lang="scss" rel="stylesheet/scss">
    header{
    	position:relative;
        width:100%;
        height:70px;
        z-index:100;
        h1{
            position: relative;
            width:100%;
            max-width:800px;
            margin:0 auto;
            line-height: 70px;
            text-align: center;
            color: #fff;
            a.t-btn{
                position: absolute;
                right:10px;
                top:22px;
                width:30px;
                height:26px;
                cursor: pointer;
            }
            span,span:before,span:after{
                position: absolute;
                left:0;
                width:30px;
                height:4px;
                content: '';
                background: #fff;
            }
            span{
                top:11px;
                &:before{
                    top:0;
                    transform: translateY(-7px);
                    transition: all .3s;
                }
                &:after{
                    transform: translateY(7px);
                    transition: all .3s;
                }
            }
        }
    }
</style>
""",
"""
<template>
    <new-component :show-tools="showTools"/>
</template>
<script>
    import NewComponent from "./NewComponent";

    export default {
        components: {NewComponent},
        methods: {
            showTools(){
                this.${'$'}emit('tools');
            }
        }
    }
</script>

<style lang="scss" rel="stylesheet/scss">
    header{
    	position:relative;
        width:100%;
        height:70px;
        z-index:100;
        h1{
            position: relative;
            width:100%;
            max-width:800px;
            margin:0 auto;
            line-height: 70px;
            text-align: center;
            color: #fff;
            span,span:before,span:after{
                position: absolute;
                left:0;
                width:30px;
                height:4px;
                content: '';
                background: #fff;
            }
            span{
                top:11px;
                &:before{
                    top:0;
                    transform: translateY(-7px);
                    transition: all .3s;
                }
                &:after{
                    transform: translateY(7px);
                    transition: all .3s;
                }
            }
        }
    }
</style>
""",
"""<template>
    <header class="">
        <h1>TEXT <a class="t-btn" @click="showTools"><span></span></a></h1>
    </header>
</template>
<script>
    export default {
        name: 'new-component',
        props: {
            showTools: {}
        }
    }
</script>
<style lang="scss" rel="stylesheet/scss">
    header {
        position: relative;
        width: 100%;
        height: 70px;
        z-index: 100;
        h1 {
            position: relative;
            width: 100%;
            max-width: 800px;
            margin: 0 auto;
            line-height: 70px;
            text-align: center;
            color: #fff;
            span, span:before, span:after {
                position: absolute;
                left: 0;
                width: 30px;
                height: 4px;
                content: '';
                background: #fff;
            }
            span {
                top: 11px;
                &:before {
                    top: 0;
                    transform: translateY(-7px);
                    transition: all .3s;
                }
                &:after {
                    transform: translateY(7px);
                    transition: all .3s;
                }
            }
        }
    }
</style>"""
      )
  }

  fun testExtractSingleTagWithImportInStyle() = doExtractTest(
    """<template>
<selection><p>Paragraph!</p></selection>
</template>
<style lang="stylus">
  @import './stylus/main'
</style>""",

"""<template>
    <new-component/>
</template>
<style lang="stylus">
  @import './stylus/main'
</style>
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
</script>
<style lang="stylus">
    @import './stylus/main'
</style>""")

  private fun doExtractTest(existing: String, modified: String, newText: String?, numTags: Int = 1,
                            newCompName: String = "new-component") {
    JSTestUtils.testES6<Exception>(project, {
      myFixture.configureByText(getTestName(false) + ".vue", existing)

      val element = myFixture.file.findElementAt(myFixture.editor.caretModel.currentCaret.offset)
      TestCase.assertNotNull(element)
      val context = VueExtractComponentIntention.getContext(myFixture.editor, element!!)
      TestCase.assertNotNull(context)
      TestCase.assertEquals(numTags, context!!.size)

      VueExtractComponentRefactoring(myFixture.project, context, myFixture.editor).perform(newCompName)

      myFixture.checkResult(modified)

      if (newText != null) {
        FileDocumentManager.getInstance().saveAllDocuments()
        val created = myFixture.file.parent!!.findFile("NewComponent.vue")
        TestCase.assertNotNull(created)
        myFixture.configureByText("NewComponent2.vue", VfsUtil.loadText(created!!.viewProvider.virtualFile))
        myFixture.checkResult(newText)
      }
    })
  }
}