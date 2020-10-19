package org.jetbrains.idea.perforce.application;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.text.UniqueNameGenerator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceChangeList;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

// change lists sync should always be done right before changes update (from PerforceChangeProvider)
@State(name = "PerforceNumberNameSynchronizer", storages = @Storage(StoragePathMacros.WORKSPACE_FILE), reportStatistic = false)
public class PerforceNumberNameSynchronizer implements PersistentStateComponent<PerforceNumberNameSynchronizer.ConfigBean> {
  private final Map<ConnectionKey, PerforceNumberNameMap> myMap = new HashMap<>();
  private final Object myLock = new Object();

  private final Project myProject;
  private ConfigBean myConfig = new ConfigBean();

  PerforceNumberNameSynchronizer(final Project project) {
    myProject = project;
  }

  public void handleChangeListSubmitted(@NotNull P4Connection connection, long changeListNumber, long newNumber) {
    if (changeListNumber > 0 && newNumber > 0) {
      synchronized (myLock) {
        PerforceNumberNameMap map = myMap.get(connection.getConnectionKey());
        if (map != null) {
          String prevName = map.removeList(changeListNumber);
          if (prevName != null) {
            map.put(prevName, newNumber);
          }
        }
      }
    }
  }

  void startListening(@NotNull Disposable parentDisposable) {
    ChangeListManager.getInstance(myProject).addChangeListListener(new PerforceChangeListListener(myProject, this), parentDisposable);
  }

  public static PerforceNumberNameSynchronizer getInstance(final Project project) {
    return project.getService(PerforceNumberNameSynchronizer.class);
  }

  void removeNonexistentKeys(Set<ConnectionKey> keys) {
    synchronized (myLock) {
      myMap.keySet().retainAll(keys);
    }
  }

  // assumes that changelist with this name already exists in IDEA; it only can not exist in perforce
  public Collection<Long> findOrCreate(@NotNull final P4Connection connection, final LocalChangeList list) throws VcsException {
    final String listName = list.getName();
    if (list.hasDefaultName()) return Collections.singletonList(-1L);
    final ConnectionKey key = connection.getConnectionKey();

    String description;
    synchronized (myLock) {
      PerforceNumberNameMap map = ensureMapping(key);
      Long number = map.getNumber(listName);
      if (number != null) {
        return Collections.singletonList(number);
      }
      description = getP4Description(list, map);
    }

    final long newNumber = createList(connection, description);
    synchronized (myLock) {
      ensureMapping(key).put(listName, newNumber);
      return Collections.singletonList(newNumber);
    }
  }

  private long createList(final P4Connection connection, String description) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(myProject);

