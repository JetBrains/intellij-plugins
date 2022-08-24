package com.jetbrains.plugins.meteor.spacebars.templates;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsFileType;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsLanguageDialect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MeteorTemplateIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> METEOR_TEMPLATES_INDEX = ID.create("MeteorTemplateIndex");
  private static final int INDEX_VERSION = 2;
  public static final String TEMPLATE_TAG = "template";
  public static final String NAME_ATTRIBUTE = "name";


  private static final class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      Map<String, Void> result = new HashMap<>();
      visitTemplateTags(inputData.getPsiFile(), tag -> {
        String name = tag.getAttributeValue(NAME_ATTRIBUTE);
        if (name != null) {
          result.put(name, null);
        }
      });

      return result;
    }
  }

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return METEOR_TEMPLATES_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return new MyDataIndexer();
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(HtmlFileType.INSTANCE, SpacebarsFileType.SPACEBARS_INSTANCE) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        return super.acceptInput(file) && !(file.getFileSystem() instanceof JarFileSystem);
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return INDEX_VERSION;
  }

  @NotNull
  public static ArrayList<PsiElement> findDeclarations(final String key, final PsiManager psiManager, GlobalSearchScope scope) {
    final ArrayList<PsiElement> result = new ArrayList<>();
    FileBasedIndex.getInstance().getFilesWithKey(METEOR_TEMPLATES_INDEX, ContainerUtil.newHashSet(key), file -> {
      PsiFile psiFile = psiManager.findFile(file);

      visitTemplateTags(psiFile, tag -> {
        XmlAttribute name = tag.getAttribute(NAME_ATTRIBUTE);
        if (name != null && key.equals(name.getValue())) {
          result.add(name.getValueElement());
        }
      });
      return false;
    }, scope);

    return result;
  }

  public static Collection<String> getKeys(final Project project) {
    Collection<String> keys = FileBasedIndex.getInstance().getAllKeys(METEOR_TEMPLATES_INDEX, project);

    return ContainerUtil.filter(keys, s -> {
      List<Void> values = FileBasedIndex.getInstance().getValues(METEOR_TEMPLATES_INDEX, s, GlobalSearchScope.allScope(project));
      return !values.isEmpty();
    });
  }

  private static void visitTemplateTags(PsiFile psiFile, final Consumer<XmlTag> processor) {
    FileViewProvider viewProvider = psiFile.getViewProvider();
    PsiFile spacebarsFile = viewProvider.getPsi(SpacebarsLanguageDialect.INSTANCE);
    if (spacebarsFile == null) {
      return;
    }

    PsiFile htmlPsiFile = viewProvider.getPsi(HTMLLanguage.INSTANCE);
    if (htmlPsiFile == null) {
      return;
    }

    htmlPsiFile.acceptChildren(new XmlRecursiveElementWalkingVisitor() {
      @Override
      public void visitXmlTag(@NotNull XmlTag tag) {
        if (TEMPLATE_TAG.equalsIgnoreCase(tag.getName())) {

          processor.consume(tag);
          //now template tag cannot have inner templates
          return;
        }
        super.visitXmlTag(tag);
      }
    });
  }
}
