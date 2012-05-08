package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.psi.impl.CompositeRootCollection;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.ModuleJdkOrderEntryImpl;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FlexCompositeSdk extends UserDataHolderBase implements Sdk, CompositeRootCollection {

  private static final String NAME_DELIM = "\t";

  public static class SdkFinderImpl extends ModuleJdkOrderEntryImpl.SdkFinder {
    public Sdk findSdk(final String name, final String sdkType) {
      if (TYPE.getName().equals(sdkType)) {
        final List<String> sdksNames = StringUtil.split(name, NAME_DELIM);
        return new FlexCompositeSdk(ArrayUtil.toStringArray(sdksNames));
      }
      return null;
    }
  }

  private final String[] myNames;

  @Nullable
  private volatile Sdk[] mySdks;

  public FlexCompositeSdk(String[] names) {
    myNames = names;
    init();
  }

  private void init() {
    Application application = ApplicationManager.getApplication();

    final Disposable d = Disposer.newDisposable();
    Disposer.register(application, d);

    application.getMessageBus().connect(d).subscribe(ProjectJdkTable.JDK_TABLE_TOPIC, new ProjectJdkTable.Listener() {
      @Override
      public void jdkAdded(final Sdk jdk) {
        resetSdks();
      }

      @Override
      public void jdkRemoved(final Sdk jdk) {
        if (jdk == FlexCompositeSdk.this) {
          Disposer.dispose(d);
        }
        resetSdks();
      }

      @Override
      public void jdkNameChanged(final Sdk jdk, final String previousName) {
        resetSdks();
      }
    });
  }

  @NotNull
  public SdkType getSdkType() {
    return TYPE;
  }

  @NotNull
  public String getName() {
    return getCompositeName(myNames);
  }

  public static String getCompositeName(final String[] names) {
    return StringUtil.join(names, NAME_DELIM);
  }

  public String getVersionString() {
    return null;
  }

  public String getHomePath() {
    return null;
  }

  public VirtualFile getHomeDirectory() {
    return null;
  }

  @NotNull
  public RootProvider getRootProvider() {
    return new RootProvider() {
      @NotNull
      public String[] getUrls(@NotNull final OrderRootType rootType) {
        final Collection<String> result = new HashSet<String>();
        forAllSdks(new Processor<Sdk>() {
          public boolean process(final Sdk sdk) {
            result.addAll(Arrays.asList(sdk.getRootProvider().getUrls(rootType)));
            return true;
          }
        });
        return ArrayUtil.toStringArray(result);
      }

      @NotNull
      public VirtualFile[] getFiles(@NotNull final OrderRootType rootType) {
        final Collection<VirtualFile> result = new HashSet<VirtualFile>();
        forAllSdks(new Processor<Sdk>() {
          public boolean process(final Sdk sdk) {
            result.addAll(Arrays.asList(sdk.getRootProvider().getFiles(rootType)));
            return true;
          }
        });
        return result.toArray(new VirtualFile[result.size()]);
      }

      public void addRootSetChangedListener(@NotNull final RootSetChangedListener listener) {
        forAllSdks(new Processor<Sdk>() {
          public boolean process(final Sdk sdk) {
            sdk.getRootProvider().addRootSetChangedListener(listener);
            return true;
          }
        });
      }

      public void addRootSetChangedListener(@NotNull final RootSetChangedListener listener, @NotNull final Disposable parentDisposable) {
        forAllSdks(new Processor<Sdk>() {
          public boolean process(final Sdk sdk) {
            sdk.getRootProvider().addRootSetChangedListener(listener, parentDisposable);
            return true;
          }
        });
      }

      public void removeRootSetChangedListener(@NotNull final RootSetChangedListener listener) {
        forAllSdks(new Processor<Sdk>() {
          public boolean process(final Sdk sdk) {
            sdk.getRootProvider().removeRootSetChangedListener(listener);
            return true;
          }
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

  @NotNull
  public Sdk[] getSdks() {
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
      allSdks = sdks.toArray(new Sdk[sdks.size()]);
      cache = false;
    }

    List<Sdk> result = ContainerUtil.findAll(allSdks, new Condition<Sdk>() {
      @Override
      public boolean value(final Sdk sdk) {
        return ArrayUtil.contains(sdk.getName(), myNames);
      }
    });

    Sdk[] resultArray = result.toArray(new Sdk[result.size()]);
    if (cache) {
      mySdks = resultArray;
    }
    return resultArray;
  }

  private void resetSdks() {
    mySdks = null;
  }

  @NotNull
  public SdkModificator getSdkModificator() {
    throw new UnsupportedOperationException();
  }

  public SdkAdditionalData getSdkAdditionalData() {
    return null;
  }

  @NotNull
  public Object clone() {
    throw new UnsupportedOperationException();
  }

  private static final OrderRootType[] RELEVANT_ROOT_TYPES = new OrderRootType[]{OrderRootType.CLASSES, OrderRootType.SOURCES};

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
    public String suggestHomePath() {
      return null;
    }

    public boolean isValidSdkHome(final String path) {
      return false;
    }

    public String suggestSdkName(final String currentSdkName, final String sdkHome) {
      return currentSdkName;
    }

    @Nullable
    public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
      return null;
    }

    public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {
    }

    public String getPresentableName() {
      return getName();
    }
  };
}
