package com.template.contracts

import com.template.states.PreAuth
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import kotlin.reflect.jvm.internal.impl.protobuf.ByteString

class PreAuthContract: Contract{

    /**
     * Define Contract ID
     */
    companion object{
        @JvmStatic
        val PRE_AUTH_CONTRACT_ID = PreAuthContract::class.java.name!!
    }

    /* Commands -> Actions that can be taken on Pre-Authorization */

interface commands: CommandData{
    class Request: TypeOnlyCommandData(), commands
    class Approve: TypeOnlyCommandData(), commands
    class Reject: TypeOnlyCommandData(), commands
    class Cancel: TypeOnlyCommandData(), commands
}


    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<PreAuthContract.commands>()
        val Signers = command.signers.toSet()

        when(command.value)
        {
            is PreAuthContract.commands.Request -> RequestPart(tx, Signers)
            is PreAuthContract.commands.Approve -> ApprovePart(tx, Signers)
            is PreAuthContract.commands.Reject -> RejectPart(tx, Signers)
            is PreAuthContract.commands.Cancel -> CancelPart(tx, Signers)
            else
                -> throw IllegalArgumentException("Unrecognised command")
        }
    }


    /* Function for PreAuth Request command
     *@param tx [ongoing transaction]
     *@param Signers [Set of Signers]
     */
    private fun RequestPart(tx: LedgerTransaction, Signers: Set<PublicKey>){

        requireThat {
            "There should only be one Output state for a PreAuth request transaction" using (tx.outputs.size == 1)
            val output = tx.outputsOfType<PreAuth>().single();
            "Status should be Pending for a PreAuth request transaction" using (output.Status == "Pending")
            "DoA should be todays date" using (output.DoA == 2020)
            "Participants for a preAuth request should be Hospital, TPA and Insurer" using (output.participants.containsAll(listOf(output.NetHospital, output.TPA)) && output.participants.size == 2)
        }
    }

    private fun ApprovePart(tx: LedgerTransaction, Signers: Set<PublicKey>){

        requireThat {
            "There should only be one Input state for a PreAuth approval transaction" using (tx.inputs.size == 1)
            "There should only be one Output state for a PreAuth approval transaction" using (tx.outputs.size == 1)
            val output = tx.outputsOfType<PreAuth>().single()
            val input = tx.inputsOfType<PreAuth>().single()
            "Status should be Pending for the Input of PreAuth approval transaction" using (input.Status == "Pending")
            "Status should be Approved for a PreAuth approval transaction" using (output.Status == "Approved")
            "DoA should be todays date" using (output.DoA == 2020)
            "Participants for a preAuth request should be Hospital, TPA and Insurer" using (output.participants.containsAll(listOf(output.NetHospital, output.TPA)) && output.participants.size == 2)
        }

    }

    private fun CancelPart(tx: LedgerTransaction, Signers: Set<PublicKey>){}

    private fun RejectPart(tx: LedgerTransaction, Signers: Set<PublicKey>){}


}