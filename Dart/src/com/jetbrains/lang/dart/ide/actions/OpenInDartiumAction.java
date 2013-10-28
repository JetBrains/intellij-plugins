package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.browsers.Url;
import com.intellij.ide.browsers.UrlOpener;
import com.intellij.ide.browsers.WebBrowserService;
import com.intellij.ide.browsers.WebBrowserUrlProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlBundle;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;

/*
  This action is a temporary solution for Google announcement at Devoxx.
  In future this action, 'Open in Browser' (OpenFileInDefaultBrowserAction) and 'Preview file in' (WebOpenInAction) actions need to be merged into one.
  todo Vladimir Krivosheev, Alexander Doroshko
 */
public class OpenInDartiumAction extends AnAction {
  public OpenInDartiumAction() {
    super(DartBundle.message("open.in.dartium.action"), DartBundle.message("open.in.dartium.action.description"), DartIcons.Dartium_16);
  }

  public void update(final AnActionEvent e) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());

    final boolean available = psiFile instanceof XmlFile &&
                              DartSettingsUtil.getDartiumPath() != null &&
                              isHtmlFileWithDartScript(((XmlFile)psiFile));
    e.getPresentation().setEnabled(available);
    e.getPresentation().setVisible(available);
  }

  public void actionPerformed(final AnActionEvent e) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (!(psiFile instanceof XmlFile)) return;
    final String dartiumPath = DartSettingsUtil.getDartiumPath();
    if (dartiumPath == null || !isHtmlFileWithDartScript(((XmlFile)psiFile))) return;

    try {
      Url url = WebBrowserService.getInstance().getUrlToOpen(psiFile, (e.getModifiers() & InputEvent.SHIFT_MASK) != 0);
      if (url != null) {
        FileDocumentManager.getInstance().saveAllDocuments(); // for file and linked resources
        UrlOpener.launchBrowser(url.toExternalForm(), DartSettingsUtil.DARTIUM);
      }
    }
    catch (WebBrowserUrlProvider.BrowserException e1) {
      Messages.showErrorDialog(e1.getMessage(), XmlBundle.message("browser.error"));
    }
  }

  public static boolean isHtmlFileWithDartScript(final @NotNull XmlFile psiFile) {
    if (!HtmlUtil.isHtmlFile(psiFile)) return false;

    final String text = psiFile.getText();
    int i = -1;
    while ((i = text.indexOf("application/dart", i + 1)) != -1) {
      final PsiElement element = psiFile.findElementAt(i);
      if (element != null && element.getParent() instanceof XmlAttributeValue) {
        final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
        if (tag != null && "script".equalsIgnoreCase(tag.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
