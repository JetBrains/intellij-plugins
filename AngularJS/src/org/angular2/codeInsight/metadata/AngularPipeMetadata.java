// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.metadata;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
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
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2Pipe;
import org.angular2.entities.impl.Angular2SourcePipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AngularPipeMetadata implements Angular2Pipe {


  @NotNull
  public static Angular2Pipe create(@NotNull JSImplicitElement declaration) {
    final TypeScriptClass context = PsiTreeUtil.getContextOfType(declaration, TypeScriptClass.class);
    if (context != null) {
      ES6Decorator dec = PsiTreeUtil.getParentOfType(Angular2DecoratorUtil.getDecorator(context, "Pipe"), ES6Decorator.class);
      if (dec != null) {
        String name = declaration.getName();
        return CachedValuesManager.getCachedValue(dec, () ->
          CachedValueProvider.Result.create(new Angular2SourcePipe(dec, name), dec));
      }
    }
    else if (declaration.getContainingFile() instanceof JsonFile) {
      return new CompiledPipeMetadata(declaration);
    }
    return new EmptyPipeMetadata(declaration);
  }

  private boolean metadataLoaded;
  @Nullable
  protected TypeScriptClass myPipeClass;
  @Nullable
  protected JSRecordType.PropertySignature myTransformMethod;

  @Override
  @Nullable
  public final TypeScriptClass getTypeScriptClass() {
    readMetadataIfNeeded();
    return myPipeClass;
  }

  @Nullable
  @Override
  public Collection<? extends TypeScriptFunction> getTransformMethods() {
    if (getTransformMethod() != null) {
      //noinspection unchecked,RedundantCast
      return (Collection<? extends TypeScriptFunction>)(Collection)getTransformMethod()
        .getMemberSource()
        .getAllSourceElements()
        .stream()
        .filter(fun -> fun instanceof TypeScriptFunction && !(fun instanceof TypeScriptFunctionSignature))
        .collect(Collectors.toList());
    }
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

  @Nullable
  public final JSRecordType.PropertySignature getTransformMethod() {
    readMetadataIfNeeded();
    readTransformMethodIfNeeded();
    return myTransformMethod;
  }

  protected final void readMetadataIfNeeded() {
    if (!metadataLoaded) {
      metadataLoaded = true;
      readMetadata();
    }
  }

  protected final void readTransformMethodIfNeeded() {
    if (myTransformMethod == null && myPipeClass != null) {
      myTransformMethod = TypeScriptTypeParser
        .buildTypeFromClass(myPipeClass, false)
        .getProperties()
        .stream()
        .filter(prop -> "transform".equals(prop.getMemberName())
                        && prop.getMemberSource().getSingleElement() instanceof TypeScriptFunction)
        .findFirst()
        .orElse(null);
    }
  }

  protected abstract void readMetadata();

  private static class EmptyPipeMetadata extends AngularPipeMetadata {

    private final JSImplicitElement myDeclaration;

    private EmptyPipeMetadata(@NotNull JSImplicitElement declaration) {
      myDeclaration = declaration;
    }

    @Override
    protected void readMetadata() {
    }

    @NotNull
    @Override
    public String getName() {
      return myDeclaration.getName();
    }

    @NotNull
    @Override
    public JSElement getNavigableElement() {
      return myDeclaration;
    }
  }

  private static class CompiledPipeMetadata extends AngularPipeMetadata {

    @NotNull
    private final JSImplicitElement myDeclaration;

    private CompiledPipeMetadata(@NotNull JSImplicitElement declaration) {
      myDeclaration = declaration;
    }

    @NotNull
    @Override
    public String getName() {
      return myDeclaration.getName();
    }

    @NotNull
    @Override
    public JSElement getNavigableElement() {
      return getTypeScriptClass() != null ? getTypeScriptClass() : myDeclaration;
    }

    @Override
    protected void readMetadata() {
      String className = getClassName();
      if (className == null) {
        return;
      }
      className = StringUtil.trimStart(className, "Deprecated");
      VirtualFile metadataJson = myDeclaration.getContainingFile().getVirtualFile();
      AngularMetadata metadata = AngularMetadataLoader.INSTANCE.load(metadataJson);
      AngularClass pipe = metadata.findClass(className);
      if (pipe == null) {
        return;
      }
      VirtualFile parentDir = metadataJson.getParent();
      String sourcePath = pipe.getSourcePath();
      sourcePath = StringUtil.trimEnd(sourcePath, ".");
      if (!sourcePath.endsWith(".d.ts")) {
        sourcePath += ".d.ts";
      }
      VirtualFile definitionFile = parentDir.findFileByRelativePath(sourcePath);
      PsiFile definitionPsi = definitionFile != null ? myDeclaration.getManager().findFile(definitionFile) : null;
      if (definitionPsi instanceof JSFile) {
        ResolveResultSink sink = new ResolveResultSink(definitionPsi, pipe.getName());
        ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));
        if (sink.getResult() instanceof TypeScriptClass) {
          myPipeClass = (TypeScriptClass)sink.getResult();
        }
      }
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
  }
}
