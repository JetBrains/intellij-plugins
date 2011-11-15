package org.osmorc;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.impl.BundleModificationListener;
import org.osmorc.impl.MyBundleManager;
import org.osmorc.settings.ProjectSettings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A project component that watches changes to the bundle structure (e.g. libraries / modules added) displays a notification bar offering to resynchronize dependencies.
 */
public class BundleIndexModificationWatcher implements EditorNotifications.Provider<BundleChangeNotificationPanel> {

  private static final Key<BundleChangeNotificationPanel> KEY = Key.create("BundleChangeNotificationPanelKey");
  private Project myProject;
  private AtomicBoolean myNeedsResync = new AtomicBoolean(false);

  public BundleIndexModificationWatcher(@NotNull Project project, @NotNull MessageBus messageBus) {
    myProject = project;
    final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD, myProject);

    // Subscribe to VFS-change events.
    messageBus.connect().subscribe(MyBundleManager.BUNDLE_INDEX_CHANGE_TOPIC, new BundleModificationListener() {
      @Override
      public void bundlesChanged() {
        if (isAtLeastOneModuleManuallyEdited()) {
          myAlarm.cancelAllRequests();
          myAlarm.addRequest(new Runnable() {
            @Override
            public void run() {
              final ProjectSettings settings = ProjectSettings.getInstance(myProject);
              if (settings.getManifestSynchronizationType() == ProjectSettings.ManifestSynchronizationType.ManuallySynchronize) {
                myNeedsResync.set(true);
                EditorNotifications.getInstance(myProject).updateAllNotifications();
              }
              if (settings.getManifestSynchronizationType() == ProjectSettings.ManifestSynchronizationType.AutomaticallySynchronize) {
                myNeedsResync.set(false);
                EditorNotifications.getInstance(myProject).updateAllNotifications();
                ModuleDependencySynchronizer.resynchronizeAll(myProject);
              }
            }
          }, 500);
        }
      }
    });
  }

  @Override
  public Key<BundleChangeNotificationPanel> getKey() {
    return KEY;
  }

  private boolean isAtLeastOneModuleManuallyEdited() {
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      OsmorcFacet instance = OsmorcFacet.getInstance(module);
      if (instance != null && instance.getConfiguration().isManifestManuallyEdited()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public BundleChangeNotificationPanel createNotificationPanel(VirtualFile file) {
    if (!myNeedsResync.get()) {
      return null;
    }

    return new BundleChangeNotificationPanel(myProject, myNeedsResync);
  }
}
