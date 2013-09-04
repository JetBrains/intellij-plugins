package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by fedorkorotkov.
 */
public class CreateConstructorFix extends BaseCreateMethodsFix<DartComponent> {
  public CreateConstructorFix(DartClass dartClass) {
    super(dartClass);
  }

  @Override
  protected void processElements(Project project, Set<DartComponent> elementsToProcess) {
    anchor = doAddMethodsForOne(project, buildFunctionsText(elementsToProcess), anchor);
  }

  protected String buildFunctionsText(Set<DartComponent> elementsToProcess) {
    final StringBuilder result = new StringBuilder();
    result.append(myDartClass.getName()).append('(');
    for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
      DartComponent component = iterator.next();
      result.append("this.").append(component.getName());
      if (iterator.hasNext()) {
        result.append(',');
      }
    }
    result.append(")");
    result.append("{\n}\n");
    return result.toString();
  }

  @Override
  protected String buildFunctionsText(DartComponent e) {
    // ignore
    return null;
  }
}
