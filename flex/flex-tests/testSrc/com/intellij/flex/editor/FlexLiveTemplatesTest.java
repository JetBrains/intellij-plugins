package com.intellij.flex.editor;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSLiveTemplatesTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
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
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testClassName() throws Exception {
    runTemplateTest(getTestName(false), "js2", "jsClassName()");
    runTemplateTest(getTestName(false), "mxml", "jsClassName()");
    runTemplateTest(getTestName(false), "as", "jsClassName()");
    runTemplateTest(getTestName(false) + "2", "as", "jsQualifiedClassName()");
  }

  protected void doTest(final String templateName, final String extension) throws Exception {
    doTest(templateName, extension, "ActionScript");
  }

  protected void doTest(final String templateName, final String extension, final Consumer<Integer> segmentHandler) throws Exception {
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

  public void testFlexIter() throws Exception {
    myFixture.setCaresAboutInjection(false);
    final String[] expectedLookupVariants = {"arguments", "paramObj", "localArr", "someGetter", "privateStaticVec", "publicString"};
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(getProject(), 0, "privateStaticVec", expectedLookupVariants));
  }

  public void testFlexItin() throws Exception {
    doTest("itin", "as");
  }

  public void testFlexItar() throws Exception {
    final String[] expectedLookupVariants = {"object", "arguments"};
    doTest("itar", "as", createLookupSelectingSegmentHandler(getProject(), 1, "object", expectedLookupVariants));
  }

  public void testArrayVarNotDuplicatesIndexVarName() throws Exception {
    myFixture.setCaresAboutInjection(false);
    doTest("itar", "mxml", segmentIndex -> {
      if (segmentIndex == 2) {
        myFixture.type("int");
      }
    });
  }

  public void testFlexRitar() throws Exception {
    doTest("ritar", "as", segmentIndex -> {
      if (segmentIndex == 2) {
        myFixture.type("ISomething");
      }
    });
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPickVectorTypeInFlexIter() throws Exception {
    doTest("iter", "as", createLookupSelectingSegmentHandler(getProject(), 0, "bar"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPickVectorTypeInMxmlIter() throws Exception {
    myFixture.setCaresAboutInjection(false);
    setUpJdk();
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(getProject(), 0, "vec"));
  }

  public void testPublicVar() throws Exception {
    doTest("pv", "as");
  }

  public void testPrivateVar() throws Exception {
    doTest("prv", "as");
  }

  public void testPublicStaticVar() throws Exception {
    doTest("psv", "as");
  }

  public void testPrivateStaticVar() throws Exception {
    doTest("prsv", "as");
  }

  public void testPublicFunctionWithTypingInMxml() throws Exception {
    doTest("pf", "mxml", segmentIndex -> {
      final CaretModel caretModel = myFixture.getEditor().getCaretModel();

      switch (segmentIndex) {
        case 0:
          myFixture.type("functionName");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
          break;
        case 1:
          myFixture.type("param:Object");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
          break;
        case 2:
          myFixture.type("String");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
          break;
      }
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPrivateFunction() throws Exception {
    doTest("prf", "as");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPublicStaticFunction() throws Exception {
    doTest("psf", "as");
  }

  public void testPrivateStaticFunctionWithTyping() throws Exception {
    doTest("prsf", "as", segmentIndex -> {
      switch (segmentIndex) {
        case 0:
          myFixture.type("functionName");
          break;
        case 1:
          myFixture.type("param:Object");
          break;
        case 2:
          myFixture.type("String");
          break;
      }
    });
  }

  public void testPublicConst() throws Exception {
    doTest("pc", "as");
  }

  public void testPrivateConst() throws Exception {
    doTest("prc", "as");
  }

  public void testPublicStaticConst() throws Exception {
    doTest("psc", "as");
  }

  public void testPrivateStaticConst() throws Exception {
    doTest("prsc", "as");
  }
}
