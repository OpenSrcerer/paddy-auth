package online.danielstefani.paddy.security

import jakarta.enterprise.context.ApplicationScoped
import online.danielstefani.paddy.configuration.JwksConfiguration
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.*
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.security.interfaces.RSAPublicKey

@ApplicationScoped
class KeychainHolder(
    private val jwksConfiguration: JwksConfiguration
) {
    private var keypair: Pair<PrivateKey, PublicKey>? = null
    private var verifer: JWTVerifier? = null

    fun keypair(): Pair<PrivateKey, PublicKey> {
        if (keypair == null) {
            keypair = makeKeyPair()
        }
        return keypair!!
    }

    fun verifier(): JWTVerifier {
        if (verifer == null) {
            val publicKey = keypair().second as RSAPublicKey

            // Private key is not needed to verify JWT
            verifer = com.auth0.jwt.JWT.require(Algorithm.RSA256(publicKey, null))
                .build()
        }
        return verifer!!
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