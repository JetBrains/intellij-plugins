package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.FontUtil;
import com.jetbrains.cidr.cpp.execution.debugger.embedded.ui.FileChooseInput;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;

import static com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileDescriptor;
import static com.intellij.openapi.util.SystemInfo.isWindows;
import static com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER;
import static com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST;
import static com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL;
import static com.intellij.uiDesigner.core.GridConstraints.FILL_NONE;
import static com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK;
import static com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED;
import static com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW;
import static com.intellij.util.SystemProperties.getUserHome;
import static com.intellij.util.ui.UIUtil.getContextHelpForeground;
import static com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle.message;
import static java.io.File.separator;

class PlatformioSettingsPanel extends JBPanel<JBPanel<?>> {
  private static final FileChooserDescriptor PLATFORMIO_UTILITY_DESCRIPTOR = createSingleFileDescriptor("exe")
      .withShowHiddenFiles(true);
  private final FileChooseInput openOcdLocation;

  PlatformioSettingsPanel() {
    super(new GridLayoutManager(4, 2));
    final var utilityName = isWindows ? "platformio.exe" : "platformio";
    final var defaultLocationPath = Paths.get(getUserHome(), ".platformio", "penv", "Scripts", utilityName);
    final var defaultLocation = defaultLocationPath.toFile();
    openOcdLocation = new FileChooseInput("PlatformIO utility Location", defaultLocation, PLATFORMIO_UTILITY_DESCRIPTOR) {
        @Override
        public boolean validateFile(final @NotNull File file) {
          return file.isFile() && file.canExecute();
        }

        @Override
        protected @NotNull File getDefaultLocation() {
          return defaultLocation;
        }
      };

    add(new TitledSeparator(message("separator.general.settings")),
        new GridConstraints(0, 0, 1, 2, ANCHOR_WEST, FILL_HORIZONTAL,
                            SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null));
    add(openOcdLocation, new GridConstraints(1, 1, 1, 1, ANCHOR_WEST,
                                             FILL_HORIZONTAL, SIZEPOLICY_WANT_GROW | SIZEPOLICY_CAN_SHRINK, SIZEPOLICY_FIXED,
                                             null, null, null));


    final var label = new JLabel(message("platformio.utility.location"));
    add(label, new GridConstraints(1, 0, 1, 1, ANCHOR_WEST, FILL_NONE,
                                   SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null));

    final var hintLabel = new JLabel(message("platformio.path.hint",
            getUserHome(), separator, isWindows ? ".exe" : ""));
    hintLabel.setFont(FontUtil.minusOne(label.getFont()));
    hintLabel.setForeground(getContextHelpForeground());
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

  public void setText(final @NotNull String text) {
    openOcdLocation.setText(text);
  }
}
