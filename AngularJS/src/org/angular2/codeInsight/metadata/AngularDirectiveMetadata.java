// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.metadata;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.impl.Angular2SourceComponent;
import org.angular2.entities.impl.Angular2SourceDirective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableCollection;

public abstract class AngularDirectiveMetadata implements Angular2Directive {

  @NotNull
  public static Angular2Directive create(@NotNull JSImplicitElement declaration) {
    final TypeScriptClass context = PsiTreeUtil.getContextOfType(declaration, TypeScriptClass.class);
    if (context != null) {
      Angular2Directive result = create(context);
      if (result != null) {
        return result;
      }
    }
    else if (declaration.getContainingFile() instanceof JsonFile) {
      return new CompiledDirectiveMetadata(declaration);
    }
    return new EmptyDirectiveMetadata(declaration);
  }

  @Nullable
  public static Angular2Directive create(@NotNull TypeScriptClass context) {
    JSCallExpression call = Angular2DecoratorUtil.getDecorator(context, "Component");
    if (call != null) {
      ES6Decorator dec = PsiTreeUtil.getParentOfType(call, ES6Decorator.class);
      if (dec != null) {
        return CachedValuesManager.getCachedValue(dec, () ->
          CachedValueProvider.Result.create(new Angular2SourceComponent(dec), dec));
      }
    }
    else {
      ES6Decorator dec = PsiTreeUtil.getParentOfType(Angular2DecoratorUtil.getDecorator(context, "Directive"), ES6Decorator.class);
      if (dec != null) {
        return CachedValuesManager.getCachedValue(dec, () ->
          CachedValueProvider.Result.create(new Angular2SourceDirective(dec), dec));
      }
    }
    return null;
  }

  private Collection<PropertyInfo> myInputs;
  private Collection<PropertyInfo> myOutputs;

  @Override
  @Nullable
  public abstract TypeScriptClass getTypeScriptClass();

  @Override
  @NotNull
  public Collection<? extends Angular2DirectiveProperty> getInputs() {
    readMetadataIfNeeded();
    return myInputs;
  }

  @Override
  @NotNull
  public Collection<? extends Angular2DirectiveProperty> getOutputs() {
    readMetadataIfNeeded();
    return myOutputs;
  }

  @Nullable
  @Override
  public String getSelector() {
    return null;
  }

  @Nullable
  @Override
  public String getExportAs() {
    return null;
  }

  @Nullable
  @Override
  public Angular2Module getModule() {
    return null;
  }

  @Nullable
  @Override
  public ES6Decorator getDecorator() {
    return null;
  }

  protected final void readMetadataIfNeeded() {
    if (myInputs == null) {
      myInputs = new ArrayList<>();
      myOutputs = new ArrayList<>();
      readMetadata(myInputs, myOutputs);
      myInputs = unmodifiableCollection(myInputs);
      myOutputs = unmodifiableCollection(myOutputs);
    }
  }

  protected abstract void readMetadata(@NotNull Collection<PropertyInfo> inputs,
                                       @NotNull Collection<PropertyInfo> outputs);

  public static class PropertyInfo implements Angular2DirectiveProperty {
    @Nullable
    public final JSRecordType.PropertySignature signature;
    @NotNull
    public final PsiElement source;
    @NotNull
    public final String name;

    public PropertyInfo(@Nullable JSRecordType.PropertySignature signature, @NotNull PsiElement source, @NotNull String name) {
      this.signature = signature;
      this.source = source;
      this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
      return name;
    }

    @Nullable
    @Override
    public JSType getType() {
      return signature != null ? signature.getType() : null;
    }

    @NotNull
    @Override
    public PsiElement getNavigableElement() {
      return source;
    }
  }

  private static class EmptyDirectiveMetadata extends AngularDirectiveMetadata {

    private final JSImplicitElement myDeclaration;

