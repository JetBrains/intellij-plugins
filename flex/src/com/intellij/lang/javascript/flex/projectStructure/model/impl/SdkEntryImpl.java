package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.components.StateStorageException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.LinkedHashMap;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ksafonov
 */
class SdkEntryImpl implements SdkEntry {
  private static Logger LOG = Logger.getInstance(SdkEntryImpl.class.getName());

  @NotNull
  private final String myLibraryId;

  @NotNull
  private final String myHomePath;

  private final LinkedHashMap<String, DependencyTypeImpl> myDependencyTypes = new LinkedHashMap<String, DependencyTypeImpl>();

  public SdkEntryImpl(State state) {
    myLibraryId = state.LIBRARY_ID;
    if (StringUtil.isEmpty(myLibraryId)) {
      throw new StateStorageException("library id is empty");
    }

    myHomePath = state.HOME_PAtH;
    if (StringUtil.isEmpty(myHomePath)) {
      throw new StateStorageException("home path is empty");
    }

    for (EntryState entryState : state.DEPENDENCY_TYPES) {
      DependencyTypeImpl dependencyType = new DependencyTypeImpl();
      dependencyType.loadState(entryState.TYPE);
      myDependencyTypes.put(entryState.URL, dependencyType);
    }
  }

  public SdkEntryImpl(@NotNull String libraryId, String homePath) {
    myLibraryId = libraryId;
    myHomePath = homePath;
  }

  public SdkEntryImpl getCopy() {
    SdkEntryImpl copy = new SdkEntryImpl(myLibraryId, myHomePath);
    applyTo(copy);
    return copy;
  }

  private void applyTo(SdkEntryImpl copy) {
    copy.myDependencyTypes.clear();
    copy.myDependencyTypes.putAll(myDependencyTypes);
  }

  public boolean isEqual(@NotNull SdkEntryImpl that) {
    if (!myLibraryId.equals(that.myLibraryId)) return false;
    if (!myHomePath.equals(that.myHomePath)) return false;
    Iterator<String> i1 = myDependencyTypes.keySet().iterator();
    Iterator<String> i2 = that.myDependencyTypes.keySet().iterator();
    while (i1.hasNext() && i2.hasNext()) {
      String url1 = i1.next();
      String url2 = i2.next();
      if (!url1.equals(url2)) return true;
      if (!Comparing.equal(myDependencyTypes.get(url1), myDependencyTypes.get(url2))) return true;
    }
    if (i1.hasNext() || i2.hasNext()) return false;
    return true;
  }

  @Override
  @NotNull
  public String getLibraryId() {
    return myLibraryId;
  }

  @Override
  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  @Override
  @Nullable
  public LibraryEx findLibrary() {
    return findLibrary(ApplicationLibraryTable.getApplicationTable().getLibraries());
  }

  public LibraryEx findLibrary(Library[] libraries) {
    Library result = ContainerUtil.find(libraries, new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library) && FlexProjectRootsUtil.getSdkLibraryId(library).equals(myLibraryId);
      }
    });
    if (result != null) {
      LOG.assertTrue(myHomePath.equals(FlexSdk.getHomePath(result)), "Unexpected home path");
    }
    return (LibraryEx)result;
  }

  public State getState() {
    State state = new State();
    state.LIBRARY_ID = myLibraryId;
    state.HOME_PAtH = myHomePath;
    for (Map.Entry<String, DependencyTypeImpl> entry : myDependencyTypes.entrySet()) {
      EntryState entryState = new EntryState();
      entryState.URL = entry.getKey();
      entryState.TYPE = entry.getValue().getState();
      state.DEPENDENCY_TYPES.add(entryState);
    }
    return state;
  }

  @Tag("sdk")
  public static class State {

    @Attribute("library-id")
    public String LIBRARY_ID;

    @Attribute("home-path")
    public String HOME_PAtH;

    @Tag("dependencies")
    public List<EntryState> DEPENDENCY_TYPES = new ArrayList<EntryState>();
  }

  @Tag("entry")
  public static class EntryState {
    @Attribute("url")
    public String URL;

    @Property(surroundWithTag = false)
    public DependencyTypeImpl.State TYPE;
  }
}
