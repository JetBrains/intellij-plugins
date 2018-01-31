package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.structureView.JSStructureItemPresentation;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.navigation.PsiElementNavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import java.util.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FlexXmlBackedMembersIndex extends ScalarIndexExtension<String> {
  private static final int INDEX_VERSION = 1;

  public static final ID<String, Void> NAME = ID.create("FlexXmlBackedMembersIndex");

  @Override
  @NotNull
  public ID<String, Void> getName() {
    return NAME;
  }

  @Override
  @NotNull
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return new DataIndexer<String, Void, FileContent>() {

      @Override
      @NotNull
      public Map<String, Void> map(@NotNull FileContent inputData) {
        final XmlFile file = (XmlFile)inputData.getPsiFile();

        final Map<String, Void> result = new HashMap<>();
        process(file, element -> {
          String name = getName(element);
          if (name != null) {
            result.put(name, null);
          }
        }, false);
        return result;
      }
    };
  }

  private static void process(XmlFile file, final Consumer<PsiElement> consumer, boolean isPhysical) {
    visitScriptTagInjectedFilesForIndexing(file, new JSResolveUtil.JSInjectedFilesVisitor() {
      @Override
      protected void process(JSFile file) {
        ResolveState state = ResolveState.initial().put(XmlBackedJSClass.PROCESS_XML_BACKED_CLASS_MEMBERS_HINT, Boolean.TRUE);
        file.processDeclarations(new ResolveProcessor(null) {
          {
            setSkipImplicitDeclarations(true);
          }

          @Override
          public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
            if (element instanceof JSFunction || element instanceof JSVariable) {
              consumer.consume(element);
            }
            return true;
          }
        }, state, null, file);
      }
    }, isPhysical);

    file.accept(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        if (element instanceof XmlTag) {
          XmlTag tag = (XmlTag)element;
          if (tag.getAttributeValue("id") != null && MxmlJSClass.canBeReferencedById(tag)) {
            consumer.consume(tag);
          }
        }
        super.visitElement(element);
      }
    });
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(JavaScriptSupportLoader.getMxmlFileType()) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        return JavaScriptSupportLoader.isMxmlOrFxgFile(file);
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return JSFileElementType.getVersion() + INDEX_VERSION;
  }

  public static Collection<String> getSymbolNames(Project project) {
    return FileBasedIndex.getInstance().getAllKeys(NAME, project);
  }

  public static Collection<NavigationItem> getItemsByName(final String name, Project project) {
    Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, name, GlobalSearchScope.projectScope(project));
    final Collection<NavigationItem> result = new ArrayList<>();
    for (VirtualFile vFile : files) {
      PsiFile file = PsiManager.getInstance(project).findFile(vFile);
      if (!(file instanceof XmlFile)) {
        continue;
      }
      process((XmlFile)file, element -> {
        if (name.equals(getName(element))) {
          if (element instanceof JSNamedElement) {
            result.add((JSNamedElement)element);
          }
          else {
            XmlAttribute id = ((XmlTag)element).getAttribute("id");
            if (id != null) {
              XmlAttributeValue valueElement = id.getValueElement();
              PsiElement[] children;
              if (valueElement != null && (children = valueElement.getChildren()).length == 3) {
                result.add(new TagNavigationItem(children[1], name));
              }
            }
          }
        }
      }, true);
    }
    return result;
  }

  @Nullable
  private static String getName(PsiElement element) {
    if (element instanceof XmlTag) {
      return ((XmlTag)element).getAttributeValue("id");
    }
    else {
      return ((JSNamedElement)element).getName();
    }
  }

  private static class TagNavigationItem extends FakePsiElement implements PsiElementNavigationItem, ItemPresentation {
    private final PsiElement myElement;
    private final String myName;

    public TagNavigationItem(PsiElement element, String name) {
      myElement = element;
      myName = name;
    }

    @Override
    public String getName() {
      return myName;
    }

    @Override
    public ItemPresentation getPresentation() {
      return this;
    }

    @Override
    public PsiElement getTargetElement() {
      return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
      ((Navigatable)myElement).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
      return ((Navigatable)myElement).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
      return ((Navigatable)myElement).canNavigateToSource();
    }

    @Override
    public String getPresentableText() {
      return getName();
    }

    @Override
    public String getLocationString() {
      PsiFile file = myElement.getContainingFile();
      String packageName = JSResolveUtil.getExpectedPackageNameFromFile(file.getVirtualFile(), myElement.getProject());
      StringBuilder result = new StringBuilder();
      result.append(StringUtil.getQualifiedName(packageName, FileUtil.getNameWithoutExtension(file.getName())));
      result.append("(").append(file.getName()).append(")");
      return result.toString();
    }

    @Override
    public Icon getIcon(boolean open) {
      return JSStructureItemPresentation.getIcon(PsiTreeUtil.getParentOfType(myElement, XmlTag.class));
    }
    @Override
    public PsiElement getParent() {
      return myElement.getParent();
    }

    @Override
    public PsiFile getContainingFile() {
      return myElement.getContainingFile();
    }
  }


  // We use light version of visitInjectedFiles that process injections only for Script tags,
  // all other tags / attributes need cross file resolve
  public static void visitScriptTagInjectedFilesForIndexing(XmlFile file, final XmlBackedJSClassImpl.InjectedFileVisitor visitor, boolean physical) {
    new XmlBackedJSClassImpl.InjectedScriptsVisitor(XmlBackedJSClassFactory.getRootTag(file), MxmlJSClassProvider.getInstance(), false, true, visitor, physical).go();
  }
}
