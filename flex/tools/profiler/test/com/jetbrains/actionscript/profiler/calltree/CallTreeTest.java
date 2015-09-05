package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.FrameUtil;
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
    final CallTree callTree = CallTreeTestUtil.getCallTreeFromXmlFile(xmlFile.getRootTag());
    final Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> maps = getMaps(callTree, xmlFile.getRootTag());
    checkResults(resultsXmlFile, maps.getFirst(), maps.getSecond());
  }

  protected Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getMaps(CallTree callTree, XmlTag rootTag) {
    return callTree.getTimeMaps();
  }

  private static void checkResults(XmlFile resultsXmlFile, Map<FrameInfo, Long> countMap, Map<FrameInfo, Long> selfTimeMap) {
    final XmlTag rootTag = resultsXmlFile.getRootTag();
    assertNotNull(rootTag);
    checkResults(rootTag.getSubTags(), countMap, selfTimeMap);
  }

  private static void checkResults(XmlTag[] subTags, Map<FrameInfo, Long> countMap, Map<FrameInfo, Long> selfTimeMap) {
    assertEquals("different sizes", subTags.length, countMap.size());
    assertEquals("different sizes", subTags.length, selfTimeMap.size());
    for (XmlTag xmlTag : subTags) {
      String name = xmlTag.getName();
      Long count = Long.parseLong(xmlTag.getAttributeValue("count"));
      Long selftime = Long.parseLong(xmlTag.getAttributeValue("selftime"));
      FrameInfo frameInfo = FrameUtil.getFrameInfo(name);
      assertEquals("bad count for " + name, count, countMap.get(frameInfo));
      assertEquals("bad selftime for " + name, selftime, selfTimeMap.get(frameInfo));
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
