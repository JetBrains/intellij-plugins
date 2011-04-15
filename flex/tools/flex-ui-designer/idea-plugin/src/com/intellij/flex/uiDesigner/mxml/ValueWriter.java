package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;

interface ValueWriter {
  int write(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException;
}
