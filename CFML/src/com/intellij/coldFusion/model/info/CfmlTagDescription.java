package com.intellij.coldFusion.model.info;

import com.intellij.util.containers.HashSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author vnikolaenko
 */
public class CfmlTagDescription {
  private String myDescription = "";
  private Collection<CfmlAttributeDescription> myAttributes = new LinkedList<CfmlAttributeDescription>();
  private Set<String> myAttributesNames = new HashSet<String>();
  private boolean myIsSingle = false;
  private boolean myIsEndTagRequired = true;
  private String myName;

  public CfmlTagDescription(String name, boolean isSingle, boolean isEndTagRequired) {
    myIsSingle = isSingle;
    myName = name;
    myIsEndTagRequired = isEndTagRequired;
  }

  public String getName() {
    return myName;
  }

  public String getDescription() {
    return myDescription;
  }

  public Collection<CfmlAttributeDescription> getAttributes() {
    return myAttributes;
  }

  public boolean hasAttribute(String attributeName) {
    return myAttributesNames.contains(attributeName);
  }

  public boolean isSingle() {
    return myIsSingle;
  }

  public boolean isEndTagRequired() {
    return myIsEndTagRequired;
  }

  public void addAttribute(CfmlAttributeDescription attribute) {
    myAttributes.add(attribute);
    myAttributesNames.add(attribute.getName());
  }

  public void setDescription(String description) {
    myDescription = description;
  }
}
