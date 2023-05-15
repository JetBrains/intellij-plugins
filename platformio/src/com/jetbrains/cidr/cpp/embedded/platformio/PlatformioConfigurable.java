package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.openapi.options.Configurable.isFieldModified;

public class PlatformioConfigurable implements SearchableConfigurable {

  private static final String ID = "PlatformIO.settings";
  private static final String PIO_LOCATION_KEY = ID + ".platformio.location";
  private PlatformioSettingsPanel mySettingsPanel;

  @Override
  @NlsContexts.ConfigurableName
  public String getDisplayName() {
    return ClionEmbeddedPlatformioBundle.message("configurable.ClionEmbeddedPlatformioBundle.display.name");
  }

  @Override
  @Nullable
  public JComponent createComponent() {
    mySettingsPanel = new PlatformioSettingsPanel();
    return mySettingsPanel;
  }

  @Override
  public boolean isModified() {
    return mySettingsPanel != null && isFieldModified(mySettingsPanel.getTextField(), getPioLocation());
  }

  @Override
  public void apply() {
    if (mySettingsPanel != null) {
      PropertiesComponent.getInstance().setValue(PIO_LOCATION_KEY, mySettingsPanel.getText().trim());
    }
  }

  @Override
  public void reset() {
    if (mySettingsPanel != null) {
      mySettingsPanel.setText(getPioLocation());
    }
  }

  @Override
  @NotNull
  public String getId() {
    return ID;
  }

  @NotNull
  public static String getPioLocation() {
    return PropertiesComponent.getInstance().getValue(PIO_LOCATION_KEY, "").trim();
  }

  @Override
  public @Nullable String getHelpTopic() {
    return "settings.plugin.platformio";
  }
}
