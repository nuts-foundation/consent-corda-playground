package nl.nuts.consent.api

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import nl.nuts.consent.CordaRPCProperties
import nl.nuts.consent.flow.ConsentFlow.GiveAccess
import nl.nuts.consent.flow.ConsentFlow.RevokeAccess
import nl.nuts.consent.schema.ConsentSchemaV1
import nl.nuts.consent.state.ConsentState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

@RestController
@RequestMapping("/api/consent")
class ConsentApi {

    @Autowired
    lateinit var cordaRPCProperties: CordaRPCProperties

    lateinit var client: CordaRPCClient

    @PostConstruct
    fun init() {
        val nodeAddress = NetworkHostAndPort(cordaRPCProperties.host, cordaRPCProperties.port)
        client = CordaRPCClient(nodeAddress)
    }

    @RequestMapping("/", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    fun find(@RequestParam("bsn") bsn: String, @RequestParam("organisation") organisation:String) : List<ConsentState> {

        // find previous state
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

        val results = getRPCProxy { rpcOps ->
            val results = builder {
                val crPatientId = QueryCriteria.VaultCustomQueryCriteria(ConsentSchemaV1.PersistentConsent::patientId.equal(bsn))
                val crOrganisationId = QueryCriteria.VaultCustomQueryCriteria(ConsentSchemaV1.PersistentConsent::organisationId.equal(organisation))

                val criteria = generalCriteria.and(crPatientId.and(crOrganisationId))

                rpcOps.vaultQueryByCriteria(criteria, ConsentState::class.java)
            }

            results.states.map{it.state.data}
        }

        return results.map { ConsentState(it.patientId, it.professionalId, it.organisationId, it.purpose, it.source, ArrayList()) }
    }

    @RequestMapping("/", method = arrayOf(RequestMethod.POST), consumes = arrayOf("application/json"), produces = arrayOf("application/json"))
    fun createConsent(@RequestParam("bsn") bsn: String, @RequestParam("agb") agb:String, @RequestParam("organisation") organisation:String) {
        getRPCProxy { rpcOps ->
            val nodeInfo = rpcOps.networkMapSnapshot()
            val myLegalName = rpcOps.nodeInfo().legalIdentities.first().name
            val parties = nodeInfo
                    .map{ it.legalIdentities.first().name }
                    .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation)}
                    .map { rpcOps.wellKnownPartyFromX500Name(it)!! }

            rpcOps.startTrackedFlow(::GiveAccess,
                    bsn,
                    agb,
                    organisation,
                    "give care",
                    "document",
                    parties).returnValue.getOrThrow()
        }
    }

    @RequestMapping("/", method = arrayOf(RequestMethod.DELETE), consumes = arrayOf("application/json"), produces = arrayOf("application/json"))
    fun revokeConsent(@RequestParam("bsn") bsn: String, @RequestParam("agb") agb:String, @RequestParam("organisation") organisation:String) {
        getRPCProxy { rpcOps ->
            rpcOps.startTrackedFlow(::RevokeAccess,
                    bsn,
                    agb,
                    organisation).returnValue.getOrThrow()
        }
    }

    private fun <T> getRPCProxy(f: (CordaRPCOps) -> T) : T {
        val conn = client.start(cordaRPCProperties.user, cordaRPCProperties.password)
        val proxy= conn.proxy
        try {
            return f(proxy)
        } finally {
            conn.close()
        }
    }
}