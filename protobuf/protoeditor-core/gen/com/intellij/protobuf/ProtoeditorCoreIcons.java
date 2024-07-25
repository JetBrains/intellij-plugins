package com.intellij.protobuf;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class ProtoeditorCoreIcons {
  private static @NotNull Icon load(@NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, ProtoeditorCoreIcons.class.getClassLoader(), cacheKey, flags);
  }
  private static @NotNull Icon load(@NotNull String expUIPath, @NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, expUIPath, ProtoeditorCoreIcons.class.getClassLoader(), cacheKey, flags);
  }
  /** 12x12 */ public static final @NotNull Icon GoToDeclaration = load("icons/goToDeclaration.svg", 401500357, 0);
  /** 12x12 */ public static final @NotNull Icon GoToImplementation = load("icons/goToImplementation.svg", -1919946991, 0);
  /** 16x16 */ public static final @NotNull Icon ProtoFile = load("icons/newui/protoFile.svg", "icons/protoFile.png", 0, 1);
  /** 16x16 */ public static final @NotNull Icon ProtoMessage = load("icons/newui/protoMessage.svg", "icons/protoMessage.png", 0, 1);
}
