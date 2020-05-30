// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

// TODO Eliminate redundancy. This gets called multiple times by CodeStyleManagerImpl.reformatText().
// The first is by a call to CodeFormatterFacade.processText() at line 235.
// The second is from a call to EditorEx.reinitSettings() at line 251.
// The second is only done when reformatting the entire file; however, when
// reformatting a selection this may be called three times.
public class DartWrappingProcessor {

  // Consider using a single key -- the grammar doesn't allow mis-use.
  private static final Key<Wrap> DART_TERNARY_EXPRESSION_WRAP_KEY = Key.create("TERNARY_EXPRESSION_WRAP_KEY");
  private static final Key<Wrap> DART_COLLECTION_ELEMENT_WRAP_KEY = Key.create("COLLECTION_ELEMENT_WRAP_KEY");
  private static final Key<Wrap> DART_ARGUMENT_LIST_WRAP_KEY = Key.create("ARGUMENT_LIST_WRAP_KEY");
  private static final Key<Wrap> DART_TYPE_LIST_WRAP_KEY = Key.create("TYPE_LIST_WRAP_KEY");
  private static final TokenSet NAMED_ARGUMENTS = TokenSet.create(NAMED_ARGUMENT);

  private final ASTNode myNode;

  public DartWrappingProcessor(ASTNode node) {
    myNode = node;
  }

  Wrap createChildWrap(ASTNode child, Wrap defaultWrap, Wrap childWrap) {
    final IElementType childType = child.getElementType();
    final IElementType elementType = myNode.getElementType();
    if (childType == COMMA || childType == SEMICOLON) return defaultWrap;

    //
    // Function definition/call
    //
    if (elementType == ARGUMENT_LIST) {
      if (child instanceof PsiErrorElement) {
        myNode.putUserData(DART_ARGUMENT_LIST_WRAP_KEY, null);
      }

      if (childWrap != null) {
        return Wrap.createChildWrap(childWrap, WrapType.NORMAL, true);
      }
      Wrap wrap;
      // First, do persistent object management.
      if (myNode.getFirstChildNode() == child && childType != NAMED_ARGUMENT) {
        ASTNode[] children = myNode.getChildren(DartIndentProcessor.EXPRESSIONS);
        if (children.length >= 7) { // Approximation; dart_style uses dynamic programming with cost-based analysis to choose.
          wrap = Wrap.createWrap(WrapType.ALWAYS, true);
        }
        else {
          wrap = Wrap.createWrap(WrapType.NORMAL, true); // NORMAL,CHOP_DOWN_IF_LONG
        }
        if (myNode.getLastChildNode() != child) {
          myNode.putUserData(DART_ARGUMENT_LIST_WRAP_KEY, wrap);
        }
      }
      else {
        if (childType == NAMED_ARGUMENT) {
          ASTNode[] named = myNode.getChildren(NAMED_ARGUMENTS);
          wrap = myNode.getUserData(DART_ARGUMENT_LIST_WRAP_KEY);
          if (child == named[0]) {
            if (named.length > 1) {
              ASTNode[] children = myNode.getChildren(DartIndentProcessor.EXPRESSIONS);
              Wrap namedWrap;
              if (children.length >= 7 || named.length > 4) { // Another approximation.
                namedWrap = Wrap.createWrap(WrapType.ALWAYS, true);
              }
              else {
                namedWrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
              }
              myNode.putUserData(DART_ARGUMENT_LIST_WRAP_KEY, namedWrap);
            }
          }
        }
        else {
          wrap = myNode.getUserData(DART_ARGUMENT_LIST_WRAP_KEY);
        }
        if (myNode.getLastChildNode() == child) {
          myNode.putUserData(DART_ARGUMENT_LIST_WRAP_KEY, null);
        }
      }
      // Second, decide what object to return.
      if (childType == MULTI_LINE_COMMENT || childType == FUNCTION_EXPRESSION) {
        return Wrap.createWrap(WrapType.NONE, false);
      }
      return wrap != null ? wrap : Wrap.createWrap(WrapType.NORMAL, false);
    }

    if (elementType == FORMAL_PARAMETER_LIST) {
      if (myNode.getFirstChildNode() == child) {
        return createWrap(false);
      }
      if (childType == RPAREN) {
        return createWrap(false);
      }
      return Wrap.createWrap(WrapType.NORMAL, true);
    }

    if (elementType == INITIALIZERS) {
      if (childType != COLON && isNotFirstInitializer(child)) {
        return Wrap.createWrap(WrapType.ALWAYS, true);
      }
      if (childType == COLON && !DartSpacingProcessor.hasMultipleInitializers(child)) {
        return Wrap.createWrap(WrapType.NORMAL, true);
      }
    }

    if (elementType == SET_OR_MAP_LITERAL_EXPRESSION || elementType == LIST_LITERAL_EXPRESSION) {
      // First, do persistent object management.
      Wrap wrap = sharedWrap(child, DART_COLLECTION_ELEMENT_WRAP_KEY);
      // Second, decide what object to return.
      if (childType == LBRACE || childType == LBRACKET) {
        return Wrap.createWrap(WrapType.NONE, false);
      }
      if (childType == MULTI_LINE_COMMENT || childType == CONST || childType == TYPE_ARGUMENTS) {
        return Wrap.createWrap(WrapType.NONE, false);
      }
      return wrap != null ? wrap : Wrap.createWrap(WrapType.NORMAL, true);
    }

    //
    // Wrap after arrows.
    //
    if (elementType == FUNCTION_BODY) {
      if (FormatterUtil.isPrecededBy(child, EXPRESSION_BODY_DEF)) {
        return createWrap(true);
      }
    }
    if (childType == CALL_EXPRESSION) {
      if (FormatterUtil.isPrecededBy(child, EXPRESSION_BODY_DEF)) {
        return createWrap(true);
      }
    }

    //
    // If
    //
    if (elementType == IF_STATEMENT) {
      if (childType == ELSE) {
        return createWrap(false);
      }
      else if (!BLOCKS.contains(childType) && child == child.getTreeParent().getLastChildNode()) {
        return createWrap(true);
      }
    }

    //
    //Binary expressions
    //
    if (BINARY_EXPRESSIONS.contains(elementType)) {
      if (isRightOperand(child)) {
        return Wrap.createWrap(WrapType.NORMAL, true);
      }
    }

    //
    // Assignment
    //
    if (elementType == ASSIGN_EXPRESSION) {
      if (childType != ASSIGNMENT_OPERATOR) {
        return Wrap.createWrap(WrapType.NORMAL, true);
      }
    }

    //
    // Ternary expressions
    //
    if (elementType == TERNARY_EXPRESSION) {
      if (myNode.getFirstChildNode() != child) {
        if (childType == QUEST) {
          final Wrap wrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
          myNode.putUserData(DART_TERNARY_EXPRESSION_WRAP_KEY, wrap);
          return wrap;
        }

        if (childType == COLON) {
          final Wrap wrap = myNode.getUserData(DART_TERNARY_EXPRESSION_WRAP_KEY);
          myNode.putUserData(DART_TERNARY_EXPRESSION_WRAP_KEY, null);
          return wrap != null ? wrap : Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
        }
      }
      return Wrap.createWrap(WrapType.NONE, true);
    }

    if (childType == HIDE_COMBINATOR || childType == SHOW_COMBINATOR) {
      return createWrap(true);
    }

    if (childType == VAR_DECLARATION_LIST && elementType != FOR_LOOP_PARTS) {
      if (varDeclListContainsVarInit(child)) {
        return Wrap.createWrap(WrapType.ALWAYS, true);
      }
      else {
        return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
      }
    }
    if (childType == VAR_DECLARATION_LIST_PART) {
      ASTNode parent = getParent();
      if (parent != null && parent.getElementType() == FOR_LOOP_PARTS) {
        return Wrap.createWrap(WrapType.NORMAL, true);
      }
      else {
        if (varDeclListContainsVarInit(myNode)) {
          return Wrap.createWrap(WrapType.ALWAYS, true);
        }
        else {
          return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
        }
      }
    }

    if (elementType == CLASS_DEFINITION) {
      if (childType == SUPERCLASS || childType == INTERFACES || childType == MIXINS) {
        return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
      }
    }
    if (elementType == MIXIN_APPLICATION && childType == MIXINS) {
      return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
    }
    if (elementType == ENUM_DEFINITION) {
      if (childType == ENUM_CONSTANT_DECLARATION) {
        return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
      }
    }
    if (elementType == TYPE_LIST) {
      if (childType == TYPE) {
        Wrap wrap = sharedWrap(child, DART_TYPE_LIST_WRAP_KEY);
        return wrap == null ? Wrap.createWrap(WrapType.NORMAL, true) : wrap;
      }
    }

    if (elementType == REFERENCE_EXPRESSION && (childType == DOT || childType == QUEST_DOT)) {
      return Wrap.createWrap(WrapType.NORMAL, true); // NORMAL,CHOP_DOWN_IF_LONG
    }

    return defaultWrap;
  }

