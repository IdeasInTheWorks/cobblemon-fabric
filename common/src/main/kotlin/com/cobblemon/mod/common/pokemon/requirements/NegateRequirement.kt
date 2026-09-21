package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon

class NegateRequirement(val inner: Requirement) : Requirement {

    override fun check(pokemon: Pokemon) = !inner.check(pokemon)

    companion object {
        const val ADAPTER_VARIANT = "negate"
    }
}