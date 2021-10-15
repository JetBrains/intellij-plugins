package com.intellij.deno.lang;

import com.intellij.lang.javascript.TypeScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DenoFileTypeOverrider implements FileTypeOverrider {
  @Override
  public @Nullable FileType getOverriddenFileType(@NotNull VirtualFile file) {
    CharSequence sequence = file.getNameSequence();
    CharSequence ext = FileUtilRt.getExtension(sequence);
    if (ext.length() != 0 || sequence.length() != 64) return null;
    String path = file.getPath();
    return path.contains("/deps/") ? TypeScriptFileType.INSTANCE : null;
  }
}
