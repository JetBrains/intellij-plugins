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
  private static @NotNull Icon load(@NotNull String path) {
    return IconManager.getInstance().getIcon(path, FlexIcons.class);
  }

  /** 16x16 */ public static final @NotNull Icon Flash_remote_debug = load("/images/flash_remote_debug.svg");
  /** 16x16 */ public static final @NotNull Icon Flash_run_config = load("/images/flash_run_config.svg");

  public final static class Flex {
    /** 16x16 */ public static final @NotNull Icon Flash_builder = load("/images/flex/flash_builder.svg");
    /** 16x16 */ public static final @NotNull Icon Flash_module_closed = load("/images/flex/flash_module_closed.svg");
    /** 16x16 */ public static final @NotNull Icon Flexunit = load("/images/flex/flexunit.svg");

    public final static class Sdk {
      /** 16x16 */ public static final @NotNull Icon Flex_sdk = load("/images/flex/sdk/flex_sdk.svg");
      /** 16x16 */ public static final @NotNull Icon MavenFlex = load("/images/flex/sdk/mavenFlex.svg");

    }

  }
}
