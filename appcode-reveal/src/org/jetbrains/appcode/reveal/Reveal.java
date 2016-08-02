package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.util.ExecUtil;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.mac.foundation.NSWorkspace;
import com.jetbrains.cidr.AppleScript;
import com.jetbrains.cidr.xcode.frameworks.ApplePlatform;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.plist.Plist;
import com.jetbrains.cidr.xcode.plist.PlistDriver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.script.IdeScriptException;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Reveal {
  public static final Logger LOG = Logger.getInstance("#" + Reveal.class.getPackage().getName());

  @NotNull
  private static List<String> applicationBundleIdentifiers() {
    List<String> identifiers = new ArrayList<String>() {{
      add("com.ittybittyapps.Reveal2");
      add("com.ittybittyapps.dev.Reveal2");
      add("com.ittybittyapps.dev.Reveal");
      add("com.ittybottyapps.Reveal");
    }};

    return identifiers;
  }

  @NotNull
  private static List<File> getRevealApplicationBundles() {
    List<File> applicationBundles = new ArrayList<File>();

    for (String identifier: applicationBundleIdentifiers()) {
      String path = NSWorkspace.absolutePathForAppBundleWithIdentifier(identifier);
      if (path != null) {
        File file = new File(path);
        if (file.exists()) {
          applicationBundles.add(file);
        }
      }
    }

    return applicationBundles;
  }

  @Nullable
  private static File getRevealInspectionScript(@NotNull File bundle) {
    File result = new File(bundle, "/Contents/Resources/InspectApplication.scpt");
    return result.exists() ? result : null;
  }

  @Nullable
  public static File getRevealLib(@NotNull File bundle, AppleSdk sdk) {
    ApplePlatform platform = sdk.getPlatform();
    String libraryPath = null;

    if (platform.isIOS()) {
      libraryPath = "/Contents/SharedSupport/iOS-Libraries/RevealServer.framework";
    } else if (platform.isTv()) {
      libraryPath = "/Contents/SharedSupport/tvOS-Libraries/RevealServer.framework";
    }

    if (libraryPath == null) return null;

    File result = new File(bundle, libraryPath);
    return result.exists() ? result : null;
  }

  public static boolean isCompatible(@NotNull File bundle) {
    Version version = getRevealVersion(bundle);
    return version != null && version.isOrGreaterThan(2299);
  }

  public static boolean isCompatibleWithRevealOnePointSixOrHigher(@NotNull File bundle) {
    Version version = getRevealVersion(bundle);
    return version != null && version.isOrGreaterThan(5589);
  }

  @Nullable
  public static Version getRevealVersion(@NotNull File bundle) {
    Plist plist = PlistDriver.readAnyFormatSafe(new File(bundle, "Contents/Info.plist"));
    if (plist == null) return null;

    String version = plist.getString("CFBundleVersion");
    if (version == null) return null;

    List<String> parts = StringUtil.split(version, ".");
    if (parts.isEmpty()) return null;

    return new Version(StringUtil.parseInt(parts.get(0), 0),
                       parts.size() > 1 ? StringUtil.parseInt(parts.get(1), 0) : 0,
                       parts.size() > 2 ? StringUtil.parseInt(parts.get(2), 0) : 0);
  }

  public static void refreshReveal(@NotNull File revealBundle, @NotNull String bundleID, @NotNull String deviceName) throws ExecutionException {
    UsageTrigger.trigger("appcode.reveal.showInReveal");

    if (isCompatibleWithRevealOnePointSixOrHigher(revealBundle)) {
      refreshRevealPostOnePointSix(revealBundle, bundleID, deviceName);
    } else {
      refreshRevealPreOnePointSix(bundleID, deviceName);
    }
  }

  private static void refreshRevealPostOnePointSix(@NotNull File revealBundle, @NotNull String bundleID, @NotNull String deviceName) throws ExecutionException {
    // Reveal 1.6 and later bundle the refresh script with the application — execute it using osascript
    File inspectionScript = getRevealInspectionScript(revealBundle);
    if (inspectionScript == null) {
      throw new ExecutionException("Cannot refresh Reveal. Inspection script could not be found.");
    }

    try {
      ProcessBuilder pb = new ProcessBuilder(
        ExecUtil.getOsascriptPath(),
        inspectionScript.toString(),
        bundleID,
        deviceName
      );

      Process p = pb.start();
      p.waitFor();
    }
    catch (Exception e) {
      throw new ExecutionException("Cannot refresh Reveal: " + e.getMessage(), e);
    }
  }

  private static void refreshRevealPreOnePointSix(@NotNull String bundleID, @NotNull String deviceName) throws ExecutionException {
    // Pre Reveal 1.6, the refresh script was not bundled with the application
    String script = "activate\n" +
            "repeat with doc in documents\n" +
            " refresh doc " +
            "   application bundle identifier \"" + StringUtil.escapeQuotes(bundleID) + "\"" +
            "   device name \"" + StringUtil.escapeQuotes(deviceName) + "\"" +
            "   when available\n" +
            "end repeat\n" +
            "activate\n";

    try {
      AppleScript.tell("Reveal",
              script,
              true
      );
    }
    catch (IdeScriptException e) {
      LOG.info("Reveal script failed:\n" + script);
      throw new ExecutionException("Cannot refresh Reveal: " + e.getMessage(), e);
    }
  }
}
