package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.fixes.CreateClassIntentionWithCallback;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlElementDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class CreateFlexSkinIntention implements CreateClassIntentionWithCallback {
  private PsiElement myElement;

  private String myPackageName;
  private String mySkinName;
  private boolean myIdentifierIsValid;

  private Consumer<String> myCreatedClassFqnConsumer;

  public CreateFlexSkinIntention(final String skinFqn, final @NotNull PsiElement element) {
    myElement = element;

    mySkinName = StringUtil.getShortName(skinFqn);
    myPackageName = StringUtil.getPackageName(skinFqn);

    myIdentifierIsValid =
      LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage()).isIdentifier(mySkinName, null);
  }

  @NotNull
  public String getText() {
    return FlexBundle.message("create.skin", mySkinName);
  }

  @NotNull
  public String getFamilyName() {
    return CodeInsightBundle.message("create.file.family");
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return myIdentifierIsValid && myElement.isValid();
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final JSClass hostComponentClass = getHostComponentClass();

    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return;
    }

    final String packageName;
    final String hostComponent;
    final PsiDirectory targetDirectory;
    final String defaultHostComponent = hostComponentClass == null ? "" : hostComponentClass.getQualifiedName();
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      final CreateFlexSkinDialog dialog = new CreateFlexSkinDialog(module, mySkinName, myPackageName,
                                                                   defaultHostComponent,
                                                                   myElement.getContainingFile());
      dialog.show();
      if (!dialog.isOK()) {
        return;
      }
      packageName = dialog.getPackageName();
      hostComponent = dialog.getHostComponent();
      targetDirectory = dialog.getTargetDirectory();
    }
    else {
      packageName = myPackageName;
      hostComponent = defaultHostComponent;
      targetDirectory = ApplicationManager.getApplication().runWriteAction(new Computable<PsiDirectory>() {
        @Override
        public PsiDirectory compute() {
          return CreateClassOrInterfaceAction.findOrCreateDirectory(packageName, file);
        }
      });
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          final String skinFileName = mySkinName + JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT;
          final PsiFile newFile = targetDirectory.createFile(skinFileName);

          final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
          final Document document = psiDocumentManager.getDocument(newFile);
          document.setText(getSkinContent(hostComponent));
          psiDocumentManager.commitDocument(document);
          CodeStyleManager.getInstance(project).reformat(newFile);
          FileEditorManager.getInstance(project).openFile(newFile.getVirtualFile(), true);

          if (myCreatedClassFqnConsumer != null) {
            myCreatedClassFqnConsumer.consume(packageName + (packageName.isEmpty() ? "" : ".") + mySkinName);
          }
        }
        catch (IncorrectOperationException e) {
          Messages.showErrorDialog(project, e.getMessage(), getText());
        }
      }
    });
  }

  private String getSkinContent(final String hostComponent) {
    final StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    // TODO insert MXML file header
    builder.append("<s:Skin xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:s=\"library://ns.adobe.com/flex/spark\">\n");
    builder.append("\n");

    if (!StringUtil.isEmpty(hostComponent)) {
      builder.append("<fx:Metadata>\n");
      builder.append("[HostComponent(\"").append(hostComponent).append("\")]\n");
      builder.append("</fx:Metadata>\n");
      builder.append("\n");
    }

    final PsiElement element = JSResolveUtil.findClassByQName(hostComponent, myElement);
    if (element instanceof JSClass) {
      final JSClass jsClass = (JSClass)element;
      final Collection<String> skinStates = getSkinStates(jsClass);
      if (!skinStates.isEmpty()) {
        builder.append("<s:states>\n");
        for (final String skinState : skinStates) {
          builder.append("<s:State name=\"").append(skinState).append("\"/>\n");
        }
        builder.append("</s:states>\n");
        builder.append("\n");
      }
    }

    builder.append("</s:Skin>\n");
    return builder.toString();
  }

  private static Collection<String> getSkinStates(final JSClass jsClass) {
    final Collection<String> skinStates = new ArrayList<String>();
    appendSkinStates(skinStates, jsClass, new THashSet<JSClass>());
    return skinStates;
  }

  private static void appendSkinStates(final Collection<String> skinStates, final JSClass jsClass, final Set<JSClass> visited) {
    visited.add(jsClass);

    final JSAttributeList attributeList = jsClass.getAttributeList();
    if (attributeList != null) {
      final JSAttribute[] attributes = attributeList.getAttributesByName("SkinState");
      for (final JSAttribute attribute : attributes) {
        final JSAttributeNameValuePair pair = attribute.getValueByName(null);
        if (pair != null) {
          final String state = pair.getSimpleValue();
          if (!skinStates.contains(state)) {
            skinStates.add(state);
          }
        }
      }
    }
    for (final JSClass superClass : jsClass.getSuperClasses()) {
      if (!visited.contains(superClass)) {
        appendSkinStates(skinStates, superClass, visited);
      }
    }
  }

  public boolean startInWriteAction() {
    return false;
  }

  @Nullable
  private JSClass getHostComponentClass() {
    final XmlTag tag = myElement instanceof XmlTag
                       ? ((XmlTag)myElement).getParentTag()
                       : myElement instanceof XmlAttributeValue ? (XmlTag)myElement.getParent().getParent() : null;
    final XmlElementDescriptor descriptor = tag == null ? null : tag.getDescriptor();
    if (descriptor instanceof ClassBackedElementDescriptor) {
      final PsiElement declaration = descriptor.getDeclaration();
      if (declaration instanceof JSClass) {
        return (JSClass)declaration;
      }
    }
    return null;
  }

  @Override
  public void setCreatedClassFqnConsumer(final Consumer<String> consumer) {
    myCreatedClassFqnConsumer = consumer;
  }
}
