package com.intellij.lang.javascript.arrangement;

import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.generation.JavaScriptGenerateEventHandler;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.arrangement.*;
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken;
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*;

public class ActionScriptArrangementEntry extends DefaultArrangementEntry implements TypeAwareArrangementEntry,
                                                                                     ModifierAwareArrangementEntry,
                                                                                     NameAwareArrangementEntry {

  private static final Logger LOG = Logger.getInstance(ActionScriptArrangementEntry.class.getName());

  static final Comparator<ActionScriptArrangementEntry> COMPARATOR = new Comparator<ActionScriptArrangementEntry>() {
    public int compare(final ActionScriptArrangementEntry entry1, final ActionScriptArrangementEntry entry2) {
      return entry1.getStartOffset() - entry2.getStartOffset();
    }
  };


  private final String                        myName;
  private       ArrangementSettingsToken      myType;
  private       Set<ArrangementSettingsToken> myModifiers;

  private ActionScriptArrangementEntry(final @Nullable String name,
                                       final @NotNull ArrangementSettingsToken type,
                                       final @NotNull Set<ArrangementSettingsToken> modifiers,
                                       final @NotNull TextRange range)
  {
    super(null, range.getStartOffset(), range.getEndOffset(), true);

    myName = name;
    myType = type;
    myModifiers = modifiers;
  }

  @Nullable
  public static ActionScriptArrangementEntry create(final @NotNull JSBlockStatement blockStatement,
                                                    final @NotNull Collection<TextRange> allowedRanges,
                                                    final @Nullable Document document)
  {
    final TextRange textRange = blockStatement.getTextRange();

    if (isWithinBounds(textRange, allowedRanges)) {
      final TextRange range = document == null ? textRange : ArrangementUtil.expandToLineIfPossible(textRange, document);
      return new ActionScriptArrangementEntry(null, STATIC_INIT, Collections.<ArrangementSettingsToken>emptySet(), range);
    }

    return null;
  }

  @Nullable
  public static ActionScriptArrangementEntry create(final @NotNull JSVarStatement varStatement,
                                                    final @NotNull Collection<TextRange> allowedRanges,
                                                    final @Nullable Document document)
  {
    final TextRange textRange = varStatement.getTextRange();

    if (isWithinBounds(textRange, allowedRanges)) {
      final TextRange range = document == null ? textRange : ArrangementUtil.expandToLineIfPossible(textRange, document);
      final JSVariable[] variables = varStatement.getVariables();

      if (variables.length > 0) {
        final JSVariable variable = variables[0];
        return new ActionScriptArrangementEntry(variable.getName(), getType(variable), getModifiers(variable), range);
      }
    }

    return null;
  }

  @Nullable
  public static ActionScriptArrangementEntry create(final @NotNull JSFunction function,
                                                    final @NotNull Collection<TextRange> allowedRanges,
                                                    final @Nullable Document document) {
    final TextRange textRange = function.getTextRange();

    if (isWithinBounds(textRange, allowedRanges)) {
      final TextRange range = document == null ? textRange : ArrangementUtil.expandToLineIfPossible(textRange, document);
      return new ActionScriptArrangementEntry(function.getName(), getType(function), getModifiers(function), range);
    }

    return null;
  }

  private static ArrangementSettingsToken getType(final JSAttributeListOwner fieldOrMethod) {
    if (fieldOrMethod instanceof JSVariable) {
      return ((JSVariable)fieldOrMethod).isConst() ? CONST : VAR;
    }

    LOG.assertTrue(fieldOrMethod instanceof JSFunction, fieldOrMethod);

    final JSFunction function = (JSFunction)fieldOrMethod;

    if (function.isConstructor()) return CONSTRUCTOR;

    if (function.isGetProperty()) return PROPERTY;
    if (function.isSetProperty()) return PROPERTY;

    if (isEventHandler(function)) return EVENT_HANDLER;

    return METHOD;
  }

  private static boolean isEventHandler(final JSFunction function) {
    final JSParameter[] parameters = function.getParameters();

    if (parameters.length == 1) {
      final PsiElement typeElement = parameters[0].getTypeElement();
      if (typeElement instanceof JSReferenceExpression) {
        final PsiElement resolve = ((JSReferenceExpression)typeElement).resolve();
        if (resolve instanceof JSClass &&
            (FlexCommonTypeNames.FLASH_EVENT_FQN.equals(((JSClass)resolve).getQualifiedName()) ||
             FlexCommonTypeNames.STARLING_EVENT_FQN.equals(((JSClass)resolve).getQualifiedName()) ||
             JavaScriptGenerateEventHandler.isEventClass((JSClass)resolve))) {
          return true;
        }
      }
    }

    return false;
  }

  private static Set<ArrangementSettingsToken> getModifiers(final JSAttributeListOwner fieldOrMethod) {
    final Set<ArrangementSettingsToken> result = ContainerUtilRt.newHashSet();

    final JSAttributeList attributes = fieldOrMethod.getAttributeList();

    if (attributes != null) {
      JSAttributeList.AccessType accessType = attributes.getExplicitAccessType();

      if (accessType == null) {
        final String namespace = attributes.getNamespace();
        if (namespace == null) {
          accessType = JSAttributeList.AccessType.PACKAGE_LOCAL;
        }
      }

      if (accessType != null) {
        switch (accessType) {
          case PUBLIC:
            result.add(StdArrangementTokens.Modifier.PUBLIC);
            break;
          case PROTECTED:
            result.add(StdArrangementTokens.Modifier.PROTECTED);
            break;
          case PACKAGE_LOCAL:
            result.add(StdArrangementTokens.Modifier.PACKAGE_PRIVATE);
            break;
          case PRIVATE:
            result.add(StdArrangementTokens.Modifier.PRIVATE);
            break;
        }
      }

      if (attributes.hasModifier(JSAttributeList.ModifierType.STATIC)) result.add(StdArrangementTokens.Modifier.STATIC);
      if (attributes.hasModifier(JSAttributeList.ModifierType.FINAL)) result.add(StdArrangementTokens.Modifier.FINAL);
      if (attributes.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) result.add(StdArrangementTokens.Modifier.OVERRIDE);
    }
    return result;
  }

  private static boolean isWithinBounds(final @NotNull TextRange range, final @NotNull Collection<TextRange> ranges) {
    for (TextRange textRange : ranges) {
      if (textRange.intersects(range)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public String getName() {
    return myName;
  }

  public void setType(final @NotNull ArrangementSettingsToken type) {
    myType = type;
  }

  @NotNull
  public ArrangementSettingsToken getType() {
    return myType;
  }

  @NotNull
  public Set<ArrangementSettingsToken> getTypes() {
    return ContainerUtilRt.newHashSet(myType);
  }

  public void setModifiers(final @NotNull Set<ArrangementSettingsToken> modifiers) {
    myModifiers = modifiers;
  }

  @NotNull
  public Set<ArrangementSettingsToken> getModifiers() {
    return myModifiers;
  }

  public String toString() {
    return StringUtil.join(myModifiers, " ").toLowerCase() + " " + myType.toString().toLowerCase() + " " + myName;
  }
}
