package ru.home.project.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.Hibernate.getClass
import org.hibernate.proxy.HibernateProxy
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "merged_level_entity")
open class MergedLevelEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
    val figi: String? = null,
    val ticker: String? = "",
    val price: Double = 0.0,
    val minLevel: Double,
    val maxLevel: Double,
    val level: Double = 0.0,
    val minLevelDate: ZonedDateTime,
    val maxLevelDate: ZonedDateTime
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MergedLevelEntity

        if (figi != other.figi) return false
        if (ticker != other.ticker) return false
        if (minLevel != other.minLevel) return false
        if (maxLevel != other.maxLevel) return false

        return true
    }

    override fun hashCode(): Int {
        return if (this is HibernateProxy) {
            (this as HibernateProxy).getHibernateLazyInitializer().getPersistentClass().hashCode()
        } else {
            getClass(this).hashCode()
        }
    }
}
