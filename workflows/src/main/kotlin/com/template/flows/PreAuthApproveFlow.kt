package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.PreAuthContract
import com.template.contracts.PreAuthContract.Companion.PRE_AUTH_CONTRACT_ID
import com.template.contracts.TemplateContract
import com.template.states.PreAuth
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException
import java.util.*


@InitiatingFlow
@StartableByRPC
class PreAuthApproveFlow( val PreAuthId: String) : FlowLogic<SignedTransaction>(){
@Suspendable
    override fun call(): SignedTransaction {

        val preAuthLinearId: UniqueIdentifier;
        preAuthLinearId = UniqueIdentifier.fromString(PreAuthId)

        val stateAndRefPreAuth: StateAndRef<PreAuth> = serviceHub.vaultService.queryBy<PreAuth>().states.
                singleOrNull{ it.state.data.Status == "Pending" && it.state.data.linearId == preAuthLinearId} ?: throw FlowException("No state found in vault")

        val preAuthState = stateAndRefPreAuth.state.data

        if (ourIdentity != preAuthState.TPA)
            throw IllegalArgumentException("Only the PreAuth assigned TPA can approve the request.")

        val approvedPreAuth = PreAuth(preAuthState.BenIdentity, preAuthState.NetHospital, preAuthState.TPA, preAuthState.Insurer,
                preAuthState.DoA, preAuthState.EstDoS, preAuthState.Diagnosis, preAuthState.Package, preAuthState.Doctor,
                "Approved", preAuthState.linearId)

        val txnCommand = Command(PreAuthContract.commands.Approve(), approvedPreAuth.participants.map { it.owningKey })

        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        val txnbuilder: TransactionBuilder = TransactionBuilder(notary)
                .addOutputState(approvedPreAuth, PRE_AUTH_CONTRACT_ID)
                .addInputState(stateAndRefPreAuth)
                .addCommand(txnCommand)

        val partiallySignedTxn = serviceHub.signInitialTransaction(txnbuilder)

        val counterSession = initiateFlow(preAuthState.NetHospital)

        val fullySignedTxn = subFlow(CollectSignaturesFlow(partiallySignedTxn, setOf(counterSession)))

        return subFlow(FinalityFlow(fullySignedTxn))
    }

}


@InitiatedBy(PreAuthApproveFlow::class)
class preAuthApprovalFlowResponder(val counterSession: FlowSession): FlowLogic<SignedTransaction>(){
    @Suspendable
    override fun call(): SignedTransaction {

        val SignTransactionFlow = object: SignTransactionFlow(counterSession){

            override fun checkTransaction(stx: SignedTransaction) = requireThat{

                val outputState = stx.tx.outputs.single().data
                "This must be a PreAuth Approval Transaction" using (outputState is PreAuth)

            }
        }

        return subFlow(SignTransactionFlow)
    }

}