package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.AbstractJsPsiTestCase;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.lang.javascript.psi.JSFile;
import junit.framework.Assert;
import org.junit.Test;

public class QUnitStructureTest extends AbstractJsPsiTestCase {

  @Test
  public void testBasicUsage() throws Exception {
    validateJsFile();
  }

  @Test
  public void testEmptyModule() throws Exception {
    validateJsFile();
  }

  @Test
  public void testMiscModules() throws Exception {
    validateJsFile();
  }

  @Test
  public void testModuleWithLifecycle() throws Exception {
    validateJsFile();
  }

  @Test
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

  private static void validateQUnitFileStructure(MarkedQUnitFileStructure markedQUnitFileStructure, QUnitFileStructure qUnitFileStructure) {
    for (MarkedQUnitModuleStructure markedQUnitModuleStructure : markedQUnitFileStructure.getModules()) {
      QUnitModuleStructure qUnitModuleStructure = qUnitFileStructure.getQUnitModuleByName(markedQUnitModuleStructure.getName());
      if (qUnitModuleStructure != null) {
        validateQUnitModule(markedQUnitModuleStructure, qUnitModuleStructure);
      }
      else {
        Assert.fail("Can't find automatically collected module with name '" + markedQUnitModuleStructure.getName() + "'");
      }
    }
    if (qUnitFileStructure.getNonDefaultModuleCount() != markedQUnitFileStructure.getModules().size()) {
      Assert.fail("Found marked " + markedQUnitFileStructure.getModules().size() + " modules, but automatically found "
                  + qUnitFileStructure.getNonDefaultModuleCount() + " modules");
    }
  }

  private static void validateQUnitModule(MarkedQUnitModuleStructure markedQUnitModuleStructure,
                                          QUnitModuleStructure qUnitModuleStructure) {
    Assert.assertEquals(markedQUnitModuleStructure.getName(), qUnitModuleStructure.getName());
    Assert.assertEquals(markedQUnitModuleStructure.getPsiElement(), qUnitModuleStructure.getEnclosingCallExpression());
    for (MarkedQUnitTestMethodStructure markedQUnitTestStructure : markedQUnitModuleStructure.getTestStructures()) {
      QUnitTestMethodStructure qUnitTestMethodStructure =
          qUnitModuleStructure.getTestMethodStructureByName(markedQUnitTestStructure.getName());
      if (qUnitTestMethodStructure == null) {
        Assert.fail("Can't find automatically collected test with name '" + markedQUnitTestStructure.getName() + "' inside module '"
                    + qUnitModuleStructure.getName() + "'");
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
