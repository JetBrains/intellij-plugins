// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.*;
import com.intellij.lang.typescript.TypeScriptStubElementTypes;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil.processUpPackageJsonFilesInAllScope;
import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.ObjectUtils.*;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2IvyUtil {

  public static final ModuleDefKind MODULE_DEF = new ModuleDefKind();
  public static final DirectiveDefKind DIRECTIVE_DEF = new DirectiveDefKind();
  public static final ComponentDefKind COMPONENT_DEF = new ComponentDefKind();
  public static final PipeDefKind PIPE_DEF = new PipeDefKind();

  private static final EntityDefKind[] ENTITY_DEFS = new EntityDefKind[]{
    MODULE_DEF, DIRECTIVE_DEF, COMPONENT_DEF, PIPE_DEF
  };

  @NonNls private static final String FIELD_DIRECTIVE_DEF = "ɵdir";
  @NonNls private static final String FIELD_MODULE_DEF = "ɵmod";
  @NonNls private static final String FIELD_PIPE_DEF = "ɵpipe";
  @NonNls private static final String FIELD_COMPONENT_DEF = "ɵcmp";

  @NonNls private static final String TYPE_DIRECTIVE_DEF = "ɵɵDirectiveDefWithMeta";
  @NonNls private static final String TYPE_MODULE_DEF = "ɵɵNgModuleDefWithMeta";
  @NonNls private static final String TYPE_PIPE_DEF = "ɵɵPipeDefWithMeta";
  @NonNls private static final String TYPE_COMPONENT_DEF = "ɵɵComponentDefWithMeta";

  public static Pair<TypeScriptField, EntityDefKind> findEntityDefField(@NotNull Object classPsiOrStub) {
    StubElement<?> stub = classPsiOrStub instanceof TypeScriptClassStub
                          ? (StubElement<?>)classPsiOrStub
                          : doIfNotNull(tryCast(classPsiOrStub, StubBasedPsiElementBase.class),
                                        StubBasedPsiElementBase::getStub);
    if (stub instanceof TypeScriptClassStub) {
      Pair<TypeScriptFieldStub, EntityDefKind> result = findEntityDefFieldStubbed((TypeScriptClassStub)stub);
      if (result == null) {
        return null;
      }
      return Pair.create((TypeScriptField)result.getFirst().getPsi(), result.second);
    }
    return findEntityDefFieldPsi((TypeScriptClass)classPsiOrStub);
  }

  @Nullable
  public static Pair<TypeScriptFieldStub, EntityDefKind> findEntityDefFieldStubbed(@NotNull TypeScriptClassStub jsClassStub) {
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
      EntityDefKind entityDefKind = getDefForFieldName(fieldStub.getName());
      if (entityDefKind == null) {
        continue;
      }
      return Pair.create((TypeScriptFieldStub)fieldStub, entityDefKind);
    }
    return null;
  }

  @Nullable
  private static Pair<TypeScriptField, EntityDefKind> findEntityDefFieldPsi(@NotNull TypeScriptClass jsClass) {
    for (JSField field : jsClass.getFields()) {
      if (!(field instanceof TypeScriptField)) {
        continue;
      }
      JSAttributeList attrs = field.getAttributeList();
      if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        continue;
      }
      EntityDefKind entityDefKind = getDefForFieldName(field.getName());
      if (entityDefKind != null) {
        return Pair.create((TypeScriptField)field, entityDefKind);
      }
    }
    return null;
  }

  @Nullable
  public static JSTypeDeclaration getDefFieldArgument(@NotNull Object fieldPsiOrStub, int index, @NotNull String typeName) {
    if (fieldPsiOrStub instanceof TypeScriptFieldStub) {
      return getDefFieldArgumentStubbed((TypeScriptFieldStub)fieldPsiOrStub, index, typeName);
    }
    assert fieldPsiOrStub instanceof TypeScriptField;
    StubElement<?> stub = doIfNotNull(tryCast(fieldPsiOrStub, StubBasedPsiElementBase.class),
                                      StubBasedPsiElementBase::getStub);
    if (stub != null) {
      return getDefFieldArgumentStubbed((TypeScriptFieldStub)stub, index, typeName);
    }
    return getDefFieldArgumentPsi((TypeScriptField)fieldPsiOrStub, index, typeName);
  }

  @Nullable
  public static EntityDefKind getDefForFieldName(@Nullable String fieldName) {
    if (fieldName != null) {
      for (EntityDefKind entityDefKind : ENTITY_DEFS) {
        if (entityDefKind.isFieldName(fieldName)) {
          return entityDefKind;
        }
      }
    }
    return null;
  }

  @Nullable
  private static String getStringGenericParam(@NotNull Object fieldPsiOrStub, int index, @NotNull String typeName) {
    JSTypeDeclaration declaration = getDefFieldArgument(fieldPsiOrStub, index, typeName);
    if (declaration instanceof TypeScriptStringLiteralType) {
      return ((TypeScriptStringLiteralType)declaration).getInnerText();
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

  @NotNull
  private static <T extends TypeScriptType, R> List<R> processTupleArgument(@NotNull Object fieldPsiOrStub,
                                                                            int index,
                                                                            @NotNull String typeName,
                                                                            @NotNull Class<T> itemsClass,
                                                                            @NotNull Function<T, R> itemMapper) {
    TypeScriptTupleType tuple = tryCast(getDefFieldArgument(fieldPsiOrStub, index, typeName), TypeScriptTupleType.class);
    if (tuple == null) {
      return Collections.emptyList();
    }
    return StreamEx.of(tuple.getElements())
      .select(itemsClass)
      .map(itemMapper)
      .toList();
  }

  @NotNull
  private static <T extends TypeScriptType, R> Map<String, R> processObjectArgument(@NotNull Object fieldPsiOrStub,
                                                                                    int index,
                                                                                    @NotNull String typeName,
                                                                                    @NotNull Class<T> valueClass,
                                                                                    @NotNull Function<T, R> valueMapper) {
    TypeScriptObjectType object = tryCast(getDefFieldArgument(fieldPsiOrStub, index, typeName), TypeScriptObjectType.class);
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

  public static boolean hasIvyMetadata(@NotNull PsiElement el) {
    return Optional.ofNullable(PsiUtilCore.getVirtualFile(el))
      .map(VirtualFile::getParent)
      .map(el.getManager()::findDirectory)
      .map(dir -> CachedValuesManager.getCachedValue(dir, () -> {
        VirtualFile f = dir.getVirtualFile();
        Ref<Boolean> result = new Ref<>(false);
        Ref<Integer> level = new Ref<>(0);
        processUpPackageJsonFilesInAllScope(f, packageJson -> {
          if (packageJson.getParent().findChild("__ivy_ngcc__") != null) { //NON-NLS
            result.set(true);
            return false;
          }
          level.set(level.get() + 1);
          //we need to check only 2 package.jsons
          return level.get() < 2;
        });
        return create(result.get(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
      }))
      .orElse(false);
  }

  public static boolean isIvyMetadataSupportEnabled() {
    return Registry.is("angular.enableIvyMetadataSupport"); //NON-NLS
  }

  public interface EntityDefKind {
    boolean isFieldName(@NonNls @NotNull String name);
  }

  public static class ModuleDefKind implements EntityDefKind {
    private ModuleDefKind() {}

    @Override
    public boolean isFieldName(@NonNls @NotNull String name) {
      return FIELD_MODULE_DEF.equals(name);
    }

    @NotNull
    public List<TypeScriptTypeofType> getTypesList(@NotNull Object fieldPsiOrStub, @NotNull String property) {
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
      return processTupleArgument(fieldPsiOrStub, index, TYPE_MODULE_DEF, TypeScriptTypeofType.class, Function.identity());
    }

    @Override
    public String toString() {
      return "ModuleDefKind";
    }
  }

  public static class DirectiveDefKind implements EntityDefKind {
    private DirectiveDefKind() {}

    @Override
    public boolean isFieldName(@NonNls @NotNull String name) {
      return FIELD_DIRECTIVE_DEF.equals(name);
    }

    protected String getDefType() {
      return TYPE_DIRECTIVE_DEF;
    }

    @Nullable
    public String getSelector(@NotNull Object fieldPsiOrStub) {
      return getStringGenericParam(fieldPsiOrStub, 1, getDefType());
    }

    @Nullable
    public TypeScriptStringLiteralType getSelectorElement(@NotNull Object fieldPsiOrStub) {
      return tryCast(getDefFieldArgument(fieldPsiOrStub, 1, getDefType()), TypeScriptStringLiteralType.class);
    }

    @NotNull
    public List<String> getExportAsList(@NotNull Object fieldPsiOrStub) {
      return processTupleArgument(fieldPsiOrStub, 2, getDefType(), TypeScriptStringLiteralType.class,
                                  TypeScriptStringLiteralType::getInnerText);
    }

    @NotNull
    public Map<String, String> readPropertyMappings(@NotNull Object fieldPsiOrStub, @NotNull String kind) {
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
      return processObjectArgument(fieldPsiOrStub, index, getDefType(), TypeScriptStringLiteralType.class,
                                   TypeScriptStringLiteralType::getInnerText);
    }

    @Override
    public String toString() {
      return "DirectiveDefKind";
    }
  }

  public static class ComponentDefKind extends DirectiveDefKind {
    private ComponentDefKind() {}

    @Override
    protected String getDefType() {
      return TYPE_COMPONENT_DEF;
    }

    @Override
    public boolean isFieldName(@NonNls @NotNull String name) {
      return FIELD_COMPONENT_DEF.equals(name);
    }

    @Override
    public String toString() {
      return "ComponentDefKind";
    }
  }

  public static class PipeDefKind implements EntityDefKind {
    private PipeDefKind() {}

    @Override
    public boolean isFieldName(@NonNls @NotNull String name) {
      return FIELD_PIPE_DEF.equals(name);
    }

    @Nullable
    public String getName(Object fieldPsiOrStub) {
      return getStringGenericParam(fieldPsiOrStub, 1, TYPE_PIPE_DEF);
    }

    @Override
    public String toString() {
      return "PipeDefKind";
    }
  }
}
