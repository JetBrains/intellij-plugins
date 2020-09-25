package com.intellij.lang.javascript.flex;

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MxmlFileType extends XmlLikeFileType {
  public static final LanguageFileType MXML = new MxmlFileType();

  private MxmlFileType() {
    super(MxmlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "MXML";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "MXML";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "mxml";
  }

  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Xml; // TODO own icon
  }
}
