// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.model.Pointer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.JBIterable;
import org.angular2.entities.*;
import org.angular2.entities.ivy.Angular2IvyDirective;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveProperty;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.web.Angular2Symbol;
import org.angular2.web.Angular2SymbolDelegate;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

/**
 * This class is intended to be a single point of origin for any hack to support a badly written library.
 */
public final class Angular2LibrariesHacks {

  @NonNls private static final String IONIC_ANGULAR_PACKAGE = "@ionic/angular";
  @NonNls private static final String NG_MODEL_CHANGE = "ngModelChange";
  @NonNls private static final String NG_FOR_OF = "ngForOf";
  @NonNls private static final String NG_ITERABLE = "NgIterable";
  @NonNls private static final String QUERY_LIST = "QueryList";

  /**
   * Hack for WEB-37879
   */
  public static @Nullable JSType hackNgModelChangeType(@Nullable JSType type, @NotNull String propertyName) {
    // Workaround issue with ngModelChange field.
    // The workaround won't execute once Angular source is corrected.
    if (propertyName.equals(NG_MODEL_CHANGE)
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
            && type.getTypeText().startsWith(Angular2LangUtil.EVENT_EMITTER)) {
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
  public static @NotNull List<? extends Angular2Symbol> hackIonicComponentAttributeNames(@NotNull Angular2Directive directive) {
    if (!isIonicDirective(directive)) {
      return Collections.emptyList();
    }
    // Add kebab case version of attribute - Ionic takes these directly from element bypassing Angular
    return JBIterable.from(directive.getInputs())
      .map(input -> new IonicComponentAttribute(input))
      .toList();
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

  private static class IonicComponentAttribute extends Angular2SymbolDelegate<Angular2DirectiveProperty> {

    private final String name;

    private IonicComponentAttribute(@NotNull Angular2DirectiveProperty input) {
      super(input);
      name = input.getName().replaceAll("([A-Z])", "-$1").toLowerCase(Locale.ENGLISH);
    }

    @NotNull
    @Override
    public String getNamespace() {
      return NAMESPACE_HTML;
    }

    @NotNull
    @Override
    public String getKind() {
      return KIND_HTML_ATTRIBUTES;
    }

    @NotNull
    @Override
    public String getName() {
      return name;
    }

    @NotNull
    @Override
    public Pointer<IonicComponentAttribute> createPointer() {
      var input = this.getDelegate().createPointer();
      return () -> {
        var newInput = input.dereference();
        return newInput != null ? new IonicComponentAttribute(newInput) : null;
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      IonicComponentAttribute attr = (IonicComponentAttribute)o;
      return getDelegate().equals(attr.getDelegate());
    }

    @Override
    public int hashCode() {
      return getDelegate().hashCode();
    }
  }

}