  private boolean isRightOperand(ASTNode child) {
    return myNode.getLastChildNode() == child;
  }

  private ASTNode getParent() {
    return myNode.getTreeParent();
  }

  private static Wrap createWrap(boolean isNormal) {
    return Wrap.createWrap(isNormal ? WrapType.NORMAL : WrapType.NONE, true);
  }

  private static boolean varDeclListContainsVarInit(ASTNode decl) {
    if (decl.findChildByType(VAR_INIT) != null) return true;
    ASTNode child = decl.getFirstChildNode();
    while (child != null) {
      if (child.findChildByType(VAR_INIT) != null) return true;
      child = child.getTreeNext();
    }
    return false;
  }

  private static boolean isNotFirstInitializer(ASTNode child) {
    ASTNode prev = child;
    boolean isFirst = false;
    while ((prev = prev.getTreePrev()) != null) {
      if (prev.getElementType() == COLON) {
        return isFirst;
      }
      if (prev.getElementType() != WHITE_SPACE && !COMMENTS.contains(prev.getElementType())) {
        isFirst = true;
      }
    }
    return isFirst;
  }

  private Wrap sharedWrap(ASTNode child, Key<Wrap> key) {
    Wrap wrap;
    if (myNode.getFirstChildNode() == child) {
      wrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
      if (myNode.getLastChildNode() != child) {
        myNode.putUserData(key, wrap);
      }
    }
    else {
      wrap = myNode.getUserData(key);
      if (myNode.getLastChildNode() == child) {
        myNode.putUserData(key, null);
      }
    }
    return wrap;
  }
}