    private EmptyDirectiveMetadata(@NotNull JSImplicitElement declaration) {
      super();
      myDeclaration = declaration;
    }

    @Nullable
    @Override
    public TypeScriptClass getTypeScriptClass() {
      return null;
    }

    @Override
    protected void readMetadata(@NotNull Collection<PropertyInfo> inputs, @NotNull Collection<PropertyInfo> outputs) {
    }

    @NotNull
    @Override
    public PsiElement getNavigableElement() {
      return myDeclaration;
    }
  }

  private static class CompiledDirectiveMetadata extends AngularDirectiveMetadata {

    @NotNull
    private final JSImplicitElement myDeclaration;
    private TypeScriptClass myDirectiveClass;
    private JSRecordType myDirectiveClassType;

    private CompiledDirectiveMetadata(@NotNull JSImplicitElement declaration) {
      myDeclaration = declaration;
    }

    @Nullable
    @Override
    public TypeScriptClass getTypeScriptClass() {
      readMetadataIfNeeded();
      return myDirectiveClass;
    }

    @NotNull
    @Override
    public PsiElement getNavigableElement() {
      return myDirectiveClass != null ? myDirectiveClass : myDeclaration;
    }

    @Override
    protected void readMetadata(@NotNull Collection<PropertyInfo> inputs,
                                @NotNull Collection<PropertyInfo> outputs) {
      String className = getClassName();
      if (className == null) {
        return;
      }
      VirtualFile metadataJson = myDeclaration.getContainingFile().getVirtualFile();
      AngularMetadata metadata = AngularMetadataLoader.INSTANCE.load(metadataJson);
      AngularClass directive = metadata.findClass(className);
      if (directive == null) {
        return;
      }
      VirtualFile parentDir = metadataJson.getParent();
      String sourcePath = directive.getSourcePath();
      sourcePath = StringUtil.trimEnd(sourcePath, ".");
      if (!sourcePath.endsWith(".d.ts")) {
        sourcePath += ".d.ts";
      }
      VirtualFile definitionFile = parentDir.findFileByRelativePath(sourcePath);
      PsiFile definitionPsi = definitionFile != null ? myDeclaration.getManager().findFile(definitionFile) : null;
      if (definitionPsi instanceof JSFile) {
        ResolveResultSink sink = new ResolveResultSink(definitionPsi, directive.getName());
        ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));
        if (sink.getResult() instanceof TypeScriptClass) {
          myDirectiveClass = (TypeScriptClass)sink.getResult();
          myDirectiveClassType = TypeScriptTypeParser
            .buildTypeFromClass(myDirectiveClass, false);
        }
      }
      stream(directive.getInputs()).map(this::locateField).forEach(inputs::add);
      stream(directive.getOutputs()).map(this::locateField).forEach(outputs::add);
    }

    @Nullable
    private String getClassName() {
      PsiFile metadataPsi = myDeclaration.getContainingFile();
      PsiElement element = metadataPsi.findElementAt(myDeclaration.getTextOffset());
      JsonProperty decorators = (JsonProperty)PsiTreeUtil.findFirstParent(element, el ->
        el instanceof JsonProperty && "decorators".equals(((JsonProperty)el).getName())
      );
      if (decorators == null || !(decorators.getParent() instanceof JsonObject)) {
        return null;
      }
      JsonProperty classDef = ObjectUtils.tryCast(decorators.getParent().getParent(), JsonProperty.class);
      return classDef != null ? classDef.getName() : null;
    }

    private PropertyInfo locateField(AngularField field) {
      if (myDirectiveClassType != null) {
        JSRecordType.PropertySignature sig = myDirectiveClassType.findPropertySignature(field.getName());
        if (sig != null) {
          PsiElement source = sig.getMemberSource().getSingleElement();
          return new PropertyInfo(sig, source != null ? source : myDeclaration, field.getName());
        }
      }
      return new PropertyInfo(null, myDeclaration, field.getName());
    }
  }
}
