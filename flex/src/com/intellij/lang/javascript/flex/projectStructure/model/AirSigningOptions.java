package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AirSigningOptions {

  private boolean myUseTempCertificate = true;
  @NotNull private String myProvisioningProfilePath = "";
  @NotNull private String myKeystorePath = "";
  @NotNull private String myKeystoreType = "PKCS12";
  @NotNull private String myKeyAlias = "";
  @NotNull private String myProvider = "";
  @NotNull private String myTsa = "";

  public AirSigningOptions() {
  }

  public AirSigningOptions(final boolean useTempCertificate,
                           @NotNull final String provisioningProfilePath,
                           @NotNull final String keystorePath,
                           @NotNull final String keystoreType,
                           @NotNull final String keyAlias,
                           @NotNull final String provider,
                           @NotNull final String tsa) {
    myUseTempCertificate = useTempCertificate;
    myProvisioningProfilePath = provisioningProfilePath;
    myKeystorePath = keystorePath;
    myKeystoreType = keystoreType;
    myKeyAlias = keyAlias;
    myProvider = provider;
    myTsa = tsa;
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
    return new AirSigningOptions(myUseTempCertificate, myProvisioningProfilePath, myKeystorePath, myKeystoreType, myKeyAlias, myProvider,
                                 myTsa);
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AirSigningOptions that = (AirSigningOptions)o;

    if (myUseTempCertificate != that.myUseTempCertificate) return false;
    if (!myKeyAlias.equals(that.myKeyAlias)) return false;
    if (!myKeystorePath.equals(that.myKeystorePath)) return false;
    if (!myKeystoreType.equals(that.myKeystoreType)) return false;
    if (!myProvider.equals(that.myProvider)) return false;
    if (!myProvisioningProfilePath.equals(that.myProvisioningProfilePath)) return false;
    if (!myTsa.equals(that.myTsa)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    return super.hashCode();
  }
}
