package org.osmorc;

import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NotNull;
import org.osmorc.settings.ProjectSettings;

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

        ManifestChangeWatcher.resynchronizeDependencies(modifiedFile);
      }
    });


    createActionLabel("Enable Automatic Synchronization", new Runnable() {
      @Override
      public void run() {
        myNeedsResync.set(false);
        EditorNotifications.getInstance(modifiedFile.getProject()).updateAllNotifications();
        ProjectSettings ps =  ProjectSettings.getInstance(modifiedFile.getProject());
        ps.setManifestSynchronizationType(ProjectSettings.ManifestSynchronizationType.AutomaticallySynchronize);
        ManifestChangeWatcher.resynchronizeDependencies(modifiedFile);
      }
    });

    myLinksPanel.setBackground(LightColors.GREEN);
  }

  @Override
  public Color getBackground() {
    return LightColors.GREEN;
  }
}
