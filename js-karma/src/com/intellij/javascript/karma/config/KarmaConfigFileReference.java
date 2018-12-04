package com.intellij.javascript.karma.config;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;

public class KarmaConfigFileReference extends FileReference {

  private final FileType myExpectedFileType;
  private final boolean myPatternUsed;

  public KarmaConfigFileReference(@NotNull FileReferenceSet fileReferenceSet,
                                  @NotNull TextRange range,
                                  int index,
                                  @NotNull String text,
                                  @NotNull FileType expectedFileType,
                                  boolean patternUsed) {
    super(fileReferenceSet, range, index, text);
    myExpectedFileType = expectedFileType;
    myPatternUsed = patternUsed;
  }

  @NotNull
  public FileType getExpectedFileType() {
    return myExpectedFileType;
  }

  @Override
  public boolean isSoft() {
    return myPatternUsed;
  }

  public enum FileType { FILE, DIRECTORY }

}
