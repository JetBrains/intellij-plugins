package jetbrains.plugins.yeoman.projectGenerator.template;

import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBCheckBox;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.YeomanRunGeneratorForm;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class YeomanProjectGeneratorRunPanel implements YeomanProjectGeneratorSubPanel {
  private final YeomanProjectGeneratorOwnerPanel myOwner;
  private final YeomanProjectGenerator.Settings mySettings;
  private final JBCheckBox myCheckBox;
  private YeomanRunGeneratorForm myForm;

  public boolean isReadyToClose() {
    return myNextClose;
  }

  private boolean myNextClose = false;
  private boolean myIsRendered = false;
  private boolean myIsEnable = true;


  public YeomanProjectGeneratorRunPanel(YeomanProjectGeneratorOwnerPanel owner,
                                        YeomanProjectGenerator.Settings settings) {
    myOwner = owner;
    mySettings = settings;

    myCheckBox = new JBCheckBox(YeomanBundle.message("checkbox.run.npm.install.bower.install"));
    myCheckBox.setSelected(true);
  }

  public boolean isRendered() {
    return myIsRendered;
  }

  @Override
  public void render() {
    if (!myIsRendered) {
      YeomanInstalledGeneratorInfo info = mySettings.info;
      final YeomanRunGeneratorForm.EventHandler handler = new YeomanRunGeneratorForm.EventHandler() {
        @Override
        public void handleEvent(YeomanRunGeneratorForm.EventTypes event) {
          switch (event) {
            case RENDERED -> {
              myIsEnable = true;
              myOwner.setMainButtonEnable(true);
            }
            case STARTING_ERROR, TERMINATED_OK, TERMINATED_ERROR -> {
              myOwner.setMainButtonEnable(true);
              myOwner.setMainButtonName("Complete");
              myNextClose = true;

              if (myForm != null) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.add(myCheckBox, BorderLayout.NORTH);
                myForm.getHolderPanel().setViewportView(wrapper);
              }
            }
          }
        }
      };
      myForm = new YeomanRunGeneratorForm(mySettings.appPath,
                                          null,
                                          YeomanGlobalSettings.getInstance(),
                                          info,
                                          handler,
                                          mySettings.options);

      myOwner.setCentralComponent(myForm.getMainPanel());
      myIsRendered = true;
    }
  }

  @Nullable
  @Override
  public String validate() {
    return null;
  }

  @Override
  @NotNull
  public YeomanProjectGeneratorSubPanel next(@Nullable ActionEvent e) {
    if (!isReadyToClose()) {
      myIsEnable = false;
      myOwner.setMainButtonEnable(false);
      myForm.next();
    }
    else {
      commitSettings();
      myOwner.close(e);
    }

    return this;
  }

  @Override
  public void commitSettings() {
    mySettings.runNpmAndBowerInstall = myCheckBox.isSelected();
  }

  @Override
  public boolean isCreateButtonEnabled() {
    return myIsEnable;
  }


  @Override
  public void dispose() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
  }
}
