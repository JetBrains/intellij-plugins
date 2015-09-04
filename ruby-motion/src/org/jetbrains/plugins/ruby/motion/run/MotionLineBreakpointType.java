package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrLineBreakpointType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;

/**
 * @author Dennis.Ushakov
 */
public class MotionLineBreakpointType extends CidrLineBreakpointType {
  public MotionLineBreakpointType() {
    super(MotionLineBreakpointType.class.getName(), "RubyMotion Line Breakpoints");
  }

  @Override
  public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    return psiFile instanceof RFile && RubyMotionUtil.getInstance().hasMacRubySupport(psiFile);
  }
}
