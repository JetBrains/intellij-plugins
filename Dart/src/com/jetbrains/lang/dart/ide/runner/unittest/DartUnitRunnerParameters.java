package com.jetbrains.lang.dart.ide.runner.unittest;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRunnerParameters {
  private String myFilePath = null;
  private String myArguments = null;
  private String myVMOptions = null;
  private String myTestName = null;
  private Scope myScope = Scope.ALL;

  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(String filePath) {
    myFilePath = filePath;
  }

  public String getArguments() {
    return myArguments;
  }

  public void setArguments(String arguments) {
    myArguments = arguments;
  }

  public String getVMOptions() {
    return myVMOptions;
  }

  public String getTestName() {
    return myTestName;
  }

  public void setTestName(String name) {
    myTestName = name;
  }

  public void setVMOptions(String VMOptions) {
    myVMOptions = VMOptions;
  }

  public Scope getScope() {
    return myScope;
  }

  public void setScope(Scope scope) {
    myScope = scope;
  }

  public enum Scope {
    METHOD, GROUP, ALL
  }
}
