package online.danielstefani.paddy.security.http

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import online.danielstefani.paddy.security.AbstractAuthorizationController
import online.danielstefani.paddy.jwt.JwtService
import online.danielstefani.paddy.jwt.dto.JwtType
import online.danielstefani.paddy.security.dto.AuthenticationRequestDto
import online.danielstefani.paddy.security.dto.AuthenticationResultDto
import org.jboss.resteasy.reactive.RestResponse
import java.time.Instant

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class HttpAuthenticationController(
    private val jwtService: JwtService
) : AbstractAuthorizationController() {

    @POST
    @Path("/validate")
    fun checkJwtValidity(authDto: AuthenticationRequestDto): RestResponse<AuthenticationResultDto> {
        // Parse JWT or immediately forbid
        val jwt = jwtService.parseJwt(authDto.jwt) ?:
        return forbid("<invalid jwt>", "<invalid jwt>")

        with(jwt.getJsonObject("payload")) {
            val sub = this.getString("sub") ?: "<missing sub claim>"
            val exp = this.getLong("exp") ?: 0
            val aud = this.getString("aud") ?: ""
            val rts = this.getString("rts") ?: ""
            val isRefresh = aud == JwtType.REFRESH.audience

            // Check signature
            if (!jwtService.isJwtValid(authDto.jwt))
                return forbid(authDto.jwt, sub)

            // Check expiration date
            if (exp < Instant.now().epochSecond)
                return forbid(authDto.jwt, sub)

            // Check serial (if refresh token)
            if (isRefresh && authDto.refreshTokenSerial != null && rts != authDto.refreshTokenSerial)
                return forbid(authDto.jwt, sub)

            return if (isRefresh) refresh(authDto.jwt, sub)
                else allow(authDto.jwt, sub)
        }
    }
}