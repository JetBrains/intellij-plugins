package org.jetbrains.plugins.ruby.motion.bridgesupport;

/**
 * @author Dennis.Ushakov
 */
public class StringConstant extends Constant {
  private final String myValue;
  private final boolean myNsString;

  public StringConstant(String name, String value, boolean nsString) {
    super(name, "NSString*");
    myValue = value;
    myNsString = nsString;
  }

  public String getValue() {
    return myValue;
  }

  public boolean isNsString() {
    return myNsString;
  }
}
