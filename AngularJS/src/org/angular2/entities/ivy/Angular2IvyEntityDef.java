// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.*;
import com.intellij.lang.typescript.TypeScriptStubElementTypes;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.intellij.util.ObjectUtils.*;
import static org.angular2.Angular2DecoratorUtil.*;

@SuppressWarnings("unused")
public abstract class Angular2IvyEntityDef {

  @Nullable
  public static Angular2IvyEntityDef get(@NotNull TypeScriptClass typeScriptClass) {
    if (!isSuitableClass(typeScriptClass)) {
      return null;
    }
    StubElement<?> stub = doIfNotNull(tryCast(typeScriptClass, StubBasedPsiElementBase.class),
                                      StubBasedPsiElementBase::getStub);
    if (stub instanceof TypeScriptClassStub) {
      return get((TypeScriptClassStub)stub);
    }
    return findEntityDefFieldPsi(typeScriptClass);
  }

  @Nullable
  public static Angular2IvyEntityDef get(@NotNull TypeScriptClassStub stub) {
    return getEntityDefStubbed(stub);
  }

  @Nullable
  public static Angular2IvyEntityDef get(@NotNull TypeScriptField field) {
    JSAttributeList attrs = field.getAttributeList();
    if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
      return null;
    }
    TypeScriptClass tsClass = PsiTreeUtil.getContextOfType(field, TypeScriptClass.class);
    if (tsClass == null || !isSuitableClass(tsClass)) {
      return null;
    }
    return createEntityDef(field.getName(), field);
  }

  public static class Module extends Angular2IvyEntityDef {
    private Module(@NotNull Object fieldStubOrPsi) {super(fieldStubOrPsi);}

    @NotNull
    public List<TypeScriptTypeofType> getTypesList(@NotNull String property) {
      int index;
      if (property.equals(DECLARATIONS_PROP)) {
        index = 1;
      }
      else if (property.equals(IMPORTS_PROP)) {
        index = 2;
      }
      else if (property.equals(EXPORTS_PROP)) {
        index = 3;
      }
      else {
        return Collections.emptyList();
      }
      return processTupleArgument(index, TypeScriptTypeofType.class, Function.identity());
    }

    @Override
    public Angular2IvyModule createEntity() {
      return new Angular2IvyModule(this);
    }

    @NotNull
    @Override
    protected String getDefTypeName() {
      return TYPE_MODULE_DEF;
    }
  }

  public static class Directive extends Angular2IvyEntityDef {
    private Directive(@NotNull Object fieldStubOrPsi) {super(fieldStubOrPsi);}

    @Override
    public Angular2IvyDirective createEntity() {
      return new Angular2IvyDirective(this);
    }

    @Nullable
    public String getSelector() {
      return getStringGenericParam(1);
    }

    @Nullable
    public TypeScriptStringLiteralType getSelectorElement() {
      return tryCast(getDefFieldArgument(1), TypeScriptStringLiteralType.class);
    }

    @NotNull
    public List<String> getExportAsList() {
      return processTupleArgument(2, TypeScriptStringLiteralType.class,
                                  TypeScriptStringLiteralType::getInnerText);
    }

    @NotNull
    public Map<String, String> readPropertyMappings(@NotNull String kind) {
      int index;
      if (kind.equals(INPUTS_PROP)) {
        index = 3;
      }
      else if (kind.equals(OUTPUTS_PROP)) {
        index = 4;
      }
      else {
        return Collections.emptyMap();
      }
      return processObjectArgument(index, TypeScriptStringLiteralType.class,
                                   TypeScriptStringLiteralType::getInnerText);
    }

    @NotNull
    @Override
    protected String getDefTypeName() {
      return TYPE_DIRECTIVE_DEF;
    }
  }

  public static class Component extends Directive {
    private Component(@NotNull Object fieldStubOrPsi) {super(fieldStubOrPsi);}

    @Override
    public Angular2IvyDirective createEntity() {
      return new Angular2IvyComponent(this);
    }

    @NotNull
    @Override
    protected String getDefTypeName() {
      return TYPE_COMPONENT_DEF;
    }
  }

  public static class Pipe extends Angular2IvyEntityDef {

    private Pipe(@NotNull Object fieldStubOrPsi) {super(fieldStubOrPsi);}

    @Override
    public Angular2IvyPipe createEntity() {
      return new Angular2IvyPipe(this);
    }

    @Nullable
    public String getName() {
      return getStringGenericParam(1);
    }
    @NotNull
    @Override
    protected String getDefTypeName() {
      return TYPE_PIPE_DEF;
    }
  }

  @NonNls private static final String FIELD_DIRECTIVE_DEF = "ɵdir";
  @NonNls private static final String FIELD_MODULE_DEF = "ɵmod";
  @NonNls private static final String FIELD_PIPE_DEF = "ɵpipe";
  @NonNls private static final String FIELD_COMPONENT_DEF = "ɵcmp";

  @NonNls private static final String TYPE_DIRECTIVE_DEF = "ɵɵDirectiveDefWithMeta";
  @NonNls private static final String TYPE_MODULE_DEF = "ɵɵNgModuleDefWithMeta";
  @NonNls private static final String TYPE_PIPE_DEF = "ɵɵPipeDefWithMeta";
  @NonNls private static final String TYPE_COMPONENT_DEF = "ɵɵComponentDefWithMeta";

  private final Object myFieldOrStub;

  private Angular2IvyEntityDef(@NotNull Object fieldOrStub) {
    this.myFieldOrStub = fieldOrStub;
  }

  @NotNull
  public TypeScriptField getField() {
    if (myFieldOrStub instanceof TypeScriptFieldStub) {
      return (TypeScriptField)((TypeScriptFieldStub)myFieldOrStub).getPsi();
    }
    return (TypeScriptField)myFieldOrStub;
  }

  public abstract Angular2IvyEntity<?> createEntity();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2IvyEntityDef entityDef = (Angular2IvyEntityDef)o;
    return getField().equals(entityDef.getField());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getField());
  }

  @NotNull
  protected abstract String getDefTypeName();

  @Nullable
  protected JSTypeDeclaration getDefFieldArgument(int index) {
    StubElement<?> stub = myFieldOrStub instanceof TypeScriptFieldStub
                          ? (TypeScriptFieldStub)myFieldOrStub
                          : doIfNotNull(tryCast(myFieldOrStub, StubBasedPsiElementBase.class),
                                        StubBasedPsiElementBase::getStub);
    if (stub != null) {
      return getDefFieldArgumentStubbed((TypeScriptFieldStub)stub, index, getDefTypeName());
    }
    return getDefFieldArgumentPsi((TypeScriptField)myFieldOrStub, index, getDefTypeName());
  }

  @Nullable
  protected String getStringGenericParam(int index) {
    JSTypeDeclaration declaration = getDefFieldArgument(index);
    if (declaration instanceof TypeScriptStringLiteralType) {
      return ((TypeScriptStringLiteralType)declaration).getInnerText();
    }
    return null;
  }

  @NotNull
  protected <T extends TypeScriptType, R> List<R> processTupleArgument(int index,
                                                                       @NotNull Class<T> itemsClass,
                                                                       @NotNull Function<T, R> itemMapper) {
    TypeScriptTupleType tuple = tryCast(getDefFieldArgument(index), TypeScriptTupleType.class);
    if (tuple == null) {
      return Collections.emptyList();
    }
    return StreamEx.of(tuple.getElements())
      .select(itemsClass)
      .map(itemMapper)
      .toList();
  }

  @NotNull
  protected <T extends TypeScriptType, R> Map<String, R> processObjectArgument(int index,
                                                                               @NotNull Class<T> valueClass,
                                                                               @NotNull Function<T, R> valueMapper) {
    TypeScriptObjectType object = tryCast(getDefFieldArgument(index), TypeScriptObjectType.class);
    if (object == null) {
      return Collections.emptyMap();
    }
    Map<String, R> result = new LinkedHashMap<>();
    for (TypeScriptTypeMember child : object.getTypeMembers()) {
      TypeScriptPropertySignature prop = tryCast(child, TypeScriptPropertySignature.class);
      if (prop != null) {
        Optional.ofNullable(tryCast(prop.getTypeDeclaration(), valueClass))
          .map(valueMapper)
          .ifPresent(value -> result.put(prop.getName(), value));
      }
    }
    return result;
  }

  @Contract("null->false") //NON-NLS
  private static boolean isSuitableClass(@NotNull TypeScriptClass tsClass) {
    return !Objects.requireNonNull(tsClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.ABSTRACT);
  }

  @Nullable
  private static Angular2IvyEntityDef getEntityDefStubbed(@NotNull TypeScriptClassStub jsClassStub) {
    JSAttributeListStub clsAttrs = jsClassStub.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST);
    // Do not index abstract classes
    if (clsAttrs == null || clsAttrs.hasModifier(JSAttributeList.ModifierType.ABSTRACT)) {
      return null;
    }
    for (StubElement<?> classChild : jsClassStub.getChildrenStubs()) {
      if (!(classChild instanceof JSVarStatementStub)) {
        continue;
      }
      JSAttributeListStub attrs = classChild.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST);
      if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        continue;
      }
      JSVariableStub<?> fieldStub = classChild.findChildStubByType(TypeScriptStubElementTypes.TYPESCRIPT_FIELD);
      if (!(fieldStub instanceof TypeScriptFieldStub)) {
        continue;
      }
      Angular2IvyEntityDef entityDefKind = createEntityDef(fieldStub.getName(), fieldStub);
      if (entityDefKind != null) {
        return entityDefKind;
      }
    }
    return null;
  }

  @Nullable
  private static Angular2IvyEntityDef findEntityDefFieldPsi(@NotNull TypeScriptClass jsClass) {
    for (JSField field : jsClass.getFields()) {
      if (!(field instanceof TypeScriptField)) {
        continue;
      }
      Angular2IvyEntityDef entityDefKind = get((TypeScriptField)field);
      if (entityDefKind != null) {
        return entityDefKind;
      }
    }
    return null;
  }

  @Nullable
  private static Angular2IvyEntityDef createEntityDef(@Nullable String fieldName, @NotNull Object fieldPsiOrStub) {
    if (fieldName == null) {
      return null;
    }
    if (fieldName.equals(FIELD_COMPONENT_DEF)) {
      return new Component(fieldPsiOrStub);
    }
    else if (fieldName.equals(FIELD_DIRECTIVE_DEF)) {
      return new Directive(fieldPsiOrStub);
    }
    else if (fieldName.equals(FIELD_MODULE_DEF)) {
      return new Module(fieldPsiOrStub);
    }
    else if (fieldName.equals(FIELD_PIPE_DEF)) {
      return new Pipe(fieldPsiOrStub);
    }
    return null;
  }

  @Nullable
  private static JSTypeDeclaration getDefFieldArgumentStubbed(@NotNull TypeScriptFieldStub field,
                                                              int index,
                                                              @NotNull String typeName) {
    TypeScriptSingleTypeStub type = field.findChildStubByType(TypeScriptStubElementTypes.SINGLE_TYPE);
    if (type != null) {
      if (type.getQualifiedTypeName().endsWith(typeName)) {
        TypeScriptTypeArgumentListStub typeArguments = type.findChildStubByType(TypeScriptStubElementTypes.TYPE_ARGUMENT_LIST);
        if (typeArguments != null) {
          @SuppressWarnings("rawtypes")
          List<StubElement> declarations = typeArguments.getChildrenStubs();
          if (index < declarations.size()) {
            return tryCast(declarations.get(index).getPsi(), JSTypeDeclaration.class);
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private static JSTypeDeclaration getDefFieldArgumentPsi(@NotNull TypeScriptField field, int index, @NotNull String typeName) {
    TypeScriptSingleType type = PsiTreeUtil.getChildOfType(field, TypeScriptSingleType.class);
    if (type != null && notNull(type.getQualifiedTypeName(), "").endsWith(typeName)) {
      JSTypeDeclaration[] declarations = type.getTypeArguments();
      if (index < declarations.length) {
        return declarations[index];
      }
    }
    return null;
  }
}
