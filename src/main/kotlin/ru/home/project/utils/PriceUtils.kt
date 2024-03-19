package ru.home.project.utils

import ru.tinkoff.piapi.contract.v1.Quotation

fun priceToDouble(quotation: Quotation) : Double {
    return quotation.units.toDouble().plus(("0." + quotation.nano).toDouble())
}