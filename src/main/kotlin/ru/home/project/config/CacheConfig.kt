package ru.home.project.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.PatternEntity
import ru.home.project.model.Candle
import java.time.Duration
import java.util.function.Supplier


/**
 * @author rlagay
 */
@Configuration
class CacheConfig(
    val redisTemplate: StringRedisTemplate
) {

    private val log: Logger = LoggerFactory.getLogger(CacheConfig::class.java)

    @Bean
    fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer {
        val objectMapper = ObjectMapper()
        objectMapper.registerModules(JavaTimeModule())
        objectMapper.registerKotlinModule()

        val tinkoffType: TypeReference<List<CandleEntity>> = object : TypeReference<List<CandleEntity>>() {}
        val tinkoffJavaType = objectMapper.typeFactory.constructType(tinkoffType)
        val tinkoffSerializer: Jackson2JsonRedisSerializer<JavaType> = Jackson2JsonRedisSerializer(objectMapper, tinkoffJavaType)

        val type: TypeReference<List<Candle>> = object : TypeReference<List<Candle>>() {}
        val javaType = objectMapper.typeFactory.constructType(type)
        val serializer: Jackson2JsonRedisSerializer<JavaType> = Jackson2JsonRedisSerializer(objectMapper, javaType)

//        val filterProvider = SimpleFilterProvider()
//        filterProvider.addFilter("extremumsFilter", SimpleBeanPropertyFilter.serializeAllExcept("minimums", "maximums"))
//        objectMapper.setFilterProvider(filterProvider)
        val patternsType: TypeReference<List<PatternEntity>> = object : TypeReference<List<PatternEntity>>() {}
        val patternJavaType = objectMapper.typeFactory.constructType(patternsType)
        val patternSerializer: Jackson2JsonRedisSerializer<JavaType> = Jackson2JsonRedisSerializer(objectMapper, patternJavaType)

        val instrumentTypeJavaType = objectMapper.typeFactory.constructType(InstrumentEntity::class.java)
        val instrumentSerializer: Jackson2JsonRedisSerializer<JavaType> = Jackson2JsonRedisSerializer(objectMapper, instrumentTypeJavaType)

        return RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
            builder
                .withCacheConfiguration(
                    "daily-crypto",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                        .prefixCacheNameWith("daily-crypto")
                        .entryTtl(Duration.ofHours(8))
                )
                .withCacheConfiguration(
                    "daily-tinkoff",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith("daily-tinkoff")
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(tinkoffSerializer))
                        .entryTtl(Duration.ofMinutes(2))
                )
                .withCacheConfiguration(
                    "instruments",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(instrumentSerializer))
                        .entryTtl(Duration.ofDays(1))
                )
                .withCacheConfiguration(
                    "patterns",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(patternSerializer))
                        .entryTtl(Duration.ofMinutes(5))
                )
        }
    }

    @Bean
    fun checkRedis(): Supplier<Boolean?> {
        try {
            return Supplier { redisTemplate.execute { connection: RedisConnection -> "PONG" == connection.ping() } }
        } catch (e: Exception) {
            log.warn("Failed to ping redis server. Getting employee data from employee-card.", e)
            return Supplier { false }
        }
    }
}