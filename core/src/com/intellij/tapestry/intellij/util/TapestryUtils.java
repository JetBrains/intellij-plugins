package com.intellij.tapestry.intellij.util;

import com.intellij.facet.FacetManager;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TemplateElement;
import com.intellij.tapestry.core.model.presentation.components.BlockComponent;
import com.intellij.tapestry.core.model.presentation.components.BodyComponent;
import com.intellij.tapestry.core.model.presentation.components.ContainerComponent;
import com.intellij.tapestry.core.model.presentation.components.ParameterComponent;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility methods related to Tapestry.
 */
public class TapestryUtils {

    private static final String MODULE_SIGNAL_KEY = "TAPESTRY_MODULE";
    private static final String MODULE_SIGNAL_VALUE = "true";

    private static final Logger _logger = Logger.getInstance(TapestryUtils.class.getName());

    /**
     * Checks if a module is a Tapestry module.
     *
     * @param module the module to check.
     * @return <code>true</code> if the module is a Tapestry module, <code>false</code> otherwise.
     */
    public static boolean isTapestryModule(Module module) {
        return FacetManager.getInstance(module).getFacetsByType(TapestryFacetType.ID).size() > 0;
    }

    /**
     * Finds all module with Tapestry support in a project.
     *
     * @param project the project to look for Tapestry modules in.
     * @return all modules in the given projecto with Tapestry support.
     */
    public static Module[] getAllTapestryModules(Project project) {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        List<Module> result = new ArrayList<Module>();

        for (Module module : modules) {
            if (isTapestryModule(module)) {
                result.add(module);
            }
        }

        return result.toArray(new Module[result.size()]);
    }

    /**
     * Finds the element in a Tapestry component tag that identifies the type of component.
     *
     * @param tag the compontent tag.
     * @return the attribute that identifies the type of component.
     */
    public static XmlElement getComponentIdentifier(XmlTag tag) {
        if (!ComponentUtils.isComponentTag(new IntellijXmlTag(tag))) {
            return null;
        }

        // embedded components
        if (tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
            return (XmlElement) tag.getChildren()[1];
        }

        // using invisible instrumentation
        XmlElement typeAttribute = tag.getAttribute("type", TapestryConstants.TEMPLATE_NAMESPACE);
        if (typeAttribute != null) {
            return typeAttribute;
        }

        XmlElement idAttribute = tag.getAttribute("id", TapestryConstants.TEMPLATE_NAMESPACE);
        if (idAttribute != null) {
            return idAttribute;
        }

        return null;
    }

