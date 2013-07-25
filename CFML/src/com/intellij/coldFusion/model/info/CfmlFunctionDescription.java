package com.intellij.coldFusion.model.info;

import java.util.LinkedList;
import java.util.List;

/**
 * @author vnikolaenko
 */
public class CfmlFunctionDescription {
  private String myName;
  private String myReturnType;
  private String myDescription;
  private List<CfmlParameterDescription> myParameters = new LinkedList<CfmlParameterDescription>();

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
    private String myName;
    private String myType;
    private boolean myIsRequired;
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
      } else {
        sb.append(paramDescription);
      }
      return sb.toString();
    }
  }

  public String getPresentableText() {
    StringBuffer buffer = new StringBuffer();
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
    StringBuffer buffer = new StringBuffer();
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
