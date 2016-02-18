/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.config;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @author Nadya Zabrodina
 */
@Tag("mapps")
public class CfmlMappingsConfig implements Cloneable {
  @NotNull
  private Map<String, String> serverMappings = new HashMap<String, String>();

  public CfmlMappingsConfig(@NotNull Map<String, String> mappings) {
    serverMappings = mappings;
  }

  @NotNull
  @Property(surroundWithTag = false)
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, keyAttributeName = "logical_path",
                 entryTagName = "mapping", valueAttributeName = "directory", surroundValueWithTag = false)
  public Map<String, String> getServerMappings() {
    return serverMappings;
  }

  public void setServerMappings(@NotNull Map<String, String> serverMappings) {
    this.serverMappings = serverMappings;
  }

  public void putToServerMappings(String logPath, String dir) {
    this.serverMappings.put(logPath, dir);
  }

  public CfmlMappingsConfig() {
  }

  public List<String> mapVirtualToReal(@NotNull String virtualPath) {
    List<String> result = new LinkedList<String>();

    Set<Map.Entry<String, String>> entries = getServerMappings().entrySet();
    for (Map.Entry<String, String> entry : entries) {
      StringTokenizer st_lp = new StringTokenizer(entry.getKey(), "\\/");
      StringTokenizer st = new StringTokenizer(virtualPath, ".");
      int numberOfTokens = st.countTokens();

      if (numberOfTokens < st_lp.countTokens()) {
        continue;
      }

      boolean checkFailed = false;
      while (st_lp.hasMoreTokens()) {
        if (!st_lp.nextToken().equals(st.nextToken())) {
          checkFailed = true;
          break;
        }
      }

      if (checkFailed) {
        continue;
      }

      StringBuilder relativePath = new StringBuilder(entry.getValue());

      while (st.hasMoreTokens()) {
        relativePath.append(File.separatorChar);
        relativePath.append(st.nextToken());
      }

      result.add(relativePath.toString());
    }
    return result;
  }

  @Override
  public CfmlMappingsConfig clone() {
    HashMap<String, String> newServerMappings = new HashMap<String, String>();
    newServerMappings.putAll(getServerMappings());
    return new CfmlMappingsConfig(newServerMappings);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CfmlMappingsConfig that = (CfmlMappingsConfig)o;

    if (!getServerMappings().equals(that.getServerMappings())) return false;

    return true;
  }

  public static void getMappingFromCfserver(String cfusionDirPath){
    String path = cfusionDirPath;
    File dir = FileUtil.findFirstThatExist(path);
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
    File neoRuntimeXml = null;
    if(result != null && result.length == 1) {
      neoRuntimeXml = result[0];
    } else return;
    SAXBuilder builder = new SAXBuilder();

    try {
      Document doc = (Document) builder.build(neoRuntimeXml);
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
      //(logicalPath -> directoryPath)
      Map<String, String> serverMap = new BidirectionalMap<String, String>();
      for(Element varElement: mapping.getChildren()){
        String logicalPath = varElement.getAttributeValue("name");
        String directoryPath = varElement.getChild("string").getValue();
        serverMap.put(logicalPath, directoryPath);
      }
      System.out.println(serverMap);
    }
    catch (JDOMException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int hashCode() {
    return getServerMappings().hashCode();
  }
}
