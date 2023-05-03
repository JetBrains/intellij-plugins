package com.thoughtworks.gauge.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.kotlin.asJava.LightClassUtil;
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass;
import org.jetbrains.kotlin.asJava.elements.KtLightMethodImpl;
import org.jetbrains.kotlin.psi.KtNamedFunction;

public final class KtUtil {

    public static boolean isKtClass(PsiElement element) {
        try {
            return Class.forName("org.jetbrains.kotlin.asJava.classes.KtUltraLightClass").isInstance(element);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isKtFunction(PsiElement element) {
        try {
            return Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction").isInstance(element);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isKtMethod(PsiElement element) {
        try {
            return Class.forName("org.jetbrains.kotlin.asJava.classes.KtUltraLightMethod")
                        .isInstance(element);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isClassUsed(final PsiElement element) {
        if (!isKtClass(element)) {
            return false;
        }
        return Inner.isClassUsed(element);
    }

    public static List<String> getGaugeStepAnnotationValues(PsiElement element) {
        return Inner.getGaugeStepAnnotationValues(element);
    }

    public static final class Inner {

        private Inner() {
        }

        private static boolean isClassUsed(final PsiElement element) {
            if (!isKtClass(element)) {
                return false;
            }
            return Arrays.stream(((KtUltraLightClass) element).getAllMethods())
                         .anyMatch(function ->
                                 StepUtil.getGaugeStepAnnotationValues(function).size() > 0 || HookUtil.isHook(function));
        }


        private static List<String> getGaugeStepAnnotationValues(PsiElement element) {
            final PsiAnnotation[] annotations;
            if (isKtFunction(element)) {
                annotations = toPsiMethod((KtNamedFunction) element).getAnnotations();
            } else if (isKtMethod(element)) {
                annotations = ((KtLightMethodImpl) element).getAnnotations();
            } else {
                annotations = PsiAnnotation.EMPTY_ARRAY;
            }
            return Arrays.stream(annotations)
                         .map(StepUtil::getGaugeStepAnnotationValues)
                         .flatMap(Collection::stream)
                         .collect(Collectors.toList());
        }

        private static PsiMethod toPsiMethod(KtNamedFunction function) {
            return LightClassUtil.INSTANCE.getLightClassMethod(function);
        }

    }
}
