package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.AbstractJsPsiTestCase;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import junit.framework.Assert;

public class QUnitStructureTest extends AbstractJsPsiTestCase {

  public void testBasicUsage() throws Exception {
    validateJsFile();
  }

  public void testEmptyModule() throws Exception {
    validateJsFile();
  }

  public void testMiscModules() throws Exception {
    validateJsFile();
  }

  public void testModuleWithLifecycle() throws Exception {
    validateJsFile();
  }

  public void testSingleTestOnDefaultModule() throws Exception {
    validateJsFile();
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath() + "/assertFramework/qunit/structure/";
  }

  @Override
  protected void validateJsFile(JSFile jsFile, String fileText) throws Exception {
    QUnitFileStructure qUnitFileStructure = buildQUnitFileStructureByJsFile(jsFile);
    MarkedQUnitFileStructure markedQUnitFileStructure = MarkedQUnitStructureBuilder.buildMarkedQUnitFileStructureByFileText(
        fileText, jsFile
    );
    validateQUnitFileStructure(markedQUnitFileStructure, qUnitFileStructure);
  }

  private static QUnitFileStructure buildQUnitFileStructureByJsFile(JSFile jsFile) {
    QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
    return builder.buildTestFileStructure(jsFile);
  }

  private static void validateQUnitFileStructure(MarkedQUnitFileStructure markedFileStructure, QUnitFileStructure fileStructure) {
    for (MarkedQUnitModuleStructure markedModuleStructure : markedFileStructure.getModules()) {
      AbstractQUnitModuleStructure moduleStructure = fileStructure.findQUnitModuleByName(markedModuleStructure.getName());
      if (moduleStructure != null) {
        validateQUnitModule(markedModuleStructure, moduleStructure);
      }
      else {
        Assert.fail("Can't find automatically collected module with name '" + markedModuleStructure.getName() + "'");
      }
    }
    if (fileStructure.getAllModuleCount() != markedFileStructure.getModules().size()) {
      Assert.fail("Found marked " + markedFileStructure.getModules().size() + " modules, but automatically found "
                  + fileStructure.getNonDefaultModuleCount() + " modules");
    }
  }

  private static void validateQUnitModule(MarkedQUnitModuleStructure markedQUnitModuleStructure,
                                          AbstractQUnitModuleStructure moduleStructure) {
    Assert.assertEquals(markedQUnitModuleStructure.getName(), moduleStructure.getName());
    JSCallExpression autoCallExpr = moduleStructure.isDefault() ? null : ((QUnitModuleStructure) moduleStructure).getEnclosingCallExpression();
    Assert.assertEquals(markedQUnitModuleStructure.getPsiElement(), autoCallExpr);
    for (MarkedQUnitTestMethodStructure markedQUnitTestStructure : markedQUnitModuleStructure.getTestStructures()) {
      QUnitTestMethodStructure qUnitTestMethodStructure =
          moduleStructure.getTestMethodStructureByName(markedQUnitTestStructure.getName());
      if (qUnitTestMethodStructure == null) {
        Assert.fail("Can't find automatically collected test with name '" + markedQUnitTestStructure.getName() + "' inside module '"
                    + moduleStructure.getName() + "'");
      }
      validateQUnitTestStructure(markedQUnitTestStructure, qUnitTestMethodStructure);
    }
  }

  private static void validateQUnitTestStructure(MarkedQUnitTestMethodStructure markedQUnitTestStructure,
                                                 QUnitTestMethodStructure qUnitTestMethodStructure) {
    Assert.assertEquals(markedQUnitTestStructure.getName(), qUnitTestMethodStructure.getName());
    Assert.assertEquals(markedQUnitTestStructure.getCallExpression(), qUnitTestMethodStructure.getCallExpression());
  }
}
