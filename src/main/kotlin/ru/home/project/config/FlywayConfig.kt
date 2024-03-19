package ru.home.project.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource

/**
 * @author rlagay
 */
@Configuration
class FlywayConfig(
    val applicationContext: ApplicationContext,
    val dataSource: DataSource
) {

    @Value("\${spring.flyway.schemas}")
    private val schema: String? = null

    @Value("\${spring.flyway.locations}")
    private val locations: String? = null

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (event.applicationContext == applicationContext) {
            Flyway.configure().dataSource(dataSource)
                .baselineOnMigrate(true)
                .schemas(schema)
                .locations(locations)
                .load().migrate()
        }
    }
}