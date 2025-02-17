package com.jetbrains.plugins.jade.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IStubFileElementType;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;

public class JadeFileElementType extends IStubFileElementType {
  public JadeFileElementType() {
    super("File:" + getMyLanguage().getDisplayName(), getMyLanguage());
  }

  private static Language getMyLanguage() {
    return JadeLanguage.INSTANCE;
  }

  @Override
  public @NotNull String getExternalId() {
    return getLanguage().getID() + ".file";
  }
}
