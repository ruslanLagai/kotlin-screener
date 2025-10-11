package ru.home.project.utils

import ru.home.project.entity.CandleEntity
import ru.tinkoff.piapi.contract.v1.Quotation

fun priceToDouble(quotation: Quotation) : Double {
    return quotation.units.toDouble().plus(("0." + quotation.nano).toDouble())
}

fun getMedianPrice(candles: List<CandleEntity>) {

}