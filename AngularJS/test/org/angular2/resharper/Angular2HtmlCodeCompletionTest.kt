// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.TestDataPath
import com.intellij.util.containers.ContainerUtil
import com.intellij.webSymbols.WebSymbol

@TestDataPath("\$R#_COMPLETION_TEST_ROOT/Angular2Html")
class Angular2HtmlCodeCompletionTest : Angular2ReSharperCompletionTestBase() {
  override fun doGetExtraFiles(): MutableList<String?> {
    val extraFiles = super.doGetExtraFiles()
    extraFiles!!.add("style.css")
    return extraFiles
  }

  override fun isExcluded(): Boolean {
    return TESTS_TO_SKIP.contains(name)
  }

  @Throws(Exception::class)
  override fun doSingleTest(testFile: String, path: String) {
    Registry.get("ide.completion.variant.limit").setValue(2000, myFixture.testRootDisposable)
    myFixture.copyFileToProject("../../package.json", "package.json")
    super.doSingleTest(testFile, path)
  }

  override fun shouldSkipItem(element: LookupElement): Boolean {
    if (HIGH_PRIORITY_ONLY.contains(name)) {
      return (element !is PrioritizedLookupElement<*>
              || element.priority < WebSymbol.Priority.HIGH.value)
    }
    val lookupString = element.getLookupString()
    return if (IGNORED_ELEMENT_ATTRS.contains(lookupString)
               || lookupString.contains("aria-") // CSS props
               || lookupString.startsWith("-moz-")
               || lookupString.startsWith("-webkit-")
               || lookupString.startsWith("-ms-")
               || lookupString.startsWith("-o-")
               || lookupString.startsWith("mso-")) {
      true
    }
    else super.shouldSkipItem(element)
  }

  companion object {
    private val TESTS_TO_SKIP: Set<String> = ContainerUtil.newHashSet(
      "test007",  // differences in standard HTML attributes and missing *directive items
      "test008",  // MathML items in content assist
      "test009" // Priority filter problems
    )
    private val HIGH_PRIORITY_ONLY: Set<String> = ContainerUtil.newHashSet(
      "test010",
      "test011",
      "test012",
      "test013",
      "test014",
      "test015"
    )
    private val IGNORED_ELEMENT_ATTRS: Set<String> = ContainerUtil.newHashSet(
      "[accessKey]", "[classList]", "[className]", "[contentEditable]", "[dir]", "[draggable]", "[hidden]", "[id]", "[innerHTML]",
      "[innerText]", "[lang]", "[outerHTML]", "[outerText]", "[scrollLeft]", "[scrollTop]", "[slot]", "[spellcheck]", "[style]",
      "[tabIndex]",
      "[textContent]", "[title]", "[translate]", "[type]", "[value]", "[charset]", "[coords]", "[download]", "[hash]", "[host]",
      "[hostname]",
      "[href]", "[hreflang]", "[name]", "[password]", "[pathname]", "[ping]", "[port]", "[protocol]", "[referrerPolicy]", "[rel]", "[rev]",
      "[search]", "[shape]", "[target]", "[text]", "[username]", "(abort)", "(autocomplete)", "(autocompleteerror)", "(auxclick)",
      "(beforecopy)", "(beforecut)", "(beforepaste)", "(blur)", "(cancel)", "(canplay)", "(canplaythrough)", "(change)", "(click)",
      "(close)",
      "(contextmenu)", "(copy)", "(cuechange)", "(cut)", "(dblclick)", "(drag)", "(dragend)", "(dragenter)", "(dragexit)", "(dragleave)",
      "(dragover)", "(dragstart)", "(drop)", "(durationchange)", "(emptied)", "(ended)", "(error)", "(focus)", "(gotpointercapture)",
      "(input)", "(invalid)", "(keydown)", "(keypress)", "(keyup)", "(load)", "(loadeddata)", "(loadedmetadata)", "(loadstart)",
      "(lostpointercapture)", "(message)", "(mousedown)", "(mouseenter)", "(mouseleave)", "(mousemove)", "(mouseout)", "(mouseover)",
      "(mouseup)", "(mousewheel)", "(mozfullscreenchange)", "(mozfullscreenerror)", "(mozpointerlockchange)", "(mozpointerlockerror)",
      "(paste)", "(pause)", "(play)", "(playing)", "(pointercancel)", "(pointerdown)", "(pointerenter)", "(pointerleave)", "(pointermove)",
      "(pointerout)", "(pointerover)", "(pointerup)", "(progress)", "(ratechange)", "(reset)", "(resize)", "(scroll)", "(search)",
      "(seeked)",
      "(seeking)", "(select)", "(selectstart)", "(show)", "(sort)", "(stalled)", "(submit)", "(suspend)", "(timeupdate)", "(toggle)",
      "(volumechange)", "(waiting)", "(webglcontextcreationerror)", "(webglcontextlost)", "(webglcontextrestored)",
      "(webkitfullscreenchange)", "(webkitfullscreenerror)", "(wheel)",  //RDFa Datatypes
      "about", "about]",
      "prefix", "prefix]",
      "property", "property]",
      "typeof", "typeof]",
      "vocab", "vocab]",
      "content", "content]",
      "datatype", "datatype]",
      "rel", "rel]",
      "resource", "resource]",
      "rev", "rev]",
      "inlist", "inlist]",  //XML stuff
      "base", "base]"
    )
  }
}
