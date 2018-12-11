package nl.nuts.consent.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ


fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("ZeroMQClient")

    val context = ZMQ.context(1)

    //  First, connect our subscriber socket
    val subscriber = context.socket(ZMQ.SUB)
    subscriber.setRcvHWM(0)
    subscriber.connect("tcp://localhost:5561")
    subscriber.subscribe("".toByteArray())

    //  Second, synchronize with publisher
    val syncclient = context.socket(ZMQ.REQ)
    syncclient.connect("tcp://localhost:5562")

    //  - send a synchronization request
    syncclient.send("".toByteArray(), 0)

    //  - wait for synchronization reply
    syncclient.recv(0)

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            logger.info("Shutdown Hook is running !")

            subscriber.close()
            syncclient.close()
            context.term()
        }
    })

    //  Third, get our updates and report how many we got
    while (true) {
        val string = subscriber.recvStr(0)
        logger.info("Received $string over ZeroMQ")
    }
}