package online.danielstefani.paddy.security.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.security.AbstractAuthorizationController
import online.danielstefani.paddy.jwt.JwtService
import online.danielstefani.paddy.jwt.dto.JwtType
import online.danielstefani.paddy.mqtt.RxMqttClient
import online.danielstefani.paddy.security.dto.AuthenticationRequestDto
import online.danielstefani.paddy.security.dto.AuthenticationResultDto
import org.jboss.resteasy.reactive.RestResponse
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MqttAuthorizationController(
    private val jwtService: JwtService,
    private val mqttClient: RxMqttClient
) : AbstractAuthorizationController() {

    companion object {
        const val SECONDS_WEEK = 604800

        val rotationDeduplicationSet = mutableSetOf<String>()
    }

    @POST
    @Path("/verify")
    fun verifyIncomingMqttJwt(authDto: AuthenticationRequestDto): RestResponse<AuthenticationResultDto> {

        // Parse JWT or immediately forbid
        val jwt = jwtService.parseJwt(authDto.jwt) ?:
            return forbid("<missing/invalid jwt>", authDto.topic!!)

        val sub = jwt.getJsonObject("payload").getString("sub")
        val exp = jwt.getJsonObject("payload").getLong("exp")

        // Check expiration date
        if (exp < Instant.now().epochSecond)
            return forbid(authDto.jwt, sub)

        // If JWT on the device is expiring in one week, rotate it
        if (shouldRotateKey(sub, exp)) {
            rotationDeduplicationSet.add(sub) // Prevent duplicates

            val newJwt = jwtService.makeJwt(sub, JwtType.DAEMON, null).jwt

            mqttClient.publish(sub, "rotate", newJwt, qos = MqttQos.EXACTLY_ONCE)
                ?.doOnSubscribe { Log.info("[JWT-ROTATOR] Rotating JWT for <$sub>...") }
                ?.subscribe()

            // Remove element from deduplication set after 60s
            Mono.just(true)
                .delayElement(Duration.ofSeconds(60))
                .doOnSuccess {
                    Log.info("[JWT-ROTATOR] Removing <$sub> from deduplication set.")
                    rotationDeduplicationSet.remove(sub)
                }
                .subscribe()
        }

        // Special case: Check if the token is for the backend
        if (sub.equals("paddy-backend")) {
            Log.debug("Received Paddy Backend JWT: <${authDto.jwt}>.")
            return allow(sub, authDto.topic!!)
        }

        // Check if the topic matches the sub -> if no match 403
        return if (subMatchTopic(sub, authDto.topic!!))
            allow(sub, authDto.topic) else forbid(sub, authDto.topic)
    }

    private fun shouldRotateKey(sub: String, exp: Long): Boolean {
        return !rotationDeduplicationSet.contains(sub)
                && exp <= Instant.now().epochSecond + SECONDS_WEEK
    }

    /*
    Topics are expected to be in the format daemon/SUB-XXXX/...
    This function makes sure that the first part is in that format.
     */
    private fun subMatchTopic(sub: String, topic: String): Boolean {
        return topic.startsWith("daemon/$sub/")
    }
}