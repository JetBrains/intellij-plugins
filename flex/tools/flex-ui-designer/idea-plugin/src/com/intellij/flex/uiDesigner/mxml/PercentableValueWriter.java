package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;

class PercentableValueWriter implements ValueWriter {
  private final String value;

  public PercentableValueWriter(String value) {
    this.value = value;
  }

  @Override
  public PropertyProcessor.PropertyKind write(AnnotationBackedDescriptor descriptor, XmlElementValueProvider valueProvider,
                                              PrimitiveAmfOutputStream out, BaseWriter writer,
                                              boolean isStyle, Context parentContext) {
    out.writeAmfDouble(value);
    return PropertyKind.PRIMITIVE;
  }
}
