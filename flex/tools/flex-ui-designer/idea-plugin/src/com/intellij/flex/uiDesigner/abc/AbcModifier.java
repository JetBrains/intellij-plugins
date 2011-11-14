package com.intellij.flex.uiDesigner.abc;

public interface AbcModifier {
  boolean writeMethodTraitName(int traitKind, int kind, DataBuffer in, Encoder encoder);

  boolean writeSlotTraitName(int name, int traitKind, DataBuffer in, Encoder encoder);
}
