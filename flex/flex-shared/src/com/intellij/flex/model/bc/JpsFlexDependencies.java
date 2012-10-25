package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.library.sdk.JpsSdk;

import java.util.List;

public interface JpsFlexDependencies extends JpsElement {

  @Nullable
  JpsSdk<?> getSdk();

  @NotNull
  String getTargetPlayer();

  void setTargetPlayer(@NotNull String targetPlayer);

  @NotNull
  ComponentSet getComponentSet();

  void setComponentSet(@NotNull ComponentSet componentSet);

  @NotNull
  LinkageType getFrameworkLinkage();

  void setFrameworkLinkage(@NotNull LinkageType frameworkLinkage);

  @NotNull
  List<JpsFlexDependencyEntry> getEntries();
}
