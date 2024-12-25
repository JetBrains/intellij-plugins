package jetbrains.plugins.yeoman.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import jetbrains.plugins.yeoman.projectGenerator.ui.list.YeomanGeneratorsMain;
import jetbrains.plugins.yeoman.projectGenerator.ui.list.YeomanInstalledGeneratorsMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class YeomanStartAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    DialogWrapper wrapper = new DialogWrapper(e.getProject(), true) {
      {
        init();
      }

      @Override
      protected @Nullable JComponent createCenterPanel() {
        final YeomanGeneratorsMain main = new YeomanInstalledGeneratorsMain();
        return main.getMainPanel();
      }
    };
    wrapper.showAndGet();
  }
}
