package nl.nuts.consent.messaging

import nl.nuts.consent.event.EventService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.zeromq.ZMQ
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class ZeroMQService {

    val logger: Logger = LoggerFactory.getLogger(EventService::class.java)

    lateinit var context:ZMQ.Context
    lateinit var publisher:ZMQ.Socket
    lateinit var control:ZMQ.Socket

    @Autowired
    lateinit var eventService: EventService

    @PostConstruct
    fun init() {
        context = ZMQ.context(1)

        // publishing socket
        publisher = context.socket(ZMQ.PUB)
        publisher.linger = 5000
        publisher.sndHWM = 0 // no limit for now
        publisher.bind("tcp://*:5561")

        logger.info("Publishing socket started")
    }

    val mon:Object = Object()

    @Scheduled(fixedDelay = 1000L)
    fun initControlSocket() {
        // control socket
        control = context.socket(ZMQ.REP)
        control.linger = 5000
        control.bind("tcp://*:5562")

        logger.info("Waiting for client")

        //  - wait for synchronization request
        control.recv(0)

        //  - send synchronization reply
        control.send("", 0)

        logger.info("Starting callback service")

        // start RPC callback listener
        eventService.run()

        // block Spring
        synchronized(mon) {
            mon.wait()
        }
    }

    fun publish(payload:String) {
        publisher.send(payload)
    }

    @PreDestroy
    fun cleanup() {
        publisher.close()
        control.close()
        context.term()
    }
}