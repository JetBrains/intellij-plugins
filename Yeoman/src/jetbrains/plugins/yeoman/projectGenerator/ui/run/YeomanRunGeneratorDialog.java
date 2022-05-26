package jetbrains.plugins.yeoman.projectGenerator.ui.run;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class YeomanRunGeneratorDialog extends DialogWrapper {

  private final VirtualFile myToProcess;
  @Nullable
  private final YeomanInstalledGeneratorInfo myInfo;


  //access only from EDT
  private Action myRealOkAction;
  private boolean myToClose;
  private YeomanRunGeneratorForm myForm;

  private final Project myProject;


  public YeomanRunGeneratorForm getForm() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    return myForm;
  }


  public YeomanRunGeneratorDialog(@NotNull Project project, @NotNull VirtualFile toProcess, @Nullable YeomanInstalledGeneratorInfo info) {
    super(false);
    myToProcess = toProcess;
    myInfo = info;
    setTitle(YeomanBundle.message("yeoman.generator.run.action.title"));
    myProject = project;
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    final YeomanRunGeneratorForm.EventHandler close = new YeomanRunGeneratorForm.EventHandler() {
      @Override
      public void handleEvent(final YeomanRunGeneratorForm.EventTypes event) {
        UIUtil.invokeLaterIfNeeded(() -> {
          ApplicationManager.getApplication().assertIsDispatchThread();
          switch (event) {
            case RENDERED: {
              getOKAction().setEnabled(true);
              break;
            }
            case TERMINATED_OK:
            case TERMINATED_ERROR: {
              getOKAction().setEnabled(true);
              getOKAction().putValue(Action.NAME, YeomanBundle.message("yeoman.generator.run.action.close"));
              getCancelAction().setEnabled(false);
              myToClose = true;
              break;
            }
            default:
              //nothing
          }
        });
      }
    };
    myForm = new YeomanRunGeneratorForm(myToProcess.getCanonicalPath(),
                                        myProject,
                                        YeomanGlobalSettings.getInstance(),
                                        myInfo,
                                        close,
                                        null) {
      @Override
      protected boolean hasWelcome() {
        return true;
      }
    };
    return myForm.getMainPanel();
  }

  @Override
  protected void createDefaultActions() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    super.createDefaultActions();
    myRealOkAction = myOKAction;
    myOKAction = new AbstractAction(YeomanBundle.message("yeoman.generator.run.action.next")) {
      @Override
      public void actionPerformed(ActionEvent e) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (myToClose) {
          myRealOkAction.actionPerformed(e);
          myToProcess.refresh(true, true);
          return;
        }
        getOKAction().setEnabled(false);

        myForm.next();
      }
    };
  }
}
