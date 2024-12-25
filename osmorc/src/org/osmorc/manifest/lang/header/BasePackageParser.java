// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.manifest.lang.header;

import com.intellij.codeInsight.daemon.JavaErrorBundle;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.ManifestBundle;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.util.OsgiPsiUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Vladislav.Soroka
 */
public class BasePackageParser extends OsgiHeaderParser {
  public static final HeaderParser INSTANCE = new BasePackageParser();

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull HeaderValuePart headerValuePart) {
    return headerValuePart.getParent() instanceof Clause ? getPackageReferences(headerValuePart) : PsiReference.EMPTY_ARRAY;
  }

  @Override
  public boolean annotate(@NotNull Header header, @NotNull AnnotationHolder holder) {
    boolean annotated = false;

    for (HeaderValue value : header.getHeaderValues()) {
      if (value instanceof Clause) {
        HeaderValuePart valuePart = ((Clause)value).getValue();
        if (valuePart != null) {
          String packageName = valuePart.getUnwrappedText();
          packageName = StringUtil.trimEnd(packageName, ".*");

          if (StringUtil.isEmptyOrSpaces(packageName)) {
            holder.newAnnotation(HighlightSeverity.ERROR, ManifestBundle.message("header.reference.invalid")).range(valuePart.getHighlightingRange()).create();
            annotated = true;
            continue;
          }

          PsiDirectory[] directories = OsgiPsiUtil.resolvePackage(header, packageName);
          if (directories.length == 0) {
            holder.newAnnotation(HighlightSeverity.ERROR, JavaErrorBundle.message("cannot.resolve.package", packageName)).range(valuePart.getHighlightingRange()).create();
            annotated = true;
          }
        }
      }
    }

    return annotated;
  }

  @Override
  public @Nullable Object getConvertedValue(@NotNull Header header) {
    List<HeaderValue> headerValues = header.getHeaderValues();
    if (!headerValues.isEmpty()) {
      List<String> packages = new ArrayList<>(headerValues.size());
      for (HeaderValue headerValue : headerValues) {
        HeaderValuePart valuePart = ((Clause)headerValue).getValue();
        if (valuePart != null) {
          packages.add(valuePart.getText().replaceAll("\\s+", ""));
        }
      }
      return packages;
    }

    return null;
  }

  protected static PsiReference[] getPackageReferences(PsiElement psiElement) {
    String packageName = psiElement.getText();
    if (StringUtil.isEmptyOrSpaces(packageName) ) {
      return PsiReference.EMPTY_ARRAY;
    }

    int offset = 0;
    if (packageName.charAt(0) == '!') {
      packageName = packageName.substring(1);
      offset = 1;
    }

    int size = packageName.length() - 1;
    if (packageName.charAt(size) == '?') {
      packageName = packageName.substring(0, size);
    }

    PackageReferenceSet refSet = new PackageReferenceSet(packageName, psiElement, offset) {
      @Override
      public Collection<PsiPackage> resolvePackageName(@Nullable PsiPackage context, String packageName) {
        if (context == null) return Collections.emptyList();
        String unwrappedPackageName = packageName.replaceAll("\\s+", "");
        return ContainerUtil.filter(context.getSubPackages(), pkg -> unwrappedPackageName.equals(pkg.getName()));
      }
    };
    return refSet.getReferences().toArray(new PsiPackageReference[0]);
  }
}