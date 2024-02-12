package online.danielstefani.paddy.db.device

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.neo4j.ogm.session.SessionFactory
import org.neo4j.ogm.session.queryForObject

@ApplicationScoped
class DeviceRepository {

    @Inject
    private lateinit var sessionFactory: SessionFactory

    fun createDevice(serial: String, jwt: String) {
        with(sessionFactory.openSession()) {
            val existingDevice = this.queryForObject<Device>(
                "MATCH (deviceNode:Device {serial: $serial}) RETURN deviceNode",
                emptyMap()
            )

            if (existingDevice != null)
                this.save(existingDevice.also { it.jwt = jwt })
            else
                this.save(Device(serial, jwt))
        }
    }

}