    for (PerforceChangeList changeList : runner.getPendingChangeLists(connection)) {
      if (Objects.equals(changeList.getName(), description) && changeList.getChanges().isEmpty()) {
        return changeList.getNumber();
      }
    }
    return runner.createChangeList(description, connection, null);
  }

  private static String getP4Description(final ChangeList list, final PerforceNumberNameMap map) {
    String description = list.getComment().trim();
    if (description.length() == 0) return list.getName();
    return map.getNumber(description) != null ? list.getName() : description;
  }

  private PerforceNumberNameMap ensureMapping(ConnectionKey key) {
    return myMap.computeIfAbsent(key, __ -> new PerforceNumberNameMap());
  }

  /** @return the set of change lists not present anymore in p4, that have to be removed from IDEA */
  Set<String> acceptInfo(ConnectionKey key, Collection<PerforceChangeList> lists, ChangeListManagerGate gate) {
    synchronized (myLock) {
      PerforceNumberNameMap prevMap = ensureMapping(key);
      PerforceNumberNameMap currentMap = new PerforceNumberNameMap();

      for (PerforceChangeList changeList : ContainerUtil.sorted(lists, Comparator.comparing(PerforceChangeList::getNumber))) {
        String currentName = obtainIdeaChangeList(gate, prevMap, currentMap, changeList);
        currentMap.put(currentName, changeList.getNumber());
      }

      return prevMap.updateMapping(currentMap);
    }
  }

  /**
   * If there was IDEA changelist previously associated with this p4 changelist, return it (possibly simplifying its name along the way).
   * Otherwise create a new changelist with a name based on the first line of p4 description. If several p4 changelists have the same first line, make the names unique by adding (2), (3), etc.
   */
  @NotNull
  private static String obtainIdeaChangeList(@NotNull ChangeListManagerGate gate,
                                             @NotNull PerforceNumberNameMap prevMap,
                                             @NotNull PerforceNumberNameMap currentMap,
                                             @NotNull PerforceChangeList changeList) {
    String p4Description = changeList.getComment();
    String baseP4Name = toOneLine(p4Description);

    String associatedName = prevMap.getName(changeList.getNumber());
    LocalChangeList existingList = associatedName == null ? null : gate.findChangeList(associatedName);
    if (existingList != null) {
      String ideaComment = StringUtil.notNullize(existingList.getComment());
      return ideaComment.equals(p4Description)
             ? simplifyNameIfPossible(gate, associatedName, baseP4Name)
             : resetFromNativeDescription(gate, p4Description, ideaComment, associatedName);
    }

    // if there's an existing list named just right, bind to it
    if (gate.findChangeList(baseP4Name) != null && prevMap.getNumber(baseP4Name) == null && currentMap.getNumber(baseP4Name) == null) {
      return baseP4Name;
    }

    return gate.addChangeList(suggestUniqueChangeListName(gate, baseP4Name), p4Description).getName();
  }

  @NotNull
  private static String resetFromNativeDescription(ChangeListManagerGate gate,
                                                   String nativeDescription,
                                                   String ideaComment,
                                                   String listName) {
    gate.editComment(listName, nativeDescription);
    String baseName = toOneLine(nativeDescription);
    if (isAutoGeneratedName(listName, toOneLine(ideaComment)) && !isAutoGeneratedName(listName, baseName)) {
      String uniqueName = suggestUniqueChangeListName(gate, baseName);
      gate.editName(listName, uniqueName);
      return uniqueName;
    }
    return listName;
  }

  private static String simplifyNameIfPossible(ChangeListManagerGate gate, String listName, String simplestName) {
    if (isAutoGeneratedName(listName, simplestName) && !listName.equals(simplestName) && gate.findChangeList(simplestName) == null) {
      gate.editName(listName, simplestName);
      return simplestName;
    }
    return listName;
  }

  private static final Pattern AUTO_GENERATED_NAME_PATTERN = Pattern.compile("(.*?)( \\(\\d+\\))?");

  private static boolean isAutoGeneratedName(String name, String baseName) {
    Matcher matcher = AUTO_GENERATED_NAME_PATTERN.matcher(name);
    return matcher.matches() && baseName.equals(matcher.group(1));
  }

  @NotNull
  private static String suggestUniqueChangeListName(ChangeListManagerGate gate, String baseName) {
    return UniqueNameGenerator.generateUniqueName(baseName, "", "", " (", ")", s -> gate.findChangeList(s) == null);
  }

  @NotNull
  @VisibleForTesting
  public static String toOneLine(String description) {
    description = description.trim();
    int pos = description.indexOf("\n");
    return pos >= 0 ? description.substring(0, pos).trim() + "..." : description;
  }

  public String getName(@NotNull ConnectionKey key, @NotNull Long number) {
    synchronized (myLock) {
      PerforceNumberNameMap map = myMap.get(key);
      return map != null ? map.getName(number) : null;
    }
  }

  @Nullable
  public Long getNumber(@NotNull ConnectionKey key, @NotNull String name) {
    synchronized (myLock) {
      PerforceNumberNameMap map = myMap.get(key);
      return map != null ? map.getNumber(name) : null;
    }
  }

  public MultiMap<ConnectionKey, Long> getAllNumbers(@NotNull String name) {
    final MultiMap<ConnectionKey, Long> numbers = MultiMap.create();
    synchronized (myLock) {
      for (Map.Entry<ConnectionKey, PerforceNumberNameMap> entry : myMap.entrySet()) {
        Long number = entry.getValue().getNumber(name);
        if (number != null) {
          numbers.putValue(entry.getKey(), number);
        }
      }
    }
    return numbers;
  }

  void setHidden(Long number, boolean hidden) {
    synchronized (myLock) {
      if (hidden) {
        myConfig.removedFromIdea.add(number);
      } else {
        myConfig.removedFromIdea.remove(number);
      }
    }
  }

  public boolean isHidden(Long number) {
    synchronized (myLock) {
      return myConfig.removedFromIdea.contains(number);
    }
  }

  void renameList(@NotNull String from, @NotNull String to) {
    synchronized (myLock) {
      myMap.values().forEach(map -> map.rename(from, to));
    }
  }

  void removeList(long nativeNumber) {
    synchronized (myLock) {
      myMap.values().forEach(map -> map.removeList(nativeNumber));
    }
    setHidden(nativeNumber, true);
  }

  @Nullable
  @Override
  public ConfigBean getState() {
    synchronized (myLock) {
      myConfig.listMappings.clear();
      for (Map.Entry<ConnectionKey, PerforceNumberNameMap> entry : myMap.entrySet()) {
        ConnectionKeyBean bean = new ConnectionKeyBean();
        bean.server = entry.getKey().getServer();
        bean.client = entry.getKey().getClient();
        bean.user   = entry.getKey().getUser();
        myConfig.listMappings.put(bean, entry.getValue());
      }
      return myConfig;
    }
  }

  @Override
  public void loadState(@NotNull ConfigBean state) {
    synchronized (myLock) {
      myConfig = state;
      myMap.clear();
      for (Map.Entry<ConnectionKeyBean, PerforceNumberNameMap> entry : myConfig.listMappings.entrySet()) {
        myMap.put(new ConnectionKey(entry.getKey().server, entry.getKey().client, entry.getKey().user), entry.getValue());
      }
    }
  }

  public static class ConfigBean {
    public Set<Long> removedFromIdea = new LinkedHashSet<>();

    public Map<ConnectionKeyBean, PerforceNumberNameMap> listMappings = new HashMap<>();
  }

  public static class ConnectionKeyBean {
    public String server;
    public String client;
    public String user;
  }
}
