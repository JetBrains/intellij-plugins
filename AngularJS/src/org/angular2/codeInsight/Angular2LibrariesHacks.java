// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyCallElement;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyContextElement;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ArrayUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.ivy.Angular2IvyDirective;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveProperty;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

/**
 * This class is intended to be a single point of origin for any hack to support a badly written library.
 */
public class Angular2LibrariesHacks {

  @NonNls private static final String IONIC_ANGULAR_PACKAGE = "@ionic/angular";
  @NonNls private static final String NG_MODEL_CHANGE = "ngModelChange";
  @NonNls private static final String EVENT_EMITTER = "EventEmitter";
  @NonNls private static final String SLICE_PIPE_NAME = "slice";
  @NonNls private static final String NG_FOR_OF = "ngForOf";
  @NonNls private static final String NG_ITERABLE = "NgIterable";
  @NonNls private static final String QUERY_LIST = "QueryList";

  /**
   * Hack for WEB-37879
   */
  public static @Nullable JSType hackNgModelChangeType(@Nullable JSType type, @NotNull String propertyName) {
    if (type != null
        // Workaround issue with ngModelChange field.
        // The workaround won't execute once Angular source is corrected.
        && propertyName.equals(NG_MODEL_CHANGE)
        && type instanceof JSRecordType
        && !((JSRecordType)type).hasProperties()) {
      return JSAnyType.get(type.getSource());
    }
    return type;
  }

  /**
   * Hack for WEB-37838
   */
  public static void hackIonicComponentOutputs(@NotNull Angular2Directive directive, @NotNull Map<String, String> outputs) {
    if (!isIonicDirective(directive)) {
      return;
    }
    TypeScriptClass cls = directive.getTypeScriptClass();
    if (cls == null) {
      return;
    }
    // We can guess outputs by looking for fields with EventEmitter type
    cls.getJSType().asRecordType().getProperties().forEach(prop -> {
      try {
        JSType type;
        if (prop instanceof TypeScriptField
            && (type = prop.getJSType()) != null
            && type.getTypeText().startsWith(EVENT_EMITTER)) {
          outputs.put(prop.getMemberName(), prop.getMemberName());
        }
      }
      catch (IllegalArgumentException ex) {
        //getTypeText may throw IllegalArgumentException - ignore it
      }
    });
  }

  /**
   * Hack for WEB-39722
   */
  public static @Nullable Function<Angular2DirectiveProperty, Angular2AttributeDescriptor> hackIonicComponentAttributeNames(
    @NotNull Angular2Directive directive,
    BiFunction<? super Angular2DirectiveProperty, ? super String, Angular2AttributeDescriptor> oneTimeBindingCreator) {
    if (!isIonicDirective(directive)) {
      return null;
    }
    // Add kebab case version of attribute - Ionic takes these directly from element bypassing Angular
    return (@NonNls Angular2DirectiveProperty property) -> oneTimeBindingCreator.apply(
      property, property.getName().replaceAll("([A-Z])", "-$1").toLowerCase(Locale.ENGLISH));
  }

  private static boolean isIonicDirective(Angular2Directive directive) {
    if (directive instanceof Angular2IvyDirective) {
      return directive.getName().startsWith("Ion") //NON-NLS
             && Optional.ofNullable(directive.getTypeScriptClass())
               .map(PsiUtilCore::getVirtualFile)
               .map(vf -> PackageJsonUtil.findUpPackageJson(vf))
               .map(NodeModuleUtil::inferNodeModulePackageName)
               .map(name -> name.equals(IONIC_ANGULAR_PACKAGE))
               .orElse(false);
    }
    return Optional.ofNullable(tryCast(directive, Angular2MetadataDirectiveBase.class))
      .map(Angular2MetadataDirectiveBase::getNodeModule)
      .map(Angular2MetadataNodeModule::getName)
      .map(name -> IONIC_ANGULAR_PACKAGE.equals(name))
      .orElse(Boolean.FALSE);
  }

  /**
   * Hack for WEB-38153. The slice pipe accepts only Strings, [] and Arrays,
   * but this hack simply mirrors the type of the argument and will not allow
   * to detect any incorrect typing.
   */
  public static boolean hackSlicePipeType(Angular2TypeEvaluator evaluator,
                                          JSEvaluateContext context,
                                          JSFunction function) {
    Angular2PipeExpression pipeExpression = Optional.ofNullable(context.getJSElementsToApply().peekFirst())
      .map(e -> tryCast(e, JSApplyCallElement.class))
      .map(JSApplyCallElement::getMethodExpression)
      .map(e -> tryCast(e, Angular2PipeReferenceExpression.class))
      // narrow down pipe call to `slice`
      .filter(pipe -> SLICE_PIPE_NAME.equals(pipe.getReferenceName()))
      .map(PsiElement::getParent)
      .map(e -> tryCast(e, Angular2PipeExpression.class))
      .orElse(null);
    if (pipeExpression != null
        // Once the slice pipe is fixed this hack won't execute
        && function.getReturnType() instanceof JSAnyType
        && SLICE_PIPE_NAME.equals(doIfNotNull(Angular2EntitiesProvider.getPipe(function), Angular2Entity::getName))) {
      JSExpression param = ArrayUtil.getFirstElement(pipeExpression.getArguments());
      if (param != null) {
        JSApplyContextElement callContext = context.popJSElementToApply();
        try {
          evaluator.addType(JSResolveUtil.getExpressionJSType(param), function);
        }
        finally {
          context.pushJSElementToApply(callContext);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Hack for WEB-38825. Make ngForOf accept QueryList in addition to NgIterable
   */
  public static @Nullable JSType hackQueryListTypeInNgForOf(@Nullable JSType type,
                                                            @NotNull Angular2MetadataDirectiveProperty property) {
    TypeScriptClass clazz;
    JSType queryListType;
    if (type instanceof JSGenericTypeImpl
        && property.getName().equals(NG_FOR_OF)
        && (clazz = PsiTreeUtil.getContextOfType(property.getSourceElement(), TypeScriptClass.class)) != null
        && type.getTypeText().contains(NG_ITERABLE)
        && (queryListType = getQueryListType(clazz)) != null) {
      return JSCompositeTypeFactory.createUnionType(type.getSource(), type,
                                                    new JSGenericTypeImpl(type.getSource(), queryListType,
                                                                          ((JSGenericTypeImpl)type).getArguments()));
    }
    return type;
  }

  private static @Nullable JSType getQueryListType(@NotNull PsiElement scope) {
    return doIfNotNull(getCachedValue(scope, () -> {
      for (PsiElement module : JSFileReferencesUtil.resolveModuleReference(scope, ANGULAR_CORE_PACKAGE)) {
        if (!(module instanceof JSElement)) continue;
        TypeScriptClass queryListClass = tryCast(
          JSResolveResult.resolve(
            ES6PsiUtil.resolveSymbolInModule(QUERY_LIST, scope, (JSElement)module)),
          TypeScriptClass.class);
        if (queryListClass != null
            && queryListClass.getTypeParameters().length == 1) {
          return create(queryListClass, queryListClass, scope);
        }
      }
      return create(null, PsiModificationTracker.MODIFICATION_COUNT);
    }), clazz -> clazz.getJSType());
  }
}
