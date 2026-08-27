package ru.home.project.entity

import jakarta.persistence.*
import ru.home.project.dto.AlertType

/**
 * @author rlagay
 */
@Entity(name = "active_trade_entity")
open class ActiveTradeEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long? = null,
    var ticker: String,
    var closed: Boolean,
    var mergedLevelId: String,
    @Enumerated(value = EnumType.STRING) var alertType: AlertType
)
