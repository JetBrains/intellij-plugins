package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModulePointerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;

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
    @Tag("library")
    public Element LIBRARY_ELEMENT;
  }

  @Tag("entries")
  @AbstractCollection(surroundWithTag = false)
  public EntryInfo[] getSerializedEntries() {
    if (myEntries.isEmpty()) {
      return EMPTY;
    }
    return ContainerUtil.mapNotNull(myEntries.toArray(new DependencyEntry[myEntries.size()]), new Function<DependencyEntry, EntryInfo>() {
      @Override
      public EntryInfo fun(DependencyEntry entry) {
        if (entry instanceof BuildConfigurationEntry) {
          BuildConfigurationEntry buildConfigurationEntry = (BuildConfigurationEntry)entry;
          FlexIdeBuildConfiguration buildConfiguration = buildConfigurationEntry.getBuildConfiguration();
          if (buildConfiguration == null) {
            LOG.error("module or BC is unexpectedly missing"); // looks like our model is inconsistent, we missed module or BC deletion
            return null;
          }

          EntryInfo entryInfo = new EntryInfo();
          entryInfo.MODULE_NAME = buildConfigurationEntry.getModuleName();
          entryInfo.BC_NAME = buildConfiguration.NAME;
          return entryInfo;
        }
        else if (entry instanceof LibraryEntry) {
          EntryInfo entryInfo = new EntryInfo();
          entryInfo.LIBRARY_ELEMENT = new Element("library");
          try {
            ((LibraryEntry)entry).writeExternal(entryInfo.LIBRARY_ELEMENT);
          }
          catch (WriteExternalException e) {
            LOG.error(e);
            return null;
          }
          return entryInfo;
        }
        else {
          throw new IllegalArgumentException("unknown type: " + entry.getClass());
        }
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
      if (info.LIBRARY_ELEMENT != null) {
        LibraryEntry libraryEntry = new LibraryEntry();
        try {
          libraryEntry.readExternal(info.LIBRARY_ELEMENT);
          myEntries.add(libraryEntry);
        }
        catch (InvalidDataException e) {
          LOG.error(e);
        }
      }
      else if (info.BC_NAME != null) {
        myEntries.add(new BuildConfigurationEntry(pointerManager.create(info.MODULE_NAME), info.BC_NAME));
      }
      else {
        LOG.error("unknown entry");
      }
    }
    myEntriesInfos = null;
  }

  protected Dependencies clone() {
    try {
      Dependencies clone = (Dependencies)super.clone();
      clone.COMPONENT_SET = COMPONENT_SET;
      clone.FRAMEWORK_LINKAGE = FRAMEWORK_LINKAGE;
      clone.myEntriesInfos = myEntriesInfos;
      clone.myEntries = new ArrayList<DependencyEntry>(myEntries);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
