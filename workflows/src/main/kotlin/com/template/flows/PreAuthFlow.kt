package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.PreAuthContract
import com.template.contracts.PreAuthContract.Companion.PRE_AUTH_CONTRACT_ID
import com.template.states.PreAuth
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


/*@InitiatingFlow
@StartableByRPC

class PreAuthFlow(val TPA: Party,
                  val Insurerer: Party,
                  val DoA: Int,
                  val EstDoS: Int,
                  val Diagnosis: String,
                  val Package: String,
                  val Doctor: String): FlowLogic<SignedTransaction>(){
    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val NetHospital = ourIdentity
        val BenIdentity = UniqueIdentifier()
        val linearIdPreAuth = UniqueIdentifier()
        val PreAuthState = PreAuth(BenIdentity, NetHospital, TPA, Insurerer, DoA, EstDoS, Diagnosis, Package, Doctor, "Pending", linearIdPreAuth, listOf(NetHospital, TPA))

        val txncommand = Command(PreAuthContract.commands.Request(), PreAuthState.participants.map { it.owningKey })

        val txnbuilder = TransactionBuilder(notary)
                .addOutputState(PreAuthState, PRE_AUTH_CONTRACT_ID)
                .addCommand(txncommand)

        txnbuilder.verify(serviceHub)

        val partiallysignedtxn = serviceHub.signInitialTransaction(txnbuilder)

        val counterSession = initiateFlow(TPA)

        val fullySignedtxn  =subFlow(CollectSignaturesFlow(partiallysignedtxn, setOf(counterSession)))

        return subFlow(FinalityFlow(fullySignedtxn))

    }
}*/



@InitiatingFlow
@StartableByRPC

class PreAuthFlow(val PreAuthState: PreAuth): FlowLogic<SignedTransaction>(){
    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        val txncommand = Command(PreAuthContract.commands.Request(), PreAuthState.participants.map { it.owningKey })

        val txnbuilder = TransactionBuilder(notary)
                .addOutputState(PreAuthState, PRE_AUTH_CONTRACT_ID)
                .addCommand(txncommand)

        txnbuilder.verify(serviceHub)

        val partiallysignedtxn = serviceHub.signInitialTransaction(txnbuilder)

        val counterSession = initiateFlow(PreAuthState.TPA)

        val fullySignedtxn  =subFlow(CollectSignaturesFlow(partiallysignedtxn, setOf(counterSession)))

        return subFlow(FinalityFlow(fullySignedtxn))

    }
}



@InitiatedBy(PreAuthFlow::class)
class PreAuthReqResponder(val CounterSesssion: FlowSession): FlowLogic<SignedTransaction>(){
    @Suspendable
    override fun call(): SignedTransaction {

        val signTransactionFlow = object : SignTransactionFlow(CounterSesssion){

            override fun checkTransaction(stx: SignedTransaction) = requireThat {

                val outputState = stx.tx.outputs.single().data
                "This must be a preAuth request transaction" using (outputState is PreAuth)
            }
        }
            return subFlow(signTransactionFlow)
    }


}