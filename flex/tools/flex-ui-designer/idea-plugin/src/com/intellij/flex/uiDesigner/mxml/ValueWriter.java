package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;

interface ValueWriter {
  PropertyKind write(AnnotationBackedDescriptor descriptor, XmlElementValueProvider valueProvider, PrimitiveAmfOutputStream out,
                     BaseWriter writer, boolean isStyle, Context parentContext) throws InvalidPropertyException;
}
