/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.resolve.calls.components.isVararg
import org.jetbrains.kotlin.resolve.isInlineClassType

class VarargsOnParametersOfInlineClassTypeChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtFunction) return
        if (descriptor !is FunctionDescriptor) return

        for (valueParameter in descriptor.valueParameters) {
            val varargElementType = valueParameter.varargElementType ?: continue
            if (valueParameter.isVararg && varargElementType.isInlineClassType()) {
                val ktParameter = declaration.valueParameters[valueParameter.index] ?: continue
                context.trace.report(Errors.VARARG_ON_INLINE_CLASS_TYPE.on(ktParameter))
            }
        }
    }
}
