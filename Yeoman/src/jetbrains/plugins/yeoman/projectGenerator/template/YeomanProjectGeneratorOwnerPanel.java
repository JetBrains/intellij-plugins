package jetbrains.plugins.yeoman.projectGenerator.template;


import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public interface YeomanProjectGeneratorOwnerPanel {

  interface ValidateHandler {

    void validate();
  }

  @Nullable
  ValidateHandler getValidateHandler();

  void setCentralComponent(@NotNull JComponent component);

  void setBottomComponent(JComponent component);

  void setMainButtonEnable(boolean isEnable);

  @NotNull
  JPanel getMainPanel();

  void setMainButtonName(@NotNull String newName);

  @NotNull
  String getLocationTitle();

  @Nullable
  LabeledComponent<TextFieldWithBrowseButton> getLocationComponent();

  void close(ActionEvent e);
}

