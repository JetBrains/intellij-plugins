package com.intellij.lang.javascript.flex;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;

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
    final String[] targetPlayers = getTargetPlayers(sdkHome);
    if (playerVersionInAnyForm != null) {
      final Pair<String, String> majorMinor = getPlayerMajorMinorVersion(playerVersionInAnyForm);
      if (ArrayUtil.contains(majorMinor.first, targetPlayers)) {
        targetPlayer = majorMinor.first;
      }
      else if (ArrayUtil.contains(majorMinor.first + "." + majorMinor.second, targetPlayers)) {
        targetPlayer = majorMinor.first + "." + majorMinor.second;
      }
    }

    return targetPlayer != null ? targetPlayer : getMaximumVersion(targetPlayers);
  }

  public static String getMaximumTargetPlayer(final String sdkHome) {
    return getMaximumVersion(getTargetPlayers(sdkHome));
  }

  @NotNull
  public static String getMaximumVersion(final String[] versions) {
    String version = versions.length > 0 ? versions[0] : "";
    for (int i = 1; i < versions.length; i++) {
      if (StringUtil.compareVersionNumbers(versions[i], version) > 0) {
        version = versions[i];
      }
    }
    return version;
  }

  private static String[] getTargetPlayers(final String sdkHome) {
    final File playerFolder = new File(sdkHome + "/frameworks/libs/player");
    if (playerFolder.isDirectory()) {
      return playerFolder.list(new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
          return new File(playerFolder, name + "/playerglobal.swc").isFile();
        }
      });
    }

    return ArrayUtil.EMPTY_STRING_ARRAY;
  }
}
