package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.CommonBundle;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * @author Max Medvedev
 */

public class GrCucumberBundle extends GroovyObjectSupport implements GroovyObject {

  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  @NonNls public static final String BUNDLE = "org.jetbrains.plugins.cucumber.groovy.GrCucumberBundle";
  private static Reference<ResourceBundle> ourBundle;

  private GrCucumberBundle() {
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<>(bundle);
    }

    return bundle;
  }
}
