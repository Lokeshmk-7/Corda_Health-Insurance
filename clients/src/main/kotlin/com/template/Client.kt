package com.template

import com.template.flows.PreAuthFlow
import com.template.states.PreAuth
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
fun main(args: Array<String>) = Client().main(args)

private class Client {
    companion object {
        val logger = loggerFor<Client>()
    }

    fun main(args: Array<String>) {
        // Create an RPC connection to the node.
        require(args.size == 3) { "Usage: Client <node address> <rpc username> <rpc password>" }
        val nodeAddress = parse(args[0])
        val rpcUsername = args[1]
        val rpcPassword = args[2]
        val client = CordaRPCClient(nodeAddress)
        val clientConnection = client.start(rpcUsername, rpcPassword)
        val proxy = clientConnection.proxy

        // Interact with the node.
        // Example #1, here we print the nodes on the network.
        val nodes = proxy.networkMapSnapshot()
        println("\n-- Here is the networkMap snapshot --")
        logger.info("{}", nodes)

        // Example #2, here we print the PartyA's node info
        val me = proxy.nodeInfo().legalIdentities.first().name
        println("\n-- Here is the node info of the node that the client connected to --")
        logger.info("{}", me)

        /*val mee = proxy.nodeInfo().legalIdentities.first()
        val tpaa = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=TPA, L=London, C=GB")) ?: throw IllegalArgumentException("Unknown party name.")
        val Insurerer = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse("O=Insurer, L=New York, C=US")) ?: throw IllegalArgumentException("Unknown party name.")
        val PreAuthState = PreAuth(UniqueIdentifier(), mee, tpaa, Insurerer, 2020, 10, "Fever", "FeverWala", "Lokesh", "Pending", UniqueIdentifier(), listOf(mee, tpaa))
        val result  = proxy.startTrackedFlow(::PreAuthFlow, PreAuthState).returnValue.get()*/

        //Close the client connection
        clientConnection.close()
    }
}