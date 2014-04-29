package com.jetbrains.lang.dart.ide.module;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DartProjectGenerator<T> implements DirectoryProjectGenerator<T> {

  public interface GeneratorPeer<T> {
    @NotNull
    JComponent getComponent();

    void buildUI(@NotNull SettingsStep settingsStep);

    @NotNull
    T getSettings();

    // null if ok
    @Nullable
    ValidationInfo validate();

    boolean isBackgroundJobRunning();

    void addSettingsStateListener(@NotNull SettingsStateListener listener);
  }

  public interface SettingsStateListener {
    void stateChanged(boolean validSettings);
  }

  @NotNull
  @Nls
  @Override
  public abstract String getName();

  @Nullable
  @Override
  public T showGenerationSettings(final VirtualFile baseDir) throws ProcessCanceledException {
    return null;
  }

  @Override
  public abstract void generateProject(@NotNull final Project project,
                              @NotNull final VirtualFile baseDir,
                              @Nullable final T settings,
                              @NotNull final Module module);

  @NotNull
  @Override
  public ValidationResult validate(@NotNull final String baseDirPath)  {
    return ValidationResult.OK;
  }
}
