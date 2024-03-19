package ru.home.project.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author rlagay
 */
@Configuration
class AsyncConfig {

    @Bean
    fun tradeEventExecutor() : Executor {
        return Executors.newFixedThreadPool(5)
    }
}