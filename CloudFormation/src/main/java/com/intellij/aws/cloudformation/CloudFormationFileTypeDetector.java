package com.intellij.aws.cloudformation;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloudFormationFileTypeDetector implements FileTypeRegistry.FileTypeDetector {
  @Nullable
  @Override
  public FileType detect(@NotNull VirtualFile virtualFile, @NotNull ByteSequence firstBytes, CharSequence firstCharsIfText) {
    if (!CloudFormationFileType.DEFAULT_EXTENSION.equalsIgnoreCase(virtualFile.getExtension())) {
      return null;
    }

    if (!StringUtil.contains(firstCharsIfText, "\"" + CloudFormationSections.FormatVersion + "\"")) {
      return null;
    }

    return CloudFormationFileType.INSTANCE;
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
