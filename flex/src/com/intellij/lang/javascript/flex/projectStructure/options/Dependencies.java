package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModulePointerManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.ArrayList;
import java.util.List;

public class Dependencies implements Cloneable {

  private static final Logger LOG = Logger.getInstance(Dependencies.class.getName());

  public FlexIdeBuildConfiguration.ComponentSet COMPONENT_SET = FlexIdeBuildConfiguration.ComponentSet.SparkAndMx;
  public FlexIdeBuildConfiguration.FrameworkLinkage FRAMEWORK_LINKAGE = FlexIdeBuildConfiguration.FrameworkLinkage.Default;

  private static final EntryInfo[] EMPTY = new EntryInfo[0];

  private EntryInfo[] myEntriesInfos = EMPTY;
  private List<DependencyEntry> myEntries = new ArrayList<DependencyEntry>();

  @Tag("entry")
  public static class EntryInfo {
    @Tag("module")
    public String MODULE_NAME;
    @Tag("buildConfiguration")
    public String BC_NAME;
  }

  @Tag("entries")
  @AbstractCollection(surroundWithTag = false)
  public EntryInfo[] getSerializedEntries() {
    if (myEntries.isEmpty()) {
      return EMPTY;
    }
    return ContainerUtil.mapNotNull(myEntries.toArray(new DependencyEntry[myEntries.size()]), new Function<DependencyEntry, EntryInfo>() {
      @Override
      public EntryInfo fun(DependencyEntry dependency) {
        BuildConfigurationEntry d = (BuildConfigurationEntry)dependency;
        FlexIdeBuildConfiguration buildConfiguration = d.getBuildConfiguration();
        if (buildConfiguration == null) {
          LOG.error("module or BC is unexpectedly missing"); // looks like our model is inconsistent, we missed module or BC deletion
          return null;
        }

        EntryInfo entryInfo = new EntryInfo();
        entryInfo.MODULE_NAME = d.getModuleName();
        entryInfo.BC_NAME = buildConfiguration.NAME;
        return entryInfo;
      }
    }, new EntryInfo[0]);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSerializedEntries(EntryInfo[] entries) {
    myEntriesInfos = entries;
  }

  public List<DependencyEntry> getEntries() {
    return myEntries;
  }

  public void materialize(Project project) {
    LOG.assertTrue(myEntries.isEmpty(), "already materialized");
    ModulePointerManager pointerManager = ModulePointerManager.getInstance(project);
    myEntries = new ArrayList<DependencyEntry>(myEntriesInfos.length);
    for (EntryInfo info : myEntriesInfos) {
      myEntries.add(new BuildConfigurationEntry(pointerManager.create(info.MODULE_NAME), info.BC_NAME));
    }
    myEntriesInfos = null;
  }

  protected Dependencies clone() {
    try {
      Dependencies clone = (Dependencies)super.clone();
      clone.COMPONENT_SET = COMPONENT_SET;
      clone.FRAMEWORK_LINKAGE = FRAMEWORK_LINKAGE;
      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
