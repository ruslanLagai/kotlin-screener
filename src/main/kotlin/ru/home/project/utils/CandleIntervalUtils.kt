package ru.home.project.utils

import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval

fun mapToCandleInterval(interval: SubscriptionInterval) : CandleInterval {
    return when (interval) {
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE -> CandleInterval.CANDLE_INTERVAL_1_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES -> CandleInterval.CANDLE_INTERVAL_5_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_UNSPECIFIED -> CandleInterval.CANDLE_INTERVAL_UNSPECIFIED
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIFTEEN_MINUTES -> CandleInterval.CANDLE_INTERVAL_15_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_HOUR -> CandleInterval.CANDLE_INTERVAL_HOUR
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_DAY -> CandleInterval.CANDLE_INTERVAL_DAY
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_2_MIN -> CandleInterval.CANDLE_INTERVAL_2_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_3_MIN -> CandleInterval.CANDLE_INTERVAL_3_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_10_MIN -> CandleInterval.CANDLE_INTERVAL_10_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_30_MIN -> CandleInterval.CANDLE_INTERVAL_30_MIN
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_2_HOUR -> CandleInterval.CANDLE_INTERVAL_2_HOUR
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_4_HOUR -> CandleInterval.CANDLE_INTERVAL_4_HOUR
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_WEEK -> CandleInterval.CANDLE_INTERVAL_WEEK
        SubscriptionInterval.SUBSCRIPTION_INTERVAL_MONTH -> CandleInterval.CANDLE_INTERVAL_MONTH
        SubscriptionInterval.UNRECOGNIZED -> CandleInterval.CANDLE_INTERVAL_UNSPECIFIED
    }
}