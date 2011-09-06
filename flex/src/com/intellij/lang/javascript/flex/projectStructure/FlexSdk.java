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

  public static final String SDK_ELEM = "sdk";
  private static final String HOME_ATTR = "home";
  public static final OrderRootType[] EDITABLE_ROOT_TYPES = new OrderRootType[]{OrderRootType.SOURCES, JavadocOrderRootType.getInstance()};

  public String getLibraryId() {
    return FlexProjectRootsUtil.getSdkLibraryId(myLibrary);
  }

  public String getHomePath() {
    return getHomePath(myLibrary);
  }

  public String getFlexVersion() {
    return getFlexVersion(myLibrary);
  }

  public static String getFlexVersion(@NotNull Library library) {
    return ((FlexSdkProperties)((LibraryEx)library).getProperties()).getVersion();
  }

  public static String getHomePath(@NotNull Library library) {
    return ((FlexSdkProperties)((LibraryEx)library).getProperties()).getHomePath();
  }

  public String[] getRoots(OrderRootType rootType) {
    return myLibrary.getUrls(rootType);
  }

  public static boolean isFlexSdk(Library library) {
    return ((LibraryEx)library).getType() instanceof FlexSdkLibraryType;
  }
}
