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

import java.util.LinkedList;
import java.util.List;

public class CfmlFunctionDescription {
  private final String myName;
  private final String myReturnType;
  private String myDescription;
  private final List<CfmlParameterDescription> myParameters = new LinkedList<>();

  public CfmlFunctionDescription(String name, String returnType) {
    myName = name;
    myReturnType = returnType;
  }

  public void addParameter(CfmlParameterDescription newParameter) {
    myParameters.add(newParameter);
  }

  public String getName() {
    return myName;
  }

  public List<CfmlParameterDescription> getParameters() {
    return myParameters;
  }

  public String getReturnType() {
    return myReturnType;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public static class CfmlParameterDescription {
    private final String myName;
    private final String myType;
    private final boolean myIsRequired;
    private String myDescription;

    public CfmlParameterDescription(String name, String type, boolean isRequired) {
      myName = name;
      myType = type;
      myIsRequired = isRequired;
    }

    public String getName() {
      return myName;
    }

    public String getType() {
      return myType;
    }

    public boolean isRequired() {
      return myIsRequired;
    }

    public String getDescription() {
      return myDescription;
    }

    public void setDescription(String description) {
      myDescription = description;
    }

    public String getPresetableText() {
      StringBuilder sb = new StringBuilder();
      String paramDescription = getName();
      final String type = getType();
      if (type != null) {
        paramDescription = paramDescription + " : " + type;
      }
      if (!isRequired()) {
        sb.append("[");
        sb.append(paramDescription);
        sb.append("]");
      }
      else {
        sb.append(paramDescription);
      }
      return sb.toString();
    }
  }

  public String getPresentableText() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getName());
    buffer.append("(");
    buffer.append(getParametersListPresentableText());
    buffer.append(")");
    final String type = getReturnType();
    if (type != null) {
      buffer.append(" : ");
      buffer.append(type);
    }
    return buffer.toString();
  }

  public String getParametersListPresentableText() {
    StringBuilder buffer = new StringBuilder();
    List<CfmlParameterDescription> params = getParameters();
    int i = 0;
    for (CfmlParameterDescription param : params) {
      buffer.append(param.getPresetableText());
      i++;
      if (i != params.size()) {
        buffer.append(", ");
      }
    }
    return buffer.toString();
  }
}
