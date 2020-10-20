// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * NOTE THIS FILE IS AUTO-GENERATED
 * DO NOT EDIT IT BY HAND, run "Generate icon classes" configuration instead
 */
public final class IdeTalkCoreIcons {
  private static @NotNull Icon load(@NotNull String path, long cacheKey, int flags) {
    return IconManager.getInstance().loadRasterizedIcon(path, IdeTalkCoreIcons.class.getClassLoader(), cacheKey, flags);
  }

  public static final class IdeTalk {
    /** 16x16 */ public static final @NotNull Icon Away = load("ideTalk/away.svg", 9153670683970055858L, 0);
    /** 16x16 */ public static final @NotNull Icon Jabber = load("ideTalk/jabber.svg", 7958218956729610372L, 0);
    /** 16x16 */ public static final @NotNull Icon Jabber_dnd = load("ideTalk/jabber_dnd.svg", -6398027185033783395L, 0);
    /** 16x16 */ public static final @NotNull Icon Notavailable = load("ideTalk/notavailable.svg", -5143096907834835402L, 2);
    /** 16x16 */ public static final @NotNull Icon Offline = load("ideTalk/offline.svg", 2509558662903530618L, 0);
    /** 16x16 */ public static final @NotNull Icon User = load("ideTalk/user.svg", 779928771673065809L, 0);
    /** 16x16 */ public static final @NotNull Icon User_dnd = load("ideTalk/user_dnd.svg", 4792144734184116863L, 0);
    /** 13x13 */ public static final @NotNull Icon User_toolwindow = load("ideTalk/user_toolwindow.svg", -742421039522691374L, 2);
  }

  /** 16x16 */ public static final @NotNull Icon Message = load("message.svg", 616900899260536227L, 0);

  public static final class Nodes {
    /** 16x16 */ public static final @NotNull Icon Group_close = load("nodes/group_close.svg", 6182776353591297754L, 0);
  }

  /** 16x16 */ public static final @NotNull Icon Stacktrace = load("stacktrace.svg", -1427887177429732651L, 0);
}
