// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.testFramework.UsefulTestCase
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.SLOT
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind.*

class VueAttributeNameParserTest : UsefulTestCase() {

  fun testPlainAttributes() {
    expect("foo", "foo", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("foo.a.bbb.c", "foo", PLAIN, setOf("a", "bbb", "c"), injectJS = false, requiresAttributeValue = false)

    expect("foo:bar", "foo:bar", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("foo:bar.a", "foo:bar", PLAIN, setOf("a"), injectJS = false, requiresAttributeValue = false)

    expect("foo-bar", "foo-bar", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("foo-bar.a", "foo-bar", PLAIN, setOf("a"), injectJS = false, requiresAttributeValue = false)
    expect("foo-bar.a.", "foo-bar", PLAIN, setOf("a", ""), injectJS = false, requiresAttributeValue = false)
    expect("foo-bar.", "foo-bar", PLAIN, setOf(""), injectJS = false, requiresAttributeValue = false)
    expect("foo-bar..", "foo-bar", PLAIN, setOf(""), injectJS = false, requiresAttributeValue = false)
    expect("foo-bar..a...b..", "foo-bar", PLAIN, setOf("", "a", "b"), injectJS = false, requiresAttributeValue = false)

    // special attributes
    expect("slot", "slot", SLOT, injectJS = false, requiresAttributeValue = true)
    expect("ref", "ref", REF, injectJS = false, requiresAttributeValue = true)
    expect("slot-scope", "slot-scope", SLOT_SCOPE, injectJS = true, requiresAttributeValue = true)
    expect("scope", "scope", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("scope", "scope", SCOPE, injectJS = true, requiresAttributeValue = true, contextTag = "template")

    // contextual attributes within non valid context
    expect("scoped", "scoped", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("module", "module", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("src", "src", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("functional", "functional", PLAIN, injectJS = false, requiresAttributeValue = true)

    // contextual attributes within valid contexts
    expect("scoped", "scoped", STYLE_SCOPED, injectJS = false, requiresAttributeValue = false,
           contextTag = "style", isTopLevelTag = true)
    expect("module", "module", STYLE_MODULE, injectJS = false, requiresAttributeValue = false,
           contextTag = "style", isTopLevelTag = true)
    expect("functional", "functional", TEMPLATE_FUNCTIONAL, injectJS = false, requiresAttributeValue = false,
           contextTag = "template", isTopLevelTag = true)


    expect("id", "id", PLAIN, injectJS = false, requiresAttributeValue = true)
    expect("id", "id", SCRIPT_ID, injectJS = false, requiresAttributeValue = true,
           contextTag = "script", isTopLevelTag = true)
    expect("id", "id", SCRIPT_ID, injectJS = false, requiresAttributeValue = true,
           contextTag = "script", isTopLevelTag = false)

    expect("src", "src", TEMPLATE_SRC, injectJS = false, requiresAttributeValue = true,
           contextTag = "template", isTopLevelTag = true)
    expect("src", "src", SCRIPT_SRC, injectJS = false, requiresAttributeValue = true,
           contextTag = "script", isTopLevelTag = true)
    expect("src", "src", STYLE_SRC, injectJS = false, requiresAttributeValue = true,
           contextTag = "style", isTopLevelTag = true)

  }

  fun testDirectives() {
    expect("v-foo", "foo", null, CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-foo:arg", "foo", "arg", CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-foo:[arg]", "foo", "[arg]", CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-foo:[arg.a].b", "foo", "[arg.a]", CUSTOM, setOf("b"), injectJS = true, requiresAttributeValue = false)
    expect("v-foo:[arg[a.b].c].d.e", "foo", "[arg[a.b].c]", CUSTOM, setOf("d", "e"), injectJS = true, requiresAttributeValue = false)
    expect("v-foo:[{a: b}].c", "foo", "[{a: b}]", CUSTOM, setOf("c"), injectJS = true, requiresAttributeValue = false)
    expect("v-foo]:[arg.a.b", "foo]", "[arg", CUSTOM, setOf("a", "b"), injectJS = true, requiresAttributeValue = false)

    expect("v-foo-bar:", "foo-bar", "", CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-foo-bar:.a", "foo-bar", "", CUSTOM, setOf("a"), injectJS = true, requiresAttributeValue = false)
    expect("v-foo-bar.a", "foo-bar", null, CUSTOM, setOf("a"), injectJS = true, requiresAttributeValue = false)
    expect("v-foo-bar.a.", "foo-bar", null, CUSTOM, setOf("a", ""), injectJS = true, requiresAttributeValue = false)
    expect("v-foo-bar:..", "foo-bar", "", CUSTOM, setOf(""), injectJS = true, requiresAttributeValue = false)
    expect("v-foo:bar..a...b..", "foo", "bar", CUSTOM, setOf("", "a", "b"), injectJS = true, requiresAttributeValue = false)

    expect("v-@foo", "@foo", null, CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-#foo", "#foo", null, CUSTOM, injectJS = true, requiresAttributeValue = false)
    expect("v-:foo", "", "foo", CUSTOM, injectJS = true, requiresAttributeValue = false)

    // Special directives
    expect("v-bind:a", "bind", "a", BIND, injectJS = true, requiresAttributeValue = true)
    expect(":a", "bind", "a", BIND, injectJS = true, requiresAttributeValue = true, isShorthand = true)
    expect(":", "bind", "", BIND, injectJS = true, requiresAttributeValue = true, isShorthand = true)

    expect("v-on:a", "on", "a", ON, injectJS = true, requiresAttributeValue = true)
    expect("@a", "on", "a", ON, injectJS = true, requiresAttributeValue = true, isShorthand = true)
    expect("@", "on", "", ON, injectJS = true, requiresAttributeValue = true, isShorthand = true)
    expect("@a.native", "on", "a", ON, setOf("native"), injectJS = true, requiresAttributeValue = false, isShorthand = true)

    expect("v-else-if", "else-if", null, ELSE_IF, injectJS = true, requiresAttributeValue = true)
    expect("v-for", "for", null, FOR, injectJS = true, requiresAttributeValue = true)
    expect("v-html", "html", null, HTML, injectJS = true, requiresAttributeValue = true)
    expect("v-if", "if", null, IF, injectJS = true, requiresAttributeValue = true)
    expect("v-model", "model", null, MODEL, injectJS = true, requiresAttributeValue = true)
    expect("v-show", "show", null, SHOW, injectJS = true, requiresAttributeValue = true)
    expect("v-text", "text", null, TEXT, injectJS = true, requiresAttributeValue = true)

    expect("v-slot", "slot", null, VueDirectiveKind.SLOT, injectJS = true, requiresAttributeValue = false)
    expect("#header", "slot", "header", VueDirectiveKind.SLOT, injectJS = true, requiresAttributeValue = false, isShorthand = true)
    expect("#", "slot", "", VueDirectiveKind.SLOT, injectJS = true, requiresAttributeValue = false, isShorthand = true)

    // No attribute value directives
    expect("v-cloak", "cloak", null, CLOAK, injectJS = false, requiresAttributeValue = false)
    expect("v-else", "else", null, ELSE, injectJS = false, requiresAttributeValue = false)
    expect("v-once", "once", null, ONCE, injectJS = false, requiresAttributeValue = false)
    expect("v-pre", "pre", null, PRE, injectJS = false, requiresAttributeValue = false)

  }

  private fun expect(attributeName: String,
                     name: String,
                     arguments: String?,
                     directiveKind: VueDirectiveKind,
                     modifiers: Set<String> = emptySet(),
                     injectJS: Boolean,
                     requiresAttributeValue: Boolean,
                     isShorthand: Boolean = false) {
    expect(attributeName, name, DIRECTIVE, modifiers, injectJS, requiresAttributeValue)
    val info = VueAttributeNameParser.parse(attributeName) as VueDirectiveInfo
    assertEquals("$attributeName - wrong directive kind", directiveKind, info.directiveKind)
    assertEquals("$attributeName - wrong directive arguments", arguments, info.arguments)
    assertEquals("$attributeName - wrong isShorthand value", isShorthand, info.isShorthand)
  }

  private fun expect(attributeName: String,
                     name: String,
                     attributeKind: VueAttributeKind,
                     modifiers: Set<String> = emptySet(),
                     injectJS: Boolean,
                     requiresAttributeValue: Boolean,
                     contextTag: String? = null,
                     isTopLevelTag: Boolean = false) {
    val info = VueAttributeNameParser.parse(attributeName, contextTag, isTopLevelTag)
    assertEquals("$attributeName - wrong parsed name", name, info.name)
    assertEquals("$attributeName - wrong kind", attributeKind, info.kind)
    assertEquals("$attributeName - wrong modifiers", modifiers, info.modifiers)
    assertEquals("$attributeName - wrong injectJS value", injectJS, info.injectJS)
    assertEquals("$attributeName - wrong requires attr value", requiresAttributeValue, info.requiresValue)
  }

}
