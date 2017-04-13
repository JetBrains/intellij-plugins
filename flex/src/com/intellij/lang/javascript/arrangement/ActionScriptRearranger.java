package com.intellij.lang.javascript.arrangement;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.arrangement.*;
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule;
import com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryMatcher;
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher;
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule;
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition;
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition;
import com.intellij.psi.codeStyle.arrangement.model.ArrangementMatchCondition;
import com.intellij.psi.codeStyle.arrangement.std.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ContainerUtilRt;
import com.intellij.util.containers.SortedList;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.General.*;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.KEEP;

public class ActionScriptRearranger implements Rearranger<ActionScriptArrangementEntry>, ArrangementStandardSettingsAware {

  private static final Logger LOG = Logger.getInstance(ActionScriptRearranger.class.getName());

  private static final Set<ArrangementSettingsToken> SUPPORTED_TYPES = ContainerUtilRt.newLinkedHashSet(
    CONSTRUCTOR, METHOD, STATIC_INIT, CONST, VAR, PROPERTY, EVENT_HANDLER
  );

  private static final Set<ArrangementSettingsToken> SUPPORTED_MODIFIERS = ContainerUtilRt.newLinkedHashSet(
    PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE, STATIC, FINAL, OVERRIDE
  );

  private static final StdArrangementSettings DEFAULT_SETTINGS;

  static {
    final List<ArrangementGroupingRule> groupingRules =
      Collections.singletonList(new ArrangementGroupingRule(GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER));
    final List<StdArrangementMatchRule> matchRules = getDefaultMatchRules();
    DEFAULT_SETTINGS = StdArrangementSettings.createByMatchRules(groupingRules, matchRules);
  }

  private static final DefaultArrangementSettingsSerializer SETTINGS_SERIALIZER = new DefaultArrangementSettingsSerializer(DEFAULT_SETTINGS);

  private static void addRule(final List<StdArrangementMatchRule> rules, @NotNull ArrangementSettingsToken... conditions) {
    if (conditions.length == 1) {
      rules.add(new StdArrangementMatchRule(
        new StdArrangementEntryMatcher(new ArrangementAtomMatchCondition(conditions[0]))));
      return;
    }

    ArrangementCompositeMatchCondition composite = new ArrangementCompositeMatchCondition();
    for (ArrangementSettingsToken condition : conditions) {
      composite.addOperand(new ArrangementAtomMatchCondition(condition));
    }
    rules.add(new StdArrangementMatchRule(new StdArrangementEntryMatcher(composite)));
  }

  @Nullable
  @Override
  public Pair<ActionScriptArrangementEntry, List<ActionScriptArrangementEntry>> parseWithNew(@NotNull PsiElement root,
                                                                                             @Nullable Document document,
                                                                                             @NotNull Collection<TextRange> ranges,
                                                                                             @NotNull PsiElement element,
                                                                                             @NotNull ArrangementSettings settings) {
    // todo implement and use in quick fixes, code generators, etc.
    return null;
  }

