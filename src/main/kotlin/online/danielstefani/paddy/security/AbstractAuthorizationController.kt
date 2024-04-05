package online.danielstefani.paddy.security

import io.quarkus.logging.Log
import online.danielstefani.paddy.security.dto.AuthenticationResultDto
import online.danielstefani.paddy.security.dto.AuthenticationResultDto.*
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.*

abstract class AbstractAuthorizationController {

    fun allow(principal: String, resource: String): RestResponse<AuthenticationResultDto> {
        Log.info("Allowing <$principal>'s access to <$resource>.")
        return ResponseBuilder.ok(AuthenticationResultDto(AuthorizationResult.ALLOW, resource))
            .status(Status.OK)
            .build()
    }

    fun forbid(principal: String, resource: String): RestResponse<AuthenticationResultDto> {
        Log.info("Forbidding <$principal>'s access to <$resource>.")
        return ResponseBuilder.ok(AuthenticationResultDto(AuthorizationResult.DENY, resource))
            .status(Status.OK)
            .build()
    }

    fun refresh(principal: String, resource: String): RestResponse<AuthenticationResultDto> {
        Log.info("Allowing refresh access for <$principal> to <$resource>.")
        return ResponseBuilder.ok(AuthenticationResultDto(AuthorizationResult.REFRESH, resource))
            .status(Status.OK)
            .build()
    }

    fun ignore(principal: String, resource: String): RestResponse<AuthenticationResultDto> {
        Log.info("Sending <$principal>'s access to <$resource> to next authorizer in chain.")
        return ResponseBuilder.ok(AuthenticationResultDto(AuthorizationResult.IGNORE, resource))
            .status(Status.OK)
            .build()
    }
}