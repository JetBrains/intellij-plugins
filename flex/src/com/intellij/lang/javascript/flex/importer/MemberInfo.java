package com.intellij.lang.javascript.flex.importer;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
*/
abstract class MemberInfo {
  Traits parentTraits;
  int id;
  Abc.TraitType kind;
  Multiname name;
  MetaData[] metadata;
  boolean isOverride;
  boolean isPublic;
  boolean isFinal;

  abstract void dump(Abc abc, String indent, String attr, final @NotNull FlexByteCodeInformationProcessor processor);

  protected void dumpMetaData(String indent, final @NotNull FlexByteCodeInformationProcessor processor) {
    if (metadata != null) {
      for (MetaData md : metadata) {
        if (processor.doDumpMetaData(md)) {
          processor.append(indent);
          processor.processMetadata(md);
          processor.append("\n");
        }
      }
    }
  }

  String getParentName() {
    return parentTraits != null ? parentTraits.getClassName() : null;
  }
}
