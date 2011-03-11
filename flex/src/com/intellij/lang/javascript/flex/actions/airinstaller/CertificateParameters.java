package com.intellij.lang.javascript.flex.actions.airinstaller;

public class CertificateParameters {

  private final String myKeystoreFilePath;
  private final String myCertificateName;
  private final String myKeyType;
  private final String myKeystorePassword;
  private final String myOrgUnit;
  private final String myOrgName;
  private final String myCountryCode;

  public CertificateParameters(final String keystoreFilePath,
                               final String certificateName,
                               final String keyType,
                               final String keystorePassword,
                               final String orgUnit,
                               final String orgName,
                               final String countryCode) {
    myKeystoreFilePath = keystoreFilePath;
    myCertificateName = certificateName;
    myKeyType = keyType;
    myKeystorePassword = keystorePassword;
    myOrgUnit = orgUnit;
    myOrgName = orgName;
    myCountryCode = countryCode;
  }

  public String getKeystoreFilePath() {
    return myKeystoreFilePath;
  }

  public String getCertificateName() {
    return myCertificateName;
  }

  public String getKeyType() {
    return myKeyType;
  }

  public String getKeystorePassword() {
    return myKeystorePassword;
  }

  public String getOrgUnit() {
    return myOrgUnit;
  }

  public String getOrgName() {
    return myOrgName;
  }

  public String getCountryCode() {
    return myCountryCode;
  }
}
