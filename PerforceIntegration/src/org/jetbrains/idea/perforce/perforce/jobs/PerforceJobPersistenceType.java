package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

public enum PerforceJobPersistenceType {
  optional("optional"),
  _default("default"),
  required("required"),
  once("once"),
  always("always");

  private final String myName;

  PerforceJobPersistenceType(final String name) {
    myName = name;
  }

  public static @Nullable PerforceJobPersistenceType parse(final String s) {
    final String l = StringUtil.toLowerCase(s);
    if (optional.myName.equals(l)) return optional;
    if (_default.myName.equals(l)) return _default;
    if (required.myName.equals(l)) return required;
    if (once.myName.equals(l)) return once;
    if (always.myName.equals(l)) return always;
    return null;
  }
}
