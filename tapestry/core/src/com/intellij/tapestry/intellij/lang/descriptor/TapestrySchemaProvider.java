package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.xml.XmlSchemaProvider;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexey Chmutov
 */
public class TapestrySchemaProvider extends XmlSchemaProvider implements DumbAware {
  @Override
  public XmlFile getSchema(@NotNull @NonNls String url, @Nullable Module module, @NotNull PsiFile baseFile) {
    final String location = ExternalResourceManager.getInstance().getResourceLocation(url, baseFile.getProject());
    return XmlUtil.findXmlFile(baseFile, location);
  }

  @Override
  public boolean isAvailable(final @NotNull XmlFile file) {
    return file.getFileType() instanceof TmlFileType;
  }

  @NotNull
  @Override
  public Set<String> getAvailableNamespaces(final @NotNull XmlFile file, final @Nullable String tagName) {
    HashSet<String> set = new HashSet<>();
    set.addAll(Arrays.asList(TapestryXmlExtension.tapestryTemplateNamespaces()));
    set.add(TapestryConstants.PARAMETERS_NAMESPACE);
    set.add(XmlUtil.XHTML_URI);
    return set;
  }

  @Nullable
  @Override
  public String getDefaultPrefix(@NotNull @NonNls String namespace, @NotNull final XmlFile context) {
    if (XmlUtil.XHTML_URI.equals(namespace)) return "";
    if (TapestryXmlExtension.isTapestryTemplateNamespace(namespace)) return "t";
    if (TapestryConstants.PARAMETERS_NAMESPACE.equals(namespace)) return "p";
    return null;
  }

  @Nullable
  @Override
  public Set<String> getLocations(@NotNull @NonNls String namespace, @NotNull final XmlFile context) {
    return null;
  }

}
