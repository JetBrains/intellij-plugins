package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.mac.foundation.NSWorkspace;
import com.jetbrains.cidr.AppleScript;
import com.jetbrains.cidr.xcode.plist.Plist;
import com.jetbrains.cidr.xcode.plist.PlistDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Reveal {
  public static final Logger LOG = Logger.getInstance("#" + Reveal.class.getPackage().getName());

  @Nullable
  private static File getRevealBundle() {

    String path = NSWorkspace.absolutePathForAppBundleWithIdentifier("com.ittybittyapps.Reveal");
    if (path == null) return null;
    
    File result = new File(path);
    return result.exists() ? result : null;
  }

  @Nullable
  private static File getRevealInspectionScript() {
    File bundle = getRevealBundle();
    if (bundle == null) return null;

    File result = new File(bundle, "/Contents/Resources/Inspect Application.scpt");
    return result.exists() ? result : null;
  }

  @Nullable
  public static File getRevealLib() {
    File bundle = getRevealBundle();
    if (bundle == null) return null;

    File result = new File(bundle, "/Contents/SharedSupport/iOS-Libraries/libReveal.dylib");
    return result.exists() ? result : null;
  }

  public static boolean isCompatible() {
    Version version = getRevealVersion();
    return version != null && version.isOrGreaterThan(2299);
  }

  public static boolean isCompatibleWithRevealOnePointSixOrHigher() {
    Version version = getRevealVersion();
    return version != null && version.isOrGreaterThan(5589);
  }

  @Nullable
  public static Version getRevealVersion() {
    File bundle = getRevealBundle();
    if (bundle == null) return null;

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

  public static void refreshReveal(@NotNull String bundleID, @NotNull String deviceName) throws ExecutionException {
    UsageTrigger.trigger("appcode.reveal.showInReveal");

    if (isCompatibleWithRevealOnePointSixOrHigher()) {
      refreshRevealPostOnePointSix(bundleID, deviceName);
    } else {
      refreshRevealPreOnePointSix(bundleID, deviceName);
    }
  }

  private static void refreshRevealPostOnePointSix(@NotNull String bundleID, @NotNull String deviceName) throws ExecutionException {
    // Reveal 1.6 bundles the refresh script with the application — execute it using osascript
    File inspectionScript = getRevealInspectionScript();
    if (inspectionScript == null) {
      throw new ExecutionException("Cannot refresh Reveal. Inspection script could not be found at path: " + inspectionScript.toString());
    }

    try {
      ProcessBuilder pb = new ProcessBuilder(
              "/usr/bin/osascript",
              inspectionScript.toString(),
              bundleID,
              deviceName
      );

      Process p = pb.start();
      p.waitFor();
    }
    catch (IOException e) {
      throw new ExecutionException("Cannot refresh Reveal: " + e.getMessage(), e);
    } catch (InterruptedException e) {
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
    catch (ScriptException e) {
      LOG.info("Reveal script failed:\n" + script);
      throw new ExecutionException("Cannot refresh Reveal: " + e.getMessage(), e);
    }
  }
}