    /**
     * Creates a new component.
     *
     * @param module                  the module to create the page in.
     * @param classSourceDirectory    the source root where to create the page class.
     * @param templateSourceDirectory the source root where to create the page template.
     * @param pageName                the page name.
     * @param replaceExistingFiles    should an existing page file be replaced.
     * @throws IllegalStateException if the page file already existed and <code>replaceExistingFiles = false</code>
     */
    public static void createComponent(
            Module module, PsiDirectory classSourceDirectory, PsiDirectory templateSourceDirectory, String pageName,
            boolean replaceExistingFiles
    ) throws IllegalStateException {
        String errorMsg = "";
        try {
            createClass(
                    classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage(), pageName, replaceExistingFiles,
                    TapestryConstants.COMPONENT_CLASS_TEMPLATE_NAME
            );

            if (templateSourceDirectory != null) {
                createTemplate(
                        module, templateSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage(), pageName, replaceExistingFiles,
                        TapestryConstants.COMPONENT_TEMPLATE_TEMPLATE_NAME
                );
            }
        } catch (IncorrectOperationException ex) {
            errorMsg = "An error occured creating the component!\n\n";

            _logger.error(ex);
        } catch (FileAlreadyExistsException ex) {
            errorMsg = "Some component file already exists, the existing version was kept!\n\n";
        } catch (NotFoundException ex) {
            errorMsg = "An error occured creating the component!\n\n";

            _logger.error(ex);
        }

        if (errorMsg.length() > 0) {
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * Creates a new page.
     *
     * @param module                  the module to create the page in.
     * @param classSourceDirectory    the source root where to create the page class.
     * @param templateSourceDirectory the source root where to create the page template.
     * @param pageName                the page name.
     * @param replaceExistingFiles    should an existing page file be replaced.
     * @throws IllegalStateException if the page file already existed and <code>replaceExistingFiles = false</code>
     */
    public static void createPage(Module module, PsiDirectory classSourceDirectory, PsiDirectory templateSourceDirectory, String pageName, boolean replaceExistingFiles) throws IllegalStateException {
        String errorMsg = "";
        try {
            createClass(classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage(), pageName, replaceExistingFiles, TapestryConstants.PAGE_CLASS_TEMPLATE_NAME);

            if (templateSourceDirectory != null) {
                createTemplate(
                        module, templateSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage(), pageName, replaceExistingFiles,
                        TapestryConstants.PAGE_TEMPLATE_TEMPLATE_NAME
                );
            }
        } catch (IncorrectOperationException ex) {
            errorMsg = "An error occured creating the page!\n\n";

            _logger.error(ex);
        } catch (FileAlreadyExistsException e) {
            errorMsg = "Some page file already exists, the existing version was kept!\n\n";
        } catch (NotFoundException ex) {
            errorMsg = "An error occured creating the page!\n\n";

            _logger.error(ex);
        }


        if (errorMsg.length() > 0) {
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * Creates a new mixin.
     *
     * @param module               the module to create the mixin in.
     * @param classSourceDirectory the source root where to create the mixin class.
     * @param mixinName            the mixin name.
     * @param replaceExistingFiles should an existing mixin file be replaced.
     * @throws IllegalStateException if the mixin file already existed and <code>replaceExistingFiles = false</code>
     */
    public static void createMixin(Module module, PsiDirectory classSourceDirectory, String mixinName, boolean replaceExistingFiles) throws IllegalStateException {
        String errorMsg = "";
        try {
            createClass(classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getMixinsRootPackage(), mixinName, replaceExistingFiles, TapestryConstants.MIXIN_CLASS_TEMPLATE_NAME);
        } catch (IncorrectOperationException ex) {
            errorMsg = "An error occured creating the mixin!\n\n";

            _logger.error(ex);
        } catch (FileAlreadyExistsException e) {
            errorMsg = "Some mixin file already exists, the existing version was kept!\n\n";
        } catch (NotFoundException ex) {
            errorMsg = "An error occured creating the mixin!\n\n";

            _logger.error(ex);
        }

        if (errorMsg.length() > 0) {
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * Builds the component object that corresponds to a HTML tag.
     *
     * @param module the module to find the component in.
     * @param tag    the component tag.
     * @return the component that the given tag represents.
     */
    public static Component getComponentFromTag(Module module, XmlTag tag) throws NotFoundException {
        if (!ComponentUtils.isComponentTag(new IntellijXmlTag(tag))) {
            throw new NotFoundException();
        }

        if (tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
            if (tag.getLocalName().toLowerCase(Locale.getDefault()).equals("body")) {
                return BodyComponent.getInstance(TapestryModuleSupportLoader.getTapestryProject(module));
            }
            if (tag.getLocalName().toLowerCase(Locale.getDefault()).equals("block")) {
                return BlockComponent.getInstance(TapestryModuleSupportLoader.getTapestryProject(module));
            }
            if (tag.getLocalName().toLowerCase(Locale.getDefault()).equals("parameter")) {
                return ParameterComponent.getInstance(TapestryModuleSupportLoader.getTapestryProject(module));
            }
            if (tag.getLocalName().toLowerCase(Locale.getDefault()).equals("container")) {
                return ContainerComponent.getInstance(TapestryModuleSupportLoader.getTapestryProject(module));
            }
        }

        XmlElement identifierElement = TapestryUtils.getComponentIdentifier(tag);

        if (identifierElement instanceof XmlAttribute && ((XmlAttribute) identifierElement).getLocalName().equals("type")) {
            if (((XmlAttribute) identifierElement).getValue() == null) {
                throw new NotFoundException();
            }

            Component component = TapestryModuleSupportLoader.getTapestryProject(module).findComponent(((XmlAttribute) identifierElement).getValue().replace('.', '/'));
            if (component == null) {
                throw new NotFoundException();
            } else {
                return component;
            }
        }

        if (identifierElement instanceof XmlAttribute && ((XmlAttribute) identifierElement).getLocalName().equals("id")) {
            IJavaClassType contextClass = ComponentUtils.findClassFromTemplate(new IntellijResource(tag.getContainingFile()), TapestryModuleSupportLoader.getTapestryProject(module));
            if (contextClass != null) {
                PresentationLibraryElement contextElement = TapestryModuleSupportLoader.getTapestryProject(module).findElement(contextClass);
                if (contextElement != null) {
                    for (TemplateElement embeddedComponent : contextElement.getEmbeddedComponents()) {
                        if (embeddedComponent.getElement().getElementId().equals(((XmlAttribute) identifierElement).getValue())) {
                            return (Component) embeddedComponent.getElement().getElement();
                        }
                    }
                }
            }
        }

        if (identifierElement instanceof XmlToken) {
            TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
            if (tapestryProject == null) {
                throw new NotFoundException();
            }

            Component component = tapestryProject.findComponent(tag.getLocalName().replace('.', '/'));
            if (component == null) {
                throw new NotFoundException();
            } else {
                return component;
            }
        }

        throw new NotFoundException();
    }

    /**
     * Finds the Tapestry namespace prefix declared in a template.
     *
     * @param template the template to search for the prefix;
     * @return the Tapestry namespace prefix declared in the given template or <code>null</code> if none is found.
     */
    public static String getTapestryNamespacePrefix(XmlFile template) {
        if (template.getDocument().getRootTag() != null) {
            for (XmlAttribute attribute : template.getDocument().getRootTag().getAttributes())
                if (attribute.getName().startsWith("xmlns:") && attribute.getValue().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
                    return attribute.getName().substring(6);
                }
        }

        return null;
    }

    /**
     * Creates a class.
     *
     * @param sourceDirectory      the source root where to create the class.
     * @param basePackage          the base package to create the class in.
     * @param pageName             the page name.
     * @param replaceExistingFiles should an existing class be replaced.
     * @param templateName         the name of the template to use for the class.
     * @throws FileAlreadyExistsException  if the page class already existed and <code>replaceExistingFiles = false</code>
     * @throws IncorrectOperationException if an error occurs creating the class.
     */
    private static void createClass(PsiDirectory sourceDirectory, String basePackage, String pageName, boolean replaceExistingFiles, String templateName) throws FileAlreadyExistsException,
            IncorrectOperationException {
        PsiDirectory classDirectory = IdeaUtils.findOrCreateDirectoryForPackage(sourceDirectory, PathUtils.getFullComponentPackage(basePackage, pageName));

        String fileName = PathUtils.getComponentFileName(pageName);
        PsiFile file = classDirectory.findFile(fileName + ".java");
        if (file != null) {
            if (!replaceExistingFiles) {
                throw new FileAlreadyExistsException();
            } else {
                file.delete();
            }
        }
        JavaDirectoryService.getInstance().createClass(classDirectory, PathUtils.getComponentFileName(pageName), templateName);
    }

    /**
     * Creates a template.
     *
     * @param module               the module to create the page template in.
     * @param sourceDirectory      the source root where to create the page template.
     * @param basePackage          the base package to create the template in.
     * @param pageName             the page name.
     * @param replaceExistingFiles should an existing page class be replaced.
     * @param template             the template to use.
     * @throws FileAlreadyExistsException  if the page template already existed and <code>replaceExistingFiles = false</code>
     * @throws IncorrectOperationException if an error occurs creating the template.
     */
    private static void createTemplate(Module module, PsiDirectory sourceDirectory, String basePackage, String pageName, boolean replaceExistingFiles, String template) throws
            FileAlreadyExistsException, IncorrectOperationException {
        PsiDirectory templateDirectory;
        if (!IdeaUtils.isWebRoot(module, sourceDirectory.getVirtualFile())) {
            templateDirectory = IdeaUtils.findOrCreateDirectoryForPackage(sourceDirectory, PathUtils.getFullComponentPackage(basePackage, pageName));
        } else {
            templateDirectory = IdeaUtils.findOrCreateDirectoryForPackage(sourceDirectory, PathUtils.getFullComponentPackage("", pageName));
        }

        String fileName = PathUtils.getComponentFileName(pageName) + TapestryConstants.TEMPLATE_FILE_EXTENSION;
        if (templateDirectory.findFile(fileName) != null) {
            if (!replaceExistingFiles) {
                throw new FileAlreadyExistsException();
            } else {
                templateDirectory.findFile(fileName).delete();
            }
        }

        PsiFile pageTemplate = PsiFileFactory.getInstance(module.getProject())
                .createFileFromText(fileName, FileTemplateManager.getInstance().getInternalTemplate(template).getText());
        templateDirectory.add(pageTemplate);
    }

    static class FileAlreadyExistsException extends Exception {

    }
}
