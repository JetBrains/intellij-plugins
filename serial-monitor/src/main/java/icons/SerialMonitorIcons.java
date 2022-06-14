package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class SerialMonitorIcons {
  private static @NotNull Icon load(@NotNull String path, int cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, SerialMonitorIcons.class.getClassLoader(), cacheKey, flags);
  }
  /** 16x16 */ public static final @NotNull Icon ConnectedSerial = load("icons/connected-serial.png", 0, 0);
  /** 16x16 */ public static final @NotNull Icon DisconnectedSerial = load("icons/disconnected-serial.png", 0, 0);
  /** 16x16 */ public static final @NotNull Icon HexSerial = load("icons/hex-serial.png", 0, 1);
  /** 16x16 */ public static final @NotNull Icon OpenSerial = load("icons/open-serial.png", 0, 0);
}
