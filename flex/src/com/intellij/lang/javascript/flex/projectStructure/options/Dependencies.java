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
import com.intellij.util.xmlb.annotations.Transient;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Dependencies implements Cloneable {

  private static final Logger LOG = Logger.getInstance(Dependencies.class.getName());

  public String TARGET_PLAYER = "";
  public FlexIdeBuildConfiguration.ComponentSet COMPONENT_SET = FlexIdeBuildConfiguration.ComponentSet.SparkAndMx;
  public LinkageType FRAMEWORK_LINKAGE = LinkageType.Default;
  private static final String DEPENDENCY_TYPE_ELEMENT_NAME = "type";
  private static final String SDK_ELEMENT_NAME = "sdk";

  private static final EntryInfo[] EMPTY = new EntryInfo[0];

  private EntryInfo[] myEntriesInfos = EMPTY;
  private List<DependencyEntry> myEntries = new ArrayList<DependencyEntry>();

  @Nullable
  private SdkEntry mySdk;

  @Tag("entry")
  public static class EntryInfo implements Cloneable {
    @Tag("module")
    public String MODULE_NAME;
    @Tag("buildConfiguration")
    public String BC_NAME;
    @Tag("library")
    public Element LIBRARY_ELEMENT;
    @Tag("type")
    public Element DEPENDENCY_TYPE_ELEMENT;

    @Override
    public EntryInfo clone() {
      try {
        return (EntryInfo)super.clone();
      }
      catch (CloneNotSupportedException ignored) {
        return null;
      }
    }
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
        try {
          EntryInfo entryInfo = new EntryInfo();
          if (entry instanceof BuildConfigurationEntry) {
            serializeBcEntry((BuildConfigurationEntry)entry, entryInfo);
            return entryInfo;
          }
          else if (entry instanceof ModuleLibraryEntry) {
            serializeModuleLibraryEntry((ModuleLibraryEntry)entry, entryInfo);
            return entryInfo;
          }
          else {
            LOG.error("Unexpected entry type: " + entry);
          }
        }
        catch (WriteExternalException e) {
          LOG.error(e);
        }
        return null;
      }
    }, new EntryInfo[0]);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSerializedEntries(EntryInfo[] entries) {
    myEntriesInfos = entries;
  }

  @SuppressWarnings("UnusedDeclaration")
  @Tag("sdk")
  public Element getSerializedSdk() {
    if (mySdk != null) {
      try {
        Element element = new Element(SDK_ELEMENT_NAME);
        mySdk.writeExternal(element);
        return element;
      }
      catch (WriteExternalException e) {
        LOG.error(e);
      }
    }
    return null;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSerializedSdk(Element element) {
    if (element != null) {
      try {
        mySdk = new SdkEntry();
        mySdk.readExternal(element);
        return;
      }
      catch (InvalidDataException e) {
        LOG.error(e);
      }
    }
    mySdk = null;
  }

  @Transient
  @Nullable
  public SdkEntry getSdk() {
    return mySdk;
  }

  public void setSdk(@Nullable SdkEntry sdk) {
    mySdk = sdk;
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
        entry = deserializeModuleLibraryEntry(info);
      }
      else if (info.BC_NAME != null) {
        entry = deserializeBcEntry(pointerManager, info);
      }
      else {
        LOG.error("unknown entry");
      }

      //noinspection ConstantConditions
      if (entry != null) {
        myEntries.add(entry);
      }
    }
    myEntriesInfos = null;
  }

  private static void serializeDependencyType(DependencyEntry entry, EntryInfo entryInfo) {
    entryInfo.DEPENDENCY_TYPE_ELEMENT = new Element(DEPENDENCY_TYPE_ELEMENT_NAME);
    entry.myDependencyType.writeExternal(entryInfo.DEPENDENCY_TYPE_ELEMENT);
  }

  private static void deserializeDependencyType(EntryInfo info, DependencyEntry entry) {
    if (info.DEPENDENCY_TYPE_ELEMENT != null) {
      entry.myDependencyType.readExternal(info.DEPENDENCY_TYPE_ELEMENT);
    }
    else {
      LOG.error("dependency type element is missing");
    }
  }

  private static void serializeModuleLibraryEntry(ModuleLibraryEntry entry, EntryInfo entryInfo) throws WriteExternalException {
    entryInfo.LIBRARY_ELEMENT = new Element("library");
    entry.writeExternal(entryInfo.LIBRARY_ELEMENT);
    serializeDependencyType(entry, entryInfo);
  }

  @Nullable
  private static ModuleLibraryEntry deserializeModuleLibraryEntry(EntryInfo info) {
    ModuleLibraryEntry libraryEntry = new ModuleLibraryEntry();
    try {
      libraryEntry.readExternal(info.LIBRARY_ELEMENT);
      deserializeDependencyType(info, libraryEntry);
      return libraryEntry;
    }
    catch (InvalidDataException e) {
      LOG.error(e);
      return null;
    }
  }


  private static void serializeBcEntry(BuildConfigurationEntry entry, EntryInfo entryInfo) throws WriteExternalException {
    entryInfo.MODULE_NAME = entry.getModuleName();
    entryInfo.BC_NAME = entry.getBcName();
    serializeDependencyType(entry, entryInfo);
  }

  private static BuildConfigurationEntry deserializeBcEntry(ModulePointerManager pointerManager, EntryInfo info) {
    BuildConfigurationEntry result = new BuildConfigurationEntry(pointerManager.create(info.MODULE_NAME), info.BC_NAME);
    deserializeDependencyType(info, result);
    return result;
  }

  protected Dependencies clone() {
    try {
      Dependencies clone = (Dependencies)super.clone();
      clone.COMPONENT_SET = COMPONENT_SET;
      clone.FRAMEWORK_LINKAGE = FRAMEWORK_LINKAGE;
      clone.myEntriesInfos =
        myEntriesInfos != null ? ContainerUtil.map2Array(myEntriesInfos, EntryInfo.class, new Function<EntryInfo, EntryInfo>() {
          @Override
          public EntryInfo fun(EntryInfo entryInfo) {
            return entryInfo.clone();
          }
        }) : null;
      clone.myEntries = myEntries != null ? ContainerUtil.map(myEntries, new Function<DependencyEntry, DependencyEntry>() {
        @Override
        public DependencyEntry fun(DependencyEntry dependencyEntry) {
          return dependencyEntry.getCopy();
        }
      }) : null;
      clone.mySdk = mySdk != null ? mySdk.getCopy() : null;
      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
