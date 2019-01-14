package nl.nuts.consent.event

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.*
import net.corda.core.utilities.NetworkHostAndPort
import nl.nuts.consent.CordaRPCProperties
import nl.nuts.consent.messaging.ZeroMQService
import nl.nuts.consent.state.ConsentState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventService {

    val logger:Logger = LoggerFactory.getLogger(EventService::class.java)

    @Autowired
    lateinit var cordaRPCProperties: CordaRPCProperties

    @Autowired
    lateinit var zeroMQService: ZeroMQService

    fun run() {
        // listen to state changes and print them

        // construct rpc client
        val nodeAddress = NetworkHostAndPort(cordaRPCProperties.host, cordaRPCProperties.port)
        val proxy = CordaRPCClient(nodeAddress).start(cordaRPCProperties.user, cordaRPCProperties.password).proxy

        val feed = proxy.vaultTrackBy(
                QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL),
                PageSpecification(DEFAULT_PAGE_NUM, 100),
                Sort(setOf(Sort.SortColumn(SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF), Sort.Direction.ASC))),
                ConsentState::class.java)
        val observable = feed.updates

        logger.info("Current states")

        feed.snapshot.states.forEach {
            logger.info(it.state.data.toString())

            zeroMQService.publish(it.state.data.toString())
        }

        logger.info("Starting observable for new states")

        observable.subscribe { update ->
            logger.info("Received update")
            update.produced.forEach {
                logger.info("Add ${it.state.data}")

                zeroMQService.publish("ADD_${it.state.data}")
            }
            update.consumed.forEach {
                logger.info("Revoke ${it.state.data}")

                zeroMQService.publish("REVOKE_${it.state.data}")
            }
        }
    }
}