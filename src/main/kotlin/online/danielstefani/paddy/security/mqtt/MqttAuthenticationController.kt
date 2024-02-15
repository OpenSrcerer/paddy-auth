package online.danielstefani.paddy.security.mqtt

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.security.JwtService


@Path("/")
class MqttAuthenticationController(
    private val jwtService: JwtService
) {

    /*
    Return a JWKS to authenticate MQTT clients.
     */
    @GET
    @Path("/jwks")
    @Produces(MediaType.APPLICATION_JSON)
    fun getJwks(): String {
        return jwtService.makeJwks()
    }

    /*
    Mint a new JWT. Should be called only by the backend
    as these JWTs have permissions to connect to all topics.
     */
    @GET
    @Path("/admin-jwt")
    fun getJwt(): String {
        return jwtService.makeJwt("paddy-backend")
    }
}