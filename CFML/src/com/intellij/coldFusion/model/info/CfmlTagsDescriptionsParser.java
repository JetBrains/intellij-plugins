// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import java.util.regex.Pattern;

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
  private final Map<String, Integer> myPredefinedVariables = new HashMap<>();
  private CfmlTagDescription myCurrentTag = null;
  private CfmlFunctionDescription myCurrentFunction = null;
  private CfmlAttributeDescription myCurrentAttribute = null;
  private final List<String> myFunctionUpperCased = new LinkedList<>();
  private String myCurrentScope = "";

  private static final int TAG_STATE = 0;
  private static final int FUNCTION_STATE = 1;
  private static final int SCOPE_STATE = 2;
  private static final int PREDEFINED_VARIABLE_STATE = 3;

  private int myState;

  @Override
  public void startDocument() throws SAXException {
    myTags = new HashMap<>();
    myFunctions = new HashMap<>();
  }

  @Override
  public void endDocument() throws SAXException {
  }

  private final Pattern myPattern = Pattern.compile("\\s{2,}");
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (!myIsTagHelpSection && !myIsFunctionHelpSection) return;
    String description = new String(ch, start, length);
    description = myPattern.matcher(description).replaceAll(" ");

    if (myIsTagHelpSection && myCurrentTag != null) {
      String previousDescription = myCurrentTag.getDescription();
      myCurrentTag.setDescription(
        StringUtil.isEmpty(previousDescription) ?description:previousDescription + "\n" + description);
    }
    else if (myIsFunctionHelpSection && myCurrentFunction != null) {
      String previousDescription = myCurrentFunction.getDescription();
      myCurrentFunction.setDescription(
        StringUtil.isEmpty(previousDescription) ?description:previousDescription + "\n" + description);
    }
  }

  @Override
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

  @Override
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
