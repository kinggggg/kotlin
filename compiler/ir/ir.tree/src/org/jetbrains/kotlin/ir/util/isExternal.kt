/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.MemberDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor

fun MemberDescriptor.isEffectivelyExternal(): Boolean {
    if (isExternal) return true

    if (this is PropertyAccessorDescriptor && correspondingProperty.isEffectivelyExternal()) {
        return true
    }

    val containingDeclaration = this.containingDeclaration
    if (containingDeclaration is MemberDescriptor && containingDeclaration.isEffectivelyExternal()) {
        return true
    }

    if (this is CallableMemberDescriptor && overriddenDescriptors.isNotEmpty()) {
        // If a member overrides an external and non-external member, it's an error.
        // So it's enough to check only first overridden descriptor, regardless of the order or anything.
        if (overriddenDescriptors.first().isEffectivelyExternal()) {
            return true
        }
    }

    return false
}