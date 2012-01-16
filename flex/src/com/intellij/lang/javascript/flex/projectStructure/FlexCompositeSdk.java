package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.ModuleJdkOrderEntryImpl;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FlexCompositeSdk extends UserDataHolderBase implements Sdk {

  private static final String NAME_DELIM = "\t";

  public static class SdkFinderImpl extends ModuleJdkOrderEntryImpl.SdkFinder {
    public Sdk findSdk(final String name, final String sdkType) {
      if (TYPE.getName().equals(sdkType)) {
        final List<String> sdksNames = StringUtil.split(name, NAME_DELIM);
        return new FlexCompositeSdk(sdksNames);
      }
      return null;
    }
  }

  private final Collection<String> myNames;

  public FlexCompositeSdk(Collection<String> names) {
    myNames = names;
  }

  @NotNull
  public SdkType getSdkType() {
    return TYPE;
  }

  @NotNull
  public String getName() {
    return StringUtil.join(myNames, NAME_DELIM);
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
    final Sdk[] allSdks = ProjectJdkTable.getInstance().getAllJdks();
    for (Sdk sdk : allSdks) {
      if (!myNames.contains(sdk.getName())) {
        continue;
      }

      if (!processor.process(sdk)) {
        return;
      }
    }
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
    return super.clone();
  }

  private static final SdkType TYPE = new SdkType("__CompositeFlexSdk__") {
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
