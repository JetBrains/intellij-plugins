package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

abstract public class CallTreeTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return File.separator + "flex" + File.separator + "tools" + File.separator + "profiler" + File.separator + "testdata";
  }

  protected void doTest(String fileName, String resultsFileName) throws IOException {
    doTest(getTestXMLFile(fileName), getTestXMLFile(resultsFileName));
  }

  private void doTest(@NotNull XmlFile xmlFile, @NotNull XmlFile resultsXmlFile) {
    final CallTree callTree = CallTreeUtil.getCallTreeFromXmlFile(xmlFile.getRootTag());
    final Pair<Map<String, Long>, Map<String, Long>> maps = getMaps(callTree, xmlFile.getRootTag());
    checkResults(resultsXmlFile, maps.getFirst(), maps.getSecond());
  }

  protected Pair<Map<String, Long>, Map<String, Long>> getMaps(CallTree callTree, XmlTag rootTag) {
    return callTree.getTimeMaps();
  }

  private static void checkResults(XmlFile resultsXmlFile, Map<String, Long> countMap, Map<String, Long> selfTimeMap) {
    final XmlTag rootTag = resultsXmlFile.getRootTag();
    checkResults(rootTag.getSubTags(), countMap, selfTimeMap);
  }

  private static void checkResults(XmlTag[] subTags, Map<String, Long> countMap, Map<String, Long> selfTimeMap) {
    assertEquals("different sizes", subTags.length, countMap.size());
    assertEquals("different sizes", subTags.length, selfTimeMap.size());
    for (XmlTag xmlTag : subTags) {
      String name = xmlTag.getName();
      Long count = Long.parseLong(xmlTag.getAttributeValue("count"));
      Long selftime = Long.parseLong(xmlTag.getAttributeValue("selftime"));
      assertEquals("bad count for " + name, count, countMap.get(name));
      assertEquals("bad selftime for " + name, selftime, selfTimeMap.get(name));
    }
  }

  @NotNull
  private XmlFile getTestXMLFile(String fileName) throws IOException {
    File file = new File(getTestDataPath() + File.separator + fileName);
    assertTrue("File doesn't exists " + fileName, file.exists());
    FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
    return (XmlFile)createLightFile(fileType, FileUtil.loadFile(file));
  }
}
