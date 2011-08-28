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
  public LinkageType FRAMEWORK_LINKAGE = LinkageType.Default;
  private static final String DEPENDENCY_TYPE_ELEMENT_NAME = "type";

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
    @Tag("type")
    public Element DEPENDENCY_TYPE_ELEMENT;
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
        EntryInfo entryInfo = new EntryInfo();
        entryInfo.DEPENDENCY_TYPE_ELEMENT = new Element(DEPENDENCY_TYPE_ELEMENT_NAME);
        entry.myDependencyType.writeExternal(entryInfo.DEPENDENCY_TYPE_ELEMENT);

        if (entry instanceof BuildConfigurationEntry) {
          BuildConfigurationEntry buildConfigurationEntry = (BuildConfigurationEntry)entry;
          FlexIdeBuildConfiguration buildConfiguration = buildConfigurationEntry.getBuildConfiguration();
          if (buildConfiguration == null) {
            LOG.error("module or BC is unexpectedly missing"); // looks like our model is inconsistent, we missed module or BC deletion
            return null;
          }

          entryInfo.MODULE_NAME = buildConfigurationEntry.getModuleName();
          entryInfo.BC_NAME = buildConfiguration.NAME;
          return entryInfo;
        }
        else if (entry instanceof ModuleLibraryEntry) {
          entryInfo.LIBRARY_ELEMENT = new Element("library");
          try {
            ((ModuleLibraryEntry)entry).writeExternal(entryInfo.LIBRARY_ELEMENT);
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

  public void initialize(Project project) {
    LOG.assertTrue(myEntries.isEmpty(), "already initialized");
    ModulePointerManager pointerManager = ModulePointerManager.getInstance(project);
    myEntries = new ArrayList<DependencyEntry>(myEntriesInfos.length);
    for (EntryInfo info : myEntriesInfos) {
      DependencyEntry entry = null;
      if (info.LIBRARY_ELEMENT != null) {
        ModuleLibraryEntry libraryEntry = new ModuleLibraryEntry();
        try {
          libraryEntry.readExternal(info.LIBRARY_ELEMENT);
          entry = libraryEntry;
        }
        catch (InvalidDataException e) {
          LOG.error(e);
        }
      }
      else if (info.BC_NAME != null) {
        entry = new BuildConfigurationEntry(pointerManager.create(info.MODULE_NAME), info.BC_NAME);
      }
      else {
        LOG.error("unknown entry");
      }

      //noinspection ConstantConditions
      if (entry != null) {
        if (info.DEPENDENCY_TYPE_ELEMENT != null) {
          entry.myDependencyType.readExternal(info.DEPENDENCY_TYPE_ELEMENT);
        }
        else {
          LOG.error("dependency type element is missing");
        }
        myEntries.add(entry);
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
