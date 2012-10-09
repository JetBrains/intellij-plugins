package com.intellij.lang.javascript.arrangement;

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
import com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryType;
import com.intellij.psi.codeStyle.arrangement.match.ArrangementModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryType.*;

public class ActionScriptArrangementEntry extends DefaultArrangementEntry implements TypeAwareArrangementEntry,
                                                                                     ModifierAwareArrangementEntry,
                                                                                     NameAwareArrangementEntry {

  private static final Logger LOG = Logger.getInstance(ActionScriptArrangementEntry.class.getName());

  static final Comparator<ActionScriptArrangementEntry> COMPARATOR = new Comparator<ActionScriptArrangementEntry>() {
    public int compare(final ActionScriptArrangementEntry entry1, final ActionScriptArrangementEntry entry2) {
      return entry1.getStartOffset() - entry2.getStartOffset();
    }
  };


  private String myName;
  private ArrangementEntryType myType;
  private Set<ArrangementModifier> myModifiers;

  private ActionScriptArrangementEntry(final @Nullable String name,
                                       final ArrangementEntryType type,
                                       final Set<ArrangementModifier> modifiers,
                                       final TextRange range) {
    super(null, range.getStartOffset(), range.getEndOffset(), true);

    myName = name;
    myType = type;
    myModifiers = modifiers;
  }

  @Nullable
  public static ActionScriptArrangementEntry create(final @NotNull JSBlockStatement blockStatement,
                                                    final @NotNull Collection<TextRange> allowedRanges,
                                                    final @Nullable Document document) {
    final TextRange textRange = blockStatement.getTextRange();

    if (isWithinBounds(textRange, allowedRanges)) {
      final TextRange range = document == null ? textRange : ArrangementUtil.expandToLine(textRange, document);
      return new ActionScriptArrangementEntry(null, STATIC_INIT, Collections.<ArrangementModifier>emptySet(), range);
    }

    return null;
  }

  @Nullable
  public static ActionScriptArrangementEntry create(final @NotNull JSAttributeListOwner fieldOrMethod,
                                                    final @NotNull Collection<TextRange> allowedRanges,
                                                    final @Nullable Document document) {
    final TextRange textRange = fieldOrMethod.getTextRange();

    if (isWithinBounds(textRange, allowedRanges)) {
      LOG.assertTrue(fieldOrMethod instanceof JSVariable ||
                     (fieldOrMethod instanceof JSFunction && !(fieldOrMethod instanceof JSFunctionExpression)),
                     fieldOrMethod);

      final TextRange range = document == null ? textRange : ArrangementUtil.expandToLine(textRange, document);
      return new ActionScriptArrangementEntry(fieldOrMethod.getName(), getType(fieldOrMethod), getModifiers(fieldOrMethod), range);
    }

    return null;
  }

  private static ArrangementEntryType getType(final JSAttributeListOwner fieldOrMethod) {
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
            (JavaScriptGenerateEventHandler.EVENT_BASE_CLASS_FQN.equals(((JSClass)resolve).getQualifiedName()) ||
             JavaScriptGenerateEventHandler.isEventClass((JSClass)resolve))) {
          return true;
        }
      }
    }

    return false;
  }

  private static Set<ArrangementModifier> getModifiers(final JSAttributeListOwner fieldOrMethod) {
    final Set<ArrangementModifier> result = EnumSet.noneOf(ArrangementModifier.class);

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
            result.add(ArrangementModifier.PUBLIC);
            break;
          case PROTECTED:
            result.add(ArrangementModifier.PROTECTED);
            break;
          case PACKAGE_LOCAL:
            result.add(ArrangementModifier.PACKAGE_PRIVATE);
            break;
          case PRIVATE:
            result.add(ArrangementModifier.PRIVATE);
            break;
        }
      }

      if (attributes.hasModifier(JSAttributeList.ModifierType.STATIC)) result.add(ArrangementModifier.STATIC);
      if (attributes.hasModifier(JSAttributeList.ModifierType.FINAL)) result.add(ArrangementModifier.FINAL);
      if (attributes.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) result.add(ArrangementModifier.OVERRIDE);
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

  public ArrangementEntryType getType() {
    return myType;
  }

  @NotNull
  public Set<ArrangementEntryType> getTypes() {
    return EnumSet.of(myType);
  }

  @NotNull
  public Set<ArrangementModifier> getModifiers() {
    return myModifiers;
  }

  public String toString() {
    return StringUtil.join(myModifiers, " ").toLowerCase() + " " + myType.toString().toLowerCase() + " " + myName;
  }
}
