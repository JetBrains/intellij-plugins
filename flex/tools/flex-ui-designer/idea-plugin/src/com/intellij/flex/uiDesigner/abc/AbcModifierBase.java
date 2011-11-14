package com.intellij.flex.uiDesigner.abc;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.TRAIT_FLAG_Override;
import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.TRAIT_Method;
import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.TRAIT_Var;

public abstract class AbcModifierBase implements AbcModifier {
  protected static boolean isNotOverridenMethod(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Method && ((traitKind >> 4) & TRAIT_FLAG_Override) == 0;
  }

  protected static boolean isVar(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Var;
  }

  @Override
  public boolean writeMethodTraitName(int traitKind, int kind, DataBuffer in, Encoder encoder) {
    return false;
  }

  @Override
  public boolean writeSlotTraitName(int name, int traitKind, DataBuffer in, Encoder encoder) {
    return false;
  }
}