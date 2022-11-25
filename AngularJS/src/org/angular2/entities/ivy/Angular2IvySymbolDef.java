// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSParameterListElement;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.*;
import com.intellij.lang.typescript.TypeScriptStubElementTypes;
import com.intellij.model.Pointer;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.*;

@SuppressWarnings({"SameParameterValue", "NonAsciiCharacters"})
public abstract class Angular2IvySymbolDef {

  public static @Nullable Entity get(@NotNull TypeScriptClass typeScriptClass, boolean allowAbstractClass) {
    return getSymbolDef(typeScriptClass, allowAbstractClass, Angular2IvySymbolDef::createEntityDef);
  }

  public static @Nullable Factory getFactory(@NotNull TypeScriptClass typeScriptClass) {
    return getSymbolDef(typeScriptClass, true, Angular2IvySymbolDef::createFactoryDef);
  }


  public static @Nullable Entity get(@NotNull TypeScriptClassStub stub, boolean allowAbstractClass) {
    return getSymbolDefStubbed(stub, allowAbstractClass, Angular2IvySymbolDef::createEntityDef);
  }

  public static @Nullable Entity get(@NotNull TypeScriptField field, boolean allowAbstractClass) {
    return getSymbolDef(field, allowAbstractClass, Angular2IvySymbolDef::createEntityDef);
  }

  public abstract static class Entity extends Angular2IvySymbolDef {

    private Entity(@NotNull Object fieldOrStub) {
      super(fieldOrStub);
    }

    public abstract Angular2IvyEntity<?> createEntity();

    public abstract boolean isStandalone();
  }

  public static final class Module extends Entity {
    private Module(@NotNull Object fieldStubOrPsi) { super(fieldStubOrPsi); }

    public @NotNull List<TypeScriptTypeofType> getTypesList(@NotNull String property) {
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
      return processTupleArgument(index, TypeScriptTypeofType.class, Function.identity(), false);
    }

    @Override
    public Angular2IvyModule createEntity() {
      return new Angular2IvyModule(this);
    }

    @Override
    public boolean isStandalone() {
      return false;
    }

    @Override
    protected @NotNull List<String> getDefTypeNames() {
      return TYPE_MODULE_DEFS;
    }
  }

  public static class Directive extends Entity {
    private Directive(@NotNull Object fieldStubOrPsi) { super(fieldStubOrPsi); }

    public Pointer<? extends Directive> createPointer() {
      var fieldPtr = createSmartPointer(getField());
      return () -> {
        var field = fieldPtr.dereference();
        return field != null ? new Directive(field) : null;
      };
    }

    @Override
    public Angular2IvyDirective createEntity() {
      return new Angular2IvyDirective(this);
    }

    @Override
    public boolean isStandalone() {
      var type = getDefFieldArgument(7);
      return type instanceof TypeScriptBooleanLiteralType && ((TypeScriptBooleanLiteralType)type).getValue();
    }

    public @Nullable String getSelector() {
      return getStringGenericParam(1);
    }

    public @Nullable TypeScriptStringLiteralType getSelectorElement() {
      return tryCast(getDefFieldArgument(1), TypeScriptStringLiteralType.class);
    }

    public @NotNull List<String> getExportAsList() {
      return processTupleArgument(2, TypeScriptStringLiteralType.class,
                                  TypeScriptStringLiteralType::getInnerText, false);
    }

    public @NotNull Map<String, String> readPropertyMappings(@NotNull String kind) {
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
                                   TypeScriptStringLiteralType::getInnerText
      );
    }

