package com.intellij.flex.editor;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSLiveTemplatesTestBase;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.Consumer;

import java.util.List;

public class FlexLiveTemplatesTest extends JSLiveTemplatesTestBase {

  @Override
  protected String getBasePath() {
    return "/flexLiveTemplates/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testClassName() {
    runTemplateTest(getTestName(false), "js2", "jsClassName()");
    runTemplateTest(getTestName(false), "mxml", "jsClassName()");
    runTemplateTest(getTestName(false), "as", "jsClassName()");
    runTemplateTest(getTestName(false) + "2", "as", "jsQualifiedClassName()");
  }

  protected void doTest(final String templateName, final String extension) {
    doTest(templateName, extension, "ActionScript");
  }

  protected void doTest(final String templateName, final String extension, final Consumer<Integer> segmentHandler) {
    final Template template = TemplateSettings.getInstance().getTemplate(templateName, "ActionScript");
    doTest(template, segmentHandler, getBasePath() + getTestName(false) + "." + extension);
  }

  private static Consumer<Integer> createLookupSelectingSegmentHandler(final Project project,
                                                                       final int segmentIndex,
                                                                       final String whatToSelectAtThisSegment,
                                                                       final String... expectedLookupVariants) {
    return currentSegmentIndex -> {
      if (currentSegmentIndex == segmentIndex) {
        final LookupEx lookup = LookupManager.getInstance(project).getActiveLookup();
        if (lookup != null) {
          LookupElement selected = null;

          final List<LookupElement> items = lookup.getItems();
          final String[] lookupStrings = new String[items.size()];
          for (int i = 0; i < items.size(); i++) {
            lookupStrings[i] = items.get(i).getLookupString();
            if (whatToSelectAtThisSegment.equals(lookupStrings[i])) {
              selected = items.get(i);
            }
          }

          if (expectedLookupVariants.length > 0) {
            assertSameElements(lookupStrings, expectedLookupVariants);
          }

          if (selected != null) {
            lookup.setCurrentItem(selected);
            ((LookupImpl)lookup).finishLookup(Lookup.NORMAL_SELECT_CHAR);
          }
        }
      }
    };
  }

  public void testFlexIter() {
    myFixture.setCaresAboutInjection(false);
    final String[] expectedLookupVariants = {"arguments", "paramObj", "localArr", "someGetter", "privateStaticVec", "publicString"};
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(getProject(), 0, "privateStaticVec", expectedLookupVariants));
  }

  public void testFlexItin() {
    doTest("itin", "as");
  }

  public void testFlexItar() {
    final String[] expectedLookupVariants = {"object", "arguments"};
    doTest("itar", "as", createLookupSelectingSegmentHandler(getProject(), 1, "object", expectedLookupVariants));
  }

  public void testArrayVarNotDuplicatesIndexVarName() {
    myFixture.setCaresAboutInjection(false);
    doTest("itar", "mxml", segmentIndex -> {
      if (segmentIndex == 2) {
        myFixture.type("int");
      }
    });
  }

  public void testFlexRitar() {
    doTest("ritar", "as", segmentIndex -> {
      if (segmentIndex == 2) {
        myFixture.type("ISomething");
      }
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPickVectorTypeInFlexIter() {
    doTest("iter", "as", createLookupSelectingSegmentHandler(getProject(), 0, "bar"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPickVectorTypeInMxmlIter() {
    myFixture.setCaresAboutInjection(false);
    setUpJdk();
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(getProject(), 0, "vec"));
  }

  public void testPublicVar() {
    doTest("pv", "as");
  }

  public void testPrivateVar() {
    doTest("prv", "as");
  }

  public void testPublicStaticVar() {
    doTest("psv", "as");
  }

  public void testPrivateStaticVar() {
    doTest("prsv", "as");
  }

  public void testPublicFunctionWithTypingInMxml() {
    doTest("pf", "mxml", segmentIndex -> {
      final CaretModel caretModel = myFixture.getEditor().getCaretModel();

      switch (segmentIndex) {
        case 0 -> {
          myFixture.type("functionName");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
        case 1 -> {
          myFixture.type("param:Object");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
        case 2 -> {
          myFixture.type("String");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
      }
    });
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testPrivateFunction() {
    doTest("prf", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testPublicStaticFunction() {
    doTest("psf", "as");
  }

  public void testPrivateStaticFunctionWithTyping() {
    doTest("prsf", "as", segmentIndex -> {
      switch (segmentIndex) {
        case 0 -> myFixture.type("functionName");
        case 1 -> myFixture.type("param:Object");
        case 2 -> myFixture.type("String");
      }
    });
  }

  public void testPublicConst() {
    doTest("pc", "as");
  }

  public void testPrivateConst() {
    doTest("prc", "as");
  }

  public void testPublicStaticConst() {
    doTest("psc", "as");
  }

  public void testPrivateStaticConst() {
    doTest("prsc", "as");
  }
}
