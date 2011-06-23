package com.intellij.tapestry.lang;

import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.util.TapestryIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 8:25:40 PM
 */
public class TmlFileType extends XmlLikeFileType {

  public static final TmlFileType INSTANCE = new TmlFileType();

  private TmlFileType() {
    super(TmlLanguage.INSTANCE);
  }

  @NotNull
  public String getName() {
    return "TML";
  }

  @NotNull
  public String getDescription() {
    return "Tapestry template file";
  }

  @NotNull
  public String getDefaultExtension() {
    return TapestryConstants.TEMPLATE_FILE_EXTENSION;
  }

  public Icon getIcon() {
    return TapestryIcons.TAPESTRY_LOGO_SMALL;
  }
}
