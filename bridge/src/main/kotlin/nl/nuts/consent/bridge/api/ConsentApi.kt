package nl.nuts.consent.bridge.api

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.NetworkHostAndPort
import nl.nuts.consent.bridge.CordaRPCProperties
import nl.nuts.consent.state.ConsentState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
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

    @RequestMapping("/me", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    fun me() : Map<String, CordaX500Name> {
        return mapOf("me" to getRPCProxy {it.nodeInfo().legalIdentities.first().name} )
    }

    @RequestMapping("/peers", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    fun peers(): Map<String, List<CordaX500Name>> {
        val peers: List<CordaX500Name> = getRPCProxy {
            val nodeInfo = it.networkMapSnapshot()
            nodeInfo
                    .map { it.legalIdentities.first().name }
                    .filter { it.organisation !in (SERVICE_NAMES) }
        }

        return mapOf("peers" to peers)
    }

    @RequestMapping("/states", method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    fun states() : Map<String, List<StateAndRef<ConsentState>>> {
        return mapOf("states" to getRPCProxy {it.vaultQueryBy<ConsentState>().states} )
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