    @Override
    protected @NotNull List<String> getDefTypeNames() {
      return TYPE_DIRECTIVE_DEFS;
    }
  }

  public static final class Component extends Directive {

    private Component(@NotNull Object fieldStubOrPsi) { super(fieldStubOrPsi); }

    @Override
    public Pointer<Component> createPointer() {
      var fieldPtr = createSmartPointer(getField());
      return () -> {
        var field = fieldPtr.dereference();
        return field != null ? new Component(field) : null;
      };
    }

    @Override
    public Angular2IvyDirective createEntity() {
      return new Angular2IvyComponent(this);
    }

    /**
     * Returns null if the type doesn't contain the argument and logic should fall back to metadata.json
     */
    public @Nullable Collection<TypeScriptStringLiteralType> getNgContentSelectors() {
      return processTupleArgument(6, TypeScriptStringLiteralType.class,
                                  Function.identity(), true);
    }

    @Override
    protected @NotNull List<String> getDefTypeNames() {
      return TYPE_COMPONENT_DEFS;
    }
  }

  public static final class Pipe extends Entity {

    private Pipe(@NotNull Object fieldStubOrPsi) { super(fieldStubOrPsi); }

    @Override
    public Angular2IvyPipe createEntity() {
      return new Angular2IvyPipe(this);
    }

    @Override
    public boolean isStandalone() {
      var type = getDefFieldArgument(2);
      return type instanceof TypeScriptBooleanLiteralType && ((TypeScriptBooleanLiteralType)type).getValue();
    }

    public @Nullable String getName() {
      return getStringGenericParam(1);
    }

    @Override
    protected @NotNull List<String> getDefTypeNames() {
      return TYPE_PIPE_DEFS;
    }
  }

  public static final class Factory extends Angular2IvySymbolDef {

    private Factory(@NotNull Object fieldStubOrPsi) { super(fieldStubOrPsi); }

    @Override
    protected @NotNull List<String> getDefTypeNames() {
      return TYPE_FACTORY_DEFS;
    }

    /**
     * Returns null if the type doesn't contain the argument and logic should fall back to metadata.json
     */
    public @Nullable Map<String, JSTypeDeclaration> getAttributeNames() {
      Map<String, JSTypeDeclaration> result = new HashMap<>();
      if (!processConstructorArguments("attribute", TypeScriptStringLiteralType.class, (name, type) -> {
        doIfNotNull(name.getInnerText(), value -> result.put(value, type));
      })) {
        return null;
      }
      return result;
    }

    private <T extends TypeScriptType> boolean processConstructorArguments(String kind, Class<T> valueClass,
                                                                           BiConsumer<@NotNull T, @Nullable TypeScriptType> consumer) {
      JSTypeDeclaration declaration = getDefFieldArgument(1);
      if (declaration == null) {
        return false;
      }
      TypeScriptClass cls = getContextClass();
      if (!(declaration instanceof TypeScriptTupleType) || cls == null) {
        return true;
      }

      TypeScriptFunction constructor = ContainerUtil.find(cls.getConstructors(), fun -> !fun.isOverloadImplementation());
      if (constructor == null) {
        // TODO support annotations in super constructors
        return true;
      }

      JSParameterListElement[] params = constructor.getParameters();
      JSTypeDeclaration[] paramsDecoratorsInfo = ((TypeScriptTupleType)declaration).getElements();
      if (params.length != paramsDecoratorsInfo.length) {
        return true;
      }
      for (int i = 0; i < params.length; i++) {
        TypeScriptObjectType info = tryCast(paramsDecoratorsInfo[i], TypeScriptObjectType.class);
        if (info != null) {
          TypeScriptPropertySignature kindInfo = tryCast(ContainerUtil.find(info.getTypeMembers(), member -> kind.equals(member.getName())),
                                                         TypeScriptPropertySignature.class);
          if (kindInfo == null) {
            continue;
          }
          T value = tryCast(kindInfo.getTypeDeclaration(), valueClass);
          if (value != null) {
            consumer.accept(value, tryCast(params[i].getTypeElement(), TypeScriptType.class));
          }
        }
      }
      return true;
    }
  }

  @NonNls private static final String FIELD_DIRECTIVE_DEF = "ɵdir";
  @NonNls private static final String FIELD_MODULE_DEF = "ɵmod";
  @NonNls private static final String FIELD_PIPE_DEF = "ɵpipe";
  @NonNls private static final String FIELD_COMPONENT_DEF = "ɵcmp";
  @NonNls private static final String FIELD_FACTORY_DEF = "ɵfac";

  /* NG 9-11: *Def(WithMeta), NG 12+: *Declaration */
  @NonNls private static final List<String> TYPE_DIRECTIVE_DEFS = List.of("ɵɵDirectiveDefWithMeta", "ɵɵDirectiveDeclaration");
  @NonNls private static final List<String> TYPE_MODULE_DEFS = List.of("ɵɵNgModuleDefWithMeta", "ɵɵNgModuleDeclaration");
  @NonNls private static final List<String> TYPE_PIPE_DEFS = List.of("ɵɵPipeDefWithMeta", "ɵɵPipeDeclaration");
  @NonNls private static final List<String> TYPE_COMPONENT_DEFS = List.of("ɵɵComponentDefWithMeta", "ɵɵComponentDeclaration");
  @NonNls private static final List<String> TYPE_FACTORY_DEFS = List.of("ɵɵFactoryDef", "ɵɵFactoryDeclaration");

  private final Object myFieldOrStub;

  private Angular2IvySymbolDef(@NotNull Object fieldOrStub) {
    this.myFieldOrStub = fieldOrStub;
  }

  public @NotNull TypeScriptField getField() {
    if (myFieldOrStub instanceof TypeScriptFieldStub) {
      return (TypeScriptField)((TypeScriptFieldStub)myFieldOrStub).getPsi();
    }
    return (TypeScriptField)myFieldOrStub;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2IvySymbolDef entityDef = (Angular2IvySymbolDef)o;
    return getField().equals(entityDef.getField());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getField());
  }

  protected abstract @NotNull List<String> getDefTypeNames();

  protected @Nullable TypeScriptClass getContextClass() {
    return PsiTreeUtil.getContextOfType(getField(), TypeScriptClass.class);
  }

  protected @Nullable JSTypeDeclaration getDefFieldArgument(int index) {
    StubElement<?> stub = myFieldOrStub instanceof TypeScriptFieldStub
                          ? (TypeScriptFieldStub)myFieldOrStub
                          : doIfNotNull(tryCast(myFieldOrStub, StubBasedPsiElementBase.class),
                                        StubBasedPsiElementBase::getStub);
    if (stub != null) {
      return getDefFieldArgumentStubbed((TypeScriptFieldStub)stub, index, getDefTypeNames());
    }
    return getDefFieldArgumentPsi((TypeScriptField)myFieldOrStub, index, getDefTypeNames());
  }

  protected @Nullable String getStringGenericParam(int index) {
    JSTypeDeclaration declaration = getDefFieldArgument(index);
    if (declaration instanceof TypeScriptStringLiteralType) {
      return ((TypeScriptStringLiteralType)declaration).getInnerText();
    }
    return null;
  }

  @Contract("_,_,_,false->!null")
  protected @Nullable <T extends TypeScriptType, R> List<R> processTupleArgument(int index,
                                                                                 @NotNull Class<T> itemsClass,
                                                                                 @NotNull Function<T, R> itemMapper,
                                                                                 boolean nullIfNotFound) {
    JSTypeDeclaration declaration = getDefFieldArgument(index);
    if (declaration == null) {
      return nullIfNotFound ? null : Collections.emptyList();
    }
    if (!(declaration instanceof TypeScriptTupleType)) {
      return Collections.emptyList();
    }
    return StreamEx.of(((TypeScriptTupleType)declaration).getElements())
      .select(itemsClass)
      .map(itemMapper)
      .toList();
  }

  protected @NotNull <T extends JSTypeDeclaration, R> Map<String, R> processObjectArgument(int index,
                                                                                           @NotNull Class<T> valueClass,
                                                                                           @NotNull Function<T, R> valueMapper) {
    JSTypeDeclaration object = getDefFieldArgument(index);
    if (!(object instanceof TypeScriptObjectType)) {
      return Collections.emptyMap();
    }
    Map<String, R> result = new LinkedHashMap<>();
    for (TypeScriptTypeMember child : ((TypeScriptObjectType)object).getTypeMembers()) {
      TypeScriptPropertySignature prop = tryCast(child, TypeScriptPropertySignature.class);
      if (prop != null) {
        Optional.ofNullable(tryCast(prop.getTypeDeclaration(), valueClass))
          .map(valueMapper)
          .ifPresent(value -> result.put(prop.getName(), value));
      }
    }
    return result;
  }

  private static boolean isAbstractClass(@NotNull TypeScriptClass tsClass) {
    return Objects.requireNonNull(tsClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.ABSTRACT);
  }

  private static @Nullable <T extends Angular2IvySymbolDef> T getSymbolDefStubbed(@NotNull TypeScriptClassStub jsClassStub,
                                                                                  boolean allowAbstractClasses,
                                                                                  BiFunction<String, Object, T> symbolFactory) {
    JSAttributeListStub clsAttrs = jsClassStub.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST);
    if (clsAttrs == null || (!allowAbstractClasses && clsAttrs.hasModifier(JSAttributeList.ModifierType.ABSTRACT))) {
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
      T entityDefKind = symbolFactory.apply(fieldStub.getName(), fieldStub);
      if (entityDefKind != null) {
        return entityDefKind;
      }
    }
    return null;
  }

  private static @Nullable <T extends Angular2IvySymbolDef> T findSymbolDefFieldPsi(@NotNull TypeScriptClass jsClass,
                                                                                    boolean allowAbstractClass,
                                                                                    BiFunction<String, Object, T> symbolFactory) {
    for (JSField field : jsClass.getFields()) {
      if (!(field instanceof TypeScriptField)) {
        continue;
      }
      T entityDefKind = getSymbolDef((TypeScriptField)field, allowAbstractClass, symbolFactory);
      if (entityDefKind != null) {
        return entityDefKind;
      }
    }
    return null;
  }

  private static @Nullable <T extends Angular2IvySymbolDef> T getSymbolDef(@NotNull TypeScriptClass typeScriptClass,
                                                                           boolean allowAbstractClass,
                                                                           BiFunction<String, Object, T> symbolFactory) {
    if (!allowAbstractClass && isAbstractClass(typeScriptClass)) {
      return null;
    }
    StubElement<?> stub = doIfNotNull(tryCast(typeScriptClass, StubBasedPsiElementBase.class),
                                      StubBasedPsiElementBase::getStub);
    if (stub instanceof TypeScriptClassStub) {
      return getSymbolDefStubbed((TypeScriptClassStub)stub, allowAbstractClass, symbolFactory);
    }
    return findSymbolDefFieldPsi(typeScriptClass, allowAbstractClass, symbolFactory);
  }

  private static @Nullable <T extends Angular2IvySymbolDef> T getSymbolDef(@NotNull TypeScriptField field,
                                                                           boolean allowAbstractClass,
                                                                           BiFunction<String, Object, T> symbolFactory) {
    JSAttributeList attrs = field.getAttributeList();
    if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
      return null;
    }
    TypeScriptClass tsClass = PsiTreeUtil.getContextOfType(field, TypeScriptClass.class);
    if (tsClass == null || (!allowAbstractClass && isAbstractClass(tsClass))) {
      return null;
    }
    return symbolFactory.apply(field.getName(), field);
  }

  private static @Nullable Angular2IvySymbolDef.Entity createEntityDef(@Nullable String fieldName, @NotNull Object fieldPsiOrStub) {
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

  private static @Nullable Factory createFactoryDef(@Nullable String fieldName, @NotNull Object fieldPsiOrStub) {
    return fieldName != null && fieldName.equals(FIELD_FACTORY_DEF) ? new Factory(fieldPsiOrStub) : null;
  }

  private static @Nullable JSTypeDeclaration getDefFieldArgumentStubbed(@NotNull TypeScriptFieldStub field,
                                                                        int index,
                                                                        @NotNull List<String> typeNames) {
    TypeScriptSingleTypeStub type = field.findChildStubByType(TypeScriptStubElementTypes.SINGLE_TYPE);
    String qualifiedName = doIfNotNull(type, TypeScriptSingleTypeStub::getQualifiedTypeName);
    if (qualifiedName == null) return null;
    if (ContainerUtil.find(typeNames, name -> qualifiedName.endsWith(name)) != null) {
      TypeScriptTypeArgumentListStub typeArguments = type.findChildStubByType(TypeScriptStubElementTypes.TYPE_ARGUMENT_LIST);
      if (typeArguments != null) {
        @SuppressWarnings("rawtypes")
        List<StubElement> declarations = typeArguments.getChildrenStubs();
        if (index < declarations.size()) {
          return tryCast(declarations.get(index).getPsi(), JSTypeDeclaration.class);
        }
      }
    }
    return null;
  }

  private static @Nullable JSTypeDeclaration getDefFieldArgumentPsi(@NotNull TypeScriptField field,
                                                                    int index,
                                                                    @NotNull List<String> typeNames) {
    TypeScriptSingleType type = PsiTreeUtil.getChildOfType(field, TypeScriptSingleType.class);
    String qualifiedName = doIfNotNull(type, TypeScriptSingleType::getQualifiedTypeName);
    if (qualifiedName == null) return null;
    if (ContainerUtil.find(typeNames, name -> qualifiedName.endsWith(name)) != null) {
      JSTypeDeclaration[] declarations = type.getTypeArguments();
      if (index < declarations.length) {
        return declarations[index];
      }
    }
    return null;
  }
}
