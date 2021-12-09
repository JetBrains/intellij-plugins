package org.jetbrains.idea.perforce.perforce.connections;

public enum P4ConfigFields {
  P4CONFIG("P4CONFIG", "xxx-unused"),
  P4IGNORE("P4IGNORE", "xxx-unused"),
  P4CLIENT("P4CLIENT", "-c"),
  P4PORT("P4PORT", "-p"),
  P4USER("P4USER", "-u"),
  P4PASSWD("P4PASSWD", "-P"),
  P4CHARSET("P4CHARSET", "-C");

  public final static String P4TICKETS = "P4TICKETS";

  private final String myName;
  private final String myFlag;

  P4ConfigFields(final String name, final String flag) {
    myName = name;
    myFlag = flag;
  }

  public String getName() {
    return myName;
  }

  public String getFlag() {
    return myFlag;
  }
}
