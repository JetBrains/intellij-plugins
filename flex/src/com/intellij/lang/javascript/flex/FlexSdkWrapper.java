package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkModificator;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class FlexSdkWrapper extends UserDataHolderBase implements Sdk, IFlexSdkType {
  @NotNull
  private final LibraryEx myLibrary;
  private SdkType mySdkType;
  private Subtype mySubtype;

  public FlexSdkWrapper(@NotNull LibraryEx library, final TargetPlatform targetPlatform) {
    myLibrary = library;
    mySdkType = targetPlatform == TargetPlatform.Web
                       ? FlexSdkType.getInstance() : targetPlatform == TargetPlatform.Desktop
                                                     ? AirSdkType.getInstance() : AirMobileSdkType.getInstance();
    mySubtype =
      targetPlatform == TargetPlatform.Web ? Subtype.Flex : targetPlatform == TargetPlatform.Desktop ? Subtype.AIR : Subtype.AIRMobile;
  }

  @NotNull
  @Override
  public SdkType getSdkType() {
    return mySdkType;
  }

  @NotNull
  @Override
  public String getName() {
    return myLibrary.getName();
  }

  @Override
  public String getVersionString() {
    return FlexSdk.getFlexVersion(myLibrary);
  }

  @Override
  public String getHomePath() {
    return FlexSdk.getHomePath(myLibrary);
  }

  @Override
  public VirtualFile getHomeDirectory() {
    String homePath = getHomePath();
    return homePath != null ? LocalFileSystem.getInstance().findFileByPath(homePath) : null;
  }

  @NotNull
  @Override
  public RootProvider getRootProvider() {
    return myLibrary.getRootProvider();
  }

  @NotNull
  @Override
  public SdkModificator getSdkModificator() {
    return new FlexSdkModificator((LibraryEx.ModifiableModelEx)myLibrary.getModifiableModel(), Collections.<String>emptyList());
  }

  @Override
  public SdkAdditionalData getSdkAdditionalData() {
    return null;
  }

  public Subtype getSubtype() {
    return mySubtype;
  }

  @Override
  public Object clone() {
    return super.clone();
  }
}
