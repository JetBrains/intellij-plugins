package org.osmorc.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class OsmorcResourceBundle {
  @NonNls
  public static final String BUNDLE = "org.osmorc.util.Osmorc";
  @NonNls
  private static Reference<ResourceBundle> bundleReference;

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (bundleReference != null) bundle = bundleReference.get();
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      bundleReference = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }

  public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }
}
