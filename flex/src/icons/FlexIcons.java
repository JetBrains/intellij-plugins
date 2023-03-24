// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
  /** 16x16 */ public static final @NotNull Icon Flash_remote_debug = load("images/flash_remote_debug.svg", -1991408401, 0);
  /** 16x16 */ public static final @NotNull Icon Flash_run_config = load("images/flash_run_config.svg", 493215227, 0);

  public static final class Flex {
    /** 16x16 */ public static final @NotNull Icon Flash_builder = load("images/flex/flash_builder.svg", -1377370821, 0);
    /** 16x16 */ public static final @NotNull Icon Flash_module_closed = load("images/flex/flash_module_closed.svg", -1732565507, 2);
    /** 16x16 */ public static final @NotNull Icon Flexunit = load("images/flex/flexunit.svg", 421372777, 0);

    public static final class Sdk {
      /** 16x16 */ public static final @NotNull Icon Flex_sdk = load("images/flex/sdk/flex_sdk.svg", 1066747944, 0);
      /** 16x16 */ public static final @NotNull Icon MavenFlex = load("images/flex/sdk/mavenFlex.svg", 707723655, 0);
    }
  }
}
