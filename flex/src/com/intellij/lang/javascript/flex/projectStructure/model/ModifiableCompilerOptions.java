package com.intellij.lang.javascript.flex.projectStructure.model;

import java.util.Map;

public interface ModifiableCompilerOptions extends CompilerOptions {
  void setAllOptions(Map<String, String> newOptions);
}
