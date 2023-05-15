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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CfmlTagDescription {
  private String myDescription = "";
  private final Collection<CfmlAttributeDescription> myAttributes = new LinkedList<>();
  private final Set<String> myAttributesNames = new HashSet<>();
  private boolean myIsSingle = false;
  private boolean myIsEndTagRequired = true;
  private final String myName;

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
