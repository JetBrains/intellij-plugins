// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.application.options.CodeStyle
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.rearranger.CssRearranger
import com.intellij.psi.codeStyle.arrangement.AbstractRearrangerTest
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueRearrangerTest : AbstractRearrangerTest() {
  override fun setUp() {
    super.setUp()
    fileType = VueFileType.INSTANCE
    language = VueLanguage.INSTANCE
  }

  fun testTypescript() {
    doTestWithDefaultSettings(
      """<script lang="ts">
    class F {
        private privateMethod2() {
        }

        protected protectedMethod2() {
        }

        constructor() {
        }

        field12: number;
        field22: number;

        public publicMethod2() {
        }
    }
</script>""", """<script lang="ts">
    class F {
        field12: number;
        field22: number;

        constructor() {
        }

        public publicMethod2() {
        }

        protected protectedMethod2() {
        }

        private privateMethod2() {
        }
    }
</script>""")
  }

  fun testES6() {
    doTestWithDefaultSettings(
      """<script>
    class F {

        constructor() {
        }

        publicMethod() {
        }

        y = 1
        z = 1
    }
</script>
""", """<script>
    class F {

        y = 1
        z = 1

        constructor() {
        }

        publicMethod() {
        }
    }
</script>
""")
  }

  fun testWithMultipleTagsAndAttributes() {
    CodeStyle.getSettings(myFixture.project)
      .getCommonSettings(CSSLanguage.INSTANCE)
      .setArrangementSettings(StdArrangementSettings.createByMatchRules(emptyList(), listOf(CssRearranger.SORT_BY_NAME_RULE)))

    doTestWithDefaultSettings(
      """<script zzz="zzz" lang="ts" aaa="aaa">
    class Component {
        private privateMethod2() {
        }

        public publicMethod2() {
        }
    }
</script>

<style>
    .someClass {
        z-index: 1000;
        background-color: red;
    }
</style>
<body>

<div zzz="zzz" aaa="aaa">
    <script>
        class ALocal {
            main() {
            }

            constructor() {
            }
        }
    </script>
</div>
</body>""", """<script aaa="aaa" lang="ts" zzz="zzz">
    class Component {
        public publicMethod2() {
        }

        private privateMethod2() {
        }
    }
</script>

<style>
    .someClass {
        background-color: red;
        z-index: 1000;
    }
</style>
<body>

<div aaa="aaa" zzz="zzz">
    <script>
        class ALocal {
            constructor() {
            }

            main() {
            }
        }
    </script>
</div>
</body>""")
  }

  fun testDefaultVueAttributesSorting() {
    doTestWithDefaultSettings(
      """
        <template>
          <div
            v-text=""
            v-html=""
            v-bindi=""
            v-bind=""
            v-on=""
            v-oni=""
            v-on:click=""
            @dblclick=""
            foo=""
            vbar=""
            v-foo=""
            v-htmli=""
            v-model=""
            v-bind:ref=""
            :ref=""
            ref=""
            key=""
            :key=""
            v-bind:key=""
            slot=""
            :slot=""
            slot-scope=""
            :slot-scope=""
            v-slot=""
            id=""
            :id=""
            v-bind:id=""
            v-pre=""
            v-once=""
            v-if=""
            v-else-if=""
            v-else=""
            v-show=""
            v-cloak=""
            v-for=""
            :is=""
            v-is=""
            v-bind:is=""
            v-bind:foo=""
            :foo=""
          />
        </template>
      """,
      """
        <template>
          <div
            :is=""
            v-bind:is=""
            v-is=""
            v-for=""
            v-cloak=""
            v-else=""
            v-else-if=""
            v-if=""
            v-show=""
            v-once=""
            v-pre=""
            :id=""
            id=""
            v-bind:id=""
            :key=""
            :ref=""
            :slot=""
            :slot-scope=""
            key=""
            ref=""
            slot=""
            slot-scope=""
            v-bind:key=""
            v-bind:ref=""
            v-slot=""
            v-model=""
            v-bindi=""
            v-foo=""
            v-htmli=""
            v-oni=""
            :foo=""
            foo=""
            v-bind=""
            v-bind:foo=""
            vbar=""
            @dblclick=""
            v-on=""
            v-on:click=""
            v-html=""
            v-text=""
          />
        </template>
      """
    )
  }

  private fun doTestWithDefaultSettings(before: String, expected: String) {
    doTestWithSettings(before, expected, null, null)
  }
}
