package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Condition;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

/**
 * User: ksafonov
 */
class FlexSdksEditor {

  private final EventDispatcher<ChangeListener> mySdkListDispatcher = EventDispatcher.create(ChangeListener.class);

  private final FlexProjectConfigurationEditor myConfigEditor;

  public FlexSdksEditor(FlexProjectConfigurationEditor configEditor) {
    myConfigEditor = configEditor;

    fireChanged();
  }

  private void fireChanged() {
    mySdkListDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  public void addSdkListListener(ChangeListener listener, Disposable parentDisposable) {
    mySdkListDispatcher.addListener(listener, parentDisposable);
  }

  @NotNull
  private FlexSdk createSdk(@NotNull String homePath) {
    LibraryEx library = myConfigEditor.createFlexSdkLibrary(homePath);
    // TODO let most recent SDK show up first
    fireChanged();
    return new FlexSdk(library);
  }

  @Nullable
  public Sdk findSdk(@NotNull final String name) {
    return ContainerUtil.find(myConfigEditor.getFlexSdks(), new Condition<Sdk>() {
      @Override
      public boolean value(Sdk sdk) {
        return name.equals(sdk.getName());
      }
    });
  }

  //@NotNull
  //public FlexSdk findOrCreateSdk(@NotNull final String name) {
  //  Library library = ContainerUtil.find(myConfigEditor.getFlexSdks(), new Condition<Sdk>() {
  //    @Override
  //    public boolean value(Sdk sdk) {
  //      return sdk.getName()
  //    }
  //  });
  //  return library != null ? new FlexSdk(library) : createSdk(homePath);
  //}

  public void commit() {
    //Collection<LibraryEx> unusedLibraries = ContainerUtil.filter(myConfigEditor.getSdksLibraries(), new Condition<LibraryEx>() {
    //  @Override
    //  public boolean value(LibraryEx library) {
    //    return !myUsedLibraries.containsValue(library) && !myLibrariesExistedBefore.contains(library);
    //  }
    //});
    //
    //for (Library unusedLibrary : unusedLibraries) {
    //  myConfigEditor.removeFlexSdkLibrary(unusedLibrary);
    //}
  }

  public boolean isModified() {
    return false;
    //List<Library> originalSdkLibraries =
    //  ContainerUtil.filter(ApplicationLibraryTable.getApplicationTable().getLibraries(),
    //                       new Condition<Library>() {
    //                         @Override
    //                         public boolean value(Library library) {
    //                           return FlexSdk.isFlexSdk(library);
    //                         }
    //                       });
    //return !Arrays.asList(myConfigEditor.getSdksLibraries()).equals(originalSdkLibraries);
  }
}
