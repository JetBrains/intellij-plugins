package com.intellij.flex.editor;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSLiveTemplatesFixture;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.intellij.lang.javascript.JSCodeInsightFixture.assertSameElements;
import static com.intellij.lang.javascript.JSLiveTemplatesFixture.jsLiveTemplatesFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexLiveTemplatesTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSLiveTemplatesFixture> liveTemplates = jsLiveTemplatesFixture(
    projectFixture,
    new JSFixtureOptions(FlexTestUtils.getTestDataPath(""))
      .withBasePath("/flexLiveTemplates/")
      .withModuleType(FlexModuleType.MODULE_TYPE_ID)
  );

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(liveTemplates.get().getTestRootDisposable(), "");
  }

  private void setUpJdk() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    FlexTestUtils.setupFlexSdk(templatesFixture.getFixture().getModule(), templatesFixture.getTestName(false), FlexLiveTemplatesTest.class,
                               templatesFixture.getTestRootDisposable());
  }

  @Test
  public void testClassName() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    String testName = templatesFixture.getTestName(false);
    templatesFixture.runTemplateTest(testName, "js2", "jsClassName()");
    templatesFixture.runTemplateTest(testName, "mxml", "jsClassName()");
    templatesFixture.runTemplateTest(testName, "as", "jsClassName()");
    templatesFixture.runTemplateTest(testName + "2", "as", "jsQualifiedClassName()");
  }

  @Test
  public void testNoEmmetInMxmlScript() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    CodeInsightTestFixture myFixture = templatesFixture.getFixture();
    String path = templatesFixture.getBasePath() + templatesFixture.getTestName(false);
    EdtTestUtil.runInEdtAndWait(() -> {
      myFixture.configureByFile(path + ".mxml");
      WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> {
        TemplateManager.getInstance(myFixture.getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
      });
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
      myFixture.checkResultByFile(path + "_after.mxml");
    });
  }

  private void doTest(final String templateName, final String extension) {
    liveTemplates.get().doTest(templateName, extension, "ActionScript");
  }

  private void doTest(final String templateName, final String extension, final Consumer<Integer> segmentHandler) {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    final Template template = TemplateSettings.getInstance().getTemplate(templateName, "ActionScript");
    String file = templatesFixture.getBasePath() + templatesFixture.getTestName(false) + "." + extension;
    templatesFixture.doTest(template, segmentHandler, file);
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
            assertSameElements(Arrays.asList(lookupStrings), expectedLookupVariants);
          }

          if (selected != null) {
            lookup.setCurrentItem(selected);
            ((LookupImpl)lookup).finishLookup(Lookup.NORMAL_SELECT_CHAR);
          }
        }
      }
    };
  }

  @Test
  public void testFlexIter() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    templatesFixture.getFixture().setCaresAboutInjection(false);
    final String[] expectedLookupVariants = {"arguments", "paramObj", "localArr", "someGetter", "privateStaticVec", "publicString"};
    doTest("iter", "mxml",
           createLookupSelectingSegmentHandler(templatesFixture.getProject(), 0, "privateStaticVec", expectedLookupVariants));
  }

  @Test
  public void testFlexItin() {
    doTest("itin", "as");
  }

  @Test
  public void testFlexItar() {
    final String[] expectedLookupVariants = {"object", "arguments"};
    doTest("itar", "as", createLookupSelectingSegmentHandler(liveTemplates.get().getProject(), 1, "object", expectedLookupVariants));
  }

  @Test
  public void testArrayVarNotDuplicatesIndexVarName() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    templatesFixture.getFixture().setCaresAboutInjection(false);
    doTest("itar", "mxml", segmentIndex -> {
      if (segmentIndex == 2) {
        templatesFixture.getFixture().type("int");
      }
    });
  }

  @Test
  public void testFlexRitar() {
    doTest("ritar", "as", segmentIndex -> {
      if (segmentIndex == 2) {
        liveTemplates.get().getFixture().type("ISomething");
      }
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testPickVectorTypeInFlexIter() {
    doTest("iter", "as", createLookupSelectingSegmentHandler(liveTemplates.get().getProject(), 0, "bar"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testPickVectorTypeInMxmlIter() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    templatesFixture.getFixture().setCaresAboutInjection(false);
    setUpJdk();
    doTest("iter", "mxml", createLookupSelectingSegmentHandler(templatesFixture.getProject(), 0, "vec"));
  }

  @Test
  public void testPublicVar() {
    doTest("pv", "as");
  }

  @Test
  public void testPrivateVar() {
    doTest("prv", "as");
  }

  @Test
  public void testPublicStaticVar() {
    doTest("psv", "as");
  }

  @Test
  public void testPrivateStaticVar() {
    doTest("prsv", "as");
  }

  @Test
  public void testPublicFunctionWithTypingInMxml() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    doTest("pf", "mxml", segmentIndex -> {
      final CaretModel caretModel = templatesFixture.getEditor().getCaretModel();

      switch (segmentIndex) {
        case 0 -> {
          templatesFixture.getFixture().type("functionName");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
        case 1 -> {
          templatesFixture.getFixture().type("param:Object");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
        case 2 -> {
          templatesFixture.getFixture().type("String");
          caretModel.moveToOffset(caretModel.getOffset() + 1);
        }
      }
    });
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPrivateFunction() {
    doTest("prf", "as");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testPublicStaticFunction() {
    doTest("psf", "as");
  }

  @Test
  public void testPrivateStaticFunctionWithTyping() {
    JSLiveTemplatesFixture templatesFixture = liveTemplates.get();
    doTest("prsf", "as", segmentIndex -> {
      switch (segmentIndex) {
        case 0 -> templatesFixture.getFixture().type("functionName");
        case 1 -> templatesFixture.getFixture().type("param:Object");
        case 2 -> templatesFixture.getFixture().type("String");
      }
    });
  }

  @Test
  public void testPublicConst() {
    doTest("pc", "as");
  }

  @Test
  public void testPrivateConst() {
    doTest("prc", "as");
  }

  @Test
  public void testPublicStaticConst() {
    doTest("psc", "as");
  }

  @Test
  public void testPrivateStaticConst() {
    doTest("prsc", "as");
  }
}
