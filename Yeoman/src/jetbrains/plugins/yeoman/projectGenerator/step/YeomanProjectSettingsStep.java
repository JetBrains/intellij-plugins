package jetbrains.plugins.yeoman.projectGenerator.step;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.DirectoryProjectGenerator;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGenerator;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorPanel;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class YeomanProjectSettingsStep extends ProjectSettingsStepBase {

  private YeomanProjectGeneratorPanel myPanel;

  public YeomanProjectSettingsStep(DirectoryProjectGenerator projectGenerator,
                                   AbstractNewProjectStep.AbstractCallback callback) {
    super(projectGenerator, callback);
  }

  @Override
  public boolean checkValid() {
    final boolean b = super.checkValid();
    if (!b) return false;

    if (myPanel != null) {
      if (!myPanel.isEnable()) {
        setErrorText("");
        return false;
      }

      final String validate = myPanel.validate();
      if (!StringUtil.isEmpty(validate)) {
        setErrorText(validate);
        return false;
      }
    }

    return true;
  }

  @Override
  public void onPanelSelected() {
    checkValid();
  }

  @Override
  public JPanel createPanel() {
    myLazyGeneratorPeer = createLazyPeer();
    if (myProjectGenerator instanceof YeomanProjectGenerator) {
      final YeomanProjectGeneratorPanel panel = ((YeomanProjectGenerator)myProjectGenerator).createPanel();

      panel.setValidateHandler(new YeomanProjectGeneratorPanel.ValidateHandler() {
        @Override
        public void validate() {
          checkValid();
        }
      });

      final ActionListener closeAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final DialogWrapper dialog = DialogWrapper.findInstance(myCreateButton);
          if (dialog != null) {
            dialog.close(DialogWrapper.OK_EXIT_CODE);
          }

          AbstractNewProjectStep.doGenerateProject(null, getProjectLocation(), myProjectGenerator, panel.getSettings());
        }
      };

      final LabeledComponent<TextFieldWithBrowseButton> component = createLocationComponent();
      component.setLabelLocation(BorderLayout.WEST);
      panel.init(component, createErrorLabel(), createActionButton(), closeAction);
      panel.showFirstStep();

      panel.getMainPanel().addAncestorListener(new AncestorListener() {
        @Override
        public void ancestorAdded(AncestorEvent event) {

        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
          Disposer.dispose(panel);
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {

        }
      });
      registerValidators();

      myPanel = panel;
      return panel.getMainPanel();
    }


    return new JPanel();
  }
}
