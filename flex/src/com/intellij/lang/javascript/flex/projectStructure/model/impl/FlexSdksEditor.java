package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkModificator;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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

  private final Collection<Library> myLibrariesExistedBefore = new HashSet<Library>();
  private final Map<Object, Library> myUsedLibraries = new HashMap<Object, Library>();
  private final LibraryTableBase.ModifiableModelEx myModifiableModel;
  private final Project myProject;

  public FlexSdksEditor(Project project, LibraryTableBase.ModifiableModelEx modifiableModel) {
    myProject = project;
    myModifiableModel = modifiableModel;

    myUsedLibraries.clear();
    myLibrariesExistedBefore.clear();
    for (Library library : myModifiableModel.getLibraries()) {
      if (FlexSdk.isFlexSdk(library)) {
        myLibrariesExistedBefore.add(library);
      }
    }
    fireChanged();
  }

  private void fireChanged() {
    mySdkListDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  public void addSdkListListener(ChangeListener listener, Disposable parentDisposable) {
    mySdkListDispatcher.addListener(listener, parentDisposable);
  }

  public Library[] getLibraries() {
    return ContainerUtil.findAllAsArray(myModifiableModel.getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library);
      }
    });
  }

  @NotNull
  private FlexSdk createSdk(@NotNull String homePath) {
    final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(homePath);

    Library library = myModifiableModel.createLibrary("Flex SDK", FlexSdkLibraryType.getInstance());
    LibraryEx.ModifiableModelEx libraryModifiableModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
    ((FlexSdkProperties)libraryModifiableModel.getProperties()).setId(UUID.randomUUID().toString());
    ((FlexSdkProperties)libraryModifiableModel.getProperties()).setHomePath(homePath);
    final FlexSdkModificator sdkModificator = new FlexSdkModificator(libraryModifiableModel);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        FlexSdkUtils.setupSdkPaths(sdkHome, null, sdkModificator);
      }
    });
    // TODO let most recent SDK show up first
    fireChanged();
    return new FlexSdk(library);
  }

  @Nullable
  public FlexSdk findSdk(@NotNull final String libraryId) {
    Library library = ContainerUtil.find(myModifiableModel.getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library) && FlexProjectRootsUtil.getSdkLibraryId(library).equals(libraryId);
      }
    });
    return library != null ? new FlexSdk(library) : null;
  }

  @NotNull
  public FlexSdk findOrCreateSdk(@NotNull final String homePath) {
    Library library = ContainerUtil.find(myModifiableModel.getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library) && FlexSdk.getHomePath(library).equals(homePath);
      }
    });
    return library != null ? new FlexSdk(library) : createSdk(homePath);
  }

  public void setUsed(Object user, @Nullable Library sdk) {
    if (sdk == null) {
      myUsedLibraries.remove(user);
    }
    else {
      myUsedLibraries.put(user, sdk);
    }
  }

  public void commit() {
    Collection<Library> unusedLibraries = ContainerUtil.filter(myModifiableModel.getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library) && !myUsedLibraries.containsValue(library) && !myLibrariesExistedBefore.contains(library);
      }
    });

    for (Library unusedLibrary : unusedLibraries) {
      myModifiableModel.removeLibrary(unusedLibrary);
    }
    myModifiableModel.commit();
  }

  public boolean isModified() {
    Set<Library> currentLibraries = new HashSet<Library>(ContainerUtil.filter(myModifiableModel.getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return !FlexSdk.isFlexSdk(library) || myUsedLibraries.containsValue(library);
      }
    }));
    currentLibraries.addAll(myLibrariesExistedBefore);
    Set<Library> originalLibraries = new HashSet<Library>(Arrays.asList(ApplicationLibraryTable.getApplicationTable().getLibraries()));
    return !currentLibraries.equals(originalLibraries);
  }

  public Project getProject() {
    return myProject;
  }

  public LibraryEditor getLibraryEditor(Library library) {
    return ((LibrariesModifiableModel)myModifiableModel).getLibraryEditor(library);
  }
}
