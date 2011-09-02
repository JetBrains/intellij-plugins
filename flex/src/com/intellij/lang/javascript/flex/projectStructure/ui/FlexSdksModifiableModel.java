package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkManager;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ksafonov
 */
public class FlexSdksModifiableModel {

  private EventDispatcher<ChangeListener> mySdkListDispatcher = EventDispatcher.create(ChangeListener.class);

  private final List<FlexSdk> mySdks = new ArrayList<FlexSdk>();

  private static final Object USED_BY_OTHERS = new Object();

  private final Map<Object, String> myUsedSkds = new HashMap<Object, String>();

  public void resetFrom(FlexSdkManager source) {
    mySdks.clear();
    myUsedSkds.clear();
    if (source != null) {
      for (FlexSdk sdk : source.getSdks()) {
        mySdks.add(sdk.getCopy());
        myUsedSkds.put(USED_BY_OTHERS, sdk.getHomePath());
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

  public String[] getHomePaths() {
    return ContainerUtil.map2Array(mySdks, String.class, new Function<FlexSdk, String>() {
      @Override
      public String fun(FlexSdk sdk) {
        return sdk.getHomePath();
      }
    });
  }

  @Nullable
  private FlexSdk tryCreateSdk(@NotNull String sdkHome) {
    FlexSdk flexSdk = new FlexSdk(sdkHome);
    if (!flexSdk.isValid()) {
      return null;
    }

    FlexSdkUtils.setupSdkPaths(LocalFileSystem.getInstance().findFileByPath(sdkHome), null, flexSdk.createRootsModificator());
    mySdks.add(0, flexSdk); // let most recent SDK show up first
    fireChanged();
    return flexSdk;
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

  @Nullable
  public FlexSdk findOrCreateSdk(@NotNull String sdkHome) {
    FlexSdk sdk = findSdk(sdkHome);
    return sdk != null ? sdk : tryCreateSdk(sdkHome);
  }

  public void setUsed(Object user, @Nullable String sdkHome) {
    if (sdkHome == null) {
      myUsedSkds.remove(user);
    }
    else {

      if (findSdk(sdkHome) == null) {
        throw new IllegalArgumentException("Unknown home path: " + sdkHome);
      }

      myUsedSkds.put(user, sdkHome);
    }
  }

  public void applyTo(FlexSdkManager target) {
    List<FlexSdk> result = new ArrayList<FlexSdk>();
    for (FlexSdk sdk : mySdks) {
      if (!myUsedSkds.containsValue(sdk.getHomePath())) {
        continue;
      }
      result.add(sdk.getCopy());
    }
    target.setSdks(result);
  }
}
