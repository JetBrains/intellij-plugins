package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.util.ArrayList;
import java.util.List;

@State(name = "LastSuccessfulUpdateTracker", storages = @Storage(StoragePathMacros.PRODUCT_WORKSPACE_FILE))
public final class LastSuccessfulUpdateTracker implements PersistentStateComponent<LastSuccessfulUpdateTracker.ChangesUpdateResult> {
  public static LastSuccessfulUpdateTracker getInstance(Project project) {
    return project.getService(LastSuccessfulUpdateTracker.class);
  }

  public static class ChangedFile {
    @Attribute("beforePath")
    public String beforePath;
    @Attribute("afterPath")
    public String afterPath;
  }

  public static class PersistentChangeList {
    public String name;
    public List<ChangedFile> files = new ArrayList<>();
  }

  public static class ChangesUpdateResult {
    public List<PersistentChangeList> changeLists = new ArrayList<>();
  }

  private final Project myProject;
  private ChangesUpdateResult myResult = new ChangesUpdateResult();
  private boolean myUpdateSuccessful;

  public LastSuccessfulUpdateTracker(Project project) {
    myProject = project;
    project.getMessageBus().connect().subscribe(ChangeListListener.TOPIC, new MyChangeListListener());
  }

  @Override
  public ChangesUpdateResult getState() {
    return myResult;
  }

  @Override
  public void loadState(@NotNull ChangesUpdateResult state) {
    myResult = state;
  }

  public void updateStarted() {
    myUpdateSuccessful = false;
  }

  public void updateSuccessful() {
    myUpdateSuccessful = true;
  }

  public List<PersistentChangeList> getChangeLists() {
    return myResult.changeLists;
  }

  private class MyChangeListListener extends ChangeListAdapter {
    @Override
    public void changeListUpdateDone() {
      if (myUpdateSuccessful && PerforceSettings.getSettings(myProject).ENABLED) {
        List<PersistentChangeList> results = new ArrayList<>();
        List<LocalChangeList> changeLists = ChangeListManager.getInstance(myProject).getChangeLists();
        for (LocalChangeList changeList : changeLists) {
          PersistentChangeList persistentList = new PersistentChangeList();
          persistentList.name = changeList.getName();
          persistentList.files = new ArrayList<>();
          results.add(persistentList);

          for (Change c : changeList.getChanges()) {
            ChangedFile f = new ChangedFile();
            ContentRevision beforeRevision = c.getBeforeRevision();
            if (beforeRevision != null) {
              f.beforePath = beforeRevision.getFile().getPath();
            }
            ContentRevision afterRevision = c.getAfterRevision();
            f.afterPath = afterRevision == null ? null : afterRevision.getFile().getPath();
            persistentList.files.add(f);
          }
        }
        myResult.changeLists = results;
      }
    }
  }
}
