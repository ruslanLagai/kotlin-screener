package ru.home.project.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.ValidationMode
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaDialect
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * @author rlagay
 */
@Configuration
@EnableConfigurationProperties(value = [JpaProperties::class])
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = ["ru.home.project.repository"])
class DbConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun entityManagerFactory(dataSource: DataSource?): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setGenerateDdl(true)
        vendorAdapter.setDatabase(Database.MYSQL)

        val factory = LocalContainerEntityManagerFactoryBean()
        factory.jpaVendorAdapter = vendorAdapter
        factory.setValidationMode(ValidationMode.AUTO)
        factory.jpaDialect = HibernateJpaDialect()
        factory.setPackagesToScan("ru.home.project.entity")
        factory.dataSource = dataSource
        return factory
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory?): PlatformTransactionManager {
        val txManager = JpaTransactionManager()
        txManager.entityManagerFactory = entityManagerFactory
        return txManager
    }
}