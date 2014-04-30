package com.jetbrains.lang.dart.ide.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.platform.ProjectTemplate;
import com.jetbrains.lang.dart.ide.template.DartEmptyApplicationGenerator;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DartProjectTemplate<T> extends DartProjectGenerator<T> implements ProjectTemplate {

  private static final DartEmptyApplicationGenerator DEFAULT_PROJECT_TYPE = new DartEmptyApplicationGenerator();

  private final NotNullLazyValue<GeneratorPeer<T>>
    myPeerHolder = new NotNullLazyValue<GeneratorPeer<T>>() {
    @NotNull
    @Override
    protected GeneratorPeer<T> compute() {
      return createPeer();
    }
  };

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @NotNull
  @Override
  public ModuleBuilder createModuleBuilder() { return new DartModuleBuilder(getGenerator()); }

  @Nullable
  @Override
  public ValidationInfo validateSettings() {
    return null;
  }

  @NotNull
  public abstract GeneratorPeer<T> createPeer();

  @NotNull
  public GeneratorPeer<T> getPeer() {
    return myPeerHolder.getValue();
  }

  @NotNull
  protected DartProjectTemplate<?> getGenerator() { return DEFAULT_PROJECT_TYPE; }
}
