package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import gnu.trove.THashMap;

import java.util.Map;

public class CompilerOptions implements Cloneable {

  @MapAnnotation
  public Map<String, String> OPTIONS = new THashMap<String, String>();

  public CompilerOptions clone() {
    try {
      final CompilerOptions clone = (CompilerOptions)super.clone();
      clone.OPTIONS = new THashMap<String, String>(OPTIONS);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CompilerOptions that = (CompilerOptions)o;

    if (OPTIONS.isEmpty() && that.OPTIONS.isEmpty()) return true;  // do not store empty map in *.iml

    if (!OPTIONS.equals(that.OPTIONS)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    return OPTIONS.hashCode();
  }
}
