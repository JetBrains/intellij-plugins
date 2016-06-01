package org.osmorc.manifest.resolve.reference.providers;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class ManifestPackageReferenceSet extends PackageReferenceSet {
  public ManifestPackageReferenceSet(String packageName, PsiElement element, int startInElement) {
    super(packageName, element, startInElement);
  }

  @Override
  public Collection<PsiPackage> resolvePackageName(@Nullable final PsiPackage context, final String packageName) {
    final String unwrappedPackageName = StringUtil.replace(packageName, "\n ", "").trim();
    if (context != null) {
      return ContainerUtil.filter(context.getSubPackages(), aPackage -> Comparing.equal(aPackage.getName(), unwrappedPackageName));
    }
    return Collections.emptyList();
  }
}
