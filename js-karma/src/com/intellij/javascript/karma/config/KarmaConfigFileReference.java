package com.intellij.javascript.karma.config;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfigFileReference extends FileReference {

  private final FileType myRequiredFileType;

  public KarmaConfigFileReference(@NotNull FileReferenceSet fileReferenceSet,
                                  @NotNull TextRange range,
                                  int index,
                                  @NotNull String text,
                                  @Nullable FileType requiredType) {
    super(fileReferenceSet, range, index, text);
    myRequiredFileType = requiredType;
  }

  @Nullable
  public FileType getRequiredFileType() {
    return myRequiredFileType;
  }

  @Override
  public boolean isSoft() {
    // true to highlight it in KarmaConfigFileInspection
    return true;
  }

  public enum FileType { FILE, DIRECTORY }

}
