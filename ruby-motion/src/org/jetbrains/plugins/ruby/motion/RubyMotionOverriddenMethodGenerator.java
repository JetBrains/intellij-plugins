/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
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
package org.jetbrains.plugins.ruby.motion;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.MemberChooserObjectBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Function;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.OverriddenMethodGenerator;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyElementFactory;
import org.jetbrains.plugins.ruby.ruby.sdk.LanguageLevel;
import org.jetbrains.plugins.ruby.settings.RubyCodeStyleSettings;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionOverriddenMethodGenerator extends OverriddenMethodGenerator {
  @Override
  public Type getSupportedType() {
    return Type.INSTANCE_METHOD;
  }

  @Nullable
  @Override
  public PsiElement generateOverriddenMethod(ClassMember baseMember, @NotNull final LanguageLevel languageLevel) {
    if (baseMember instanceof FunctionMember) {
      final FunctionSymbol symbol = ((FunctionMember)baseMember).getMethodSymbol();
      return generateObjCOverride(symbol.getProject(), symbol.getFunction(), languageLevel);
    }
    return null;
  }

  private static PsiElement generateObjCOverride(Project project, Function function, @NotNull final LanguageLevel languageLevel) {
    final StringBuilder text = new StringBuilder();
    text.append("def ");
    text.append(MotionSymbolUtil.getSelectorNames(function).get(0));
    final List<Pair<String,String>> arguments = function.getArguments();
    if (arguments.size() > 0) {
      final RubyCodeStyleSettings settings = CodeStyleSettingsManager.getSettings(project).getCustomSettings(RubyCodeStyleSettings.class);
      final boolean generateParenthesesAroundArguments = settings.PARENTHESES_AROUND_METHOD_ARGUMENTS;
      text.append(generateParenthesesAroundArguments ? "(" : " ");
      text.append(arguments.get(0).first);
      final String[] namedArguments = function.getName().split(":");
      for (int i = 1; i < namedArguments.length; i++) {
        String argument = namedArguments[i];
        text.append(", ").append(argument).append(":").append(arguments.get(i).first);
      }
      text.append(generateParenthesesAroundArguments ? ")" : "");
    }
    text.append("\n").append("  super\nend\n");
    return RubyElementFactory.createElementFromText(project, text.toString(), languageLevel);
  }

  @Nullable
  @Override
  public ClassMember createMemberToOverride(final Symbol methodSymbol) {
    if (methodSymbol instanceof FunctionSymbol) {
      final FunctionSymbol symbol = (FunctionSymbol)methodSymbol;
      final Function function = symbol.getFunction();
      if (!function.isClassMethod() && symbol.getParentSymbol() != null) {
        return new FunctionMember(symbol);
      }
    }
    return null;
  }

  @Override
  public boolean isContainerNode(MemberChooserObject member) {
    return member instanceof ObjCClass;
  }

  private static class FunctionMember extends MemberChooserObjectBase implements ClassMember{
    private final FunctionSymbol myMethodSymbol;

    FunctionMember(FunctionSymbol methodSymbol) {
      super(methodSymbol.getFunction().getName(), AllIcons.Nodes.Method);
      myMethodSymbol = methodSymbol;
    }

    public FunctionSymbol getMethodSymbol() {
      return myMethodSymbol;
    }

    @Override
    public MemberChooserObject getParentNodeDelegate() {
      return new ObjCClass(myMethodSymbol.getParentSymbol());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null) return false;
      if (obj.getClass() != FunctionMember.class) return false;

      return ((FunctionMember)obj).myMethodSymbol.equals(myMethodSymbol);
    }

    @Override
    public int hashCode() {
      return myMethodSymbol.hashCode();
    }
  }

  private static class ObjCClass extends MemberChooserObjectBase implements ClassMember {
    private final Symbol myClazz;

    ObjCClass(Symbol clazz) {
      super(clazz.getName(), AllIcons.Nodes.Class);
      myClazz = clazz;
    }

    @Override
    public MemberChooserObject getParentNodeDelegate() {
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null) return false;
      if (obj.getClass() != ObjCClass.class) return false;

      return ((ObjCClass)obj).myClazz.equals(myClazz);
    }

    @Override
    public int hashCode() {
      return myClazz.hashCode();
    }
  }
}
