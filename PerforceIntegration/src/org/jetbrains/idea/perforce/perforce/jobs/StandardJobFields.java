package org.jetbrains.idea.perforce.perforce.jobs;

public enum StandardJobFields {
  name (101),
  status (102),
  user (103),
  date (104),
  description (105);

  private final int myFixedCode;

  StandardJobFields(int fixedCode) {
    myFixedCode = fixedCode;
  }

  public static boolean isStandardField(final PerforceJobField field) {
    final StandardJobFields[] allFields = values();
    for (StandardJobFields standardField : allFields) {
      if (standardField.myFixedCode == field.getCode()) {
        return true;
      }
    }
    return false;
  }

  public int getFixedCode() {
    return myFixedCode;
  }
}
