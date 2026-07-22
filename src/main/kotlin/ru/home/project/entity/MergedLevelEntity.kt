package ru.home.project.entity

import jakarta.persistence.*
import org.hibernate.Hibernate.getClass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 * @author rlagay
 */
@Entity(name = "merged_level_entity")
@Table(indexes = [
    Index(name = "idx_mergedlevelentity_figi", columnList = "figi")
])
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = [
    NamedQuery(name = "figi", query = "select count(m) from merged_level_entity m where upper(m.figi) like upper(:figi)")
])

open class MergedLevelEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String? = null,
    var figi: String? = null,
    var ticker: String? = "",
    var price: Double = 0.0,
    @Column(name = "minLevel", nullable = false) var minLevel: Double,
    @Column(name = "maxLevel", nullable = false) var maxLevel: Double,
    var level: Double = 0.0,
//    @Convert(converter = DateConverter::class.java)
    @Column(name = "minLevelDate") var minLevelDate: ZonedDateTime,
    @Column(name = "maxLevelDate") var maxLevelDate: ZonedDateTime,
    @CreationTimestamp var createdOn: LocalDateTime? = null,
    @UpdateTimestamp var lastModified: LocalDateTime? = null

//    @Transient val ignoreOptions: String?
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val oEffectiveClass = if (o is HibernateProxy) o.getHibernateLazyInitializer().getPersistentClass() else o.javaClass
        val thisEffectiveClass = if (this is HibernateProxy) (this as HibernateProxy).getHibernateLazyInitializer().getPersistentClass() else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        val level: MergedLevelEntity = o as MergedLevelEntity

        return figi != null && Objects.equals(figi, level.figi)
                && Objects.equals(minLevel, level.minLevel)
                && Objects.equals(maxLevel, level.maxLevel)
    }

    override fun hashCode(): Int {
        return if (this is HibernateProxy) {
            (this as HibernateProxy).getHibernateLazyInitializer().getPersistentClass().hashCode()
        } else {
            getClass(this).hashCode()
        }
    }

    @CreatedDate
    @Column(name = "created_date")
    open var createdDate: Instant? = null

    @LastModifiedDate
    @Column(name = "last_modified_date")
    open var lastModifiedDate: Instant? = null

    @Version
    @Column(name = "version")
    open var version: Int? = null
        protected set

    @PrePersist
    open fun prePersist() {

    }
}
