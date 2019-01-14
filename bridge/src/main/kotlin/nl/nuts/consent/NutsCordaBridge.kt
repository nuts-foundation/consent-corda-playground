package nl.nuts.consent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ConfigurationProperties("corda.rpc")
data class CordaRPCProperties(var host: String = "localhost", var port:Int = 10009, var user:String = "user1", var password:String = "test")

@EnableAsync
@EnableScheduling
@SpringBootApplication
class NutsCordaBridge

fun main(args: Array<String>) {
    runApplication<NutsCordaBridge>(*args)
}