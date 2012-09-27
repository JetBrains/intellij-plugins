package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AirSigningOptions {

  private boolean myUseTempCertificate = true;
  @NotNull private String myProvisioningProfilePath = "";
  @NotNull private String myKeystorePath = "";
  @NotNull private String myIOSSdkPath = "";
  @NotNull private String myADTOptions = "";
  @NotNull private String myKeystoreType = "PKCS12";
  @NotNull private String myKeyAlias = "";
  @NotNull private String myProvider = "";
  @NotNull private String myTsa = "";

  public AirSigningOptions() {
  }

  @Attribute("use-temp-certificate")
  public boolean isUseTempCertificate() {
    return myUseTempCertificate;
  }

  public void setUseTempCertificate(final boolean useTempCertificate) {
    myUseTempCertificate = useTempCertificate;
  }

  @NotNull
  @Attribute("provisioning-profile-path")
  public String getProvisioningProfilePath() {
    return myProvisioningProfilePath;
  }

  public void setProvisioningProfilePath(@NotNull final String provisioningProfilePath) {
    myProvisioningProfilePath = FileUtil.toSystemIndependentName(provisioningProfilePath);
  }

  @NotNull
  @Attribute("keystore-path")
  public String getKeystorePath() {
    return myKeystorePath;
  }

  public void setKeystorePath(@NotNull final String keystorePath) {
    myKeystorePath = FileUtil.toSystemIndependentName(keystorePath);
  }

  @NotNull
  @Attribute("sdk")
  public String getIOSSdkPath() {
    return myIOSSdkPath;
  }

  public void setIOSSdkPath(@NotNull final String iOSSdkPath) {
    myIOSSdkPath = FileUtil.toSystemIndependentName(iOSSdkPath);
  }

  @NotNull
  @Attribute("adt-options")
  public String getADTOptions() {
    return myADTOptions;
  }

  public void setADTOptions(@NotNull final String adtOptions) {
    myADTOptions = adtOptions;
  }

  @NotNull
  @Attribute("keystore-type")
  public String getKeystoreType() {
    return myKeystoreType;
  }

  public void setKeystoreType(@NotNull final String keystoreType) {
    myKeystoreType = keystoreType;
  }

  @NotNull
  @Attribute("key-alias")
  public String getKeyAlias() {
    return myKeyAlias;
  }

  public void setKeyAlias(@NotNull final String keyAlias) {
    myKeyAlias = keyAlias;
  }

  @NotNull
  @Attribute("provider")
  public String getProvider() {
    return myProvider;
  }

  public void setProvider(@NotNull final String provider) {
    myProvider = provider;
  }

  @NotNull
  @Attribute("tsa")
  public String getTsa() {
    return myTsa;
  }

  public void setTsa(@NotNull final String tsa) {
    myTsa = tsa;
  }

  public AirSigningOptions getCopy() {
    final AirSigningOptions copy = new AirSigningOptions();

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

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AirSigningOptions options = (AirSigningOptions)o;

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

  public int hashCode() {
    assert false;
    return super.hashCode();
  }
}
