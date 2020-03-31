/*
 * Copyright 2011 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.dom.validator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ResourceFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PackageScope;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.struts2.facet.ui.StrutsVersionDetector;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public class ValidatorManagerImpl extends ValidatorManager {

  @NonNls
  private static final String VALIDATORS_XML = "validators.xml";

  @NonNls
  private static final String VALIDATORS_DEFAULT_XML = "default.xml";

  @Override
  public boolean isValidatorsFile(@NotNull final XmlFile xmlFile) {
    return DomManager.getDomManager(xmlFile.getProject()).getFileElement(xmlFile, Validators.class) != null;
  }

  @Nullable
  private static DomFileElement<ValidatorsConfig> getValidatorsConfigFileElement(@NotNull final XmlFile xmlFile) {
    return DomManager.getDomManager(xmlFile.getProject()).getFileElement(xmlFile, ValidatorsConfig.class);
  }

  @Override
  public boolean isCustomValidatorConfigFile(@NotNull final PsiFile psiFile) {
    return !Objects.equals(psiFile.getName(), VALIDATORS_DEFAULT_XML);
  }

  @Override
  public List<ValidatorConfig> getValidators(@NotNull final Module module) {
    final XmlFile validatorsFile = getValidatorConfigFile(module);
    if (validatorsFile == null) {
      return Collections.emptyList();
    }

    final DomFileElement<ValidatorsConfig> validatorsConfigElement = getValidatorsConfigFileElement(validatorsFile);
    if (validatorsConfigElement == null) {
      return Collections.emptyList();
    }

    final List<ValidatorConfig> validatorConfigs = validatorsConfigElement.getRootElement().getValidatorConfigs();
    if (!isCustomValidatorConfigFile(validatorsFile)) {
      return validatorConfigs;
    }

    // add validators from default.xml for Struts > 2.0.8
    final String version = StrutsVersionDetector.detectStrutsVersion(module);
    if (StringUtil.compareVersionNumbers(version, "2.0.8") == 1) {
      final XmlFile defaultValidatorFile = findDefaultValidatorsFile(module);
      if (defaultValidatorFile != null) {
        final DomFileElement<ValidatorsConfig> fileElement = getValidatorsConfigFileElement(defaultValidatorFile);
        if (fileElement == null) {
          return validatorConfigs;
        }

        final List<ValidatorConfig> defaultValidators = fileElement.getRootElement().getValidatorConfigs();

        final List<ValidatorConfig> allValidatorConfigs = new ArrayList<>(defaultValidators);
        allValidatorConfigs.addAll(validatorConfigs); // custom overrides defaults
        return allValidatorConfigs;
      }
    }

    return validatorConfigs;
  }

  @Override
  @Nullable
  public XmlFile getValidatorConfigFile(@NotNull final Module module) {
    final Project project = module.getProject();
    final PsiManager psiManager = PsiManager.getInstance(project);

    final VirtualFile validatorsVirtualFile =
        ResourceFileUtil.findResourceFileInScope(VALIDATORS_XML, project,
                                                 GlobalSearchScope.moduleRuntimeScope(module, false));

    if (validatorsVirtualFile != null) {
      final PsiFile file = psiManager.findFile(validatorsVirtualFile);
      if (file != null &&
          getValidatorsConfigFileElement((XmlFile) file) != null) {
        return (XmlFile) file;
      }
    }

    return findDefaultValidatorsFile(module);
  }

  @NotNull
  @Override
  public List<XmlFile> findValidationFilesFor(@NotNull final PsiClass clazz) {
    final PsiFile psiFile = clazz.getContainingFile().getOriginalFile();
    final PsiDirectory containingDirectory = psiFile.getContainingDirectory();
    if (containingDirectory == null) {
      return Collections.emptyList();
    }

    final PsiPackage containingPackage = JavaDirectoryService.getInstance().getPackage(containingDirectory);
    if (containingPackage == null) {
      return Collections.emptyList();
    }

    final PackageScope searchScope = new PackageScope(containingPackage, false, true);
    final List<DomFileElement<Validators>> validationRoots =
        DomService.getInstance().getFileElements(Validators.class, clazz.getProject(), searchScope);

    final List<DomFileElement<Validators>> filtered =
        ContainerUtil.filter(validationRoots, validatorDomFileElement -> {
          final String fileName = validatorDomFileElement.getFile().getName();
          return StringUtil.startsWith(fileName, clazz.getName());
        });

    return ContainerUtil.map(filtered, DomFileElement::getFile);
  }

  /**
   * Find {@code com/opensymphony/xwork2/validator/validators/default.xml} from {@code xwork.jar}.
   *
   * @param module Current module.
   * @return {@code null} if not found.
   */
  @Nullable
  private static XmlFile findDefaultValidatorsFile(final Module module) {
    final Project project = module.getProject();

    final PsiClass emailValidatorClass = JavaPsiFacade.getInstance(project).findClass(
        "com.opensymphony.xwork2.validator.validators.EmailValidator",
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
    if (emailValidatorClass == null) {
      return null;
    }

    final VirtualFile file = PsiUtilCore.getVirtualFile(emailValidatorClass);
    if (file == null ||
        file.getFileSystem() != JarFileSystem.getInstance()) {
      return null;
    }

    // go up one level to ../validators/
    final VirtualFile parent = file.getParent();
    assert parent != null : "error walking up to parent from EmailValidator.class, xwork JAR file=" + file;

    final VirtualFile vfDefaultXml = parent.findChild(VALIDATORS_DEFAULT_XML);
    assert vfDefaultXml != null : "VF for default.xml null, parent=" + parent;

    return (XmlFile) PsiManager.getInstance(project).findFile(vfDefaultXml);
  }

}