package jetbrains.plugins.yeoman.projectGenerator.builder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.StepWithSubSteps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorPanel;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorRunPanel;

import javax.swing.*;
import java.awt.*;

public class YeomanRunGeneratorWizardStep extends ModuleWizardStep implements StepWithSubSteps, Disposable {
  private final YeomanModuleBuilder myBuilder;
  private JPanel myOwnerPanel;
  private YeomanProjectGeneratorRunPanel myPanel;
  private JScrollPane myScrollPane;

  public YeomanRunGeneratorWizardStep(YeomanModuleBuilder builder, WizardContext context) {
    super();
    myBuilder = builder;

    Disposer.register(context.getDisposable(), this);
  }


  @Override
  public JComponent getComponent() {
    if (myOwnerPanel == null) {
      myOwnerPanel = new JPanel(new BorderLayout());
      myScrollPane = YeomanProjectGeneratorPanel.createScrollPane();
      myOwnerPanel.add(myScrollPane, BorderLayout.CENTER);

    }

    return myOwnerPanel;
  }

  private void createPanelIfNecessary() {
    if (myPanel == null) {
      myPanel = new YeomanProjectGeneratorRunPanel(YeomanSelectGeneratorWizardStep.createOwnerPanel(myScrollPane, myOwnerPanel),
                                                   myBuilder.getSettings());
    }
  }

  @Override
  public void updateDataModel() {
    if (myPanel != null) {
      if (myPanel.isReadyToClose()) {
        myPanel.commitSettings();
        Disposer.dispose(myPanel);
      }
      else {
        if (myPanel.isRendered()) {
          myPanel.next(null);
        }
      }
    }
  }

  @Override
  public void updateStep() {
    if (myOwnerPanel != null) {
      createPanelIfNecessary();

      if (!myPanel.isRendered()) {
        myPanel.render();
      }
    }
  }

  @Override
  public boolean isLast() {
    return myPanel == null || myPanel.isReadyToClose();
  }

  @Override
  public boolean isFirst() {
    return true;
  }

  //kill(!)
  @Override
  public void doPreviousAction() {
    if (myPanel != null) {
      Disposer.dispose(myPanel);
      myPanel = null;
    }
  }

  @Override
  public void dispose() {
    if (myPanel != null) {
      Disposer.dispose(myPanel);
      myPanel = null;
    }
  }
}
