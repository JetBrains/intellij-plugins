package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BINARY_EXPRESSIONS;


/**
 * @author: Fedor.Korotkov
 */
public class DartAlignmentProcessor {
  private final ASTNode myNode;
  private final Alignment myBaseAlignment;
  private final CommonCodeStyleSettings mySettings;

  public DartAlignmentProcessor(ASTNode node, CommonCodeStyleSettings settings) {
    myNode = node;
    mySettings = settings;
    myBaseAlignment = Alignment.createAlignment();
  }

  @Nullable
  public Alignment createChildAlignment() {
    IElementType elementType = myNode.getElementType();

    if (BINARY_EXPRESSIONS.contains(elementType) && mySettings.ALIGN_MULTILINE_BINARY_OPERATION) {
      return myBaseAlignment;
    }

    if (elementType == TERNARY_EXPRESSION && mySettings.ALIGN_MULTILINE_TERNARY_OPERATION) {
      return myBaseAlignment;
    }

    if (elementType == FORMAL_PARAMETER_LIST) {
      if (mySettings.ALIGN_MULTILINE_PARAMETERS) {
        return myBaseAlignment;
      }
    }
    if (elementType == ARGUMENTS) {
      if (mySettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS) {
        return myBaseAlignment;
      }
    }

    return null;
  }
}
