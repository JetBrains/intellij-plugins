// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.info;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
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
// TODO: parse functions' parameters descriptions
// TODO: parse predefined variables
public class CfmlTagsDescriptionsParser extends DefaultHandler {
  private boolean myIsTagHelpSection = false;
  private boolean myIsAttributeHelpSection = false;
  private boolean myIsFunctionHelpSection = false;
  private Map<String, CfmlTagDescription> myTags;
  private Map<String, CfmlFunctionDescription> myFunctions;
  private final Map<String, Integer> myPredefinedVariables = new HashMap<>();
  private CfmlTagDescription myCurrentTag = null;
  private CfmlFunctionDescription myCurrentFunction = null;
  private CfmlAttributeDescription myCurrentAttribute = null;
  private final List<String> myFunctionUpperCased = new LinkedList<>();
  private String myOnlineDocumentationLink = "https://helpx.adobe.com/support/coldfusion.html"; //as default online documentation link
  private String myCurrentScope = "";

  private static final int TAG_STATE = 0;
  private static final int FUNCTION_STATE = 1;
  private static final int SCOPE_STATE = 2;
  private static final int PREDEFINED_VARIABLE_STATE = 3;
  private static final int ONLINE_DOC_LINK_STATE = 4;
  private static final int TAG_PARAMETER_STATE = 5;

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
    if (!myIsTagHelpSection &&  !myIsAttributeHelpSection && !myIsFunctionHelpSection) return;
    String description = new String(ch, start, length);
    description = myPattern.matcher(description).replaceAll(" ");

    if (myIsTagHelpSection && myCurrentTag != null) {
      String previousDescription = myCurrentTag.getDescription();
      myCurrentTag.setDescription(
        StringUtil.isEmpty(previousDescription) ? description : previousDescription + "\n" + description);
    }
    if (myIsAttributeHelpSection && myCurrentAttribute != null) {
      String previousDescription = myCurrentAttribute.getDescription();
      myCurrentAttribute.setDescription(
        StringUtil.isEmpty(previousDescription) ? description : previousDescription + "\n" + description);
    }
    else if (myIsFunctionHelpSection && myCurrentFunction != null) {
      String previousDescription = myCurrentFunction.getDescription();
      myCurrentFunction.setDescription(
        StringUtil.isEmpty(previousDescription) ? description : previousDescription + "\n" + description);
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
    else if (localName.equals("doc")) {
      myState = ONLINE_DOC_LINK_STATE;
      String link = attr.getValue("link");
      if (link != null) myOnlineDocumentationLink = link;
    }
    else if (myState == TAG_STATE || myState == TAG_PARAMETER_STATE) {
      myIsTagHelpSection = false;
      if (localName.equals("tag")) {
        myState = TAG_STATE;
        final String isSingle = attr.getValue("single");
        final String endTagRequired = attr.getValue("endtagrequired");
        myCurrentTag = new CfmlTagDescription(attr.getValue("name"),
                                              Boolean.parseBoolean(isSingle), Boolean.parseBoolean(endTagRequired));
      }
      else if (localName.equals("help")) {
        if (myState == TAG_STATE) {
          myIsTagHelpSection = true;
        }
        else {
          myIsAttributeHelpSection = true;
        }
      }
      else if (localName.equals("parameter")) {
        myIsAttributeHelpSection = false;
        myState = TAG_PARAMETER_STATE;
        String aName = attr.getValue("name");
        int aType = CfmlTypesInfo.getTypeByString(attr.getValue("type"));
        boolean aRequired = Boolean.parseBoolean(attr.getValue("required"));
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
        boolean aRequired = Boolean.parseBoolean(attr.getValue("required"));

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
        myPredefinedVariables.put(StringUtil.toLowerCase(aName), aType);
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
      myFunctions.put(StringUtil.toLowerCase(functioName), myCurrentFunction);
      myCurrentFunction = null;
    }
    else if (localName.equals("parameter") && myCurrentTag != null && myCurrentAttribute != null) {
      myCurrentTag.addAttribute(myCurrentAttribute);
      myCurrentAttribute = null;
    }
    else if (localName.equals("scopevar")) {
      if (!StringUtil.isEmpty(myCurrentScope)) {
        if (myCurrentScope.charAt(myCurrentScope.length() - 1) != '.') {
          myPredefinedVariables.put(StringUtil.toLowerCase(myCurrentScope), CfmlTypesInfo.ANY_TYPE);
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
    return ArrayUtilRt.toStringArray(myFunctionUpperCased);
  }

  public String[] getFunctionsListLowerCased() {
    return ArrayUtilRt.toStringArray(myFunctions.keySet());
  }

  public Map<String, Integer> getPredefinedVariables() {
    return myPredefinedVariables;
  }

  public Map<String, CfmlTagDescription> getTags() {
    return myTags;
  }

  public String getOnlineDocumentationLink() {
    return myOnlineDocumentationLink;
  }
}
