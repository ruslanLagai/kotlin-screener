package ru.home.project.service.impl

import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.MetadataUtils
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.telegram.bot.TelegramData.TelegramMsg
import ru.home.project.telegram.bot.TelegramMessageServiceGrpc
import ru.home.project.telegram.bot.TelegramMessageServiceGrpc.TelegramMessageServiceBlockingStub
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * @author rlagay
 */
@Service
class TelegramBotGrpcServiceImpl(
    val telegramBotProperties: TelegramBotProperties
) : TelegramBotGrpcService {

    private val log: Logger = LoggerFactory.getLogger(TelegramBotGrpcServiceImpl::class.java)

    private var emaTransferServiceBlockingStub: TelegramMessageServiceBlockingStub? = null
    private var channel: ManagedChannel? = null

    @PostConstruct
    fun init() {
        channel = getChannel("")
        this.emaTransferServiceBlockingStub = TelegramMessageServiceGrpc.newBlockingStub(channel)
    }

    override fun sendMessage(figi: String?, ticker: String?, text: String?, accountId: String?) {
        try {
            val emaRequest = TelegramMsg.newBuilder()
                .setFigi(if (StringUtils.isNotBlank(figi)) figi else "")
                .setText(text)
                .setTicker(if (StringUtils.isNotBlank(ticker)) ticker else "")
                .setAccountId(accountId)
                .build()
            emaTransferServiceBlockingStub!!.processMessage(emaRequest)
        } catch (e: Exception) {
            log.error("Failed send data to telegram bot, figi {}, ticker {}", figi, ticker, e)
        }
    }

    override fun sendMessageToAdmin(figi: String?, ticker: String?, text: String?) {
        try {
            val emaRequest = TelegramMsg.newBuilder()
                .setFigi(if (StringUtils.isNotBlank(figi)) figi else "")
                .setText(text)
                .setTicker(if (StringUtils.isNotBlank(ticker)) ticker else "")
                .setAccountId(telegramBotProperties.adminAccount)
                .build()
            emaTransferServiceBlockingStub!!.processMessage(emaRequest)
        } catch (e: Exception) {
            log.error("Failed send data to telegram bot, figi {}, ticker {}", figi, ticker, e)
        }
    }

    @PreDestroy
    @Throws(InterruptedException::class)
    fun close() {
        try {
            channel!!.shutdown()
        } finally {
            if (!channel!!.awaitTermination(3, TimeUnit.SECONDS)) {
                channel!!.shutdownNow()
            }
        }
    }
    private fun getChannel(token: String): ManagedChannel {
        val headers = Metadata()
        addAuthHeader(headers, token)
        return NettyChannelBuilder.forTarget(telegramBotProperties.url)
            .intercept(MetadataUtils.newAttachHeadersInterceptor(headers))
            .usePlaintext()
            .keepAliveWithoutCalls(true)
            .build()
    }

    private fun addAuthHeader(@Nonnull metadata: Metadata, @Nonnull token: String) {
        val authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(authKey, "Bearer $token")
    }

}