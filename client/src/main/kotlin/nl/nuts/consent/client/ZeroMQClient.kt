package nl.nuts.consent.client

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.ZMQ


fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("ZeroMQClient")

    val context = ZMQ.context(1)

    logger.info("Starting subscriber socket")

    //  First, connect our subscriber socket
    val subscriber = context.socket(ZMQ.SUB)
    subscriber.setRcvHWM(0)
    subscriber.connect("tcp://localhost:5561")
    subscriber.subscribe("".toByteArray())

    logger.info("Starting req queue")

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

    logger.info("Starting main loop")

    //  Third, get our updates and report how many we got
    while (true) {
        val string = subscriber.recvStr(0)

        // Example:
        // METHOD_00000007_123456782_NE0001_4b548f3d-5219-4dff-ac31-47b47bc795bd

        logger.info("Received $string over ZeroMQ")

        val parts = string.split('_')

        val client = OkHttpClient()

        val request = if (parts[0] == "ADD") {
            Request.Builder()
                    .method("POST", RequestBody.create(MediaType.get("application/json"), ""))
                    .url("http://localhost:8081/api/single?bsn=${parts[2]}&agb=${parts[1]}&organisation=${parts[3]}")
                    .build()
        } else { //revoke
            Request.Builder()
                    .method("DELETE", RequestBody.create(MediaType.get("application/json"), ""))
                    .url("http://localhost:8081/api/single?bsn=${parts[2]}&agb=${parts[1]}&organisation=${parts[3]}")
                    .build()
        }

        try {
            val response = client.newCall(request).execute()
            response.use {
                println("Succubus returned ${response.code()}")
            }
        } catch (e:Exception) {
            println(e.message)
        }
    }
}