/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirField
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.impl.*
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.substitution.ChainedSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculatorWithJump
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneTypeUnsafe
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirResolvedTypeRefImpl
import org.jetbrains.kotlin.name.Name

class FirClassSubstitutionScope(
    private val session: FirSession,
    private val useSiteMemberScope: FirScope,
    scopeSession: ScopeSession,
    substitution: Map<FirTypeParameterSymbol, ConeKotlinType>
) : FirScope() {

    private val fakeOverrideFunctions = mutableMapOf<FirFunctionSymbol<*>, FirFunctionSymbol<*>>()
    private val fakeOverrideProperties = mutableMapOf<FirPropertySymbol, FirPropertySymbol>()
    private val fakeOverrideFields = mutableMapOf<FirFieldSymbol, FirFieldSymbol>()
    private val fakeOverrideAccessors = mutableMapOf<FirAccessorSymbol, FirAccessorSymbol>()

    private val substitutor = substitutorByMap(substitution)

    override fun processFunctionsByName(name: Name, processor: (FirFunctionSymbol<*>) -> ProcessorAction): ProcessorAction {
        useSiteMemberScope.processFunctionsByName(name) process@{ original ->

            val function = fakeOverrideFunctions.getOrPut(original) { createFakeOverrideFunction(original) }
            processor(function)
        }


        return super.processFunctionsByName(name, processor)
    }

    override fun processPropertiesByName(name: Name, processor: (FirCallableSymbol<*>) -> ProcessorAction): ProcessorAction {
        return useSiteMemberScope.processPropertiesByName(name) process@{ original ->
            when (original) {
                is FirPropertySymbol -> {
                    val property = fakeOverrideProperties.getOrPut(original) { createFakeOverrideProperty(original) }
                    processor(property)
                }
                is FirFieldSymbol -> {
                    val field = fakeOverrideFields.getOrPut(original) { createFakeOverrideField(original) }
                    processor(field)
                }
                is FirAccessorSymbol -> {
                    val accessor = fakeOverrideAccessors.getOrPut(original) { createFakeOverrideAccessor(original) }
                    processor(accessor)
                }
                else -> {
                    processor(original)
                }
            }
        }
    }

    override fun processClassifiersByName(name: Name, processor: (FirClassifierSymbol<*>) -> ProcessorAction): ProcessorAction {
        return useSiteMemberScope.processClassifiersByName(name, processor)
    }

    private val typeCalculator by lazy { ReturnTypeCalculatorWithJump(session, scopeSession) }

    private fun ConeKotlinType.substitute(): ConeKotlinType? {
        return substitutor.substituteOrNull(this)
    }

    private fun ConeKotlinType.substitute(substitutor: ConeSubstitutor): ConeKotlinType? {
        return substitutor.substituteOrNull(this)
    }

    private fun createFakeOverrideFunction(original: FirFunctionSymbol<*>): FirFunctionSymbol<*> {
        val member = when (original) {
            is FirNamedFunctionSymbol -> original.fir
            is FirConstructorSymbol -> return original
            else -> throw AssertionError("Should not be here")
        }

        val newTypeParameters = member.typeParameters.map { originalParameter ->
            FirTypeParameterImpl(
                originalParameter.source, originalParameter.session, originalParameter.name,
                FirTypeParameterSymbol(), originalParameter.variance, originalParameter.isReified
            ).apply {
                annotations += originalParameter.annotations
            }
        }

        val newSubstitutor =
            if (member.typeParameters.isEmpty())
                substitutor
            else {
                val substitutionMapForNewParameters = member.typeParameters.zip(newTypeParameters).map {
                    Pair(it.first.symbol, ConeTypeParameterTypeImpl(it.second.symbol.toLookupTag(), isNullable = false))
                }.toMap()
                ChainedSubstitutor(substitutor, substitutorByMap(substitutionMapForNewParameters))
            }

        val wereChangesInTypeParameters = fillBoundsForTypeParameters(newTypeParameters, member, newSubstitutor)

        val receiverType = member.receiverTypeRef?.coneTypeUnsafe<ConeKotlinType>()
        val newReceiverType = receiverType?.substitute(newSubstitutor)

        val returnType = typeCalculator.tryCalculateReturnType(member).type
        val newReturnType = returnType.substitute(newSubstitutor)

        val newParameterTypes = member.valueParameters.map {
            it.returnTypeRef.coneTypeUnsafe<ConeKotlinType>().substitute(newSubstitutor)
        }

        if (newReceiverType == null && newReturnType == null && newParameterTypes.all { it == null } && !wereChangesInTypeParameters) {
            return original
        }

        return createFakeOverrideFunction(
            session, member, original, newReceiverType, newReturnType, newParameterTypes, newTypeParameters
        )
    }

    private fun fillBoundsForTypeParameters(
        newTypeParameters: List<FirTypeParameterImpl>,
        member: FirSimpleFunction,
        newSubstitutor: ConeSubstitutor
    ): Boolean {
        var wereChangesInTypeParameters = false
        for ((newTypeParameter, oldTypeParameter) in newTypeParameters.zip(member.typeParameters)) {
            for (boundTypeRef in oldTypeParameter.bounds) {
                val typeForBound = boundTypeRef.coneTypeUnsafe<ConeKotlinType>()
                val substitutedBound = typeForBound.substitute(newSubstitutor)
                if (substitutedBound == null) {
                    newTypeParameter.bounds += boundTypeRef
                } else {
                    newTypeParameter.bounds += FirResolvedTypeRefImpl(boundTypeRef.source, substitutedBound)
                    wereChangesInTypeParameters = true
                }
            }
        }

        return wereChangesInTypeParameters
    }

    private fun createFakeOverrideProperty(original: FirPropertySymbol): FirPropertySymbol {
        val member = original.fir

        val receiverType = member.receiverTypeRef?.coneTypeUnsafe<ConeKotlinType>()
        val newReceiverType = receiverType?.substitute()

        val returnType = typeCalculator.tryCalculateReturnType(member).type
        val newReturnType = returnType.substitute()

        if (newReceiverType == null && newReturnType == null) {
            return original
        }

        return createFakeOverrideProperty(session, member, original, newReceiverType, newReturnType)
    }

    private fun createFakeOverrideField(original: FirFieldSymbol): FirFieldSymbol {
        val member = original.fir

        val returnType = typeCalculator.tryCalculateReturnType(member).type
        val newReturnType = returnType.substitute() ?: return original

        return createFakeOverrideField(session, member, original, newReturnType)
    }

    private fun createFakeOverrideAccessor(original: FirAccessorSymbol): FirAccessorSymbol {
        val member = original.fir

        val returnType = typeCalculator.tryCalculateReturnType(member).type
        val newReturnType = returnType.substitute()

        val newParameterTypes = member.valueParameters.map {
            it.returnTypeRef.coneTypeUnsafe<ConeKotlinType>().substitute()
        }

        if (newReturnType == null && newParameterTypes.all { it == null }) {
            return original
        }

        return createFakeOverrideAccessor(session, member, original, newReturnType, newParameterTypes)
    }

    companion object {
        private fun createFakeOverrideFunction(
            fakeOverrideSymbol: FirFunctionSymbol<FirSimpleFunction>,
            session: FirSession,
            baseFunction: FirSimpleFunction,
            newReceiverType: ConeKotlinType? = null,
            newReturnType: ConeKotlinType? = null,
            newParameterTypes: List<ConeKotlinType?>? = null,
            newTypeParameters: List<FirTypeParameter>? = null
        ): FirSimpleFunction {
            return with(baseFunction) {
                // TODO: consider using here some light-weight functions instead of pseudo-real FirMemberFunctionImpl
                // As second alternative, we can invent some light-weight kind of FirRegularClass
                FirSimpleFunctionImpl(
                    source,
                    session,
                    baseFunction.returnTypeRef.withReplacedConeType(newReturnType),
                    baseFunction.receiverTypeRef?.withReplacedConeType(newReceiverType),
                    name,
                    baseFunction.status,
                    fakeOverrideSymbol
                ).apply {
                    resolvePhase = baseFunction.resolvePhase
                    valueParameters += baseFunction.valueParameters.zip(
                        newParameterTypes ?: List(baseFunction.valueParameters.size) { null }
                    ) { valueParameter, newType ->
                        with(valueParameter) {
                            FirValueParameterImpl(
                                source,
                                session,
                                this.returnTypeRef.withReplacedConeType(newType),
                                name,
                                FirVariableSymbol(valueParameter.symbol.callableId),
                                defaultValue,
                                isCrossinline,
                                isNoinline,
                                isVararg
                            )
                        }
                    }

                    // TODO: Fix the hack for org.jetbrains.kotlin.fir.backend.Fir2IrVisitor.addFakeOverrides
                    // We might have added baseFunction.typeParameters in case new ones are null
                    // But it fails at org.jetbrains.kotlin.ir.AbstractIrTextTestCase.IrVerifier.elementsAreUniqueChecker
                    // because it shares the same declarations of type parameters between two different two functions
                    if (newTypeParameters != null) {
                        typeParameters += newTypeParameters
                    }
                }
            }
        }

        fun createFakeOverrideFunction(
            session: FirSession,
            baseFunction: FirSimpleFunction,
            baseSymbol: FirNamedFunctionSymbol,
            newReceiverType: ConeKotlinType? = null,
            newReturnType: ConeKotlinType? = null,
            newParameterTypes: List<ConeKotlinType?>? = null,
            newTypeParameters: List<FirTypeParameter>? = null
        ): FirNamedFunctionSymbol {
            val symbol = FirNamedFunctionSymbol(baseSymbol.callableId, true, baseSymbol)
            createFakeOverrideFunction(
                symbol, session, baseFunction, newReceiverType, newReturnType, newParameterTypes, newTypeParameters
            )
            return symbol
        }

        fun createFakeOverrideProperty(
            session: FirSession,
            baseProperty: FirProperty,
            baseSymbol: FirPropertySymbol,
            newReceiverType: ConeKotlinType? = null,
            newReturnType: ConeKotlinType? = null
        ): FirPropertySymbol {
            val symbol = FirPropertySymbol(baseSymbol.callableId, true, baseSymbol)
            with(baseProperty) {
                FirPropertyImpl(
                    source,
                    session,
                    baseProperty.returnTypeRef.withReplacedConeType(newReturnType),
                    baseProperty.receiverTypeRef?.withReplacedConeType(newReceiverType),
                    name,
                    null,
                    null,
                    isVar,
                    symbol,
                    false,
                    baseProperty.status
                ).apply {
                    resolvePhase = baseProperty.resolvePhase
                }
            }
            return symbol
        }

        fun createFakeOverrideField(
            session: FirSession,
            baseField: FirField,
            baseSymbol: FirFieldSymbol,
            newReturnType: ConeKotlinType? = null
        ): FirFieldSymbol {
            val symbol = FirFieldSymbol(baseSymbol.callableId)
            with(baseField) {
                FirFieldImpl(
                    source, session,
                    baseField.returnTypeRef.withReplacedConeType(newReturnType),
                    name, symbol, isVar, baseField.status
                ).apply {
                    resolvePhase = baseField.resolvePhase
                }
            }
            return symbol
        }

        fun createFakeOverrideAccessor(
            session: FirSession,
            baseFunction: FirSimpleFunction,
            baseSymbol: FirAccessorSymbol,
            newReturnType: ConeKotlinType? = null,
            newParameterTypes: List<ConeKotlinType?>? = null
        ): FirAccessorSymbol {
            val symbol = FirAccessorSymbol(baseSymbol.callableId, baseSymbol.accessorId)
            createFakeOverrideFunction(symbol, session, baseFunction, null, newReturnType, newParameterTypes)
            return symbol
        }
    }
}


fun FirTypeRef.withReplacedConeType(newType: ConeKotlinType?): FirResolvedTypeRef {
    require(this is FirResolvedTypeRef)
    if (newType == null) return this

    return FirResolvedTypeRefImpl(source, newType).apply {
        annotations += this@withReplacedConeType.annotations
    }

}
