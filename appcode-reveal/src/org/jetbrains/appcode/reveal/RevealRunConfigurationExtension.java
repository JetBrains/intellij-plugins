// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.DoNotAskOption;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.cidr.execution.*;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.frameworks.buildSystem.BuildSettingNames;
import com.jetbrains.cidr.xcode.model.*;
import com.jetbrains.cidr.xcode.plist.Plist;
import com.jetbrains.cidr.xcode.plist.PlistDriver;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class RevealRunConfigurationExtension extends AppCodeRunConfigurationExtension {
  private static final String REVEAL_SETTINGS_TAG = "REVEAL_SETTINGS";
  private static final Key<RevealSettings> REVEAL_SETTINGS_KEY = Key.create(REVEAL_SETTINGS_TAG);
  private static final Key<String> BUNDLE_ID_KEY = Key.create("BUNDLE_INFO");
  private static final Key<File> FILE_TO_INJECT = Key.create("reveal.library.to.inject");
  private static final List<String> KNOWN_FRAMEWORK_NAMES = List.of("RevealServer", "Reveal");

  @NotNull
  public static RevealSettings getRevealSettings(@NotNull AppCodeRunConfiguration config) {
    RevealSettings settings = config.getUserData(REVEAL_SETTINGS_KEY);
    return settings == null ? new RevealSettings() : settings;
  }

  public static void setRevealSettings(@NotNull AppCodeRunConfiguration runConfiguration, @Nullable RevealSettings settings) {
    runConfiguration.putUserData(REVEAL_SETTINGS_KEY, settings);
  }

  @Override
  protected void readExternal(@NotNull AppCodeRunConfiguration runConfiguration, @NotNull Element element)
    throws InvalidDataException {
    Element settingsTag = element.getChild(REVEAL_SETTINGS_TAG);
    RevealSettings settings = null;
    if (settingsTag != null) {
      settings = new RevealSettings();
      settings.autoInject = getAttributeValue(settingsTag.getAttributeValue("autoInject"), settings.autoInject);
      settings.autoInstall = getAttributeValue(settingsTag.getAttributeValue("autoInstall"), settings.autoInstall);
      settings.askToEnableAutoInstall =
        getAttributeValue(settingsTag.getAttributeValue("askToEnableAutoInstall"), settings.askToEnableAutoInstall);
    }
    setRevealSettings(runConfiguration, settings);
  }

  private static boolean getAttributeValue(@Nullable String value, boolean defaultValue) {
    return value == null ? defaultValue : "true".equals(value);
  }

  @Override
  protected void writeExternal(@NotNull AppCodeRunConfiguration runConfiguration, @NotNull Element element) {
    RevealSettings settings = getRevealSettings(runConfiguration);

    Element settingsTag = new Element(REVEAL_SETTINGS_TAG);
    settingsTag.setAttribute("autoInject", String.valueOf(settings.autoInject));
    settingsTag.setAttribute("autoInstall", String.valueOf(settings.autoInstall));
    settingsTag.setAttribute("askToEnableAutoInstall", String.valueOf(settings.askToEnableAutoInstall));
    element.addContent(settingsTag);
  }

  @Override
  public void copyConfiguration(@NotNull AppCodeRunConfiguration from, @NotNull AppCodeRunConfiguration to) {
    RevealSettings settings = getRevealSettings(from);
    setRevealSettings(to, settings.clone());
  }

  @Nullable
  @Override
  protected <P extends AppCodeRunConfiguration> SettingsEditor<P> createEditor(@NotNull P configuration) {
    return new MyEditor<>();
  }

  @Nullable
  @Override
  protected String getEditorTitle() {
    return RevealBundle.message("dialog.title.reveal");
  }

  @Override
  public boolean isApplicableFor(@NotNull AppCodeRunConfiguration configuration) {
    if (ApplicationManager.getApplication().isUnitTestMode()) return false;
    return configuration instanceof AppCodeAppRunConfiguration;
  }

  @Override
  public boolean isEnabledFor(@NotNull AppCodeRunConfiguration config, @Nullable RunnerSettings runnerSettings) {
    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle == null) return false;

    if (Reveal.getRevealLib(appBundle, getBuildSettings(config)) == null) return false;
    return isAvailableForPlatform(config);
  }

  @Nullable
  private static XCBuildSettings getBuildSettings(@NotNull AppCodeRunConfiguration config) {
    return ReadAction.compute(() -> {
      BuildDestination destination = ContainerUtil.getFirstItem(config.getDestinations());
      if (destination == null) return null;

      OCResolveConfiguration configuration = config.getResolveConfiguration(destination);
      if (configuration == null) return null;
      return XcodeMetaData.getInstance(configuration).getBuildSettings(configuration);
    });
  }

  private static boolean isAvailableForPlatform(@NotNull final AppCodeRunConfiguration config) {
    return ReadAction.compute(() -> {
      AppleSdk sdk = getSdk(config);
      return sdk != null && (sdk.getPlatform().isIOS() || sdk.getPlatform().isTv());
    });
  }

  @Nullable
  private static AppleSdk getSdk(@NotNull final AppCodeRunConfiguration config) {
    return ReadAction.compute(() -> {
      XCBuildConfiguration xcBuildConfiguration = config.getConfiguration();
      return xcBuildConfiguration == null ? null : XCBuildSettings.getRawBuildSettings(xcBuildConfiguration).getBaseSdk();
    });
  }

  @Override
  public void createAdditionalActions(@NotNull AppCodeRunConfiguration configuration,
                                      @NotNull File product,
                                      @NotNull ExecutionEnvironment environment,
                                      @NotNull BuildConfiguration buildConfiguration,
                                      @NotNull ExecutionConsole console,
                                      @NotNull ProcessHandler processHandler,
                                      @NotNull List<? super AnAction> actions) throws ExecutionException {
    super.createAdditionalActions(configuration, product, environment, buildConfiguration, console, processHandler, actions);

    actions.add(new RefreshRevealAction(configuration,
                                        environment,
                                        processHandler,
                                        buildConfiguration.getDestination(),
                                        getBundleID(environment, product)));
  }

  @Override
  public void installIntoApplicationBundle(@NotNull AppCodeRunConfiguration configuration,
                                           @NotNull ExecutionEnvironment environment,
                                           @NotNull BuildConfiguration buildConfiguration,
                                           @NotNull File mainExecutable) throws ExecutionException {
    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle == null) return;

    if (!Reveal.isCompatible(appBundle)) return;

    RevealSettings settings = getRevealSettings(configuration);
    if (!settings.autoInject) return;

    File toInject = installReveal(configuration, buildConfiguration, mainExecutable, settings);
    if (toInject == null) return;

    RevealUsageTriggerCollector.INJECT.log(configuration.getProject());

    environment.putUserData(FILE_TO_INJECT, toInject);
  }

  @Nullable
  private static File installReveal(@NotNull final AppCodeRunConfiguration configuration,
                                    @NotNull BuildConfiguration buildConfiguration,
                                    @NotNull File mainExecutable,
                                    @NotNull final RevealSettings settings) throws ExecutionException {
    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle == null) throw new ExecutionException(RevealBundle.message("dialog.message.reveal.application.bundle.not.found"));

    XCBuildSettings buildSettings =
      ReadAction.compute(() -> XCBuildSettings.withOverriddenSdk(buildConfiguration.getConfiguration(), buildConfiguration.getSdk()));
    File libReveal = Reveal.getRevealLib(appBundle, buildSettings);
    if (libReveal == null) throw new ExecutionException(RevealBundle.message("dialog.message.reveal.library.not.found"));

    Reveal.LOG.info("Reveal lib found at " + libReveal);

    if (hasBundledRevealLib(buildConfiguration, libReveal)) {
      return new File(libReveal.getName());
    }

    if (hasRevealFramework(buildConfiguration)) {
      return null;
    }

    BuildDestination destination = buildConfiguration.getDestination();
    if (!destination.isDevice()) {
      return libReveal;
    }

    if (!settings.autoInstall) {
      if (!settings.askToEnableAutoInstall) return null;

      boolean[] response = new boolean[1];
      UIUtil.invokeAndWaitIfNeeded(() -> {
        response[0] = MessageDialogBuilder.yesNo(RevealBundle.message("dialog.title.reveal"),
                                                 RevealBundle.message("project.is.not.configured.with.reveal.library"))
          .doNotAsk(new DoNotAskOption() {
            @Override
            public boolean isToBeShown() {
              return true;
            }

            @Override
            public void setToBeShown(boolean value, int exitCode) {
              settings.askToEnableAutoInstall = value;
            }

            @Override
            public boolean canBeHidden() {
              return true;
            }

            @Override
            public boolean shouldSaveOptionsOnCancel() {
              return false;
            }

            @NotNull
            @Override
            public String getDoNotShowMessage() {
              return UIBundle.message("dialog.options.do.not.show");
            }
          })
          .ask(configuration.getProject());
      });
      if (!response[0]) {
        return null;
      }

      settings.autoInstall = true;
      settings.askToEnableAutoInstall = true; // is user changes autoInstall in future, ask him/her again
      setRevealSettings(configuration, settings);
    }

    RevealUsageTriggerCollector.INSTALL_ON_DEVICE.log(configuration.getProject());

    return signAndInstall(libReveal, buildConfiguration, mainExecutable);
  }

  private static boolean hasBundledRevealLib(@NotNull final BuildConfiguration buildConfiguration, @NotNull final File libReveal) {
    return ReadAction.compute(() -> {
      PBXTarget target = buildConfiguration.getConfiguration().getTarget();
      if (target != null) {
        for (PBXBuildFile eachFile : target.getBuildFiles(PBXBuildPhase.Type.RESOURCES)) {
          PBXReference ref = eachFile.getFileRef();
          String name = ref == null ? null : ref.getFileName();
          if (FileUtil.namesEqual(libReveal.getName(), name)) return true;
        }
      }
      return false;
    });
  }

  private static boolean hasRevealFramework(@NotNull BuildConfiguration buildConfiguration) {
    return ReadAction.compute(() -> {
      List<String> flags = buildConfiguration.getBuildSetting(BuildSettingNames.OTHER_LDFLAGS).getStringList();
      return flags.stream().anyMatch(flag -> KNOWN_FRAMEWORK_NAMES.contains(StringUtil.unquoteString(flag)));
    });
  }

  @NotNull
  private static File signAndInstall(@NotNull File libReveal,
                                     @NotNull final BuildConfiguration buildConfiguration,
                                     @NotNull File mainExecutable) throws ExecutionException {
    File frameworksDir = new File(mainExecutable.getParent(), "Frameworks");
    File libRevealCopy = new File(frameworksDir, libReveal.getName());

    try {
      FileUtil.copy(libReveal, libRevealCopy);
    }
    catch (IOException e) {
      throw new ExecutionException(RevealBundle.message("dialog.message.cannot.create.temporary.copy.reveal.library"), e);
    }

    AppCodeInstaller.codesignBinary(buildConfiguration, mainExecutable, frameworksDir.getAbsolutePath(), libRevealCopy.getName());

    return new File("Frameworks", libRevealCopy.getName());
  }


  @NotNull
  private static String getBundleID(@NotNull ExecutionEnvironment environment,
                                    @NotNull File product) throws ExecutionException {
    String result = environment.getUserData(BUNDLE_ID_KEY);
    if (result != null) return result;

    File plistFile = new File(product, "Info.plist");
    if (!plistFile.isFile()) {
      plistFile = new File(product, "Contents/Info.plist");
    }

    Plist plist = PlistDriver.readAnyFormatSafe(plistFile);
    if (plist == null) throw new ExecutionException(RevealBundle.message("dialog.message.info.plist.not.found.at", plistFile));

    result = plist.getString("CFBundleIdentifier");
    if (result == null) throw new ExecutionException(RevealBundle.message("dialog.message.cfbundleidentifier.not.found.in", plistFile));

    environment.putUserData(BUNDLE_ID_KEY, result);
    return result;
  }

  @Override
  public void patchCommandLine(@NotNull ExecutionEnvironment environment, @NotNull GeneralCommandLine cmdLine) {
    File toInject = FILE_TO_INJECT.get(environment);

    if (toInject != null) {
      if (!toInject.isAbsolute()) {
        File bundle = new File(cmdLine.getExePath()).getParentFile();
        toInject = new File(bundle, toInject.getPath());
      }

      Reveal.LOG.info("Injecting Reveal lib: " + toInject);

      CidrExecUtil.appendSearchPath(cmdLine.getEnvironment(), EnvParameterNames.DYLD_INSERT_LIBRARIES, toInject.getPath());
    }
  }

  private static class MyEditor<T extends AppCodeRunConfiguration> extends SettingsEditor<T> {
    private HyperlinkLabel myRevealNotFoundOrIncompatible;
    private JBLabel myNotAvailable;

    private JBCheckBox myInjectCheckBox;
    private JBLabel myInjectHint;
    private JBCheckBox myInstallCheckBox;
    private JBLabel myInstallHint;

    boolean isFound;
    boolean isAvailable;

    @Override
    protected void resetEditorFrom(@NotNull AppCodeRunConfiguration s) {
      RevealSettings settings = getRevealSettings(s);

      myInjectCheckBox.setSelected(settings.autoInject);
      myInstallCheckBox.setSelected(settings.autoInstall);

      String notFoundText = null;
      boolean found = false;
      boolean compatible = false;

      File appBundle = Reveal.getDefaultRevealApplicationBundle();
      if (appBundle != null) {
        found = (Reveal.getRevealLib(appBundle, getBuildSettings(s)) != null);
        compatible = Reveal.isCompatible(appBundle);
      }

      if (!found) {
        notFoundText = RevealBundle.message("link.label.reveal.app.not.found");
      }
      else if (!compatible) {
        notFoundText = RevealBundle.message("link.label.incompatible.version.reveal.app");
      }
      if (notFoundText != null) {
        myRevealNotFoundOrIncompatible.setTextWithHyperlink(notFoundText);
      }

      isFound = found && compatible;
      isAvailable = isAvailableForPlatform(s);

      updateControls();
    }

    @Override
    protected void applyEditorTo(@NotNull AppCodeRunConfiguration s) {
      RevealSettings settings = getRevealSettings(s);

      settings.autoInject = myInjectCheckBox.isSelected();
      settings.autoInstall = myInstallCheckBox.isSelected();

      setRevealSettings(s, settings);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
      FormBuilder builder = new FormBuilder();

      myRevealNotFoundOrIncompatible = new HyperlinkLabel();
      myRevealNotFoundOrIncompatible.setIcon(AllIcons.General.BalloonError);
      myRevealNotFoundOrIncompatible.setHyperlinkTarget("https://revealapp.com");

      myNotAvailable = new JBLabel(RevealBundle.message("label.reveal.integration.only.available.for.ios.applications"));

      myInjectCheckBox = new JBCheckBox(IdeBundle.message("checkbox.inject.reveal.library.on.launch"));
      myInstallCheckBox = new JBCheckBox(IdeBundle.message("checkbox.upload.reveal.library.on.the.device.if.necessary"));

      myInjectHint = new JBLabel(UIUtil.ComponentStyle.SMALL);
      myInstallHint = new JBLabel(UIUtil.ComponentStyle.SMALL);

      myInjectCheckBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          updateControls();
        }
      });

      builder.addComponent(myNotAvailable);
      builder.addComponent(myInjectCheckBox, UIUtil.DEFAULT_VGAP * 3);
      builder.setFormLeftIndent(UIUtil.DEFAULT_HGAP * 4).setHorizontalGap(UIUtil.DEFAULT_HGAP * 4);
      builder.addComponent(myInjectHint);
      builder.setFormLeftIndent(UIUtil.DEFAULT_HGAP).setHorizontalGap(UIUtil.DEFAULT_HGAP);
      builder.addComponent(myInstallCheckBox);
      builder.setFormLeftIndent(UIUtil.DEFAULT_HGAP * 5).setHorizontalGap(UIUtil.DEFAULT_HGAP * 5);
      builder.addComponent(myInstallHint);

      JPanel controls = builder.getPanel();

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(controls, BorderLayout.NORTH);
      panel.add(Box.createGlue(), BorderLayout.CENTER);
      panel.add(myRevealNotFoundOrIncompatible, BorderLayout.SOUTH);
      return panel;
    }

    private void updateControls() {
      boolean controlsEnabled = isFound && isAvailable;

      myRevealNotFoundOrIncompatible.setVisible(!isFound);
      myNotAvailable.setVisible(!isAvailable);

      updateStatusAndHint(
        myInjectCheckBox, myInjectHint, controlsEnabled,
        RevealBundle.message("label.library.injected.on.launch.using.dyld.insert.libraries.variable")
      );

      boolean installButtonEnabled = controlsEnabled && myInjectCheckBox.isSelected();
      updateStatusAndHint(
        myInstallCheckBox, myInstallHint, installButtonEnabled,
        RevealBundle.message("label.it.s.not.necessary.to.configure.project.manually.br.library.signed.uploaded.automatically")
      );
    }

    private static void updateStatusAndHint(JComponent comp, JBLabel label, boolean enabled, @NlsContexts.Label String text) {
      comp.setEnabled(enabled);
      label.setEnabled(enabled);

      Color color = enabled ? UIUtil.getLabelForeground() : UIUtil.getLabelDisabledForeground();

      label.setText(
        new HtmlBuilder()
          .append(text)
          .wrapWith(HtmlChunk.font("#" + ColorUtil.toHex(color)))
          .wrapWith(HtmlChunk.html())
          .toString()
      );
    }
  }

  public static class RevealSettings implements Cloneable {
    public boolean autoInject;
    public boolean autoInstall = true;
    public boolean askToEnableAutoInstall = true;

    @Override
    public RevealSettings clone() {
      try {
        return (RevealSettings)super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
