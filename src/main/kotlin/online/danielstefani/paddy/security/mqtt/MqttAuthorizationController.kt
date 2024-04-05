package online.danielstefani.paddy.security.mqtt

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.security.AbstractAuthorizationController
import online.danielstefani.paddy.jwt.JwtService
import online.danielstefani.paddy.security.dto.AuthenticationRequestDto
import online.danielstefani.paddy.security.dto.AuthenticationResultDto
import org.jboss.resteasy.reactive.RestResponse

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MqttAuthorizationController(
    private val jwtService: JwtService
) : AbstractAuthorizationController() {

    @POST
    @Path("/verify")
    fun verifyIncomingMqttJwt(authDto: AuthenticationRequestDto): RestResponse<AuthenticationResultDto> {

        // Parse JWT or immediately forbid
        val jwt = jwtService.parseJwt(authDto.jwt) ?:
            return forbid("<missing/invalid jwt>", authDto.topic!!)

        val sub = jwt.getJsonObject("payload").getString("sub")

        // Special case: Check if the token is for the backend
        if (sub.equals("paddy-backend")) {
            Log.debug("Received Paddy Backend JWT: <${authDto.jwt}>.")
            return allow(sub, authDto.topic!!)
        }

        // Check if the topic matches the sub -> if no match 403
        return if (subMatchTopic(sub, authDto.topic!!))
            allow(sub, authDto.topic) else forbid(sub, authDto.topic)
    }

    /*
    Topics are expected to be in the format daemon/SUB-XXXX/...
    This function makes sure that the first part is in that format.
     */
    private fun subMatchTopic(sub: String, topic: String): Boolean {
        return topic.startsWith("daemon/$sub/")
    }
}