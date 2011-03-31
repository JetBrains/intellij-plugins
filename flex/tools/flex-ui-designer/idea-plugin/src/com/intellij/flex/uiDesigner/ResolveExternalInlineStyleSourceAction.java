package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.DataInput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ResolveExternalInlineStyleSourceAction implements Runnable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.flex.uiDesigner.ResolveExternalInlineStyleSourceAction");

  private final String parentFqn;
  private final String elementFqn;
  private final String targetStyleName;
  private final Map<String, String> properties;

  private final GlobalSearchScope scope;
  private final Project project;

  private final static Set<String> ignoredRootTags = new THashSet<String>(5);

  static {
    ignoredRootTags.add("Metadata");
    ignoredRootTags.add("states");
    ignoredRootTags.add(XmlBackedJSClassImpl.SCRIPT_TAG_NAME);
    ignoredRootTags.add("Library");
    ignoredRootTags.add("Private");
  }

  public ResolveExternalInlineStyleSourceAction(DataInput input, Module module) throws IOException {
    parentFqn = input.readUTF();
    elementFqn = input.readUTF();
    targetStyleName = input.readUTF();
    scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    project = module.getProject();

    int size = input.readShort();
    properties = new THashMap<String, String>(size);
    assert size > 0;
    for (int i = 0; i < size; i++) {
      String name = input.readUTF();
      if (name.length() == 0) {
        continue;
      }

      properties.put(name, input.readUTF());
    }

    assert properties.size() > 0;
  }

  public ResolveExternalInlineStyleSourceAction(String parentFqn,
                                                String elementFqn,
                                                String targetStyleName,
                                                Map<String, String> properties,
                                                Module module) {
    this.parentFqn = parentFqn;
    this.elementFqn = elementFqn;
    this.targetStyleName = targetStyleName;
    this.properties = properties;

    scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    project = module.getProject();
  }

  @Override
  public void run() {
    Navigatable target = find();
    if (target == null) {
      LOG.error("Can't find target property " + targetStyleName + " of " + elementFqn + " in " + parentFqn);
    }
    else {
      target.navigate(true);
      ProjectUtil.focusProjectWindow(project, true);

      if (true) {
        return;
      }

      try {
        Client client = FlexUIDesignerApplicationManager.getInstance().getClient();
        client.qualifyExternalInlineStyleSource();
        AmfOutputStream out = client.getOutput();

        PsiElement psiElement = (PsiElement)target;
//        ClientFileManager clientFileManager = FlexUIDesignerApplicationManager.getInstance().getClientFileManager();
        //noinspection ConstantConditions
//        out.writeInt(clientFileManager.add(psiElement.getContainingFile().getVirtualFile()));


        out.flush();
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  public Navigatable find() {
    JSClass classElement = ((JSClass)JSResolveUtil.findClassByQName(parentFqn, scope));
    assert classElement != null;

    XmlTag rootTag = ((XmlFile)classElement.getNavigationElement().getContainingFile()).getRootTag();
    assert rootTag != null && rootTag.getDescriptor() != null && rootTag.getDescriptor() instanceof ClassBackedElementDescriptor;
    return find(rootTag, true);
  }

  private Navigatable find(XmlTag parent, boolean firstLevel) {
    for (XmlTag xmlTag : parent.getSubTags()) {
      String localName = xmlTag.getLocalName();
      if (firstLevel && ignoredRootTags.contains(localName)) {
        continue;
      }

      XmlElementDescriptor xmlTagDescriptor = xmlTag.getDescriptor();
      if (xmlTagDescriptor instanceof ClassBackedElementDescriptor) {
        Navigatable result;
        if (xmlTagDescriptor.getQualifiedName().equals(elementFqn)) {
          result = findTargetIfStyleDeclarationOwner(xmlTag);
          if (result != null) {
            return result;
          }
        }

        result = find(xmlTag, false);
        if (result != null) {
          return result;
        }
      }
    }

    return null;
  }

  private Navigatable findTargetIfStyleDeclarationOwner(XmlTag parent) {
    int foundCount = 0;
    Navigatable target = null;
    for (XmlAttribute attribute : parent.getAttributes()) {
      XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      // 8
      if (descriptor instanceof AnnotationBackedDescriptor) {
        String ourValue = properties.get(descriptor.getName());
        if (ourValue != null) {
          if (attribute.getDisplayValue().equals(ourValue)) {
            foundCount++;
            if (descriptor.getName().equals(targetStyleName)) {
              target = (Navigatable)attribute;
            }

            if (foundCount == properties.size()) {
              return target;
            }
          }
        }
      }
    }

    for (XmlTag tag : parent.getSubTags()) {
      XmlElementDescriptor descriptor = tag.getDescriptor();
      if (descriptor instanceof AnnotationBackedDescriptor &&
          ((PsiPresentableMetaData)descriptor).getTypeName().equals(FlexAnnotationNames.STYLE)) {
        String ourValue = properties.get(descriptor.getName());
        if (ourValue != null) {
          if (tag.getSubTags().length == 0 && tag.getValue().getTrimmedText().equals(ourValue)) {
            foundCount++;
            if (descriptor.getName().equals(targetStyleName)) {
              target = (Navigatable)tag;
            }

            if (foundCount == properties.size()) {
              return target;
            }
          }
        }
      }
    }

    return null;
  }
}
