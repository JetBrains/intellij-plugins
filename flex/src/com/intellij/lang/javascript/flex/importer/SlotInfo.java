package com.intellij.lang.javascript.flex.importer;

/**
 * @author Maxim.Mossienko
*/
class SlotInfo extends MemberInfo {
  Multiname type;
  Object value;

  SlotInfo(Multiname _name, Abc.TraitType _kind) {
    name = _name;
    kind = _kind;
  }

  void dump(Abc abc, String indent, String attr, final FlexByteCodeInformationProcessor processor) {
    if (!processor.doDumpMember(this)) return;

    if (kind == Abc.TraitType.Const || kind == Abc.TraitType.Slot) {
      processor.processVariable(this, indent, attr);
      return;
    }

    processor.processClass(this, abc, attr, indent);
  }

  boolean isInterfaceClass() {
    if (!(value instanceof Traits)) return false;
    return (((Traits)value).itraits.flags & Abc.CLASS_FLAG_interface) != 0;
  }

  public boolean isConst() {
    return kind == Abc.TraitType.Const;
  }
}
