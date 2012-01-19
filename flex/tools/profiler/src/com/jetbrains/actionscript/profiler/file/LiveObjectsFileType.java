package com.jetbrains.actionscript.profiler.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.actionscript.profiler.ProfilerBundle;
import com.jetbrains.actionscript.profiler.ProfilerIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class LiveObjectsFileType implements FileType {
  public static final String LIVE_OBJECTS = "live.snapshot";

  @NotNull
  @Override
  public String getName() {
    return "LIVE.OBJECTS";
  }

  @NotNull
  @Override
  public String getDescription() {
    return ProfilerBundle.message("live.objects.file.type.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return LIVE_OBJECTS;
  }

  @Override
  public Icon getIcon() {
    return ProfilerIcons.LIVE_OBJECTS;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public String getCharset(@NotNull VirtualFile file, byte[] content) {
    return null;
  }
}
