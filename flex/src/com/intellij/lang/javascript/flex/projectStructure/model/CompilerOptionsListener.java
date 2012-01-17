package com.intellij.lang.javascript.flex.projectStructure.model;

import java.util.EventListener;

public interface CompilerOptionsListener extends EventListener {
  void optionsInTableChanged();

  void additionalOptionsChanged();
}
