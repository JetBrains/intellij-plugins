package com.intellij.flex.uiDesigner.libraries;

import java.util.Collection;

public interface Library {
  boolean hasDefinitions();
  Collection<Library> getParents();
}
