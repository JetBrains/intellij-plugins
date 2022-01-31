package com.intellij.tapestry.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public final class TelFileType extends LanguageFileType {
  public static final TelFileType INSTANCE = new TelFileType();
  private TelFileType() {
    super(TelLanguage.INSTANCE);
  }

  @Override
  @NotNull
  @NonNls
  public String getName() {
    return "TEL";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "Expression Language in Tapestry 5";
  }

  @Override
  @NotNull
  @NonNls
  public String getDefaultExtension() {
    return "tel";
  }

  @Override
  public Icon getIcon() {
    return PlatformIcons.CUSTOM_FILE_ICON;
  }
}
