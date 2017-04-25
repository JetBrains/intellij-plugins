package com.intellij.flex.refactoring;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.introduceConstant.IntroduceConstantInfoProvider;
import com.intellij.lang.javascript.refactoring.introduceConstant.JSIntroduceConstantHandler;
import com.intellij.lang.javascript.refactoring.introduceConstant.JSIntroduceConstantSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexIntroduceConstantTest extends CodeInsightTestCase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceConstant/");
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  private void doTest(final String varName,
                      final boolean replaceAll,
                      final JSAttributeList.AccessType accessType,
                      String fileName,
                      String ext) throws Exception {
    doTest(varName, replaceAll, accessType, null, fileName, ext);
  }

  private void doTest(final String varName,
                      final boolean replaceAll,
                      final JSAttributeList.AccessType accessType,
                      final String className,
                      String fileName,
                      String ext) throws Exception {
    doTest(new JSIntroduceConstantHandler() {
      @Override
      protected JSIntroduceConstantSettings getSettings(Project project,
                                                        Editor editor,
                                                        Pair<JSExpression, TextRange> expressionDescriptor,
                                                        JSExpression[] occurrences, PsiElement scope) {
        return new JSIntroduceConstantSettings() {
          @Override
          public JSAttributeList.AccessType getAccessType() {
            return accessType;
          }

          @Override
          public String getClassName() {
            return className;
          }

          @Override
          public boolean isReplaceAllOccurrences() {
            return replaceAll;
          }

          @Override
          public String getVariableName() {
            return varName;
          }

          @Override
          public String getVariableType() {
            return null;
          }
        };
      }
    }, fileName, ext);
  }

  private void doTest(final JSIntroduceConstantHandler handler, String fileName, String ext) throws Exception {
    configureByFile(fileName + "." + ext);
    Editor injectedEditor = BaseCodeInsightAction.getInjectedEditor(myProject, myEditor);
    if (injectedEditor != null) {
      myEditor = injectedEditor;
      myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    }
    handler.invoke(getProject(), getEditor(), getFile(), null);
    if (injectedEditor instanceof EditorWindow) {
      myEditor = ((EditorWindow)injectedEditor).getDelegate();
      myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    }
    checkResultByFile(fileName + "_after." + ext);
  }

  private void doTest(String varName, String fileName, String ext) throws Exception {
    doTest(varName, true, JSAttributeList.AccessType.PUBLIC, fileName, ext);
  }

  public void testBasic() throws Exception {
    doTest("created", getTestName(false), "js2");
  }

  public void testBasic2() throws Exception {
    doTest("created", getTestName(false), "js2");
  }

  public void testInMxml() throws Exception {
    doTest("created", getTestName(false), "mxml");
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testInMxml2() throws Exception {
    doTest("created", getTestName(false), "mxml");
  }

  public void testModifier() throws Exception {
    doTest("created", false, JSAttributeList.AccessType.PRIVATE, getTestName(false), "js2");
  }

  public void testCannotIntroduce() throws Exception {
    try {
      doTest("created", false, JSAttributeList.AccessType.PRIVATE, getTestName(false), "js2");
      assertFalse(true);
    }
    catch (RuntimeException ex) {
      assertEquals(ex.getMessage(), JSBundle.message("javascript.introduce.constant.error.not.constant.expression.selected"));
    }
  }

  public void testNiceNameWhenIntroducingFromLiteral() throws Exception {
    doNiceNameTest(0);
  }

  public void testNiceNameWhenIntroducingFromLiteral_2() throws Exception {
    doNiceNameTest(3);
  }

  private void doNiceNameTest(final int i) throws Exception {
    doTest(new JSIntroduceConstantHandler() {
      @Override
      protected JSIntroduceConstantSettings getSettings(Project project,
                                                        Editor editor,
                                                        Pair<JSExpression, TextRange> expressionDescriptor,
                                                        JSExpression[] occurrences, PsiElement scope) {
        final String[] strings = new IntroduceConstantInfoProvider(expressionDescriptor.first, occurrences, scope).suggestCandidateNames();
        assertTrue(strings != null && i < strings.length);
        return new JSIntroduceConstantSettings() {
          @Override
          public JSAttributeList.AccessType getAccessType() {
            return JSAttributeList.AccessType.PRIVATE;
          }

          @Override
          public String getClassName() {
            return null;
          }

          @Override
          public boolean isReplaceAllOccurrences() {
            return false;
          }

          @Override
          public String getVariableName() {
            return strings[i];
          }

          @Override
          public String getVariableType() {
            return null;
          }
        };
      }
    }, getTestName(false), "js2");
  }

  public void testIntroduceConstantInDifferentClass() throws Exception {
    doTest("XXX", true, JSAttributeList.AccessType.PUBLIC, "B", getTestName(false), "js2");
  }

  public void testIntroduceConstantInDifferentClass_2() throws Exception {
    doTest("XXX", true, JSAttributeList.AccessType.PUBLIC, "A", getTestName(false), "js2");
  }

  public void testIntroduceConstantInDifferentClass_3() throws Exception {
    doTest("XXX", true, JSAttributeList.AccessType.PACKAGE_LOCAL, "foo.B", getTestName(false), "js2");
  }

  public void testIntroduceConstantInDifferentClass_4() throws Exception {
    doTest("XXX", true, JSAttributeList.AccessType.PACKAGE_LOCAL, "foo.B", getTestName(false), "js2");
  }
}
