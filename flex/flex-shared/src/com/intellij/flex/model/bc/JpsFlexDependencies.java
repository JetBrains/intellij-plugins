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
  JpsComponentSet getComponentSet();

  void setComponentSet(@NotNull JpsComponentSet componentSet);

  @NotNull
  JpsLinkageType getFrameworkLinkage();

  void setFrameworkLinkage(@NotNull JpsLinkageType frameworkLinkage);

  @NotNull
  List<JpsFlexDependencyEntry> getEntries();
}
