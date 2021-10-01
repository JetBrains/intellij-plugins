// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@TestDataPath("$R#_COMPLETION_TEST_ROOT/Angular2Html")
public class Angular2HtmlCodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "test003", // missing [style. support
    "test004", // missing [style. support
    "test007", // differences in standard HTML attributes and missing *directive items
    "test008", // MathML items in content assist
    "test009"  // missing [style. support
  );

  private static final Set<String> HIGH_PRIORITY_ONLY = ContainerUtil.newHashSet(
    "test010",
    "test011",
    "test012",
    "test013",
    "test014",
    "test015"
  );

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    extraFiles.add("style.css");
    return extraFiles;
  }

  @Override
  protected boolean isExcluded() {
    return TESTS_TO_SKIP.contains(getName());
  }

  @Override
  protected void doSingleTest(@NotNull String testFile, @NotNull String path) throws Exception {
    myFixture.copyFileToProject("../../package.json", "package.json");
    super.doSingleTest(testFile, path);
  }

  @Override
  protected boolean shouldSkipItem(@NotNull LookupElement element) {
    if (HIGH_PRIORITY_ONLY.contains(getName())) {
      return !(element instanceof PrioritizedLookupElement)
             || ((PrioritizedLookupElement<?>)element).getPriority() < Angular2AttributeDescriptor.AttributePriority.HIGH.getValue();
    }
    if (IGNORED_ELEMENT_ATTRS.contains(element.getLookupString())
        || element.getLookupString().contains("aria-")) {
      return true;
    }
    return super.shouldSkipItem(element);
  }

  private static final Set<String> IGNORED_ELEMENT_ATTRS = ContainerUtil.newHashSet(
    "[accessKey]", "[classList]", "[className]", "[contentEditable]", "[dir]", "[draggable]", "[hidden]", "[id]", "[innerHTML]",
    "[innerText]", "[lang]", "[outerHTML]", "[outerText]", "[scrollLeft]", "[scrollTop]", "[slot]", "[spellcheck]", "[style]", "[tabIndex]",
    "[textContent]", "[title]", "[translate]", "[type]", "[value]", "[charset]", "[coords]", "[download]", "[hash]", "[host]", "[hostname]",
    "[href]", "[hreflang]", "[name]", "[password]", "[pathname]", "[ping]", "[port]", "[protocol]", "[referrerPolicy]", "[rel]", "[rev]",
    "[search]", "[shape]", "[target]", "[text]", "[username]", "(abort)", "(autocomplete)", "(autocompleteerror)", "(auxclick)",
    "(beforecopy)", "(beforecut)", "(beforepaste)", "(blur)", "(cancel)", "(canplay)", "(canplaythrough)", "(change)", "(click)", "(close)",
    "(contextmenu)", "(copy)", "(cuechange)", "(cut)", "(dblclick)", "(drag)", "(dragend)", "(dragenter)", "(dragexit)", "(dragleave)",
    "(dragover)", "(dragstart)", "(drop)", "(durationchange)", "(emptied)", "(ended)", "(error)", "(focus)", "(gotpointercapture)",
    "(input)", "(invalid)", "(keydown)", "(keypress)", "(keyup)", "(load)", "(loadeddata)", "(loadedmetadata)", "(loadstart)",
    "(lostpointercapture)", "(message)", "(mousedown)", "(mouseenter)", "(mouseleave)", "(mousemove)", "(mouseout)", "(mouseover)",
    "(mouseup)", "(mousewheel)", "(mozfullscreenchange)", "(mozfullscreenerror)", "(mozpointerlockchange)", "(mozpointerlockerror)",
    "(paste)", "(pause)", "(play)", "(playing)", "(pointercancel)", "(pointerdown)", "(pointerenter)", "(pointerleave)", "(pointermove)",
    "(pointerout)", "(pointerover)", "(pointerup)", "(progress)", "(ratechange)", "(reset)", "(resize)", "(scroll)", "(search)", "(seeked)",
    "(seeking)", "(select)", "(selectstart)", "(show)", "(sort)", "(stalled)", "(submit)", "(suspend)", "(timeupdate)", "(toggle)",
    "(volumechange)", "(waiting)", "(webglcontextcreationerror)", "(webglcontextlost)", "(webglcontextrestored)",
    "(webkitfullscreenchange)", "(webkitfullscreenerror)", "(wheel)",
    //RDFa Datatypes
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
    "inlist", "inlist]",
    //XML stuff
    "base", "base]"
  );
}
