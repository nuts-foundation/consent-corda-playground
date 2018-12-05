package nl.nuts.consent.bridge.event

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import nl.nuts.consent.bridge.CordaRPCProperties
import nl.nuts.consent.state.ConsentState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class EventService {

    val logger:Logger = LoggerFactory.getLogger(EventService::class.java)

    @Autowired
    lateinit var cordaRPCProperties: CordaRPCProperties

    val mon = Object()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        // listen to state changes and print them

        // construct rpc client
        val nodeAddress = NetworkHostAndPort(cordaRPCProperties.host, cordaRPCProperties.port)
        val proxy = CordaRPCClient(nodeAddress).start(cordaRPCProperties.user, cordaRPCProperties.password).proxy

        val feed= proxy.vaultTrack(ConsentState::class.java)
        val observable = feed.updates

        logger.info("Current states")

        feed.snapshot.states.forEach {
            logger.info(it.state.data.toString())
        }

        logger.info("Starting observable for new states")

        observable.subscribe { update ->
            update.produced.forEach {
                logger.info("NEW STATE!")
                logger.info(it.state.data.toString())
            }
        }

        synchronized(mon) {
            mon.wait()
        }
    }
}