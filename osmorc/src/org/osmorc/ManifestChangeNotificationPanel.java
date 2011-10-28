package org.osmorc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Notification panel for manifest changes.
 */
public class ManifestChangeNotificationPanel extends EditorNotificationPanel {
  private AtomicBoolean myNeedsResync;

  public ManifestChangeNotificationPanel(@NotNull final PsiFile modifiedFile, AtomicBoolean needsResync) {
    myNeedsResync = needsResync;

    setText("You have modified a manifest in your project. This may have change the dependencies of your bundle.");

    createActionLabel("Synchronize Dependencies", new Runnable() {
      @Override
      public void run() {
        myNeedsResync.set(false);
        EditorNotifications.getInstance(modifiedFile.getProject()).updateAllNotifications();

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            new Task.Backgroundable(modifiedFile.getProject(), "Processing manifest change", false) {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Synchronizing dependencies.");
                indicator.setIndeterminate(true);
                // sync the dependencies of ALL modules
                ModuleDependencySynchronizer.resynchronizeAll(modifiedFile.getProject());
              }
            }.queue();
          }
        });

      }
    });

    myLinksPanel.setBackground(LightColors.GREEN);
  }

  @Override
  public Color getBackground() {
    return LightColors.GREEN;
  }
}
