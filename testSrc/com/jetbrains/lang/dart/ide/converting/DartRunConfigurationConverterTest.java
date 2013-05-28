package com.jetbrains.lang.dart.ide.converting;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.testFramework.PlatformTestCase;
import com.jetbrains.lang.dart.ide.DartRunConfigurationConverterProvider;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;

public class DartRunConfigurationConverterTest extends LightPlatformTestCase {
  public DartRunConfigurationConverterTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  private static String getBaseDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/converter/");
  }

  public void doTest(String oldFileName, String newFileName) throws Exception {
    final Element oldRoot = loadElement(oldFileName);
    assertTrue(DartRunConfigurationConverterProvider.isConversionNeeded(oldRoot));
    DartRunConfigurationConverterProvider.converter(oldRoot);
    assertElementEquals(loadElement(newFileName), oldRoot);
  }

  protected Element loadElement(final String filePath) throws Exception {
    final Document document = JDOMUtil.loadDocument(new File(getBaseDataPath(), filePath));
    return document.getRootElement();
  }

  private static void assertElementEquals(final Element expected, final Element actual) {
    String expectedText = JDOMUtil.createOutputter("\n").outputString(expected);
    String actualText = JDOMUtil.createOutputter("\n").outputString(actual);
    assertEquals(expectedText, actualText);
  }

  public void testLocal() throws Exception {
    doTest("LocalDartApplicationConfiguration.xml", "LocalJSConfiguration.xml");
  }

  public void testRemote() throws Exception {
    doTest("RemoteDartApplicationConfiguration.xml", "RemoteJSConfiguration.xml");
  }
}
