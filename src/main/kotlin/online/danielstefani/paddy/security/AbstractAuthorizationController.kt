package online.danielstefani.paddy.security

import io.quarkus.logging.Log
import online.danielstefani.paddy.security.dto.AuthorizationResultDto
import online.danielstefani.paddy.security.dto.AuthorizationResultDto.*
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.*

abstract class AbstractAuthorizationController {

    fun allow(principal: String, resource: String): RestResponse<AuthorizationResultDto> {
        Log.info("Allowing <$principal>'s access to <$resource>.")
        return ResponseBuilder.ok(AuthorizationResultDto(AuthorizationResult.ALLOW, resource))
            .status(Status.OK)
            .build()
    }

    fun forbid(principal: String, resource: String): RestResponse<AuthorizationResultDto> {
        Log.info("Forbidding <$principal>'s access to <$resource>.")
        return ResponseBuilder.ok(AuthorizationResultDto(AuthorizationResult.DENY, resource))
            .status(Status.OK)
            .build()
    }

    fun ignore(principal: String, resource: String): RestResponse<AuthorizationResultDto> {
        Log.info("Sending <$principal>'s access to <$resource> to next authorizer in chain.")
        return ResponseBuilder.ok(AuthorizationResultDto(AuthorizationResult.IGNORE, resource))
            .status(Status.OK)
            .build()
    }
}