package com.intellij.tapestry.lang;

import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.tapestry.core.TapestryConstants;
import icons.TapestryIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public final class TmlFileType extends XmlLikeFileType {

  public static final TmlFileType INSTANCE = new TmlFileType();

  private TmlFileType() {
    super(TmlLanguage.INSTANCE);
  }

  @Override
  @NotNull
  public String getName() {
    return "TML";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "Tapestry template";
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return TapestryConstants.TEMPLATE_FILE_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return TapestryIcons.Tapestry_logo_small;
  }
}