  @NotNull
  @Override
  public List<ActionScriptArrangementEntry> parse(final @NotNull PsiElement root,
                                                  final @Nullable Document document,
                                                  final @NotNull Collection<TextRange> ranges,
                                                  final @NotNull ArrangementSettings settings) {
    final PsiFile file = root.getContainingFile();
    if (!(file instanceof JSFile)) return Collections.emptyList();

    final JSClass jsClass = JSPsiImplUtils.findClass((JSFile)file);
    if (jsClass == null) return Collections.emptyList();

    final List<ActionScriptArrangementEntry> result = new SortedList<>(ActionScriptArrangementEntry.COMPARATOR);

    // static init blocks
    final JSBlockStatement[] blockStatements = PsiTreeUtil.getChildrenOfType(jsClass, JSBlockStatement.class);
    if (blockStatements != null) {
      for (JSBlockStatement blockStatement : blockStatements) {
        ContainerUtil.addIfNotNull(result, ActionScriptArrangementEntry.create(blockStatement, ranges, document));
      }
    }

    // vars and consts. Added to map only if there's only one var in JSVarStatement
    final Map<JSVariable, ActionScriptArrangementEntry> varToEntry = new THashMap<>();

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
    final Map<JSFunction, ActionScriptArrangementEntry> functionToEntry = new THashMap<>();

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
    if (groupPropertyFieldWithGetterSetter(settings)) {
      final JSCodeStyleSettings codeStyleSettings =
        CodeStyleSettingsManager.getSettings(jsClass.getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);

      for (Map.Entry<JSVariable, ActionScriptArrangementEntry> mapEntry : varToEntry.entrySet()) {
        final JSVariable jsVar = mapEntry.getKey();
        final ActionScriptArrangementEntry varEntry = mapEntry.getValue();

        if (StringUtil.startsWith(jsVar.getName(), codeStyleSettings.FIELD_PREFIX)) {
          final String propertyName = JSRefactoringUtil.transformVarNameToAccessorName(jsVar.getName(), jsClass.getProject());

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
      if (rule.getGroupingType() == GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER) {
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
    ArrangementSettingsToken type = target.getType();
    if (VAR.equals(type) || CONST.equals(type)) {
      return commonSettings.BLANK_LINES_AROUND_FIELD;
    }
    else if (STATIC_INIT.equals(type) || CONSTRUCTOR.equals(type) || METHOD.equals(type) || PROPERTY.equals(type)
             || EVENT_HANDLER.equals(type))
    {
      return commonSettings.BLANK_LINES_AROUND_METHOD;
    }
    else {
      LOG.error(target.getType());
      return 0;
    }
  }

  @NotNull
  @Override
  public ArrangementSettingsSerializer getSerializer() {
    return SETTINGS_SERIALIZER;
  }

  @Nullable
  @Override
  public StdArrangementSettings getDefaultSettings() {
    return DEFAULT_SETTINGS;
  }

  public static List<StdArrangementMatchRule> getDefaultMatchRules() {
    // more or less close to Coding Conventions at http://sourceforge.net/adobe/flexsdk/wiki/Coding%20Conventions/
    final List<StdArrangementMatchRule> matchRules = new ArrayList<>();

    final ArrangementSettingsToken[] visibility = {PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE};

    // static initialization blocks
    addRule(matchRules, STATIC_INIT);

    // constants
    addRule(matchRules, CONST);

    // mix-ins (static vars of Function type)

    // resources (what's this?)

    // static vars
    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, VAR, modifier, STATIC);
    }

    // static properties
    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, PROPERTY, modifier, STATIC);
    }

    // static methods
    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, METHOD, modifier, STATIC);
    }

    // constructor
    addRule(matchRules, CONSTRUCTOR);

    // vars
    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, VAR, modifier);
    }

    // properties
    addRule(matchRules, PROPERTY, OVERRIDE);

    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, PROPERTY, modifier);
    }

    // methods
    addRule(matchRules, METHOD, OVERRIDE);

    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, METHOD, modifier);
    }

    // event handlers
    addRule(matchRules, EVENT_HANDLER, OVERRIDE);

    for (ArrangementSettingsToken modifier : visibility) {
      addRule(matchRules, EVENT_HANDLER, modifier);
    }

    return matchRules;
  }

  @Override
  public boolean isEnabled(@NotNull ArrangementSettingsToken token, @Nullable ArrangementMatchCondition current) {
    if (SUPPORTED_TYPES.contains(token) || KEEP.equals(token) || BY_NAME.equals(token) || StdArrangementTokens.Regexp.NAME.equals(token)) {
      return true;
    }
    
    // Assuming that the token is a modifier then.
    if (current == null) {
      return SUPPORTED_MODIFIERS.contains(token);
    }

    ArrangementSettingsToken type = ArrangementUtil.parseType(current);
    if (type == null) {
      return true;
    }

    if (STATIC_INIT.equals(type)) {
      return false;
    }
    else if (CONST.equals(type)) {
      // const can also be static/not static, but there's no sense in non-static constants
      return PUBLIC.equals(token) || PROTECTED.equals(token) || PACKAGE_PRIVATE.equals(token) || PRIVATE.equals(token);
    }
    else if (VAR.equals(type)) {
      return STATIC.equals(token) || PUBLIC.equals(token) || PROTECTED.equals(token) || PACKAGE_PRIVATE.equals(token)
             || PRIVATE.equals(token);
    }
    else if (CONSTRUCTOR.equals(type)) {
      return false; // constructor can have visibility modifier, but there's no sense in selecting it 'cuz constructor is only one
    }
    else if (METHOD.equals(type) || PROPERTY.equals(type) || EVENT_HANDLER.equals(type)) {
      Map<ArrangementSettingsToken, Object> tokens = ArrangementUtil.extractTokens(current);
      if (OVERRIDE.equals(token) && tokens.keySet().contains(STATIC)) {
        return false;
      }
      else if (STATIC.equals(token) && (tokens.keySet().contains(OVERRIDE) || tokens.keySet().contains(FINAL))) {
        return false;
      }
      else if (FINAL.equals(token) && tokens.keySet().contains(STATIC)) {
        return false;
      }
      else {
        return true;
      }
    }
    else {
      LOG.error(type);
      return true;
    }
  }

  @NotNull
  @Override
  public Collection<Set<ArrangementSettingsToken>> getMutexes() {
    final Collection<Set<ArrangementSettingsToken>> result = ContainerUtilRt.newArrayList();

    result.add(SUPPORTED_TYPES);
    result.add(ContainerUtilRt.newHashSet(PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE));

    return result;
  }

  @Nullable
  @Override
  public List<CompositeArrangementSettingsToken> getSupportedGroupingTokens() {
    return Collections.singletonList(new CompositeArrangementSettingsToken(GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER));
  }

  @Nullable
  @Override
  public List<CompositeArrangementSettingsToken> getSupportedMatchingTokens() {
    return ContainerUtilRt.newArrayList(
      new CompositeArrangementSettingsToken(TYPE, SUPPORTED_TYPES),
      new CompositeArrangementSettingsToken(MODIFIER, SUPPORTED_MODIFIERS),
      new CompositeArrangementSettingsToken(StdArrangementTokens.Regexp.NAME),
      new CompositeArrangementSettingsToken(ORDER, KEEP, BY_NAME)
    );
  }

  @NotNull
  @Override
  public ArrangementEntryMatcher buildMatcher(@NotNull ArrangementMatchCondition condition) throws IllegalArgumentException {
    throw new IllegalArgumentException("Can't build a matcher for condition " + condition);
  }
}

