package online.danielstefani.paddy.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.advanced.Mqtt5ClientAdvancedConfig
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import io.quarkus.logging.Log
import io.quarkus.runtime.StartupEvent
import io.reactivex.*
import io.reactivex.Observable
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import online.danielstefani.paddy.jwt.JwtService
import online.danielstefani.paddy.jwt.dto.JwtType
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.time.Duration
import java.util.*

@ApplicationScoped
class RxMqttClient(
    private val mqttConfig: MqttConfiguration,
    private val jwtService: JwtService
) {
    // Singleton
    private var mqttClient: Mqtt5RxClient? = null
    private val mqttClientId = UUID.randomUUID()

    private var jwtUsername: String? = null

    // Build the client on startup
    fun startup(@Observes event: StartupEvent) {
        // Since this is the auth service it generates its own credential
        jwtUsername = jwtService.makeJwt("paddy-backend", JwtType.ADMIN, null).jwt
        rebuildMqttClient()
    }

    fun publish(
        daemonId: String,
        action: String,
        message: String = "",
        qos: MqttQos = MqttQos.AT_MOST_ONCE,
    ): Flowable<Mqtt5PublishResult>? {
        val topic = mqttConfig.deviceReadTopic().replace("+", daemonId) + "/$action"

        return mqttClient?.publish(
            Flowable.just(Mqtt5Publish.builder()
                .topic(topic)
                .qos(qos)
                .payload(message.toByteArray())
                .build()))
            ?.map { if (it.error.isPresent) throw it.error.get() else it }
    }

    /**
     * Rebuilds (replaces) current singleton client.
     */
    fun rebuildMqttClient(): Mqtt5RxClient {
        shutdownClient().blockingAwait() // Kill current client (null checks inside)

        // ---- Build Client ----
        val client = Mqtt5Client.builder()
            .identifier(
                "${mqttConfig.clientId()}-$mqttClientId".apply {
                    Log.info("[client->mqtt->reaper] // Building new MQTT client: $this")
                }
            )
            .serverHost(mqttConfig.host())
            .serverPort(mqttConfig.port())
            .also {
                it.simpleAuth(
                    Mqtt5SimpleAuth.builder()
                        .username(jwtUsername!!)
                        .build()
                )
            }
            .advancedConfig(
                Mqtt5ClientAdvancedConfig.builder()
                    .allowServerReAuth(true)
                    .build()
            )
            .buildRx()

        return client.apply {
            mqttClient = this
            mqttClient!!
                .connectScenario()
                // Need to do this because RxJava's
                // error handling mechanism is futile
                // Project Reactor >>>>
                .`as` { Flux.from(it.toFlowable(BackpressureStrategy.BUFFER)) }
                .retryWhen(
                    Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry {
                            Log.info("[client->mqtt] // " +
                                    "Connecting to... ${mqttConfig.host()}:${mqttConfig.port()}")
                        }
                )
                .subscribe()
        }
    }

    /**
     * Define connection rules for MQTT Client building above.
     */
    private fun Mqtt5RxClient.connectScenario(): Observable<Mqtt5ConnAck> {
        return this.connectWith()
            .cleanStart(true)
            .applyConnect()
            .doOnSubscribe { Log.info("[client->mqtt] // " +
                    "Connecting to... ${mqttConfig.host()}:${mqttConfig.port()}") }
            .doOnSuccess { Log.info("[client->mqtt] // " +
                    "Connected to ${mqttConfig.host()}:${mqttConfig.port()}, ${it.reasonCode}") }
            .doOnError { Log.error("[client->mqtt] // " +
                    "Connection failed to ${mqttConfig.host()}:${mqttConfig.port()}, ${it.message}") }
            .onErrorResumeNext {
                // if client is already connected, continue with a single that never
                // emits to stop reconnections
                if (mqttClient?.state?.isConnected == true)
                    return@onErrorResumeNext Single.never<Mqtt5ConnAck>()

                Single.error(it)
            }
            .toObservable()
    }

    private fun shutdownClient(): Completable {
        if (mqttClient == null || !mqttClient!!.state.isConnected)
            return Completable.complete()
                .doOnComplete { Log.info("[client->mqtt->reaper] // " +
                        "Old MQTT client was null or disconnected, ignoring it") }

        return mqttClient!!.disconnect()
            .doOnComplete { Log.info("[client->mqtt->reaper] // " +
                    "Reaped old MQTT client ${mqttClient!!.config.clientIdentifier.get()}") }
    }
}