package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.projectStructure.model.AirSigningOptions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.DialogMessage;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class PasswordStore {

  public static class SigningOptionsException extends Exception {
    public final boolean wrongKeystorePassword;
    public final boolean wrongKeyPassword;

    public SigningOptionsException(@DialogMessage String message) {
      this(message, false, false);
    }

    public SigningOptionsException(@DialogMessage String message, final boolean wrongKeystorePassword, final boolean wrongKeyPassword) {
      super(message);
      this.wrongKeystorePassword = wrongKeystorePassword;
      this.wrongKeyPassword = wrongKeyPassword;
    }
  }

  @Transient
  private boolean rememberPasswords = true;

  /**
   * key is either keystorePath or keystorePath + "*" + keyAlias
   */
  @Transient
  private final Map<String, String> myStoredPasswords = new HashMap<>();

  public static PasswordStore getInstance(final Project project) {
    return AirPackageProjectParameters.getPasswordStore(project);
  }

  public boolean isRememberPasswords() {
    return rememberPasswords;
  }

  public void setRememberPasswords(final boolean rememberPasswords) {
    this.rememberPasswords = rememberPasswords;
  }

  @Nullable
  public String getKeystorePassword(final String keystorePath) {
    return myStoredPasswords.get(keystorePath);
  }

  @Nullable
  public String getKeyPassword(final String keystorePath, final String keyAlias) {
    return myStoredPasswords.get(keystorePath + "*" + keyAlias);
  }

  public void clearPasswords() {
    myStoredPasswords.clear();
  }

  public void storeKeystorePassword(final String keystorePath, final String keystorePassword) {
    myStoredPasswords.put(keystorePath, keystorePassword);
  }

  public void storeKeyPassword(final String keystorePath, final String keyAlias, final String keyPassword) {
    myStoredPasswords.put(keystorePath + "*" + keyAlias, keyPassword);
  }

  public static boolean isPasswordKnown(final Project project, final AirSigningOptions signingOptions) {
    final PasswordStore passwordStore = getInstance(project);
    final String keystorePassword = passwordStore.getKeystorePassword(signingOptions.getKeystorePath());
    final String keyPassword = signingOptions.getKeyAlias().isEmpty()
                               ? ""
                               : passwordStore.getKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias());
    if (keystorePassword == null || keyPassword == null) {
      return false;
    }

    try {
      checkPassword(signingOptions, keystorePassword, keyPassword);
    }
    catch (SigningOptionsException e) {
      return false;
    }

    return true;
  }

  public static void checkPassword(final AirSigningOptions signingOptions,
                                   final String keystorePassword,
                                   String keyPassword) throws SigningOptionsException {
    final KeyStore keyStore;
    try {
      keyStore = signingOptions.getProvider().isEmpty()
                 ? KeyStore.getInstance(signingOptions.getKeystoreType())
                 : KeyStore.getInstance(signingOptions.getKeystoreType(), signingOptions.getProvider());
    }
    catch (KeyStoreException ex) {
      throw new SigningOptionsException("Keystore type is not available: " + signingOptions.getKeystoreType());
    }
    catch (NoSuchProviderException ex) {
      throw new SigningOptionsException("Provider is not available: " + signingOptions.getProvider());
    }

    try {
      try {
        keyStore.load(new FileInputStream(signingOptions.getKeystorePath()), keystorePassword.toCharArray());
      }
      catch (IOException ex) {
        throw new SigningOptionsException("Incorrect keystore password", true, false);
      }
      catch (CertificateException ex) {
        throw new SigningOptionsException("Failed to load a certificate");
      }

      String keyAlias = signingOptions.getKeyAlias();
      if (keyAlias.isEmpty()) {
        if (!keyStore.aliases().hasMoreElements()) {
          throw new SigningOptionsException("Failed to obtain the key.");
        }

        keyAlias = keyStore.aliases().nextElement();
      }
      try {
        if (keyPassword.isEmpty()) keyPassword = keystorePassword;

        final PrivateKey key = (PrivateKey)keyStore.getKey(keyAlias, keyPassword.toCharArray());
      }
      catch (UnrecoverableKeyException ex) {
        throw new SigningOptionsException("Incorrect key password", false, true);
      }
    }
    catch (KeyStoreException ex) {
      throw new RuntimeException("Failed to load keystore");
    }
    catch (NoSuchAlgorithmException ex) {
      throw new SigningOptionsException("required crypto algorithm not available");
    }
  }
}
