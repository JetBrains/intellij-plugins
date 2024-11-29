// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AirSigningOptions {

  public static final String ARCH_ARMV7 = "armv7";
  public static final String ARCH_X86 = "x86";
  public static final String ARCH_ARMV8 = "armv8";
  public static final String ARCH_DEFAULT = ARCH_ARMV7;

  private @NotNull String myArch = ARCH_DEFAULT;
  private boolean myUseTempCertificate = true;
  private @NotNull String myProvisioningProfilePath = "";
  private @NotNull String myKeystorePath = "";
  private @NotNull String myIOSSdkPath = "";
  private @NotNull String myADTOptions = "";
  private @NotNull String myKeystoreType = "PKCS12";
  private @NotNull String myKeyAlias = "";
  private @NotNull String myProvider = "";
  private @NotNull String myTsa = "";

  public AirSigningOptions() {
  }

  @Attribute("arch")
  public @NotNull String getArch() {
    return myArch;
  }

  public void setArch(@NotNull String arch) {
    myArch = arch;
  }

  @Attribute("use-temp-certificate")
  public boolean isUseTempCertificate() {
    return myUseTempCertificate;
  }

  public void setUseTempCertificate(final boolean useTempCertificate) {
    myUseTempCertificate = useTempCertificate;
  }

  @Attribute("provisioning-profile-path")
  public @NotNull String getProvisioningProfilePath() {
    return myProvisioningProfilePath;
  }

  public void setProvisioningProfilePath(final @NotNull String provisioningProfilePath) {
    myProvisioningProfilePath = FileUtil.toSystemIndependentName(provisioningProfilePath);
  }

  @Attribute("keystore-path")
  public @NotNull String getKeystorePath() {
    return myKeystorePath;
  }

  public void setKeystorePath(final @NotNull String keystorePath) {
    myKeystorePath = FileUtil.toSystemIndependentName(keystorePath);
  }

  @Attribute("sdk")
  public @NotNull String getIOSSdkPath() {
    return myIOSSdkPath;
  }

  public void setIOSSdkPath(final @NotNull String iOSSdkPath) {
    myIOSSdkPath = FileUtil.toSystemIndependentName(iOSSdkPath);
  }

  @Attribute("adt-options")
  public @NotNull String getADTOptions() {
    return myADTOptions;
  }

  public void setADTOptions(final @NotNull String adtOptions) {
    myADTOptions = adtOptions;
  }

  @Attribute("keystore-type")
  public @NotNull String getKeystoreType() {
    return myKeystoreType;
  }

  public void setKeystoreType(final @NotNull String keystoreType) {
    myKeystoreType = keystoreType;
  }

  @Attribute("key-alias")
  public @NotNull String getKeyAlias() {
    return myKeyAlias;
  }

  public void setKeyAlias(final @NotNull String keyAlias) {
    myKeyAlias = keyAlias;
  }

  @Attribute("provider")
  public @NotNull String getProvider() {
    return myProvider;
  }

  public void setProvider(final @NotNull String provider) {
    myProvider = provider;
  }

  @Attribute("tsa")
  public @NotNull String getTsa() {
    return myTsa;
  }

  public void setTsa(final @NotNull String tsa) {
    myTsa = tsa;
  }

  public AirSigningOptions getCopy() {
    final AirSigningOptions copy = new AirSigningOptions();

    copy.myArch = myArch;
    copy.myUseTempCertificate = myUseTempCertificate;
    copy.myProvisioningProfilePath = myProvisioningProfilePath;
    copy.myKeystorePath = myKeystorePath;
    copy.myIOSSdkPath = myIOSSdkPath;
    copy.myADTOptions = myADTOptions;
    copy.myKeystoreType = myKeystoreType;
    copy.myKeyAlias = myKeyAlias;
    copy.myProvider = myProvider;
    copy.myTsa = myTsa;

    return copy;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AirSigningOptions options = (AirSigningOptions)o;

    if (!myArch.equals(options.myArch)) return false;
    if (myUseTempCertificate != options.myUseTempCertificate) return false;
    if (!myProvisioningProfilePath.equals(options.myProvisioningProfilePath)) return false;
    if (!myKeystorePath.equals(options.myKeystorePath)) return false;
    if (!myIOSSdkPath.equals(options.myIOSSdkPath)) return false;
    if (!myADTOptions.equals(options.myADTOptions)) return false;
    if (!myKeystoreType.equals(options.myKeystoreType)) return false;
    if (!myKeyAlias.equals(options.myKeyAlias)) return false;
    if (!myProvider.equals(options.myProvider)) return false;
    if (!myTsa.equals(options.myTsa)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    assert false;
    return super.hashCode();
  }
}
