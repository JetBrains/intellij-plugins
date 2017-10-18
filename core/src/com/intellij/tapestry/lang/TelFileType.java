package com.intellij.tapestry.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 8:52:51 PM
 */
public class TelFileType extends LanguageFileType {
  public static final TelFileType INSTANCE = new TelFileType();
  private TelFileType() {
    super(TelLanguage.INSTANCE);
  }

  @NotNull
  @NonNls
  public String getName() {
    return "TEL";
  }

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

  @Nullable
  public Icon getIcon() {
    return PlatformIcons.CUSTOM_FILE_ICON;
  }
}
