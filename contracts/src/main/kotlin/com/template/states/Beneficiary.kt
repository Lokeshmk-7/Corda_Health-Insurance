package com.template.states

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class Beneficiary(override val linearId: UniqueIdentifier = UniqueIdentifier(), val AadharID: String, val RationId: String,val Registerer: Party, val TPA: Party, override val participants: List<AbstractParty> = listOf(Registerer, TPA)):LinearState