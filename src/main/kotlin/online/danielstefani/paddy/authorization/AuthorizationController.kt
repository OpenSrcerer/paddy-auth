package online.danielstefani.paddy.authorization

import io.quarkus.logging.Log
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.impl.jose.JWT
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.authorization.dto.AuthorizationRequestDto
import online.danielstefani.paddy.authorization.dto.AuthorizationResultDto
import org.jboss.resteasy.reactive.RestResponse

import online.danielstefani.paddy.authorization.dto.AuthorizationResultDto.Companion.forbid
import online.danielstefani.paddy.authorization.dto.AuthorizationResultDto.Companion.allow

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AuthorizationController {

    @Path("/verify")
    @POST
    fun getJwt(authDto: AuthorizationRequestDto): RestResponse<AuthorizationResultDto> {

        // Parse JWT
        val jwt: JsonObject? =
            try {
                JWT.parse(authDto.username)
            } catch (ex: Exception) {
                Log.debug("Received invalid JWT: <${authDto.username}>.")
                null
            }
        if (jwt == null) return forbid(authDto.topic, "<missing/invalid jwt>")

        val sub = jwt.getJsonObject("payload").getString("sub")

        // Special case: Check if the token is for the backend
        if (sub.equals("paddy-backend")) {
            Log.debug("Received Paddy Backend JWT: <${authDto.username}>.")
            return allow(authDto.topic, sub)
        }

        // Check if the topic matches the sub -> if no match 403
        return if (topicMatchSub(authDto.topic, sub))
            allow(authDto.topic, sub) else forbid(authDto.topic, sub)
    }

    /*
    Topics are expected to be in some-topic-here/test/abc/SUB-XXXX.
    This function extracts the SUB-XXXX part and checks it against the sub in the claim.
     */
    private fun topicMatchSub(topic: String, sub: String): Boolean {
        val topicSerial: String? = with(topic.split("/")) {
            if (this.isEmpty()) null
            else this.last()
        }
        return topicSerial.equals(sub)
    }
}