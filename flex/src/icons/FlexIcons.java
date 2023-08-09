// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class FlexIcons {
  private static @NotNull Icon load(@NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, FlexIcons.class.getClassLoader(), cacheKey, flags);
  }
  /** 16x16 */ public static final @NotNull Icon Flash_remote_debug = load("images/flash_remote_debug.svg", 1838364291, 0);
  /** 16x16 */ public static final @NotNull Icon Flash_run_config = load("images/flash_run_config.svg", -185729959, 0);

  public static final class Flex {
    /** 16x16 */ public static final @NotNull Icon Flash_builder = load("images/flex/flash_builder.svg", 913161402, 0);
    /** 16x16 */ public static final @NotNull Icon Flash_module_closed = load("images/flex/flash_module_closed.svg", 211531465, 2);
    /** 16x16 */ public static final @NotNull Icon Flexunit = load("images/flex/flexunit.svg", -1307029887, 0);

    public static final class Sdk {
      /** 16x16 */ public static final @NotNull Icon Flex_sdk = load("images/flex/sdk/flex_sdk.svg", 466154479, 0);
      /** 16x16 */ public static final @NotNull Icon MavenFlex = load("images/flex/sdk/mavenFlex.svg", -1550864337, 0);
    }
  }
}
