package com.intellij.jps.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

public interface JpsAirSigningOptions extends JpsElement {

  boolean isUseTempCertificate();

  void setUseTempCertificate(boolean useTempCertificate);

  @NotNull
  String getProvisioningProfilePath();

  void setProvisioningProfilePath(@NotNull String provisioningProfilePath);

  @NotNull
  String getKeystorePath();

  void setKeystorePath(@NotNull String keystorePath);

  @NotNull
  String getKeystoreType();

  void setKeystoreType(@NotNull String keystoreType);

  @NotNull
  String getKeyAlias();

  void setKeyAlias(@NotNull String keyAlias);

  @NotNull
  String getProvider();

  void setProvider(@NotNull String provider);

  @NotNull
  String getTsa();

  void setTsa(@NotNull String tsa);
}
