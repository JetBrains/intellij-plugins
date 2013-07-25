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
package com.intellij.coldFusion.model.info;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.reference.SoftReference;
import com.intellij.util.text.LineReader;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;

/**
 * @author vnikolaenko
 */
public class CfmlLangInfo {
  private final Project myProject;
  private Reference<CfmlLangDictionary> myCF8Dictionary;
  private Reference<CfmlLangDictionary> myCF9Dictionary;
  private Reference<CfmlLangDictionary> myRailoDictionary;

  public static CfmlLangInfo getInstance(Project project) {
    return ServiceManager.getService(project, CfmlLangInfo.class);
  }

  public CfmlLangInfo(Project project) {
    myProject = project;
  }

  public static class CfmlLangDictionary {
    public CfmlLangDictionary(String scopesFileName, String tagsFileName) {
      if (StringUtil.isEmpty(scopesFileName) || StringUtil.isEmpty(tagsFileName)) {
        return;
      }
      myVariableScopes = readStringsFromFile(scopesFileName);

      final CfmlTagsDescriptionsParser cfmlTagsParser = new CfmlTagsDescriptionsParser();
      try {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setContentHandler(cfmlTagsParser);
        xr.parse(new InputSource(CfmlLangInfo.class.getResourceAsStream(tagsFileName)));
      }
      catch (Exception e) {
        LOG.error(e);
      }
      myTagAttributes = cfmlTagsParser.getTags();
      myFunctionParameters = cfmlTagsParser.getFunctions();
      myPredefinedFunctions = cfmlTagsParser.getFunctionsList();
      myPredefinedFunctionsInLowCase = cfmlTagsParser.getFunctionsListLowerCased();
      myPredefinedVariables = cfmlTagsParser.getPredefinedVariables();
    }

    public String[] myPredefinedFunctions;
    public Map<String, Integer> myPredefinedVariables;
    public String[] myPredefinedFunctionsInLowCase;
    public String[] myVariableScopes;
    public Map<String, CfmlTagDescription> myTagAttributes;
    public Map<String, CfmlFunctionDescription> myFunctionParameters;
  }

  private CfmlLangDictionary getProjectDictionary() {
    String languageLevel = getLanguageLevel();
    Reference<CfmlLangDictionary> ref;
    CfmlLangDictionary dictionary;
    if (languageLevel.equals(CfmlLanguage.CF8)) {
      ref = myCF8Dictionary;
      dictionary = ref == null ? null : ref.get();
      if (dictionary == null) {
        synchronized (CfmlLangInfo.class) {
          ref = myCF8Dictionary;
          dictionary = ref == null ? null : ref.get();
          if (dictionary == null) {
            dictionary = new CfmlLangDictionary("scopes.txt", "cf8_tags.xml");
            myCF8Dictionary = new SoftReference<CfmlLangDictionary>(dictionary);
          }
        }
      }
    }
    else if (languageLevel.equals(CfmlLanguage.RAILO)) {
      ref = myRailoDictionary;
      dictionary = ref == null ? null : ref.get();
      if (dictionary == null) {
        synchronized (CfmlLangInfo.class) {
          ref = myRailoDictionary;
          dictionary = ref == null ? null : ref.get();
          if (dictionary == null) {
            dictionary = new CfmlLangDictionary("scopes.txt", "Railo_tags.xml");
            myRailoDictionary = new SoftReference<CfmlLangDictionary>(dictionary);
          }
        }
      }
    }
    else /*if (languageLevel.equals(CfmlLanguage.CF9))*/ {
      ref = myCF9Dictionary;
      dictionary = ref == null ? null : ref.get();
      if (dictionary == null) {
        synchronized (CfmlLangInfo.class) {
          ref = myCF9Dictionary;
          dictionary = ref == null ? null : ref.get();
          if (dictionary == null) {
            dictionary = new CfmlLangDictionary("scopes.txt", "tags.xml");
            myCF9Dictionary = new SoftReference<CfmlLangDictionary>(dictionary);
          }
        }
      }
    }
    return dictionary;
  }

  public String getLanguageLevel() {
    CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(myProject).getState();
    return state != null ? state.getLanguageLevel() : CfmlLanguage.CF10;
  }

  public String[] getPredefinedFunctionsLowCase() {
    return getProjectDictionary().myPredefinedFunctionsInLowCase;
  }

  public String[] getPredefinedFunctions() {
    return getProjectDictionary().myPredefinedFunctions;
  }

  public Map<String, Integer> getPredefinedVariables() {
    return getProjectDictionary().myPredefinedVariables;
  }

  public String[] getPredefinedFunctionsInLowCase() {
    return getProjectDictionary().myPredefinedFunctionsInLowCase;
  }

  public String[] getVariableScopes() {
    return getProjectDictionary().myVariableScopes;
  }

  public Map<String, CfmlTagDescription> getTagAttributes() {
    return getProjectDictionary().myTagAttributes;
  }

  public Map<String, CfmlFunctionDescription> getFunctionParameters() {
    return getProjectDictionary().myFunctionParameters;
  }

  private static final Logger LOG = Logger.getInstance(CfmlLangInfo.class.getName());

  @Nullable
  private static String[] readStringsFromFile(String fileName) {
    String[] result = null;
    try {
      InputStream predefined = CfmlLangInfo.class.getResourceAsStream(fileName);
      if (predefined != null) {
        LineReader lineReader = new LineReader(predefined);

        //noinspection unchecked
        List<byte[]> list = lineReader.readLines();
        result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
          byte[] bytes = list.get(i);
          final String s = new String(bytes);
          result[i] = s;
        }
      }
    }
    catch (Exception e) {
      LOG.error(e);
    }
    return result;
  }
}
