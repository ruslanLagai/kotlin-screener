package ru.home.project.listener

import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.home.project.service.TinkoffStreamingService

/**
 * @author rlagay
 */
@Component
class ContextRefreshedEventListener(
    val tradesStreamingService: TinkoffStreamingService
) {

    @EventListener
    fun processEvent(even: ContextRefreshedEvent) {
        tradesStreamingService.subscribeTradingStream()
    }
}