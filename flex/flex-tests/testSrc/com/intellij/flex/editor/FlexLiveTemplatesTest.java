package com.intellij.flex.editor;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSLiveTemplatesTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.util.Consumer;

import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

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
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    TemplateManagerImpl.setTemplateTesting(getProject(), getTestRootDisposable());
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void testClassName() throws Exception {
    runTemplateTest(getTestName(false), "js2", "jsClassName()");
    runTemplateTest(getTestName(false), "mxml", "jsClassName()");
    runTemplateTest(getTestName(false), "as", "jsClassName()");
    runTemplateTest(getTestName(false) + "2", "as", "jsQualifiedClassName()");
  }

  protected void doTest(final String templateName, final String extension, final String group) throws Exception {
    final Template template = TemplateSettings.getInstance().getTemplate(templateName, group);
    //noinspection unchecked
    doTest(template, Consumer.EMPTY_CONSUMER, getBasePath() + getTestName(false) + "." + extension);
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
    final String[] expectedLookupVariants = {"arguments", "paramObj", "localArr", "someGetter", "privateStaticVec", "publicString"};
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(myProject, 0, "privateStaticVec", expectedLookupVariants));
  }

  public void testFlexItin() throws Exception {
    doTest("itin", "as");
  }

  public void testFlexItar() throws Exception {
    final String[] expectedLookupVariants = {"object", "arguments"};
    doTest("itar", "as", createLookupSelectingSegmentHandler(myProject, 1, "object", expectedLookupVariants));
  }

  public void testArrayVarNotDuplicatesIndexVarName() throws Exception {
    doTest("itar", "mxml", segmentIndex -> {
      if (segmentIndex == 2) {
        type("int");
      }
    });
  }

  public void testFlexRitar() throws Exception {
    doTest("ritar", "as", segmentIndex -> {
      if (segmentIndex == 2) {
        type("ISomething");
      }
    });
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPickVectorTypeInFlexIter() throws Exception {
    doTest("iter", "as", createLookupSelectingSegmentHandler(myProject, 0, "bar"));
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPickVectorTypeInMxmlIter() throws Exception {
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(myProject, 0, "vec"));
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
      final CaretModel caretModel = getEditor().getCaretModel();

      switch (segmentIndex) {
        case 0:
          type("functionName");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
          break;
        case 1:
          type("param:Object");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
          break;
        case 2:
          type("String");
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
          type("functionName");
          break;
        case 1:
          type("param:Object");
          break;
        case 2:
          type("String");
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
