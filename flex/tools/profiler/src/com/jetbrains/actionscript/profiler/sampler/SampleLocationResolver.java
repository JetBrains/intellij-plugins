package com.jetbrains.actionscript.profiler.sampler;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 15.11.10
 * Time: 10:06
 */
public class SampleLocationResolver implements Navigatable {
  private @Nullable VirtualFile resolvedFile;
  private @Nullable PsiElement resolvedElement;
  private final GlobalSearchScope scope;
  private final FrameInfo frameInfo;

  public SampleLocationResolver(FrameInfo frameInfo, GlobalSearchScope scope) {
    this.frameInfo = frameInfo;
    this.scope = scope;
    // resolve via qname package::Class/((get|set) )?method name()
    PsiElement element = JSResolveUtil.findClassByQName(frameInfo.getQName(), scope);
    if (element instanceof XmlBackedJSClassImpl) {
      final PsiElement candidateElement = frameInfo.findFunctionOrField((XmlBackedJSClassImpl)element);
      if (candidateElement != null) element = candidateElement;
    }
    else {
      if (element != null) element = element.getNavigationElement();
      if (element instanceof JSClass) {
        final PsiElement candidateElement = frameInfo.findFunctionOrField((JSClass)element);
        if (candidateElement != null) element = candidateElement;
      }
    }

    if (element instanceof Navigatable) {
      resolvedElement = element;
    }

    boolean resolved = resolvedFile != null || resolvedElement != null;
    if (!resolved && frameInfo.getFileName() != null) {
      // try navigate from debug info [path;package path;file:line] , btw it can be invalid for SDK
      String dir = frameInfo.getFileDirectory();
      String packageName = frameInfo.getPackageName();
      String filename = frameInfo.getFileName();

      if (packageName != null && packageName.length() > 0) {
        dir += File.separatorChar + packageName.replace('.', File.separatorChar);
      }
      dir += File.separatorChar;
      final int endIndex = filename != null ? filename.indexOf(':') : -1;
      dir += endIndex != -1 ? filename.substring(0, endIndex) : filename;
      VirtualFile relativeFile = VfsUtil.findRelativeFile(dir, null);

      if (relativeFile == null && packageName != null && filename != null) {
        String className = packageName.replace('\\', '.').replace('/', '.');
        if (className.length() > 0) className += ".";
        className += filename.substring(0, filename.indexOf('.'));

        PsiElement classElement = JSResolveUtil.findClassByQName(className, scope);
        if (classElement != null) classElement = classElement.getNavigationElement();
        if (classElement != null && classElement.isWritable()) {
          relativeFile = classElement.getContainingFile().getVirtualFile();
        }
      }

      if (relativeFile != null) {
        resolvedFile = relativeFile;
        resolved = true;
      }
    }
  }

  @Override
  public void navigate(boolean requestFocus) {
    if (resolvedElement != null) {
      ((Navigatable)resolvedElement).navigate(true);
    }
    else if (resolvedFile != null && scope.getProject() != null) {
      int line = Math.max(0, frameInfo.getFileLine() - 1);
      new OpenFileDescriptor(scope.getProject(), resolvedFile, line, 0).navigate(true);
    }
  }

  @Override
  public boolean canNavigate() {
    return canNavigateToSource();
  }

  @Override
  public boolean canNavigateToSource() {
    return resolvedElement != null || resolvedFile != null;
  }
}
