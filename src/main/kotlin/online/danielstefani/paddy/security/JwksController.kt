package online.danielstefani.paddy.security

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/")
class JwksController(
    private val jwtService: JwtService
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
}