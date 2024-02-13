package online.danielstefani.paddy.authorization.dto

import com.fasterxml.jackson.annotation.JsonValue
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.*
import java.util.*

class AuthorizationResultDto private constructor(
    val result: DeviceAuthorizationResult
) {
    companion object {
        fun allow(): RestResponse<AuthorizationResultDto> {
            return ResponseBuilder.ok(AuthorizationResultDto(DeviceAuthorizationResult.ALLOW))
                .status(Status.OK)
                .build()
        }

        fun forbid(): RestResponse<AuthorizationResultDto> {
            return ResponseBuilder.ok(AuthorizationResultDto(DeviceAuthorizationResult.DENY))
                .status(Status.OK)
                .build()
        }

        fun ignore(): RestResponse<AuthorizationResultDto> {
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