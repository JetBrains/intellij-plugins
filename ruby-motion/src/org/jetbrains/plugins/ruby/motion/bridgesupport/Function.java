package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class Function extends Sealable {
  private String myName;
  private final boolean myVariadic;
  private final boolean myClassMethod;
  private String myReturnValue;
  private List<Pair<String, String>> myArguments = new ArrayList<>();

  public Function(final String name, final boolean isVariadic, final boolean isClassMethod) {
    myName = name;
    myVariadic = isVariadic;
    myClassMethod = isClassMethod;
  }

  public String getName() {
    return myName;
  }

  public String getReturnValue() {
    return myReturnValue;
  }

  public List<Pair<String, String>> getArguments() {
    return myArguments;
  }

  void addArgument(final String name, final String declaredType) {
    checkSeal();
    myArguments.add(Pair.create(name, declaredType));
  }

  void setReturnValue(String returnValue) {
    checkSeal();
    myReturnValue = returnValue;
  }

  public boolean isVariadic() {
    return myVariadic;
  }

  public boolean isClassMethod() {
    return myClassMethod;
  }

  public boolean isId() {
    return "id".equals(getReturnValue()) || "instancetype".equals(getReturnValue());
  }
}
