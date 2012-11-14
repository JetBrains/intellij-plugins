package com.intellij.lang.javascript.arrangement;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings;
import com.intellij.psi.codeStyle.arrangement.ArrangementUtil;
import com.intellij.psi.codeStyle.arrangement.Rearranger;
import com.intellij.psi.codeStyle.arrangement.StdArrangementSettings;
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule;
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingType;
import com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryType;
import com.intellij.psi.codeStyle.arrangement.match.ArrangementModifier;
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher;
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule;
import com.intellij.psi.codeStyle.arrangement.model.*;
import com.intellij.psi.codeStyle.arrangement.order.ArrangementEntryOrderType;
import com.intellij.psi.codeStyle.arrangement.settings.ArrangementConditionsGrouper;
import com.intellij.psi.codeStyle.arrangement.settings.ArrangementStandardSettingsAware;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.SortedList;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryType.*;
import static com.intellij.psi.codeStyle.arrangement.match.ArrangementModifier.*;

public class ActionScriptRearranger implements Rearranger<ActionScriptArrangementEntry>, ArrangementStandardSettingsAware,
                                               ArrangementConditionsGrouper {

  private static final Logger LOG = Logger.getInstance(ActionScriptRearranger.class.getName());

  private static final Set<ArrangementEntryType> SUPPORTED_TYPES =
    EnumSet.of(STATIC_INIT, CONST, VAR, METHOD, CONSTRUCTOR, PROPERTY, EVENT_HANDLER);

  private static final Set<ArrangementModifier> SUPPORTED_MODIFIERS =
    EnumSet.of(PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE, STATIC, FINAL, OVERRIDE);

  private static void addRule(final List<StdArrangementMatchRule> rules, @NotNull Object... conditions) {
    if (conditions.length == 1) {
      rules.add(new StdArrangementMatchRule(
        new StdArrangementEntryMatcher(new ArrangementAtomMatchCondition(ArrangementUtil.parseType(conditions[0]), conditions[0]))));
      return;
    }

    ArrangementCompositeMatchCondition composite = new ArrangementCompositeMatchCondition();
    for (Object condition : conditions) {
      composite.addOperand(new ArrangementAtomMatchCondition(ArrangementUtil.parseType(condition), condition));
    }
    rules.add(new StdArrangementMatchRule(new StdArrangementEntryMatcher(composite)));
  }

  @Nullable
  @Override
  public Pair<ActionScriptArrangementEntry, List<ActionScriptArrangementEntry>> parseWithNew(@NotNull PsiElement root,
                                                                                             @Nullable Document document,
                                                                                             @NotNull Collection<TextRange> ranges,
                                                                                             @NotNull PsiElement element,
                                                                                             @Nullable ArrangementSettings settings) {
    // todo implement and use in quick fixes, code generators, etc.
    return null;
  }

  @NotNull
  @Override
  public List<ActionScriptArrangementEntry> parse(final @NotNull PsiElement root,
                                                  final @Nullable Document document,
                                                  final @NotNull Collection<TextRange> ranges,
                                                  final @Nullable ArrangementSettings settings) {
    final PsiFile file = root.getContainingFile();
    if (!(file instanceof JSFile)) return Collections.emptyList();

    final JSClass jsClass = JSPsiImplUtils.findClass((JSFile)file);
    if (jsClass == null) return Collections.emptyList();

    final List<ActionScriptArrangementEntry> result = new SortedList<ActionScriptArrangementEntry>(ActionScriptArrangementEntry.COMPARATOR);

    // static init blocks
    final JSBlockStatement[] blockStatements = PsiTreeUtil.getChildrenOfType(jsClass, JSBlockStatement.class);
    if (blockStatements != null) {
      for (JSBlockStatement blockStatement : blockStatements) {
        ContainerUtil.addIfNotNull(result, ActionScriptArrangementEntry.create(blockStatement, ranges, document));
      }
    }

    // vars and consts. Added to map only if there's only one var in JSVarStatement
    final Map<JSVariable, ActionScriptArrangementEntry> varToEntry = new THashMap<JSVariable, ActionScriptArrangementEntry>();

    final JSVarStatement[] varStatements = PsiTreeUtil.getChildrenOfType(jsClass, JSVarStatement.class);
    if (varStatements != null) {
      for (final JSVarStatement varStatement : varStatements) {
        final ActionScriptArrangementEntry entry = ActionScriptArrangementEntry.create(varStatement, ranges, document);

        if (entry != null) {
          result.add(entry);

          final JSVariable[] variables = varStatement.getVariables();
          if (entry.getType() == VAR && variables.length == 1) {
            varToEntry.put(variables[0], entry);
          }
        }
      }
    }

    // methods, getters/setters and event handlers
    final Map<JSFunction, ActionScriptArrangementEntry> functionToEntry = new THashMap<JSFunction, ActionScriptArrangementEntry>();

    for (final JSFunction function : jsClass.getFunctions()) {
      final ActionScriptArrangementEntry entry = ActionScriptArrangementEntry.create(function, ranges, document);
      if (entry != null) {
        result.add(entry);
        functionToEntry.put(function, entry);
      }
    }

    // group getters and setters, getters first
    for (Map.Entry<JSFunction, ActionScriptArrangementEntry> mapEntry : functionToEntry.entrySet()) {
      final JSFunction function = mapEntry.getKey();

      final String name = function.getName();
      if (function.isSetProperty() && name != null) {
        final ActionScriptArrangementEntry setterEntry = mapEntry.getValue();

        final JSFunction getter = jsClass.findFunctionByNameAndKind(name, JSFunction.FunctionKind.GETTER);
        final ActionScriptArrangementEntry getterEntry = functionToEntry.get(getter);

        if (getterEntry != null) {
          setterEntry.addDependency(getterEntry);
        }
      }
    }

    // group property fields with getters/setters
    if (settings != null && groupPropertyFieldWithGetterSetter(settings)) {
      final JSCodeStyleSettings codeStyleSettings =
        CodeStyleSettingsManager.getSettings(jsClass.getProject()).getCustomSettings(JSCodeStyleSettings.class);

      for (Map.Entry<JSVariable, ActionScriptArrangementEntry> mapEntry : varToEntry.entrySet()) {
        final JSVariable jsVar = mapEntry.getKey();
        final ActionScriptArrangementEntry varEntry = mapEntry.getValue();

        if (StringUtil.startsWith(jsVar.getName(), codeStyleSettings.FIELD_PREFIX)) {
          final String propertyName = JSResolveUtil.transformVarNameToAccessorName(jsVar.getName(), jsClass.getProject());

          JSFunction getterOrSetter = jsClass.findFunctionByNameAndKind(propertyName, JSFunction.FunctionKind.GETTER);
          if (getterOrSetter == null) getterOrSetter = jsClass.findFunctionByNameAndKind(propertyName, JSFunction.FunctionKind.SETTER);

          final ActionScriptArrangementEntry propertyEntry = getterOrSetter == null ? null : functionToEntry.get(getterOrSetter);
          if (propertyEntry != null) {
            // arrangement engine sorts group according to the first entry, so we pretend that var is a property
            varEntry.setType(propertyEntry.getType());
            varEntry.setModifiers(propertyEntry.getModifiers());

            propertyEntry.addDependency(varEntry);
          }
        }
      }
    }

    return result;
  }

  private static boolean groupPropertyFieldWithGetterSetter(final @NotNull ArrangementSettings settings) {
    for (ArrangementGroupingRule rule : settings.getGroupings()) {
      if (rule.getGroupingType() == ArrangementGroupingType.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int getBlankLines(@NotNull CodeStyleSettings settings,
                           @Nullable ActionScriptArrangementEntry parent,
                           @Nullable ActionScriptArrangementEntry previous,
                           @NotNull ActionScriptArrangementEntry target) {
    if (previous == null) {
      return -1;
    }

    final CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    switch (target.getType()) {
      case VAR:
      case CONST:
        return commonSettings.BLANK_LINES_AROUND_FIELD;

      case STATIC_INIT:
      case CONSTRUCTOR:
      case METHOD:
      case PROPERTY:
      case EVENT_HANDLER:
        return commonSettings.BLANK_LINES_AROUND_METHOD;

      default:
        LOG.error(target.getType());
        return 0;
    }
  }

  @Nullable
  @Override
  public StdArrangementSettings getDefaultSettings() {
    final List<ArrangementGroupingRule> groupingRules =
      Collections.singletonList(new ArrangementGroupingRule(ArrangementGroupingType.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER));
    final List<StdArrangementMatchRule> matchRules = getDefaultMatchRules();

    return new StdArrangementSettings(groupingRules, matchRules);
  }

  @Override
  public boolean isNameFilterSupported() {
    return true;
  }

  public static List<StdArrangementMatchRule> getDefaultMatchRules() {
    // more or less close to Coding Conventions at http://sourceforge.net/adobe/flexsdk/wiki/Coding%20Conventions/
    final List<StdArrangementMatchRule> matchRules = new ArrayList<StdArrangementMatchRule>();

    final ArrangementModifier[] visibility = {PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE};

    // static initialization blocks
    addRule(matchRules, STATIC_INIT);

    // constants
    addRule(matchRules, CONST);

    // mix-ins (static vars of Function type)

    // resources (what's this?)

    // static vars
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, VAR, modifier, STATIC, FINAL);
    }
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, VAR, modifier, STATIC);
    }

    // static properties
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, PROPERTY, modifier, STATIC);
    }

    // static methods
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, METHOD, modifier, STATIC);
    }

    // constructor
    addRule(matchRules, CONSTRUCTOR);

    // vars
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, VAR, modifier, FINAL);
    }
    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, VAR, modifier);
    }

    // properties
    addRule(matchRules, PROPERTY, OVERRIDE);

    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, PROPERTY, modifier);
    }

    // methods
    addRule(matchRules, METHOD, OVERRIDE);

    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, METHOD, modifier);
    }

    // event handlers
    addRule(matchRules, EVENT_HANDLER, OVERRIDE);

    for (ArrangementModifier modifier : visibility) {
      addRule(matchRules, EVENT_HANDLER, modifier);
    }

    return matchRules;
  }

  @Override
  public boolean isEnabled(@NotNull ArrangementEntryType type, @Nullable ArrangementMatchCondition current) {
    return SUPPORTED_TYPES.contains(type);
  }

  @Override
  public boolean isEnabled(@NotNull ArrangementModifier modifier, @Nullable ArrangementMatchCondition current) {
    if (current == null) {
      return SUPPORTED_MODIFIERS.contains(modifier);
    }

    final Ref<ArrangementEntryType> typeRef = new Ref<ArrangementEntryType>();
    final Ref<Boolean> staticRef = new Ref<Boolean>(false);
    final Ref<Boolean> finalRef = new Ref<Boolean>(false);
    final Ref<Boolean> overrideRef = new Ref<Boolean>(false);

    current.invite(new ArrangementMatchConditionVisitor() {
      @Override
      public void visit(@NotNull ArrangementAtomMatchCondition setting) {
        if (setting.getType() == ArrangementSettingType.TYPE) {
          typeRef.set((ArrangementEntryType)setting.getValue());
        }
        else if (setting.getValue() == STATIC) {
          staticRef.set(true);
        }
        else if (setting.getValue() == FINAL) {
          finalRef.set(true);
        }
        else if (setting.getValue() == OVERRIDE) {
          overrideRef.set(true);
        }
      }

      @Override
      public void visit(@NotNull ArrangementCompositeMatchCondition setting) {
        for (ArrangementMatchCondition n : setting.getOperands()) {
          n.invite(this);
        }
      }

      @Override
      public void visit(@NotNull ArrangementNameMatchCondition condition) {
      }
    });


    final ArrangementEntryType type = typeRef.get();
    if (type == null) {
      return true;
    }

    switch (type) {
      case STATIC_INIT:
        return false;

      case CONST:
        // const can also be static/not static, but there's no sense in non-static constants
        return modifier == PUBLIC || modifier == PROTECTED || modifier == PACKAGE_PRIVATE || modifier == PRIVATE;

      case VAR:
        return modifier == STATIC || modifier == PUBLIC || modifier == PROTECTED || modifier == PACKAGE_PRIVATE || modifier == PRIVATE;

      case CONSTRUCTOR:
        return false; // constructor can have visibility modifier, but there's no sense in selecting it 'cuz constructor is only one

      case METHOD:
      case PROPERTY:
      case EVENT_HANDLER:
        if (staticRef.get() && modifier == OVERRIDE) return false;
        if (overrideRef.get() && modifier == STATIC) return false;

        if (staticRef.get() && modifier == FINAL) return false;
        if (finalRef.get() && modifier == STATIC) return false;

        return true;

      default:
        LOG.error(type);
        return true;
    }
  }

  @Override
  public boolean isEnabled(@NotNull ArrangementGroupingType groupingType, @Nullable ArrangementEntryOrderType orderType) {
    return groupingType == ArrangementGroupingType.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER && orderType == null;
  }

  @NotNull
  @Override
  public Collection<Set<?>> getMutexes() {
    final Collection<Set<?>> result = new ArrayList<Set<?>>();

    result.add(SUPPORTED_TYPES);
    result.add(EnumSet.of(PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE));

    return result;
  }

  @NotNull
  @Override
  public List<Set<ArrangementMatchCondition>> getGroupingConditions() {
    return Collections.emptyList();
  }
}

