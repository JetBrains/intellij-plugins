// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class FlexIcons {
  private static @NotNull Icon load(@NotNull String path, long cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, FlexIcons.class.getClassLoader(), cacheKey, flags);
  }
  /** 16x16 */ public static final @NotNull Icon Flash_remote_debug = load("images/flash_remote_debug.svg", 7751487802686202347L, 0);
  /** 16x16 */ public static final @NotNull Icon Flash_run_config = load("images/flash_run_config.svg", 7671178286207599942L, 0);

  public static final class Flex {
    /** 16x16 */ public static final @NotNull Icon Flash_builder = load("images/flex/flash_builder.svg", -1291878355993643350L, 0);
    /** 16x16 */ public static final @NotNull Icon Flash_module_closed = load("images/flex/flash_module_closed.svg", -3973933812017392037L, 2);
    /** 16x16 */ public static final @NotNull Icon Flexunit = load("images/flex/flexunit.svg", 3195619687069073383L, 0);

    public static final class Sdk {
      /** 16x16 */ public static final @NotNull Icon Flex_sdk = load("images/flex/sdk/flex_sdk.svg", -4518111805383723414L, 0);
      /** 16x16 */ public static final @NotNull Icon MavenFlex = load("images/flex/sdk/mavenFlex.svg", 7471006958863975616L, 0);
    }
  }
}
