package com.intellij.coldFusion;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.BidirectionalMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by karashevich on 19/02/16.
 */
public class CfmlServerUtil {

  /**
   * @param cfusionDirPath paht to <b>cfusion</b> dir
   * @return A server mapping holding at neo-runtime.xml from directoryPath to logicalPath
   */
  @Nullable
  public static Map<String, String> getMappingFromCfserver(String cfusionDirPath){

    try {
      if (cfusionDirPath == null) return null;
      Document doc = getNeoRuntimeXmlDoc(cfusionDirPath);
      if (doc == null) return null;
      Element mapping = getMappingElement(doc);
      //(directoryPath, logicalPath)
      Map<String, String> serverMap = new BidirectionalMap<String, String>();
      if (mapping == null) return null;
      for(Element varElement: mapping.getChildren()){
        String dirPath = varElement.getAttributeValue("name");
        String logicPath = varElement.getChild("string").getValue();
        serverMap.put(dirPath, logicPath);
      }
      return serverMap;
    }
    catch (JDOMException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void saveMappingToCfserver(Map<String, String> mapping, String cfusionDirPath){
    Document doc;
    File neoRuntimeXml = getNeoRuntimeXml(cfusionDirPath);
    //TODO: add exception here
    if (neoRuntimeXml == null) return;
    try {
      doc = getNeoRuntimeXmlDoc(cfusionDirPath);
      if (doc == null) return;
      Element mappingElement = getMappingElement(doc);
      if (mappingElement == null) return;
      mappingElement.removeContent();
      //generate content from map
      for (String keyDirectoryPath : mapping.keySet()) {
        Element entryVar = new Element("var");
        entryVar.setAttribute("name", keyDirectoryPath);
        Element entryString = new Element("string");
        entryString.setText(mapping.get(keyDirectoryPath));
        entryVar.setContent(entryString);
        mappingElement.addContent(entryVar);
      }
      XMLOutputter xmlOutput = new XMLOutputter();
      xmlOutput.setFormat(Format.getRawFormat());
      xmlOutput.output(doc, new FileWriter(neoRuntimeXml));
    }
    catch (JDOMException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Nullable
  private static Element getMappingElement(Document doc) {
    Element rootNode = doc.getRootElement();
    List<Element> arrayElements = rootNode.getChild("data").getChild("array").getChildren();
    Element mapping = null;
    for(Element el: arrayElements) {
      if (el.getChildren() != null && el.getChildren().size() > 0 && el.getChildren().get(0) != null && el.getChildren().get(0).getAttributeValue("name") != null) {
        for(Element varEl: el.getChildren()) {
          if (varEl.getAttributeValue("name").length() >= 6 && varEl.getAttributeValue("name").substring(0, 6).equals("/CFIDE")) {
            mapping = el;
            break;
          }
        }
      }
    }
    return mapping;
  }

  @Nullable
  private static Document getNeoRuntimeXmlDoc(String pathToCfusionServerDir) throws JDOMException, IOException {
    File neoRuntimeXml = getNeoRuntimeXml(pathToCfusionServerDir);
    if (neoRuntimeXml == null) return null;
    SAXBuilder builder = new SAXBuilder();
    return builder.build(neoRuntimeXml);
  }

  @Nullable
  private static File getNeoRuntimeXml(String pathToCfusionServerDir){
    File neoRuntimeXml;
    if (pathToCfusionServerDir == null) return null;
    File dir = FileUtil.findFirstThatExist(pathToCfusionServerDir);
    if (dir == null) return null;
    File[] libs = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals("lib");
      }
    });
    File[] result = null;
    if (libs != null && libs.length == 1) {
      result = libs[0].listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.equals("neo-runtime.xml");
        }
      });
    }
    if (result != null && result.length == 1) {
      neoRuntimeXml = result[0];
    }
    else {
      return null;
    }
    return neoRuntimeXml;
  }

  public static void restartColdfusionServer(String pathToCfusionServerDir) throws Exception {
    Runtime runTime = Runtime.getRuntime();
    runTime.exec(getColdfusionAppPath(pathToCfusionServerDir) + " restart");
  }

  @Nullable
  private static String getColdfusionAppPath(String pathToCfusionServerDir){
    File coldFusionApp;
    if (pathToCfusionServerDir == null) return null;
    File dir = FileUtil.findFirstThatExist(pathToCfusionServerDir);
    if (dir == null) return null;
    File[] libs = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals("bin");
      }
    });
    File[] result = null;
    if (libs != null && libs.length == 1) {
      result = libs[0].listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.equals("coldfusion");
        }
      });
    }
    if (result != null && result.length == 1) {
      coldFusionApp = result[0];
    }
    else {
      return null;
    }
    return coldFusionApp.getPath();
  }
}
