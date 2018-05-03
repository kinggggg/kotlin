/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.script

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.NotFoundClasses
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.findNonGenericClassAcrossDependencies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

fun KotlinScriptDefinition.getScriptParameters(module: ModuleDescriptor): List<ScriptParameter> =
    constructorParameters.map { ScriptParameter(Name.identifier(it.name!!), it.type.kotlinTypeInModule(module)) }
            ?: emptyList()

fun getKotlinTypeByKClass(module: ModuleDescriptor, kClass: KClass<out Any>): KotlinType =
    getKotlinTypeByFqName(
        module,
        kClass.qualifiedName ?: throw RuntimeException("Cannot get FQN from $kClass")
    )

private fun getKotlinTypeByFqName(module: ModuleDescriptor, fqName: String): KotlinType =
    module.findNonGenericClassAcrossDependencies(
        ClassId.topLevel(FqName(fqName)),
        NotFoundClasses(LockBasedStorageManager.NO_LOCKS, module)
    ).defaultType

// TODO: support star projections
// TODO: support annotations on types and type parameters
// TODO: support type parameters on types and type projections
internal fun getKotlinTypeByKType(module: ModuleDescriptor, kType: KType): KotlinType {
    val classifier = kType.classifier
    if (classifier !is KClass<*>)
        throw java.lang.UnsupportedOperationException("Only classes are supported as parameters in script template: $classifier")

    val type = getKotlinTypeByKClass(module, classifier)
    val typeProjections = kType.arguments.map { getTypeProjection(module, it) }
    val isNullable = kType.isMarkedNullable

    return KotlinTypeFactory.simpleType(Annotations.EMPTY, type.constructor, typeProjections, isNullable)
}

private fun getTypeProjection(module: ModuleDescriptor, kTypeProjection: KTypeProjection): TypeProjection {
    val kType = kTypeProjection.type ?: throw java.lang.UnsupportedOperationException("Star projections are not supported")

    val type = getKotlinTypeByKType(module, kType)

    val variance = when (kTypeProjection.variance) {
        KVariance.IN -> Variance.IN_VARIANCE
        KVariance.OUT -> Variance.OUT_VARIANCE
        KVariance.INVARIANT -> Variance.INVARIANT
        null -> throw java.lang.UnsupportedOperationException("Star projections are not supported")
    }

    return TypeProjectionImpl(variance, type)
}