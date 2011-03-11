package com.intellij.lang.javascript.flex.actions.htmlwrapper;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class HTMLWrapperParameters {

  private final @NotNull VirtualFile myHtmlWrapperRootDir;
  private final @NotNull String myHtmlFileName;
  private final @NotNull VirtualFile myHtmlFileLocation;
  private final @NotNull String myHtmlPageTitle;
  private final @NotNull String myFlexApplicationName;
  // this variable does not contain ".swf" extension, extension is hardcoded in wrappers templates
  private final @NotNull String mySwfFileName;
  private final @NotNull String myApplicationWidth;
  private final @NotNull String myApplicationHeight;
  private final @NotNull String bgColor;
  private final int myFlashPlayerVersionMajor;
  private final int myFlashPlayerVersionMinor;
  private final int myFlashPlayerVersionRevision;

  public HTMLWrapperParameters(@NotNull VirtualFile htmlWrapperRootDir,
                               @NotNull String htmlFileName,
                               @NotNull VirtualFile htmlFileLocation,
                               @NotNull String htmlPageTitle,
                               @NotNull String flexApplicationName,
                               @NotNull String swfFileName,
                               @NotNull String applicationWidth,
                               @NotNull String applicationHeight,
                               @NotNull String bgColor,
                               int flashPlayerVersionMajor,
                               int flashPlayerVersionMinor,
                               int flashPlayerVersionRevision) {
    myHtmlWrapperRootDir = htmlWrapperRootDir;
    myHtmlFileName = htmlFileName;
    myHtmlFileLocation = htmlFileLocation;
    myHtmlPageTitle = htmlPageTitle;
    myFlexApplicationName = flexApplicationName;
    mySwfFileName = swfFileName;
    myApplicationWidth = applicationWidth;
    myApplicationHeight = applicationHeight;
    this.bgColor = bgColor;
    myFlashPlayerVersionMajor = flashPlayerVersionMajor;
    myFlashPlayerVersionMinor = flashPlayerVersionMinor;
    myFlashPlayerVersionRevision = flashPlayerVersionRevision;
  }

  @NotNull
  public VirtualFile getHtmlWrapperRootDir() {
    return myHtmlWrapperRootDir;
  }

  @NotNull
  public String getHtmlFileName() {
    return myHtmlFileName;
  }

  @NotNull
  public VirtualFile getHtmlFileLocation() {
    return myHtmlFileLocation;
  }

  @NotNull
  public String getHtmlPageTitle() {
    return myHtmlPageTitle;
  }

  @NotNull
  public String getFlexApplicationName() {
    return myFlexApplicationName;
  }

  /**
   * Returned value does not contain extension. ".swf" extension is hardcoded in wrappers templates
   */
  @NotNull
  public String getSwfFileName() {
    return mySwfFileName;
  }

  @NotNull
  public String getApplicationWidth() {
    return myApplicationWidth;
  }

  @NotNull
  public String getApplicationHeight() {
    return myApplicationHeight;
  }

  @NotNull
  public String getBgColor() {
    return bgColor;
  }

  public int getFlashPlayerVersionMajor() {
    return myFlashPlayerVersionMajor;
  }

  public int getFlashPlayerVersionMinor() {
    return myFlashPlayerVersionMinor;
  }

  public int getFlashPlayerVersionRevision() {
    return myFlashPlayerVersionRevision;
  }
}
