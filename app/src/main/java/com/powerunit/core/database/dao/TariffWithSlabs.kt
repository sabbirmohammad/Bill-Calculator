package com.powerunit.core.database.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity

data class TariffWithSlabs(
    @Embedded
    val tariff: TariffEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tariffId"
    )
    val slabs: List<TariffSlabEntity>
)
