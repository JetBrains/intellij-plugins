package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.FontUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.cidr.cpp.execution.debugger.embedded.ui.FileChooseInput;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.intellij.uiDesigner.core.GridConstraints.*;

class PlatformioSettingsPanel extends JBPanel<JBPanel<?>> {
  private static final FileChooserDescriptor PLATFORMIO_UTILITY_DESCRIPTOR =
    FileChooserDescriptorFactory.createSingleFileDescriptor("exe")
      .withShowHiddenFiles(true);
  private final FileChooseInput openOcdLocation;

  PlatformioSettingsPanel() {
    super(new GridLayoutManager(4, 2));
    String utilityName = SystemInfo.isWindows ? "platformio.exe" : "platformio";
    Path defaultLocationPath =
      Paths.get(SystemProperties.getUserHome(), ".platformio", "penv", "Scripts", utilityName);
    File defaultLocation = defaultLocationPath.toFile();
    openOcdLocation =
      new FileChooseInput("PlatformIO utility Location", defaultLocation, PLATFORMIO_UTILITY_DESCRIPTOR) {
        @Override
        public boolean validateFile(@NotNull File file) {
          return file.isFile() && file.canExecute();
        }

        @Override
        protected @NotNull File getDefaultLocation() {
          return defaultLocation;
        }
      };

    add(new TitledSeparator(ClionEmbeddedPlatformioBundle.message("separator.general.settings")),
        new GridConstraints(0, 0, 1, 2, ANCHOR_WEST, FILL_HORIZONTAL,
                            SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null));
    add(openOcdLocation, new GridConstraints(1, 1, 1, 1, ANCHOR_WEST,
                                             FILL_HORIZONTAL, SIZEPOLICY_WANT_GROW | SIZEPOLICY_CAN_SHRINK, SIZEPOLICY_FIXED,
                                             null, null, null));


    JLabel label = new JLabel(ClionEmbeddedPlatformioBundle.message("platformio.utility.location"));
    add(label, new GridConstraints(1, 0, 1, 1, ANCHOR_WEST, FILL_NONE,
                                   SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null));

    JLabel hintLabel = new JLabel(
      ClionEmbeddedPlatformioBundle.message("platformio.path.hint", SystemProperties.getUserHome(),
                                            File.separator, SystemInfo.isWindows ? ".exe" : ""));
    hintLabel.setFont(FontUtil.minusOne(label.getFont()));
    hintLabel.setForeground(UIUtil.getContextHelpForeground());
    add(hintLabel, new GridConstraints(2, 1, 1, 1, ANCHOR_WEST, FILL_NONE,
                                       SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null));
    label.setLabelFor(openOcdLocation);
    add(new Spacer(), new GridConstraints(3, 0, 1, 2, ANCHOR_CENTER, FILL_NONE,
                                          SIZEPOLICY_FIXED, SIZEPOLICY_WANT_GROW, null, null, null));
  }

  @NotNull
  public JTextField getTextField() {
    return openOcdLocation.getTextField();
  }

  @NotNull
  public String getText() {
    return openOcdLocation.getText();
  }

  public void setText(@NotNull String text) {
    openOcdLocation.setText(text);
  }
}
