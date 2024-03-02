package online.danielstefani.paddy.jwt

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.jwt.dto.JwtRequestDto
import online.danielstefani.paddy.jwt.dto.JwtResponseDto

@Path("/jwt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class JwtController(
    private val jwtService: JwtService
) {

    /*
    Mint a new JWT with permissions as required by the internal caller.
    Reminder that this is supposed to be called from inside the VPC.
     */
    @POST
    @Path("/")
    fun getJwt(dto: JwtRequestDto): JwtResponseDto {
        return jwtService.makeJwt(dto.subject, dto.jwtType)
    }

}