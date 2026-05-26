package org.intellij.plugins.postcss.descriptors;

import com.intellij.css.util.CssConstants;
import com.intellij.psi.css.CssMediaFeatureDescriptor;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.descriptor.CssMediaGroup;
import com.intellij.psi.css.descriptor.value.CssNullValue;
import com.intellij.psi.css.descriptor.value.CssValueDescriptor;
import org.intellij.plugins.postcss.PostCssBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CssMediaFeatureDescriptorStub extends CssNullValue implements CssMediaFeatureDescriptor {

  private final String myName;

  protected CssMediaFeatureDescriptorStub(final @NotNull String name) {
    myName = name;
  }

  @Override
  public @NotNull String getId() {
    return myName;
  }

  @Override
  public @NotNull String getPresentableName() {
    return getId();
  }

  @Override
  public boolean isAllowedInContextType(@NotNull CssContextType contextType) {
    return true;
  }

  @Override
  public @Nullable String getAppliesToValue() {
    return "";
  }

  @Override
  public CssMediaGroup @NotNull [] getMediaGroups() {
    return CssConstants.DEFAULT_MEDIA_GROUPS;
  }

  @Override
  public @NotNull String getElementTypeName() {
    return PostCssBundle.message("postcss.custom.media.query");
  }

  @Override
  public @NotNull CssValueDescriptor getValueDescriptor() {
    return new CssNullValue();
  }
}