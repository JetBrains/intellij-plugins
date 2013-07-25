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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author vnikolaenko
 */
// TODO: parse attributes descriptions
// TODO: parse functions' parameters descriptions
// TODO: parse predefined variables
public class CfmlTagsDescriptionsParser extends DefaultHandler {
  private boolean myIsTagHelpSection = false;
  private boolean myIsFunctionHelpSection = false;
  private Map<String, CfmlTagDescription> myTags;
  private Map<String, CfmlFunctionDescription> myFunctions;
  private Map<String, Integer> myPredefinedVariables = new HashMap<String, Integer>();
  private CfmlTagDescription myCurrentTag = null;
  private CfmlFunctionDescription myCurrentFunction = null;
  private CfmlAttributeDescription myCurrentAttribute = null;
  private List<String> myFunctionUpperCased = new LinkedList<String>();
  private String myCurrentScope = "";

  private static final int TAG_STATE = 0;
  private static final int FUNCTION_STATE = 1;
  private static final int SCOPE_STATE = 2;
  private static final int PREDEFINED_VARIABLE_STATE = 3;

  private int myState;

  public void startDocument() throws SAXException {
    myTags = new HashMap<String, CfmlTagDescription>();
    myFunctions = new HashMap<String, CfmlFunctionDescription>();
  }

  public void endDocument() throws SAXException {
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String description = new String(ch, start, length);
    description = description.replaceAll("\\s*", " ");
    if (myIsTagHelpSection && myCurrentTag != null) {
      myCurrentTag.setDescription(description);
    }
    else if (myIsFunctionHelpSection && myCurrentFunction != null) {
      myCurrentFunction.setDescription(description);
    }
  }

  public void startElement(String namespaceURI, String localName, String qName,
                           Attributes attr) throws SAXException {
    if (localName.equals("tags")) {
      myState = TAG_STATE;
    }
    else if (localName.equals("functions")) {
      myState = FUNCTION_STATE;
    }
    else if (localName.equals("cfscopes")) {
      myState = SCOPE_STATE;
    }
    else if (localName.equals("scopes")) {
      myState = PREDEFINED_VARIABLE_STATE;
    }
    else if (myState == TAG_STATE) {
      myIsTagHelpSection = false;
      if (localName.equals("tag")) {
        final String isSingle = attr.getValue("single");
        final String isEndtagrequired = attr.getValue("endtagrequired");
        myCurrentTag = new CfmlTagDescription(attr.getValue("name"),
                                              Boolean.valueOf(isSingle), Boolean.valueOf(isEndtagrequired));
      }
      else if (localName.equals("help")) {
        myIsTagHelpSection = true;
      }
      else if (localName.equals("parameter")) {
        String aName = attr.getValue("name");
        int aType = CfmlTypesInfo.getTypeByString(attr.getValue("type"));
        boolean aRequired = Boolean.valueOf(attr.getValue("required"));
        String aDescription = "";
        myCurrentAttribute = new CfmlAttributeDescription(aName, aType, aRequired, aDescription);
      }
      else if (localName.equals("value") && myCurrentAttribute != null) {
        myCurrentAttribute.addValue(attr.getValue("option"));
      }
    }
    else if (myState == FUNCTION_STATE) {
      myIsFunctionHelpSection = false;
      if (localName.equals("function")) {
        myCurrentFunction = new CfmlFunctionDescription(attr.getValue("name"), attr.getValue("returns"));
      }
      else if (localName.equals("parameter") && myCurrentFunction != null) {
        String aName = attr.getValue("name");
        String aType = attr.getValue("type");
        boolean aRequired = Boolean.valueOf(attr.getValue("required"));

        myCurrentFunction.addParameter(new CfmlFunctionDescription.CfmlParameterDescription(aName, aType, aRequired));
      }
      else if (localName.equals("help")) {
        myIsFunctionHelpSection = true;
      }
    }
    else if (myState == PREDEFINED_VARIABLE_STATE) {
      if (localName.equals("scope")) {
        int aType = CfmlTypesInfo.getTypeByString(attr.getValue("type"));
        String aName = attr.getValue("value");
        myPredefinedVariables.put(aName.toLowerCase(), aType);
      }
    }
    else if (myState == SCOPE_STATE) {
      if (localName.equals("scopevar")) {
        if (!StringUtil.isEmpty(myCurrentScope) && myCurrentScope.charAt(myCurrentScope.length() - 1) != '.') {
          myCurrentScope += ".";
        }
        myCurrentScope += attr.getValue("name");
      }
    }
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if (localName.equals("tag") && myCurrentTag != null) {
      myTags.put(myCurrentTag.getName(), myCurrentTag);
      myCurrentTag = null;
    }
    else if (localName.equals("function") && myCurrentFunction != null) {
      String functioName = myCurrentFunction.getName();
      myFunctionUpperCased.add(functioName);
      myFunctions.put(functioName.toLowerCase(), myCurrentFunction);
      myCurrentFunction = null;
    }
    else if (localName.equals("parameter") && myCurrentTag != null && myCurrentAttribute != null) {
      myCurrentTag.addAttribute(myCurrentAttribute);
      myCurrentAttribute = null;
    }
    else if (localName.equals("scopevar")) {
      if (!StringUtil.isEmpty(myCurrentScope)) {
        if (myCurrentScope.charAt(myCurrentScope.length() - 1) != '.') {
          myPredefinedVariables.put(myCurrentScope.toLowerCase(), CfmlTypesInfo.ANY_TYPE);
        }
        else {
          myCurrentScope = myCurrentScope.substring(0, myCurrentScope.length() - 1);
        }
        int i = myCurrentScope.lastIndexOf('.');
        if (i != -1) {
          myCurrentScope = myCurrentScope.substring(0, i + 1);
        }
        else {
          myCurrentScope = "";
        }
      }
    }
  }

  public Map<String, CfmlFunctionDescription> getFunctions() {
    return myFunctions;
  }

  public String[] getFunctionsList() {
    return ArrayUtil.toStringArray(myFunctionUpperCased);
  }

  public String[] getFunctionsListLowerCased() {
    return ArrayUtil.toStringArray(myFunctions.keySet());
  }

  public Map<String, Integer> getPredefinedVariables() {
    return myPredefinedVariables;
  }

  public Map<String, CfmlTagDescription> getTags() {
    return myTags;
  }
}
