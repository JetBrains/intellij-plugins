package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class MarkdownUtil {
  private static final Logger LOG = Logger.getInstance(MarkdownUtil.class);

  public static String md5(String buffer, @NonNls String key) {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      LOG.error("Cannot find 'md5' algorithm; ", e);
    }

    Objects.requireNonNull(md5).update(buffer.getBytes(StandardCharsets.UTF_8));
    byte[] code = md5.digest(key.getBytes(StandardCharsets.UTF_8));
    BigInteger bi = new BigInteger(code).abs();
    return bi.abs().toString(16);
  }
}