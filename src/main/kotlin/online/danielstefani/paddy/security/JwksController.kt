package online.danielstefani.paddy.security

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.db.device.DeviceRepository
import org.jboss.resteasy.reactive.RestPath
import java.util.*


@Path("/")
class JwksController(
    private val jwtService: JwtService,
    private val deviceRepository: DeviceRepository
) {

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jwks")
    @GET
    fun getJwks(): String {
        return jwtService.makeJwks()
    }

    @Path("/jwt")
    @GET
    fun getJwt(): String {
        return jwtService.makeJwt()
    }

    @Path("/create-device/{serial}")
    @POST
    fun createDevice(@RestPath serial: String?): Map<String, String> {
        val jwt = jwtService.makeJwt()
        val deviceSerial = if (serial.isNullOrEmpty()) UUID.randomUUID().toString() else serial

        deviceRepository.createDevice(deviceSerial, jwt)

        return mapOf(
            Pair("serial", deviceSerial),
            Pair("jwt", jwt)
        )
    }
}