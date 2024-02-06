package online.danielstefani.paddy.configuration

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "auth")
interface JwksConfiguration {
    // This is the private key that will be used
    // to generate the JWKS and JWTs

    // This is a key in PEM format that has had its lines joined
    // and headers stripped
    fun privateKey(): String
}