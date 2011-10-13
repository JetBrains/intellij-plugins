package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.jstestdriver.idea.AbstractJsPsiTestCase;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.lang.javascript.psi.JSFile;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collection;

public class JasmineStructureTest extends AbstractJsPsiTestCase {

  @Test
  public void testEmailValidator() throws Exception {
    validateJsFile();
  }

  @Test
  public void testEmptySuite() throws Exception {
    validateJsFile();
  }

  @Test
  public void testNestedDescribers() throws Exception {
    validateJsFile();
  }

  @Test
  public void testPlayerSpec() throws Exception {
    validateJsFile();
  }

  @Test
  public void testSuiteSpec() throws Exception {
    validateJsFile();
  }

  @Test
  public void testSuiteSuiteSpec() throws Exception {
    validateJsFile();
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath() + "/assertFramework/jasmine/structure/";
  }

  protected void validateJsFile(JSFile jsFile, String fileText) throws Exception {
    JasmineFileStructure jasmineFileStructure = buildJasmineFileStructureByJsFile(jsFile);
    MarkedJasmineFileStructure markedJasmineFileStructure = MarkedJasmineFileStructureBuilder.buildMarkedJasmineFileStructure(
        fileText, jsFile
    );
    matchJasmineFileStructures(jasmineFileStructure, markedJasmineFileStructure);
  }

  private static JasmineFileStructure buildJasmineFileStructureByJsFile(JSFile jsFile) {
    JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
    return builder.buildTestFileStructure(jsFile);
  }

  private static void matchJasmineFileStructures(@NotNull JasmineFileStructure jasmineFileStructure,
                                                 @NotNull MarkedJasmineFileStructure markedJasmineFileStructure) {
    Collection<MarkedJasmineSuiteStructure> markedJasmineSuiteStructures = markedJasmineFileStructure.getInnerSuiteStructures();
    for (MarkedJasmineSuiteStructure markedJasmineSuiteStructure : markedJasmineSuiteStructures) {
      JasmineSuiteStructure jasmineSuiteStructure = jasmineFileStructure.findTopLevelSuiteByName(markedJasmineSuiteStructure.getName());
      if (jasmineSuiteStructure == null) {
        Assert.fail("Can't find automatically collected Jasmine suite with name '" + markedJasmineSuiteStructure.getName() + "'!");
      }
      matchJasmineSuites(jasmineSuiteStructure, markedJasmineSuiteStructure);
    }
    if (jasmineFileStructure.getTopLevelSuiteCount() != markedJasmineSuiteStructures.size()) {
      Assert.fail("Marked Jasmine suite count is " + markedJasmineSuiteStructures.size()
                  + ", but automatically collected Jasmine suite count is " + jasmineFileStructure.getTopLevelSuiteCount());
    }
  }

  private static void matchJasmineSuites(@NotNull JasmineSuiteStructure jasmineSuiteStructure,
                                         @NotNull MarkedJasmineSuiteStructure markedJasmineSuiteStructure) {
    Assert.assertEquals(jasmineSuiteStructure.getName(), markedJasmineSuiteStructure.getName());
    Assert.assertEquals(jasmineSuiteStructure.getEnclosingCallExpression(), markedJasmineSuiteStructure.getPsiElement());
    Collection<MarkedJasmineSuiteStructure> innerMarkedSuiteStructures = markedJasmineSuiteStructure.getInnerSuiteStructures();
    for (MarkedJasmineSuiteStructure innerMarkedSuiteStructure : innerMarkedSuiteStructures) {
      JasmineSuiteStructure innerSuiteStructure = jasmineSuiteStructure.getInnerSuiteByName(innerMarkedSuiteStructure.getName());
      if (innerSuiteStructure == null) {
        Assert.fail("Can't find automatically collected Jasmine suite with name '" + innerMarkedSuiteStructure.getName() + "'!");
      }
      matchJasmineSuites(innerSuiteStructure, innerMarkedSuiteStructure);
    }
    Assert.assertEquals(
        "Checking inner suite count of '" + jasmineSuiteStructure.getName() + "' suite",
        innerMarkedSuiteStructures.size(),
        jasmineSuiteStructure.getSuiteChildrenCount()
    );

    Collection<MarkedJasmineSpecStructure> innerMarkedSpecStructures = markedJasmineSuiteStructure.getInnerSpecStructures();
    for (MarkedJasmineSpecStructure innerMarkedSpecStructure : innerMarkedSpecStructures) {
      JasmineSpecStructure innerSpecStructure = jasmineSuiteStructure.getInnerSpecByName(innerMarkedSpecStructure.getName());
      if (innerSpecStructure == null) {
        Assert.fail("Can't find automatically collected Jasmine spec with name '" + innerMarkedSpecStructure.getName() + "'!");
      }
      matchJasmineSpecs(innerSpecStructure, innerMarkedSpecStructure);
    }
    Assert.assertEquals(
        "Checking inner spec count of '" + jasmineSuiteStructure.getName() + "' suite",
        innerMarkedSpecStructures.size(),
        jasmineSuiteStructure.getSpecChildrenCount()
    );
  }

  private static void matchJasmineSpecs(@NotNull JasmineSpecStructure specStructure,
                                        @NotNull MarkedJasmineSpecStructure markedSpecStructure) {
    Assert.assertEquals(markedSpecStructure.getName(), specStructure.getName());
    Assert.assertEquals(markedSpecStructure.getPsiElement(), specStructure.getEnclosingCallExpression());
  }
}
