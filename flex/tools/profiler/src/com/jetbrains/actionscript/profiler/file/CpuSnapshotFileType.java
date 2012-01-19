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
public class CpuSnapshotFileType implements FileType {
  public static final String DEFAULT_EXTENSION = "cpu.snapshot";

  @NotNull
  @Override
  public String getName() {
    return "CPU.SNAPSHOT";
  }

  @NotNull
  @Override
  public String getDescription() {
    return ProfilerBundle.message("cpu.snapshot.file.type.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return ProfilerIcons.SNAPSHOT_CPU;
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
