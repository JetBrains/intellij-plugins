package org.jetbrains.plugins.ruby.motion.symbols;

import com.jetbrains.cidr.CocoaDocumentationManagerImpl;

/**
 * @author Dennis.Ushakov
 */
public interface MotionSymbol {
  CocoaDocumentationManagerImpl.DocTokenType getInfoType();

  String getInfoName();
}
