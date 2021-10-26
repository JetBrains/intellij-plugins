// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.javascript.web.symbols.impl.PsiElementNavigationTarget;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.model.Pointer;
import com.intellij.navigation.NavigationTarget;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static org.angular2.entities.source.Angular2SourceDirective.getPropertySources;

public class Angular2SourceDirectiveProperty implements Angular2DirectiveProperty {

  private final TypeScriptClass mySource;
  private final JSRecordType.PropertySignature mySignature;
  private final String myName;
  private final String myKind;

  public Angular2SourceDirectiveProperty(@NotNull TypeScriptClass source,
                                         @NotNull JSRecordType.PropertySignature signature,
                                         @NotNull String kind,
                                         @NotNull String bindingName) {
    mySource = source;
    mySignature = signature;
    myName = bindingName;
    myKind = kind;
  }

  @NotNull
  @Override
  public String getKind() {
    return myKind;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getRawJsType() {
    return mySignature.getJSType();
  }

  @Override
  public boolean isVirtual() {
    return false;
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return getSources().get(0);
  }

  @NotNull
  @Override
  public Collection<NavigationTarget> getNavigationTargets(@NotNull Project project) {
    return ContainerUtil.map(getSources(), s -> new PsiElementNavigationTarget(s));
  }

  @NotNull
  public List<PsiElement> getSources() {
    var sources = getPropertySources(mySignature.getMemberSource().getSingleElement());
    var decorated = ContainerUtil.filter(
      sources, s -> {
        var attrList = s.getAttributeList();
        if (attrList == null) return false;
        return attrList.getDecorators().length > 0;
      });
    if (!decorated.isEmpty()) {
      //noinspection unchecked
      return (List<PsiElement>)(List<?>)decorated;
    }
    else if (!sources.isEmpty()) {
      //noinspection unchecked
      return (List<PsiElement>)(List<?>)sources;
    }
    else {
      return Collections.singletonList(mySource);
    }
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2SourceDirectiveProperty property = (Angular2SourceDirectiveProperty)o;
    return mySource.equals(property.mySource)
           && mySignature.getMemberName().equals(property.mySignature.getMemberName())
           && myName.equals(property.myName)
           && myKind.equals(property.myKind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mySource, mySignature.getMemberName(), myName, myKind);
  }

  @NotNull
  @Override
  public Pointer<Angular2SourceDirectiveProperty> createPointer() {
    var sourcePtr = createSmartPointer(mySource);
    var propertyName = mySignature.getMemberName();
    var name = myName;
    var kind = myKind;
    return () -> {
      var source = sourcePtr.dereference();
      if (source == null) return null;
      var propertySignature = TypeScriptTypeParser
        .buildTypeFromClass(source, false)
        .findPropertySignature(propertyName);
      return propertySignature != null
             ? new Angular2SourceDirectiveProperty(source, propertySignature, kind, name)
             : null;
    };
  }
}
