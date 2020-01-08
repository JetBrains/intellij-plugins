package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;


import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class PhoneGapBundle extends DynamicBundle {

  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return ourInstance.getMessage(key, params);
  }

  @NonNls public static final String BUNDLE = "messages.PhoneGapBundle";
  private static final PhoneGapBundle ourInstance = new PhoneGapBundle();

  private PhoneGapBundle() {
    super(BUNDLE);
  }
}
