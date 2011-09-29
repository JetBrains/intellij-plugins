package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.components.StateStorageException;
import com.intellij.openapi.module.ModulePointerManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class DependenciesImpl implements ModifiableDependencies {

  private static final LinkageType DEFAULT_FRAMEWORK_LINKAGE = LinkageType.Default;

  @NotNull
  private String myTargetPlayer = "";

  @NotNull
  private ComponentSet myComponentSet = ComponentSet.SparkAndMx;

  @NotNull
  private LinkageType myFrameworkLinkage = DEFAULT_FRAMEWORK_LINKAGE;

  private final List<ModifiableDependencyEntry> myEntries = new ArrayList<ModifiableDependencyEntry>();

  @Nullable
  private SdkEntryImpl mySdk;

  @Override
  @Nullable
  public SdkEntry getSdkEntry() {
    return mySdk;
  }

  @Override
  public void setSdkEntry(@Nullable SdkEntry sdk) {
    mySdk = (SdkEntryImpl)sdk;
  }

  @Override
  public DependencyEntry[] getEntries() {
    return myEntries.toArray(new DependencyEntry[myEntries.size()]);
  }

  @Override
  public List<ModifiableDependencyEntry> getModifiableEntries() {
    return myEntries;
  }

  @Override
  @NotNull
  public LinkageType getFrameworkLinkage() {
    return myFrameworkLinkage;
  }

  @Override
  public void setFrameworkLinkage(@NotNull LinkageType frameworkLinkage) {
    myFrameworkLinkage = frameworkLinkage;
  }

  @Override
  @NotNull
  public String getTargetPlayer() {
    return myTargetPlayer;
  }

  @Override
  public void setTargetPlayer(@NotNull String targetPlayer) {
    myTargetPlayer = targetPlayer;
  }

  @Override
  public void setComponentSet(@NotNull ComponentSet componentSet) {
    myComponentSet = componentSet;
  }

  @Override
  @NotNull
  public ComponentSet getComponentSet() {
    return myComponentSet;
  }

  public DependenciesImpl getCopy() {
    DependenciesImpl copy = new DependenciesImpl();
    applyTo(copy);
    return copy;
  }

  void applyTo(DependenciesImpl copy) {
    copy.myTargetPlayer = myTargetPlayer;
    copy.myComponentSet = myComponentSet;
    copy.myFrameworkLinkage = myFrameworkLinkage;
    copy.myEntries.clear();
    copy.myEntries.addAll(ContainerUtil.map(myEntries, new Function<ModifiableDependencyEntry, ModifiableDependencyEntry>() {
      @Override
      public ModifiableDependencyEntry fun(ModifiableDependencyEntry e) {
        if (e instanceof ModuleLibraryEntryImpl) {
          return ((ModuleLibraryEntryImpl)e).getCopy();
        }
        else if (e instanceof BuildConfigurationEntryImpl) {
          return ((BuildConfigurationEntryImpl)e).getCopy();
        }
        else {
          throw new StateStorageException("Unexpected entry type: " + e);
        }
      }
    }));
    copy.mySdk = mySdk != null ? mySdk.getCopy() : null;
  }

  public boolean isEqual(DependenciesImpl other) {
    if (!other.myTargetPlayer.equals(myTargetPlayer)) return false;
    if (other.myComponentSet != myComponentSet) return false;
    if (other.myFrameworkLinkage != myFrameworkLinkage) return false;
    if (myEntries.size() != other.myEntries.size()) return false;
    if (mySdk != null ? (other.mySdk == null || !mySdk.isEqual(other.mySdk)) : other.mySdk != null) return false;
    for (int i = 0; i < myEntries.size(); i++) {
      if (!myEntries.get(i).isEqual(other.myEntries.get(i))) {
        return false;
      }
    }
    return true;
  }

  public State getState() {
    State state = new State();
    state.TARGET_PLAYER = myTargetPlayer;
    state.COMPONENT_SET = myComponentSet;
    state.FRAMEWORK_LINKAGE = myFrameworkLinkage.getSerializedText();
    state.ENTRIES = ContainerUtil.mapNotNull(myEntries.toArray(new ModifiableDependencyEntry[myEntries.size()]),
                                             new Function<ModifiableDependencyEntry, EntryState>() {
                                               @Override
                                               public EntryState fun(ModifiableDependencyEntry entry) {
                                                 if (entry instanceof StatefulDependencyEntry) {
                                                   return ((StatefulDependencyEntry)entry).getState();
                                                 }
                                                 else {
                                                   throw new StateStorageException("Unexpected entry type: " + entry);
                                                 }
                                               }
                                             }, new EntryState[0]);

    if (mySdk != null) {
      state.SDK = mySdk.getState();
    }
    return state;
  }

  public void loadState(@NotNull State state, Project project) {
    myTargetPlayer = state.TARGET_PLAYER;
    myComponentSet = state.COMPONENT_SET;
    myFrameworkLinkage = LinkageType.valueOf(state.FRAMEWORK_LINKAGE, DEFAULT_FRAMEWORK_LINKAGE);

    ModulePointerManager pointerManager = ModulePointerManager.getInstance(project);
    myEntries.clear();
    for (EntryState info : state.ENTRIES) {
      if (info.LIBRARY_ID != null) {
        ModuleLibraryEntryImpl libraryEntry = new ModuleLibraryEntryImpl(info.LIBRARY_ID);
        libraryEntry.getDependencyType().loadState(info.DEPENDENCY_TYPE);
        myEntries.add(libraryEntry);
      }
      else if (info.BC_NAME != null) {
        BuildConfigurationEntryImpl bcEntry = new BuildConfigurationEntryImpl(pointerManager.create(info.MODULE_NAME), info.BC_NAME);
        bcEntry.getDependencyType().loadState(info.DEPENDENCY_TYPE);
        myEntries.add(bcEntry);
      }
      else {
        throw new StateStorageException("unknown entry");
      }
    }
    mySdk = state.SDK != null ? new SdkEntryImpl(state.SDK) : null;
  }

  @Tag("dependencies")
  public static class State {
    @Attribute("target-player")
    public String TARGET_PLAYER = "";

    @Attribute("component-set")
    public ComponentSet COMPONENT_SET = ComponentSet.SparkAndMx;

    @Attribute("framework-linkage")
    public String FRAMEWORK_LINKAGE = DEFAULT_FRAMEWORK_LINKAGE.getSerializedText();

    @Tag("entries")
    @AbstractCollection(surroundWithTag = false)
    public EntryState[] ENTRIES = new EntryState[0];

    @Property(surroundWithTag = false)
    public SdkEntryImpl.State SDK;
  }
}
