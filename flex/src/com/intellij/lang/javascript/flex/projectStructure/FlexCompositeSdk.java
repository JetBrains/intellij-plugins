// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.psi.impl.CompositeRootCollection;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.SdkFinder;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexCompositeSdk extends UserDataHolderBase implements Sdk, CompositeRootCollection {

  private static final String NAME_DELIM = "\t";

  public static class SdkFinderImpl extends SdkFinder {
    @Override
    public Sdk findSdk(@NotNull String name, @NotNull final String sdkType) {
      if (TYPE.getName().equals(sdkType)) {
        final List<String> sdksNames = StringUtil.split(name, NAME_DELIM);
        return FlexCompositeSdkManager.getInstance().getOrCreateSdk(ArrayUtilRt.toStringArray(sdksNames));
      }
      return null;
    }
  }

  private final String[] myNames;

  private volatile Sdk @Nullable [] mySdks;

  FlexCompositeSdk(String[] names, Disposable parentDisposable) {
    myNames = names;
    init(parentDisposable);
  }

  private void init(Disposable parentDisposable) {
    Application application = ApplicationManager.getApplication();

    final Disposable d = Disposer.newDisposable();
    Disposer.register(parentDisposable, d);

    application.getMessageBus().connect(d).subscribe(ProjectJdkTable.JDK_TABLE_TOPIC, new ProjectJdkTable.Listener() {
      @Override
      public void jdkAdded(@NotNull final Sdk jdk) {
        resetSdks();
      }

      @Override
      public void jdkRemoved(@NotNull final Sdk jdk) {
        if (jdk == FlexCompositeSdk.this) {
          Disposer.dispose(d);
        }
        resetSdks();
      }

      @Override
      public void jdkNameChanged(@NotNull final Sdk jdk, @NotNull final String previousName) {
        resetSdks();
      }
    });
  }

  @Override
  @NotNull
  public SdkType getSdkType() {
    return TYPE;
  }

  @Override
  @NotNull
  public String getName() {
    return getCompositeName(myNames);
  }

  public static String getCompositeName(final String[] names) {
    return StringUtil.join(names, NAME_DELIM);
  }

  @Override
  public String getVersionString() {
    return null;
  }

  @Override
  public String getHomePath() {
    return null;
  }

  @Override
  public VirtualFile getHomeDirectory() {
    return null;
  }

  @Override
  @NotNull
  public RootProvider getRootProvider() {
    return new RootProvider() {
      @Override
      public String @NotNull [] getUrls(@NotNull final OrderRootType rootType) {
        final Collection<String> result = new HashSet<>();
        forAllSdks(sdk -> {
          result.addAll(Arrays.asList(sdk.getRootProvider().getUrls(rootType)));
          return true;
        });
        return ArrayUtilRt.toStringArray(result);
      }

      @Override
      public VirtualFile @NotNull [] getFiles(@NotNull final OrderRootType rootType) {
        final Collection<VirtualFile> result = new HashSet<>();
        forAllSdks(sdk -> {
          result.addAll(Arrays.asList(sdk.getRootProvider().getFiles(rootType)));
          return true;
        });
        return result.toArray(VirtualFile.EMPTY_ARRAY);
      }

      @Override
      public void addRootSetChangedListener(@NotNull final RootSetChangedListener listener) {
        forAllSdks(sdk -> {
          final RootProvider rootProvider = sdk.getRootProvider();
          rootProvider.removeRootSetChangedListener(listener);
          rootProvider.addRootSetChangedListener(listener);
          return true;
        });
      }

      @Override
      public void addRootSetChangedListener(@NotNull final RootSetChangedListener listener, @NotNull final Disposable parentDisposable) {
        forAllSdks(sdk -> {
          sdk.getRootProvider().addRootSetChangedListener(listener, parentDisposable);
          return true;
        });
      }

      @Override
      public void removeRootSetChangedListener(@NotNull final RootSetChangedListener listener) {
        forAllSdks(sdk -> {
          sdk.getRootProvider().removeRootSetChangedListener(listener);
          return true;
        });
      }
    };
  }

  private void forAllSdks(Processor<Sdk> processor) {
    Sdk[] sdks = getSdks();
    for (Sdk sdk : sdks) {
      if (!processor.process(sdk)) {
        return;
      }
    }
  }

  public Sdk @NotNull [] getSdks() {
    if (mySdks != null) {
      return mySdks;
    }

    Sdk[] allSdks;
    boolean cache;
    final FlexProjectConfigurationEditor currentEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    if (currentEditor == null) {
      allSdks = ProjectJdkTable.getInstance().getAllJdks();
      cache = true;
    }
    else {
      final Collection<Sdk> sdks =
        ProjectStructureConfigurable.getInstance(currentEditor.getProject()).getProjectJdksModel().getProjectSdks().values();
      allSdks = sdks.toArray(new Sdk[0]);
      cache = false;
    }

    List<Sdk> result = ContainerUtil.findAll(allSdks, sdk -> ArrayUtil.contains(sdk.getName(), myNames));

    Sdk[] resultArray = result.toArray(new Sdk[0]);
    if (cache) {
      mySdks = resultArray;
    }
    return resultArray;
  }

  private void resetSdks() {
    mySdks = null;
  }

  @Override
  @NotNull
  public SdkModificator getSdkModificator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SdkAdditionalData getSdkAdditionalData() {
    return null;
  }

  @Override
  @NotNull
  public Object clone() {
    throw new UnsupportedOperationException();
  }

  private static final OrderRootType[] RELEVANT_ROOT_TYPES = {OrderRootType.CLASSES, OrderRootType.SOURCES};

  @Override
  public VirtualFile[] getFiles(final OrderRootType rootType, final VirtualFile hint) {
    Sdk[] sdks = getSdks();
    for (Sdk sdk : sdks) {
      for (OrderRootType t : RELEVANT_ROOT_TYPES) {
        VirtualFile[] files = sdk.getRootProvider().getFiles(t);

        if (isAncestorOf(files, hint)) {
          return t == rootType ? files : sdk.getRootProvider().getFiles(rootType);
        }
      }
    }

    return VirtualFile.EMPTY_ARRAY;
  }

  private static boolean isAncestorOf(VirtualFile[] ancestors, VirtualFile file) {
    VirtualFile fileInLocalFs = JarFileSystem.getInstance().getVirtualFileForJar(file);

    for (VirtualFile ancestor : ancestors) {
      if (VfsUtilCore.isAncestor(ancestor, file, false)) return true;
      if (fileInLocalFs != null && VfsUtilCore.isAncestor(ancestor, fileInLocalFs, false)) return true;
    }
    return false;
  }

  @Override
  public String getName(final VirtualFile hint) {
    Sdk[] sdks = getSdks();
    if (sdks.length >= 2) {
      for (Sdk sdk : sdks) {
        for (OrderRootType t : RELEVANT_ROOT_TYPES) {
          if (isAncestorOf(sdk.getRootProvider().getFiles(t), hint)) {
            return sdk.getName();
          }
        }
      }
    }

    return getName();
  }


  public static final String TYPE_ID = "__CompositeFlexSdk__";

  private static final SdkType TYPE = new SdkType(TYPE_ID) {
    @Override
    public String suggestHomePath() {
      return null;
    }

    @Override
    public boolean isValidSdkHome(final @NotNull String path) {
      return false;
    }

    @NotNull
    @Override
    public String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
      return Objects.requireNonNull(currentSdkName);
    }

    @Override
    @Nullable
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
      return null;
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
    }

    @Override
    @NotNull
    public String getPresentableName() {
      return getName();
    }
  };
}
