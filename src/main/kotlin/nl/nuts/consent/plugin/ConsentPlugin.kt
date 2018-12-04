package nl.nuts.consent.plugin

import net.corda.webserver.services.WebServerPluginRegistry
import nl.nuts.consent.api.ConsentApi
import java.util.function.Function

class ConsentPlugin  : WebServerPluginRegistry {
    /**
     * A list of classes that expose web APIs.
     */
    override val webApis = listOf(Function(::ConsentApi))

    /**
     * A list of directories in the resources directory that will be served by Jetty under /web.
     */
    override val staticServeDirs = HashMap<String, String>()
}
