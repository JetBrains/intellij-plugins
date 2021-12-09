// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.info;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlLanguage;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author vnikolaenko
 */
public class CfmlLangInfo {
  private final Project myProject;
  private Reference<CfmlLangDictionary> myCFDictionary;
  private String myCFDictionaryLevel;

  private static final class InstanceWithoutApplication {
    static CfmlLangInfo instanceWithoutApplication = new CfmlLangInfo(null);
  }

  public static CfmlLangInfo getInstance(@Nullable Project project) {
    if (project != null) {
      return project.getService(CfmlLangInfo.class);
    }
    else {
      return InstanceWithoutApplication.instanceWithoutApplication;
    }
  }

  public CfmlLangInfo(@Nullable Project project) {
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
      myOnlineDocumentationLink = cfmlTagsParser.getOnlineDocumentationLink();
    }

    public String[] myPredefinedFunctions;
    public Map<String, Integer> myPredefinedVariables;
    public String[] myPredefinedFunctionsInLowCase;
    public String[] myVariableScopes;
    public Map<String, CfmlTagDescription> myTagAttributes;
    public Map<String, CfmlFunctionDescription> myFunctionParameters;
    public String myOnlineDocumentationLink;
  }

  private CfmlLangDictionary getProjectDictionary() {
    String languageLevel = getLanguageLevel();
    CfmlLangDictionary dictionary;

    if (Objects.equals(myCFDictionaryLevel, languageLevel)) {
      dictionary = SoftReference.dereference(myCFDictionary);
      if (dictionary != null) return dictionary;
    }

    synchronized (CfmlLangInfo.class) {
      dictionary = SoftReference.dereference(myCFDictionary);
      if (dictionary == null || !Objects.equals(myCFDictionaryLevel, languageLevel)) {
        dictionary = new CfmlLangDictionary("scopes.txt", languageLevel);
        myCFDictionary = new SoftReference<>(dictionary);
        myCFDictionaryLevel = languageLevel;
      }
    }

    return dictionary;
  }

  public String getLanguageLevel() {
    if (myProject == null) return CfmlLanguage.CF10;
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

  public String getOnlineDocumentationLink() {
    return getProjectDictionary().myOnlineDocumentationLink;
  }

  private static final Logger LOG = Logger.getInstance(CfmlLangInfo.class.getName());

  private static String @Nullable [] readStringsFromFile(String fileName) {
    String[] result = null;
    try {
      InputStream predefined = CfmlLangInfo.class.getResourceAsStream(fileName);
      if (predefined != null) {
        LineReader lineReader = new LineReader(predefined);

        List<byte[]> list = lineReader.readLines();
        result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
          byte[] bytes = list.get(i);
          final String s = new String(bytes, StandardCharsets.UTF_8);
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
