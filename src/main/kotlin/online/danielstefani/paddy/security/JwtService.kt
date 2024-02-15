package online.danielstefani.paddy.security

import io.quarkus.logging.Log
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.impl.jose.JWT
import jakarta.enterprise.context.ApplicationScoped
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

@ApplicationScoped
class JwtService(
    private val keychainHolder: KeychainHolder
) {
    fun makeJwt(
        sub: String,
        jwtLifetimeSeconds: Long = 31540000
    ): String {
        val (privateKey, _) = keychainHolder.keypair()

        val jwtHeader: String = Base64.getUrlEncoder().withoutPadding().encodeToString(
            """
            { "alg": "RS256", "typ": "JWT", "kid": "1" }
            
            """.trimIndent().replace(" ", "").replace("\n", "")
                .toByteArray(StandardCharsets.UTF_8)
        )

        val jwtPayloadTemplate = """
            {
                "sub": "$sub",
                "iss": "https://danielstefani.online",
                "iat": %s,
                "exp": %s,
                "aud": "Paddy MQTT Broker Clients"
            }
            
            """.trimIndent().replace(" ", "").replace("\n", "")

        val jwtPayload: String = Base64.getUrlEncoder().withoutPadding().encodeToString(
            String.format(
                jwtPayloadTemplate,
                Instant.now().epochSecond,
                Instant.now().plusSeconds(jwtLifetimeSeconds).epochSecond
            ).replace(" ", "").replace("\n", "")
                .toByteArray(StandardCharsets.UTF_8)
        )

        val jwtContent = "$jwtHeader.$jwtPayload"
        val signature = Signature.getInstance("SHA256withRSA")
            .also {
                it.initSign(privateKey)
                it.update(jwtContent.toByteArray())
            }
        val jwtSignature: String = Base64.getUrlEncoder().withoutPadding().encodeToString(
            signature.sign()
        )

        return "$jwtContent.$jwtSignature"
    }

    fun makeJwks(): String {
        val rsaPublicKey = keychainHolder.keypair().second as RSAPublicKey

        val jwksResponse = java.lang.String.format(
            """
            {
                "keys": [{
                    "kty": "%s",
                    "kid": "1",
                    "n": "%s",
                    "e": "%s",
                    "alg": "RS256",
                    "use": "sig"
                }]
            }
            """.trimIndent()
                .replace("\n", "")
                .replace(" ", ""),
            rsaPublicKey.algorithm,
            Base64.getUrlEncoder().encodeToString(rsaPublicKey.modulus.toByteArray()),
            Base64.getUrlEncoder().encodeToString(rsaPublicKey.publicExponent.toByteArray())
        )

        return jwksResponse
    }

    fun parseJwt(jwt: String): JsonObject? {
        return try {
                JWT.parse(jwt)
            } catch (ex: Exception) {
                Log.debug("Received invalid JWT: <${jwt}>.")
                null
            }
    }

    /*
    Check the signature of the JWT.
    */
    fun isJwtValid(jwt: String): Boolean {
        try {
            keychainHolder.verifier().verify(jwt)
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}