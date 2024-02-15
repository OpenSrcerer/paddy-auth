package online.danielstefani.paddy.security.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class AuthorizationResultDto(
    val result: AuthorizationResult,
    val resource: String? = null
) {
    enum class AuthorizationResult {
        ALLOW, DENY, IGNORE;

        @JsonValue
        fun toLowerCase(): String {
            return toString().lowercase(Locale.getDefault())
        }
    }
}