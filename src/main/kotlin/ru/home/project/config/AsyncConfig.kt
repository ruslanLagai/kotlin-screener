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
        return Executors.newFixedThreadPool(1)
    }

    @Bean
    fun cryptoEventExecutor() : Executor {
        return Executors.newFixedThreadPool(3)
    }

    @Bean
    fun alertEventExecutor() : Executor {
        return Executors.newFixedThreadPool(1)
    }
}