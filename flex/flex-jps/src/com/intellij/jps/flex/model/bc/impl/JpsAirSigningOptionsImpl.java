package com.intellij.jps.flex.model.bc.impl;

import com.intellij.jps.flex.model.bc.JpsAirSigningOptions;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.impl.JpsElementBase;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;

public class JpsAirSigningOptionsImpl extends JpsElementBase<JpsAirSigningOptionsImpl> implements JpsAirSigningOptions {

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

  @NotNull
  public JpsAirSigningOptionsImpl createCopy() {
    return new JpsAirSigningOptionsImpl(this);
  }

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

  public boolean isUseTempCertificate() {
    return myUseTempCertificate;
  }

  public void setUseTempCertificate(final boolean useTempCertificate) {
    myUseTempCertificate = useTempCertificate;
  }

  @NotNull
  public String getProvisioningProfilePath() {
    return myProvisioningProfilePath;
  }

  public void setProvisioningProfilePath(final @NotNull String provisioningProfilePath) {
    myProvisioningProfilePath = FileUtil.toSystemIndependentName(provisioningProfilePath);
  }

  @NotNull
  public String getKeystorePath() {
    return myKeystorePath;
  }

  public void setKeystorePath(final @NotNull String keystorePath) {
    myKeystorePath = FileUtil.toSystemIndependentName(keystorePath);
  }

  @NotNull
  public String getKeystoreType() {
    return myKeystoreType;
  }

  public void setKeystoreType(final @NotNull String keystoreType) {
    myKeystoreType = keystoreType;
  }

  @NotNull
  public String getKeyAlias() {
    return myKeyAlias;
  }

  public void setKeyAlias(final @NotNull String keyAlias) {
    myKeyAlias = keyAlias;
  }

  @NotNull
  public String getProvider() {
    return myProvider;
  }

  public void setProvider(final @NotNull String provider) {
    myProvider = provider;
  }

  @NotNull
  public String getTsa() {
    return myTsa;
  }

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
