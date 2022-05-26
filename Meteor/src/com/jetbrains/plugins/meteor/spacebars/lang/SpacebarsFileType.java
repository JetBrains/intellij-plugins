package com.jetbrains.plugins.meteor.spacebars.lang;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.jetbrains.plugins.meteor.MeteorBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Required because language substitutor used FileType for providing file association
 */
public final class SpacebarsFileType extends HbFileType {
  public static final LanguageFileType SPACEBARS_INSTANCE = new SpacebarsFileType();
  private static final String FILE_EX_SPACEBARS = "spacebars";

  private SpacebarsFileType() {
    super(SpacebarsLanguageDialect.INSTANCE);
  }


  @NotNull
  @Override
  public String getName() {
    return "Spacebars";
  }

  @NotNull
  @Override
  public String getDescription() {
    return MeteorBundle.message("ui.filetype.spacebars.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return FILE_EX_SPACEBARS;
  }
}
