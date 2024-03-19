package ru.home.project.service

/**
 * @author rlagay
 */
interface TelegramBotGrpcService {

    fun sendMessageToAdmin(figi: String?, ticker: String?, text: String?)

    fun sendMessage(figi: String?, ticker: String?, text: String?, accountId: String?)
}