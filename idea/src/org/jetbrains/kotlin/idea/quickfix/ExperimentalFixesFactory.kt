/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import org.jetbrains.kotlin.cfg.pseudocode.containingDeclarationForPseudocode
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.toDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.resolve.AnnotationChecker
import org.jetbrains.kotlin.resolve.BindingContext.FQNAME_TO_CLASS_DESCRIPTOR

object ExperimentalFixesFactory : KotlinIntentionActionsFactory() {
    override fun doCreateActions(diagnostic: Diagnostic): List<IntentionAction> {
        val element = diagnostic.psiElement as? KtElement
        val containingDeclaration = element?.containingDeclarationForPseudocode ?: return emptyList()
        val annotationFqName = when (diagnostic) {
            is DiagnosticWithParameters1<*, *> -> diagnostic.a as? FqName
            is DiagnosticWithParameters2<*, *, *> -> diagnostic.a as? FqName
            else -> null
        } ?: return emptyList()

        val context = element.analyze()
        val annotationClassDescriptor = context[FQNAME_TO_CLASS_DESCRIPTOR, annotationFqName.toUnsafe()] ?: return emptyList()
        val applicableTargets = AnnotationChecker.applicableTargetSet(annotationClassDescriptor) ?: KotlinTarget.DEFAULT_TARGET_SET

        fun isApplicableTo(declaration: KtDeclaration, applicableTargets: Set<KotlinTarget>): Boolean {
            val actualTargetList = AnnotationChecker.getDeclarationSiteActualTargetList(
                declaration, declaration.toDescriptor() as? ClassDescriptor, context
            )
            return actualTargetList.any { it in applicableTargets }
        }

        val result = mutableListOf<IntentionAction>()
        if (isApplicableTo(containingDeclaration, applicableTargets)) {
            result.add(AddAnnotationFix(containingDeclaration, annotationFqName, " to '${containingDeclaration.name}'"))
        }
        if (containingDeclaration is KtCallableDeclaration) {
            val containingClassOrObject = containingDeclaration.containingClassOrObject
            if (containingClassOrObject != null && isApplicableTo(containingDeclaration, applicableTargets)) {
                result.add(
                    AddAnnotationFix(containingClassOrObject, annotationFqName, " to containing class '${containingClassOrObject.name}'")
                )
            }
        }

        return result
    }
}