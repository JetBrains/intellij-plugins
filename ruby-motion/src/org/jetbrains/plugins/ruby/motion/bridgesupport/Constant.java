package org.jetbrains.plugins.ruby.motion.bridgesupport;

/**
 * @author Dennis.Ushakov
 */
public class Constant {
  private String myName;
  private final String myDeclaredType;

  public Constant(String name, String declaredType) {
    myName = name;
    myDeclaredType = declaredType;
  }

  public String getName() {
    return myName;
  }

  public String getDeclaredType() {
    return myDeclaredType;
  }
}
