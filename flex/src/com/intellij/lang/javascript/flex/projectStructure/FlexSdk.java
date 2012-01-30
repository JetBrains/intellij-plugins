package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;

/**
 * @author ksafonov
 */
public class FlexSdk {

  @NotNull
  public Library getLibrary() {
    return myLibrary;
  }

  @NotNull
  private final Library myLibrary;

  public FlexSdk(Library library) {
    myLibrary = library;
  }

  public String getLibraryId() {
    return null;

  }

}
