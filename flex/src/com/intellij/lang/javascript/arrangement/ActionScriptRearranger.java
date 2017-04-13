package com.intellij.lang.javascript.arrangement;

import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.generation.ActionScriptGenerateEventHandler;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.arrangement.ArrangementSettingsSerializer;
import com.intellij.psi.codeStyle.arrangement.ArrangementUtil;
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementSettingsSerializer;
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule;
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule;
import com.intellij.psi.codeStyle.arrangement.model.ArrangementMatchCondition;
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken;
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings;
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME;
import static com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.KEEP;

public class ActionScriptRearranger extends JSRearrangerBase {

  private static final Logger LOG = Logger.getInstance(ActionScriptRearranger.class.getName());

  private static final Set<ArrangementSettingsToken> SUPPORTED_TYPES = ContainerUtilRt.newLinkedHashSet(
    CONSTRUCTOR, METHOD, STATIC_INIT, CONST, VAR, PROPERTY, EVENT_HANDLER
  );

  private static final Set<ArrangementSettingsToken> SUPPORTED_MODIFIERS = ContainerUtilRt.newLinkedHashSet(
    PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE, STATIC, FINAL, OVERRIDE
  );

  private static final Set<ArrangementSettingsToken> VISIBILITY_MODIFIERS = ContainerUtilRt.newLinkedHashSet(
    PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE
  );

  private final StdArrangementSettings DEFAULT_SETTINGS;
  private final DefaultArrangementSettingsSerializer SETTINGS_SERIALIZER;

  public ActionScriptRearranger() {
    final List<ArrangementGroupingRule> groupingRules =
      Collections.singletonList(new ArrangementGroupingRule(GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER));
    final List<StdArrangementMatchRule> matchRules = getDefaultMatchRules();
    DEFAULT_SETTINGS = StdArrangementSettings.createByMatchRules(groupingRules, matchRules);
    SETTINGS_SERIALIZER = new DefaultArrangementSettingsSerializer(DEFAULT_SETTINGS);
  }

  @NotNull
  @Override
  protected ArrangementSettingsToken detectFieldType(JSVariable variable) {
    return variable != null && variable.isConst() ? CONST : VAR;
  }

  @NotNull
  @Override
  protected ArrangementSettingsToken detectFunctionType(@NotNull JSFunction function) {
    if (isEventHandler(function)) {
      return EVENT_HANDLER;
    }
    return super.detectFunctionType(function);
  }

  @NotNull
  @Override
  protected Set<ArrangementSettingsToken> getSupportedTypes() {
    return SUPPORTED_TYPES;
  }

  @NotNull
  @Override
  protected Set<ArrangementSettingsToken> getVisibilityModifiers() {
    return VISIBILITY_MODIFIERS;
  }

  @NotNull
  @Override
  protected Set<ArrangementSettingsToken> getSupportedModifiers() {
    return SUPPORTED_MODIFIERS;
  }

  @Nullable
  @Override
  public StdArrangementSettings getDefaultSettings() {
    return DEFAULT_SETTINGS;
  }

  @NotNull
  @Override
  public ArrangementSettingsSerializer getSerializer() {
    return SETTINGS_SERIALIZER;
  }

  @Override
  public int getBlankLines(@NotNull CodeStyleSettings settings,
                           @Nullable JSArrangementEntry parent,
                           @Nullable JSArrangementEntry previous,
                           @NotNull JSArrangementEntry target) {
    if (previous == null) {
      return -1;
    }

    final CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    ArrangementSettingsToken type = target.getType();
    if (VAR.equals(type) || CONST.equals(type)) {
      return commonSettings.BLANK_LINES_AROUND_FIELD;
    }
    else if (STATIC_INIT.equals(type) || CONSTRUCTOR.equals(type) || METHOD.equals(type) || PROPERTY.equals(type)
             || EVENT_HANDLER.equals(type)) {
      return commonSettings.BLANK_LINES_AROUND_METHOD;
    }
    else {
      LOG.error(target.getType());
      return 0;
    }
  }

  @NotNull
  protected Set<ArrangementSettingsToken> detectModifiers(@NotNull final JSAttributeListOwner fieldOrMethod) {
    final Set<ArrangementSettingsToken> result = ContainerUtil.newHashSet();

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
            result.add(PUBLIC);
            break;
          case PROTECTED:
            result.add(PROTECTED);
            break;
          case PACKAGE_LOCAL:
            result.add(PACKAGE_PRIVATE);
            break;
          case PRIVATE:
            result.add(PRIVATE);
            break;
        }
      }

      if (attributes.hasModifier(JSAttributeList.ModifierType.STATIC)) result.add(STATIC);
      if (attributes.hasModifier(JSAttributeList.ModifierType.FINAL)) result.add(FINAL);
      if (attributes.hasModifier(JSAttributeList.ModifierType.OVERRIDE)) result.add(OVERRIDE);
    }
    return result;
  }

  private static boolean isEventHandler(final JSFunction function) {
    final JSParameter[] parameters = function.getParameterVariables();

    if (parameters.length == 1) {
      final PsiElement typeElement = parameters[0].getTypeElement();
      if (typeElement instanceof JSReferenceExpression) {
        final PsiElement resolve = ((JSReferenceExpression)typeElement).resolve();
        if (resolve instanceof JSClass &&
            (FlexCommonTypeNames.FLASH_EVENT_FQN.equals(((JSClass)resolve).getQualifiedName()) ||
             FlexCommonTypeNames.STARLING_EVENT_FQN.equals(((JSClass)resolve).getQualifiedName()) ||
             ActionScriptGenerateEventHandler.isEventClass((JSClass)resolve))) {
          return true;
        }
      }
    }

    return false;
  }

  public static List<StdArrangementMatchRule> getDefaultMatchRules() {
    // more or less close to Coding Conventions at http://sourceforge.net/adobe/flexsdk/wiki/Coding%20Conventions/
    final List<StdArrangementMatchRule> matchRules = new ArrayList<>();

    final Set<ArrangementSettingsToken> visibility = VISIBILITY_MODIFIERS;

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
    if (getSupportedTypes().contains(token) ||
        KEEP.equals(token) ||
        BY_NAME.equals(token) ||
        StdArrangementTokens.Regexp.NAME.equals(token)) {
      return true;
    }
    
    // Assuming that the token is a modifier then.
    if (current == null) {
      return getSupportedModifiers().contains(token);
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
}

