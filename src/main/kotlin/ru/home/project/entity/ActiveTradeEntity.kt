package ru.home.project.entity

import jakarta.persistence.*
import ru.home.project.dto.AlertType

/**
 * @author rlagay
 */
@Entity(name = "active_trade_entity")
data class ActiveTradeEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
    val ticker: String,
    val closed: Boolean,
    val mergedLevelId: String,
    @Enumerated(value = EnumType.STRING) val alertType: AlertType
)
