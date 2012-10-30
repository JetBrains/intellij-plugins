package com.intellij.lang.javascript.flex;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetPlayerUtils {

  private TargetPlayerUtils() {
  }

  public static Pair<String, String> getPlayerMajorMinorVersion(final @NotNull String targetPlayerVersion) throws NumberFormatException {
    final int firstDotIndex = targetPlayerVersion.indexOf('.');

    if (firstDotIndex != -1) {
      final int secondDotIndex = targetPlayerVersion.indexOf('.', firstDotIndex + 1);
      final String majorVersion = targetPlayerVersion.substring(0, firstDotIndex);
      return secondDotIndex == -1
             ? Pair.create(majorVersion, targetPlayerVersion.substring(firstDotIndex + 1))
             : Pair.create(majorVersion, targetPlayerVersion.substring(firstDotIndex + 1, secondDotIndex));
    }
    else {
      return Pair.create(targetPlayerVersion, "0");
    }
  }

  /**
   * Returned string is equal to folder name in which playerglobal.swc resides
   */
  public static String getTargetPlayer(final @Nullable String playerVersionInAnyForm, final String sdkHome) {
    String targetPlayer = null;
    final String[] targetPlayers = FlexCommonUtils.getTargetPlayers(sdkHome);
    if (playerVersionInAnyForm != null) {
      final Pair<String, String> majorMinor = getPlayerMajorMinorVersion(playerVersionInAnyForm);
      if (ArrayUtil.contains(majorMinor.first, targetPlayers)) {
        targetPlayer = majorMinor.first;
      }
      else if (ArrayUtil.contains(majorMinor.first + "." + majorMinor.second, targetPlayers)) {
        targetPlayer = majorMinor.first + "." + majorMinor.second;
      }
    }

    return targetPlayer != null ? targetPlayer : FlexCommonUtils.getMaximumVersion(targetPlayers);
  }
}
