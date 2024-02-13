package online.danielstefani.paddy.authentication

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.db.device.DeviceRepository
import online.danielstefani.paddy.security.JwtService
import org.jboss.resteasy.reactive.RestPath
import java.util.*


@Path("/")
class DeviceAuthenticationController(
    private val jwtService: JwtService,
    private val deviceRepository: DeviceRepository
) {

    /*
    Return a JWKS to authenticate MQTT clients.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jwks")
    @GET
    fun getJwks(): String {
        return jwtService.makeJwks()
    }

    /*
    Mint a new JWT. Should be called only by the backend
    as these JWTs have permissions to connect to all topics.
     */
    @Path("/admin-jwt")
    @GET
    fun getJwt(): String {
        return jwtService.makeJwt("paddy-backend")
    }

    @Path("/create-device/{serial}")
    @POST
    fun createDevice(@RestPath serial: String): Map<String, String> {
        val deviceSerial = serial.ifEmpty { UUID.randomUUID().toString() }
        val jwt = jwtService.makeJwt(deviceSerial)

        deviceRepository.createDevice(deviceSerial, jwt)

        return mapOf(
            Pair("serial", deviceSerial),
            Pair("jwt", jwt)
        )
    }
}