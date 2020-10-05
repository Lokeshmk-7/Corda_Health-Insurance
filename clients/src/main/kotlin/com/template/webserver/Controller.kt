package com.template.webserver

import com.template.flows.PreAuthApproveFlow
import com.template.flows.PreAuthFlow
import com.template.states.PreAuth
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.NodeInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.stream.Collectors

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api/preAuth") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {

        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private var proxy = rpc.proxy
    private var me: CordaX500Name? = proxy.nodeInfo().legalIdentities[0].name

    fun MainController(rpc: NodeRPCConnection) {
        me = proxy.nodeInfo().legalIdentities[0].name
    }

    /** Helpers for filtering the network map cache.  */
    fun toDisplayString(name: X500Name?): String? {
        return BCStyle.INSTANCE.toString(name)
    }

    private fun isNotary(nodeInfo: NodeInfo): Boolean {
        return !proxy.notaryIdentities()
                .stream().filter { el: Party? -> nodeInfo.isLegalIdentity(el!!) }
                .collect(Collectors.toList()).isEmpty()
    }

    private fun isMe(nodeInfo: NodeInfo): Boolean {
        return nodeInfo.legalIdentities[0].name == me
    }

    private fun isNetworkMap(nodeInfo: NodeInfo): Boolean {
        return nodeInfo.legalIdentities[0].name.organisation == "Network Map Service"
    }

    @GetMapping(value = ["/templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @GetMapping(value = ["/templateendpoint2"], produces = ["text/plain"])
    private fun templateendpoint2(): String {
        return "Define a second endpoint here."
    }

        @GetMapping(value = ["/peers"], produces = [MediaType.APPLICATION_JSON_VALUE])
        fun getPeers(): HashMap<String, List<String>>? {
            val myMap = HashMap<String, List<String>>()
            // Find all nodes that are not notaries, ourself, or the network map.
            val filteredNodes = proxy.networkMapSnapshot().stream()
                    .filter { el: NodeInfo? -> !el?.let { isNotary(it) }!! && !el?.let { isMe(it) } && !el?.let { isNetworkMap(it) } }
            // Get their names as strings
            val nodeNames = filteredNodes.map { el: NodeInfo -> el.legalIdentities[0].name.toString() }
                    .collect(Collectors.toList<String>())
            myMap["peers"] = nodeNames
            return myMap
        }

        @GetMapping(value = ["/me"], produces = [MediaType.APPLICATION_JSON_VALUE])
         fun whoami(): HashMap<String, String>? {
            val myMap = HashMap<String, String>()
            myMap["me"] = me.toString()
            return myMap
        }


        @GetMapping(value = ["/preAuths"], produces = [APPLICATION_JSON_VALUE])
         fun getPreAuths(): List<StateAndRef<ContractState>>{
            // Filter by state type: IOU.
            return proxy.vaultQueryBy<PreAuth>().states
            //return proxy.vaultQuery<PreAuth>(PreAuth::class.java).states
        }


    /*@PutMapping(value = ["/issuePreAuthEndpoint"], produces = [TEXT_PLAIN_VALUE])
    fun createPreAuth(@RequestParam(value = "TPA") TPA: String,
                 @RequestParam(value = "Insurer") Insurer: String,
                 @RequestParam(value = "DoA") DoA: Int,
                 @RequestParam(value = "EstDos") EstDoS: Int,
                      @RequestParam(value = "Diagnosis") Diagnosis: String,
                @RequestParam(value = "Package") Package: String,
                @RequestParam(value = "Doctor") Doctor: String): ResponseEntity<String> { // Get party objects for myself and the counterparty.
        val me = proxy.nodeInfo().legalIdentities.first()
        val Tpaa = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(TPA!!)) ?: throw IllegalArgumentException("Unknown party name of TPA")
        val Insurerer = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(Insurer!!)) ?: IllegalArgumentException("Unknown party name of Insurer")
        // Create a new IOU state using the parameters given.
        return try {
            // Start the IOUIssueFlow. We block and waits for the flow to return.
            val result = proxy.startTrackedFlowDynamic(PreAuthFlow::class.java, Tpaa, Insurerer, DoA, EstDoS, Diagnosis, Package, Doctor).returnValue.get()
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single()}")
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (e: Exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Cannot create PreAuth request")
        }
    }*/
    /**
     * Initiates a flow to agree an IOU between two parties.
     * Example request:
     * curl -X PUT 'http://localhost:10007/api/iou/issue-iou?amount=99&currency=GBP&party=O=ParticipantC,L=New%20York,C=US'
     */
    @PutMapping(value = [ "/issuePreAuthEndpoint" ], produces = [ TEXT_PLAIN_VALUE ])
    fun issuePreAuth(@RequestParam(value = "TPA") TPA: String,
                 @RequestParam(value = "Insurer") Insurer: String,
                 @RequestParam(value = "DoA") DoA: Int,
                 @RequestParam(value = "EstDoS") EstDoS: Int,
                 @RequestParam(value = "Diagnosis") Diagnosis: String,
                 @RequestParam(value = "Package") Package: String,
                 @RequestParam(value = "Doctor") Doctor: String): ResponseEntity<String> {
        // Get party objects for myself and the counterparty.
        val mee = proxy.nodeInfo().legalIdentities.first()
        val tpaa = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(TPA)) ?: throw IllegalArgumentException("Unknown party name.")
        val Insurerer = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(Insurer)) ?: throw IllegalArgumentException("Unknown party name.")
        // Create a new IOU state using the parameters given.
        try {
                // Start the IOUIssueFlow. We block and waits for the flow to return.
            val PreAuthState = PreAuth(UniqueIdentifier(), mee, tpaa, Insurerer, DoA, EstDoS, Diagnosis, Package, Doctor, "Pending", UniqueIdentifier())
            val result = proxy.startTrackedFlow(::PreAuthFlow, PreAuthState).returnValue.get()
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single()}")

            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (e: Exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.message)

        }

    }


    /**
     * Settles an IOU. Requires cash in the right currency to be able to settle.
     * Example request:
     * curl -X GET 'http://localhost:10007/api/iou/settle-iou?id=705dc5c5-44da-4006-a55b-e29f78955089&amount=98&currency=USD'
     */
    @GetMapping(value = [ "/approvePreAuthEndpoint" ], produces = [ TEXT_PLAIN_VALUE ])
    fun settleIOU(@RequestParam(value = "id") id: String): ResponseEntity<String> {

        return try {
            proxy.startFlow(::PreAuthApproveFlow, id).returnValue.get()
            ResponseEntity.status(HttpStatus.CREATED).body("$id has been approved.")

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PreAuth request approval unsuccessful")
        }
    }





}