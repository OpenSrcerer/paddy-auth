package online.danielstefani.paddy.authorization.dto

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.logging.Log
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.*
import java.util.*

class AuthorizationResultDto private constructor(
    val result: DeviceAuthorizationResult
) {
    companion object {
        fun allow(sub: String, topic: String): RestResponse<AuthorizationResultDto> {
            Log.info("Allowing <$sub>'s access to <$topic>.")
            return ResponseBuilder.ok(AuthorizationResultDto(DeviceAuthorizationResult.ALLOW))
                .status(Status.OK)
                .build()
        }

        fun forbid(sub: String, topic: String): RestResponse<AuthorizationResultDto> {
            Log.info("Forbidding <$sub>'s access to <$topic>.")
            return ResponseBuilder.ok(AuthorizationResultDto(DeviceAuthorizationResult.DENY))
                .status(Status.OK)
                .build()
        }

        fun ignore(sub: String, topic: String): RestResponse<AuthorizationResultDto> {
            Log.info("Sending <$sub>'s access to <$topic> to next authorizer in chain.")
            return ResponseBuilder.ok(AuthorizationResultDto(DeviceAuthorizationResult.IGNORE))
                .status(Status.OK)
                .build()
        }
    }

    enum class DeviceAuthorizationResult {
        ALLOW, DENY, IGNORE;

        @JsonValue
        fun toLowerCase(): String {
            return toString().lowercase(Locale.getDefault())
        }
    }
}