package com.intellij.flex.uiDesigner.abc;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

public abstract class AbcModifierBase implements AbcModifier {
  protected static boolean isNotOverridenMethod(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Method && ((traitKind >> 4) & TRAIT_FLAG_Override) == 0;
  }

  protected static boolean isOverridenMethod(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Method && ((traitKind >> 4) & TRAIT_FLAG_Override) != 0;
  }

  protected static boolean isVar(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Var;
  }

  protected static boolean isSetter(int traitKind) {
    return (traitKind & 0x0f) == TRAIT_Setter;
  }

  @Override
  public boolean methodTrait(int traitKind, int name, DataBuffer in, Encoder encoder) {
    return false;
  }

  @Override
  public boolean methodTraitName(int traitKind, int kind, DataBuffer in, Encoder encoder) {
    return false;
  }

  @Override
  public boolean slotTraitName(int name, int traitKind, DataBuffer in, Encoder encoder) {
    return false;
  }

  @Override
  public int methodTraitDelta() {
    return 0;
  }
}