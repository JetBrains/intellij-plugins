package com.jetbrains.lang.dart.ide.info;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartParameterInfoTest extends LightPlatformCodeInsightTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/paramInfo/");
  }

  private void doTest(String infoText, int highlightedParameterIndex) throws Exception {
    configureByFile(getTestName(false) + "" + DartFileType.DEFAULT_EXTENSION);

    final DartParameterInfoHandler parameterInfoHandler = new DartParameterInfoHandler();
    MockCreateParameterInfoContext createContext = new MockCreateParameterInfoContext(myEditor, myFile);
    PsiElement elt = parameterInfoHandler.findElementForParameterInfo(createContext);
    assertNotNull(elt);
    parameterInfoHandler.showParameterInfo(elt, createContext);
    Object[] items = createContext.getItemsToShow();
    assertTrue(items != null);
    assertTrue(items.length > 0);
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<PsiElement>(elt);
    parameterInfoHandler.updateUI((DartFunctionDescription)items[0], context);
    assertEquals(infoText, parameterInfoHandler.myParametersListPresentableText);

    // index check
    MockUpdateParameterInfoContext updateContext = new MockUpdateParameterInfoContext(myEditor, myFile);
    final PsiElement element = parameterInfoHandler.findElementForUpdatingParameterInfo(updateContext);
    assertNotNull(element);
    parameterInfoHandler.updateParameterInfo(element, updateContext);
    assertEquals(highlightedParameterIndex, updateContext.getCurrentParameter());
  }

  public void testParamInfo1() throws Throwable {
    doTest("int p1, p2, Node p3", 0);
  }

  public void testParamInfo2() throws Throwable {
    doTest("int p1, p2, Node p3", 2);
  }

  public void testParamInfo3() throws Throwable {
    doTest("int x, int y", 0);
  }

  public void testParamInfo4() throws Throwable {
    doTest("int x, int y", 0);
  }

  public void testParamInfo5() throws Throwable {
    doTest("int x, int y", 1);
  }

  public void testParamInfo6() throws Throwable {
    doTest("int x, int y = 239", 1);
  }

  public void testParamInfo7() throws Throwable {
    doTest("int x, int y = 239", 0);
  }

  public void testParamInfo8() throws Throwable {
    doTest("String one, [String two, String three]", 0);
  }

  public void testParamInfo9() throws Throwable {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo10() throws Throwable {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo11() throws Throwable {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo13() throws Throwable {
    doTest("Foo<Foo, Foo> param", 0);
  }
}
