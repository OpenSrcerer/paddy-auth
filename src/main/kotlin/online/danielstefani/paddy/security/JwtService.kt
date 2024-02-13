package online.danielstefani.paddy.security

import jakarta.enterprise.context.ApplicationScoped
import online.danielstefani.paddy.configuration.JwksConfiguration
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.util.*

@ApplicationScoped
class JwtService(
    private val jwksConfiguration: JwksConfiguration
) {
    fun makeJwt(
        sub: String,
        jwtLifetimeSeconds: Long = 31540000
    ): String {
        val (privateKey, _) = makeKeyPair()

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
        val rsaPublicKey = makeKeyPair().second as RSAPublicKey

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

    private fun makeKeyPair(): Pair<PrivateKey, PublicKey> {
        val keyFactory = KeyFactory.getInstance("RSA")

        val privateKey = with(Base64.getDecoder().decode(jwksConfiguration.privateKey())) {
            keyFactory.generatePrivate(PKCS8EncodedKeySpec(this))
        }

        val publicKey = with(
            RSAPublicKeySpec(
                (privateKey as RSAPrivateCrtKey).modulus,
                privateKey.publicExponent
            )
        ) {
            keyFactory.generatePublic(this)
        }

        return Pair(privateKey, publicKey)
    }
}