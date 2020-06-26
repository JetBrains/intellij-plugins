// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirSigningOptions;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

public final class JpsAirSigningOptionsImpl extends JpsElementBase<JpsAirSigningOptionsImpl> implements JpsAirSigningOptions {

  static final JpsAirSigningOptionsRole ROLE = new JpsAirSigningOptionsRole();

  private boolean myUseTempCertificate = true;
  private @NotNull String myProvisioningProfilePath = "";
  private @NotNull String myKeystorePath = "";
  private @NotNull String myKeystoreType = "PKCS12";
  private @NotNull String myKeyAlias = "";
  private @NotNull String myProvider = "";
  private @NotNull String myTsa = "";

  private JpsAirSigningOptionsImpl() {
  }

  private JpsAirSigningOptionsImpl(JpsAirSigningOptionsImpl original) {
    myUseTempCertificate = original.myUseTempCertificate;
    myProvisioningProfilePath = original.myProvisioningProfilePath;
    myKeystorePath = original.myKeystorePath;
    myKeystoreType = original.myKeystoreType;
    myKeyAlias = original.myKeyAlias;
    myProvider = original.myProvider;
    myTsa = original.myTsa;
  }

  @Override
  @NotNull
  public JpsAirSigningOptionsImpl createCopy() {
    return new JpsAirSigningOptionsImpl(this);
  }

  @Override
  public void applyChanges(@NotNull final JpsAirSigningOptionsImpl modified) {
    setUseTempCertificate(modified.isUseTempCertificate());
    setProvisioningProfilePath(modified.getProvisioningProfilePath());
    setKeystorePath(modified.getKeystorePath());
    setKeystoreType(modified.getKeystoreType());
    setKeyAlias(modified.getKeyAlias());
    setProvider(modified.getProvider());
    setTsa(modified.getTsa());
  }

// -----------------------------

  @Override
  public boolean isUseTempCertificate() {
    return myUseTempCertificate;
  }

  @Override
  public void setUseTempCertificate(final boolean useTempCertificate) {
    myUseTempCertificate = useTempCertificate;
  }

  @Override
  @NotNull
  public String getProvisioningProfilePath() {
    return myProvisioningProfilePath;
  }

  @Override
  public void setProvisioningProfilePath(final @NotNull String provisioningProfilePath) {
    myProvisioningProfilePath = FileUtil.toSystemIndependentName(provisioningProfilePath);
  }

  @Override
  @NotNull
  public String getKeystorePath() {
    return myKeystorePath;
  }

  @Override
  public void setKeystorePath(final @NotNull String keystorePath) {
    myKeystorePath = FileUtil.toSystemIndependentName(keystorePath);
  }

  @Override
  @NotNull
  public String getKeystoreType() {
    return myKeystoreType;
  }

  @Override
  public void setKeystoreType(final @NotNull String keystoreType) {
    myKeystoreType = keystoreType;
  }

  @Override
  @NotNull
  public String getKeyAlias() {
    return myKeyAlias;
  }

  @Override
  public void setKeyAlias(final @NotNull String keyAlias) {
    myKeyAlias = keyAlias;
  }

  @Override
  @NotNull
  public String getProvider() {
    return myProvider;
  }

  @Override
  public void setProvider(final @NotNull String provider) {
    myProvider = provider;
  }

  @Override
  @NotNull
  public String getTsa() {
    return myTsa;
  }

  @Override
  public void setTsa(final @NotNull String tsa) {
    myTsa = tsa;
  }

// -----------------------------

  State getState() {
    final State state = new State();
    state.USE_TEMP_CERTIFICATE = myUseTempCertificate;
    state.PROVISIONING_PROFILE_PATH = myProvisioningProfilePath;
    state.KEYSTORE_PATH = myKeystorePath;
    state.KEYSTORE_TYPE = myKeystoreType;
    state.KEY_ALIAS = myKeyAlias;
    state.PROVIDER = myProvider;
    state.TSA = myTsa;
    return state;
  }

  void loadState(final State state) {
    myUseTempCertificate = state.USE_TEMP_CERTIFICATE;
    myProvisioningProfilePath = state.PROVISIONING_PROFILE_PATH;
    myKeystorePath = state.KEYSTORE_PATH;
    myKeystoreType = state.KEYSTORE_TYPE;
    myKeyAlias = state.KEY_ALIAS;
    myProvider = state.PROVIDER;
    myTsa = state.TSA;
  }

  private static class JpsAirSigningOptionsRole extends JpsElementChildRoleBase<JpsAirSigningOptions>
    implements JpsElementCreator<JpsAirSigningOptions> {

    protected JpsAirSigningOptionsRole() {
      super("air signing options");
    }

    @Override
    @NotNull
    public JpsAirSigningOptions create() {
      return new JpsAirSigningOptionsImpl();
    }
  }

  @Tag("AirSigningOptions")
  public static class State {
    @Attribute("use-temp-certificate")
    public boolean USE_TEMP_CERTIFICATE = true;
    @Attribute("provisioning-profile-path")
    public String PROVISIONING_PROFILE_PATH = "";
    @Attribute("keystore-path")
    public String KEYSTORE_PATH = "";
    @Attribute("keystore-type")
    public String KEYSTORE_TYPE = "";
    @Attribute("key-alias")
    public String KEY_ALIAS = "";
    @Attribute("provider")
    public String PROVIDER = "";
    @Attribute("tsa")
    public String TSA = "";
  }
}
