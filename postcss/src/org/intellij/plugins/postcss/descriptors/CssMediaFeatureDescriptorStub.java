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

  protected CssMediaFeatureDescriptorStub(@NotNull final String name) {
    myName = name;
  }

  @NotNull
  @Override
  public String getId() {
    return myName;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return getId();
  }

  @Override
  public boolean isAllowedInContextType(@NotNull CssContextType contextType) {
    return true;
  }

  @Nullable
  @Override
  public String getAppliesToValue() {
    return "";
  }

  @Override
  public CssMediaGroup @NotNull [] getMediaGroups() {
    return CssConstants.DEFAULT_MEDIA_GROUPS;
  }

  @NotNull
  @Override
  public String getElementTypeName() {
    return PostCssBundle.message("postcss.custom.media.query");
  }

  @NotNull
  @Override
  public CssValueDescriptor getValueDescriptor() {
    return new CssNullValue();
  }
}