package com.intellij.lang.javascript.flex;

import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Maxim.Mossienko
 * Date: 12.04.11
 * Time: 13:50
 */
public class MxmlFileType extends XmlLikeFileType {
  //public static final XMLLanguage LANGUAGE = new XMLLanguage("mxml") {};

  public MxmlFileType() {
    super(XMLLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "MXML";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "MXML files";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "mxml";
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  private static final Icon ICON = IconLoader.getIcon("/fileTypes/xml.png");
}
