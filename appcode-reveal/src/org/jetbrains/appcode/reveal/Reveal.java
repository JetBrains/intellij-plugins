// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.util.ExecUtil;
import com.intellij.ide.script.IdeScriptException;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Version;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.mac.foundation.NSWorkspace;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.cidr.AppleScript;
import com.jetbrains.cidr.OCPathManager;
import com.jetbrains.cidr.xcode.frameworks.ApplePlatform;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.frameworks.XCFramework;
import com.jetbrains.cidr.xcode.model.XCBuildSettings;
import com.jetbrains.cidr.xcode.plist.Plist;
import com.jetbrains.cidr.xcode.plist.PlistDriver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public final class Reveal {
  public static final Logger LOG = Logger.getInstance("#" + Reveal.class.getPackage().getName());

  private static final List<String> APPLICATION_BUNDLE_IDENTIFIERS = List.of("com.ittybittyapps.Reveal2", "com.ittybittyapps.Reveal");

  public static @Nullable File getDefaultRevealApplicationBundle() {
    for (String identifier : APPLICATION_BUNDLE_IDENTIFIERS) {
      String path = NSWorkspace.absolutePathForAppBundleWithIdentifier(identifier);
      if (path != null) {
        File file = new File(path);
        if (file.exists()) {
          return file;
        }
      }
    }

    return null;
  }

  private static @Nullable File getRevealInspectionScript(@NotNull File bundle) {
    File result = new File(bundle, "/Contents/Resources/InspectApplication.scpt");
    return result.exists() ? result : null;
  }

  @Contract("_, null -> null")
  public static File getRevealLib(@NotNull File bundle, @Nullable XCBuildSettings buildSettings) {
    if (buildSettings == null) return null;

    AppleSdk sdk = buildSettings.getBaseSdk();
    if (sdk == null) return null;

    ApplePlatform platform = sdk.getPlatform();
    File result;

    if (isCompatibleWithReveal27OrHigher(bundle)) {
      Path xcFrameworkIoFile = OCPathManager.getUserApplicationSupportSubFile("Reveal/RevealServer/RevealServer.xcframework").toPath();
      Path frameworkRoot = ReadAction.compute(() -> new XCFramework(xcFrameworkIoFile).resolveFrameworkRoot(buildSettings));
      if (frameworkRoot == null) return null;
      result = new File(frameworkRoot.toFile(), "RevealServer");
    }
    else if (isCompatibleWithReveal23OrHigher(bundle)) {
      String libraryPath = "Reveal/RevealServer/";
      if (platform.isIOS()) {
        libraryPath += "iOS/";
      }
      else if (platform.isTv()) {
        libraryPath += "tvOS/";
      }
      libraryPath += "RevealServer.framework/RevealServer";

      result = OCPathManager.getUserApplicationSupportSubFile(libraryPath);
    }
    else {
      String libraryPath = "/Contents/SharedSupport/";

      if (platform.isIOS()) {
        libraryPath += "iOS-Libraries/";
      }
      else if (platform.isTv()) {
        libraryPath += "tvOS-Libraries/";
      }

      if (isCompatibleWithRevealTwoOrHigher(bundle)) {
        libraryPath += "RevealServer.framework/RevealServer";
      }
      else if (platform.isTv()) {
        libraryPath += "libReveal-tvOS.dylib";
      }
      else {
        libraryPath += "libReveal.dylib";
      }
      result = new File(bundle, libraryPath);
    }

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

  public static boolean isCompatibleWithRevealTwoOrHigher(@NotNull File bundle) {
    Version version = getRevealVersion(bundle);
    return version != null && version.isOrGreaterThan(8378);
  }

  public static boolean isCompatibleWithReveal23OrHigher(@NotNull File bundle) {
    Version version = getRevealVersion(bundle);
    return version != null && version.isOrGreaterThan(12724);
  }

  public static boolean isCompatibleWithReveal27OrHigher(@NotNull File bundle) {
    Version version = getRevealVersion(bundle);
    return version != null && version.isOrGreaterThan(13901);
  }

  public static @Nullable Version getRevealVersion(@NotNull File bundle) {
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

  public static void refreshReveal(@NotNull Project project,
                                   @NotNull File revealBundle,
                                   @NotNull String bundleID,
                                   @Nullable String deviceName) throws ExecutionException {
    RevealUsageTriggerCollector.SHOW_IN_REVEAL.log(project);

    if (isCompatibleWithRevealOnePointSixOrHigher(revealBundle)) {
      refreshRevealPostOnePointSix(revealBundle, bundleID, deviceName);
    }
    else {
      refreshRevealPreOnePointSix(bundleID, deviceName);
    }
  }

  private static void refreshRevealPostOnePointSix(@NotNull File revealBundle, @NotNull String bundleID, @Nullable String deviceName)
    throws ExecutionException {
    // Reveal 1.6 and later bundle the refresh script with the application - execute it using osascript
    File inspectionScript = getRevealInspectionScript(revealBundle);
    if (inspectionScript == null) {
      throw new ExecutionException(RevealBundle.message("dialog.message.cannot.refresh.reveal.inspection.script.could.not.be.found"));
    }

    try {
      List<String> args = List.of(ExecUtil.getOsascriptPath(), inspectionScript.toString(), bundleID);
      if (deviceName != null) {
        args = ContainerUtil.append(args, deviceName);
      }

      CapturingProcessHandler handler = new CapturingProcessHandler(new GeneralCommandLine(args));
      handler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          if (outputType == ProcessOutputTypes.STDERR) {
            LOG.warn(event.getText());
          }
        }
      });
      handler.startNotify();
    }
    catch (Exception e) {
      throw new ExecutionException(RevealBundle.message("dialog.message.cannot.refresh.reveal", e.getMessage()), e);
    }
  }

  private static void refreshRevealPreOnePointSix(@NotNull String bundleID, @Nullable String deviceName) throws ExecutionException {
    // Pre Reveal 1.6, the refresh script was not bundled with the application
    @NonNls String script = "activate\n" +
                            "repeat with doc in documents\n" +
                            " refresh doc " +
                            "   application bundle identifier \"" + StringUtil.escapeQuotes(bundleID) + "\"";

    if (deviceName != null) {
      script += "   device name \"" + StringUtil.escapeQuotes(deviceName) + "\"";
    }

    script += """
         when available
      end repeat
      activate
      """;

    try {
      AppleScript.tell("Reveal",
                       script,
                       true
      );
    }
    catch (IdeScriptException e) {
      LOG.info("Reveal script failed:\n" + script);
      throw new ExecutionException(RevealBundle.message("dialog.message.cannot.refresh.reveal", e.getMessage()), e);
    }
  }
}
