package org.jetbrains.plugins.ruby.motion.bridgesupport;

/**
 * @author Dennis.Ushakov
 */
public class Enum extends Constant {
  private final String myValue;

  public Enum(String name, String value) {
    super(name, "int");
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }
}
