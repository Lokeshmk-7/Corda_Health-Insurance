package com.template.states

import com.template.contracts.PreAuthContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(PreAuthContract :: class)
data class PreAuth(
    val BenIdentity: UniqueIdentifier,
    val NetHospital: Party,
    val TPA: Party,
    val Insurer: Party,
    val DoA: Int,
    val EstDoS: Int,
    val Diagnosis: String,
    val Package: String,
    val Doctor: String,
    val Status: String,
    override val linearId: UniqueIdentifier):LinearState{
    override val participants: List<Party> get() = listOf(NetHospital, TPA)
}