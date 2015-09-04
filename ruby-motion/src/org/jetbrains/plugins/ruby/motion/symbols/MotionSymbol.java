package org.jetbrains.plugins.ruby.motion.symbols;

import com.jetbrains.cidr.CocoaDocumentationManager;

/**
 * @author Dennis.Ushakov
 */
public interface MotionSymbol {
  CocoaDocumentationManager.DocTokenType getInfoType();

  String getInfoName();
}
