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
import org.jetbrains.ide.script.IdeScriptException;

import java.io.File;
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
