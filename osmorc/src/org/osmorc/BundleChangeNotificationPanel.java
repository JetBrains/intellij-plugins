package org.osmorc;

import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.osmorc.settings.ProjectSettings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Notification panel for bundle changes.
 */
public class BundleChangeNotificationPanel extends EditorNotificationPanel {
  private AtomicBoolean myNeedsResync;
  private Project myProject;

  public BundleChangeNotificationPanel(Project project, AtomicBoolean needsResync) {
    myNeedsResync = needsResync;
    myProject = project;

    setText("Some libraries have changed in the project. This may have change the dependencies of your bundle.");

    createActionLabel("Synchronize Dependencies", new Runnable() {
      @Override
      public void run() {
        myNeedsResync.set(false);
        EditorNotifications.getInstance(myProject).updateAllNotifications();
        ModuleDependencySynchronizer.resynchronizeAll(myProject);
      }
    });


    createActionLabel("Enable Automatic Synchronization", new Runnable() {
      @Override
      public void run() {
        myNeedsResync.set(false);
        EditorNotifications.getInstance(myProject).updateAllNotifications();
        ProjectSettings ps = ProjectSettings.getInstance(myProject);
        ps.setManifestSynchronizationType(ProjectSettings.ManifestSynchronizationType.AutomaticallySynchronize);
        ModuleDependencySynchronizer.resynchronizeAll(myProject);
      }
    });
  }
}
