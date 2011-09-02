package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ksafonov
 */
@State(name = "FlexSdkManager", storages = {@Storage(file = "$APP_CONFIG$/flexSdks.xml")})
public class FlexSdkManager implements PersistentStateComponent<Element> {

  private final List<FlexSdk> mySdks = new ArrayList<FlexSdk>();

  public static FlexSdkManager getInstance() {
    return ServiceManager.getService(FlexSdkManager.class);
  }

  @Override
  public Element getState() {
    Element root = new Element("sdks");
    try {
      for (FlexSdk sdk : mySdks) {
        root.addContent(sdk.getElement());
      }
    }
    catch (WriteExternalException e) {
      throw new StateStorageException(e);
    }
    return root;
  }

  @Override
  public void loadState(Element state) {
    mySdks.clear();
    try {
      for (Object o : state.getChildren(FlexSdk.SDK_ELEM)) {
        mySdks.add(new FlexSdk((Element)o));
      }
    }
    catch (InvalidDataException e) {
      throw new StateStorageException(e);
    }
    refreshSdkRoots();
  }

  private void refreshSdkRoots() {
    Collection<VirtualFile> sdkRoots = ContainerUtil.mapNotNull(mySdks, new Function<FlexSdk, VirtualFile>() {
      @Override
      public VirtualFile fun(FlexSdk flexSdk) {
        return LocalFileSystem.getInstance().findFileByPath(flexSdk.getHomePath());
      }
    });
    LocalFileSystem.getInstance().refreshFiles(sdkRoots, false, true, null);
  }

  public void applyTo(FlexSdkManager other) {
    other.mySdks.clear();
    for (FlexSdk sdk : mySdks) {
      other.mySdks.add(sdk.getCopy());
    }
  }

  public FlexSdk[] getSdks() {
    return mySdks.toArray(new FlexSdk[mySdks.size()]);
  }

  @Nullable
  public FlexSdk findSdk(@NotNull String sdkHome) {
    for (FlexSdk sdk : mySdks) {
      if (sdk.getHomePath().equals(sdkHome)) {
        return sdk;
      }
    }
    return null;
  }

  public void setSdks(List<FlexSdk> sdks) {
    mySdks.clear();
    mySdks.addAll(sdks);
  }
}
