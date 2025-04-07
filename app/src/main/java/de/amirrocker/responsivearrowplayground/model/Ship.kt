package de.amirrocker.responsivearrowplayground.model

import de.amirrocker.responsivearrowplayground.model.VehicleProperty.ShipProperty.ShipRegister
import de.amirrocker.responsivearrowplayground.model.VehicleProperty.ShipProperty.ShipType

sealed interface VehicleProperty {

    sealed interface ShipProperty {

        @JvmInline
        value class ShipName(val name: String)

        @JvmInline
        value class ShipRegisterId(val name: String)

        data class ShipRegister(
            val id: Int,
            val name: ShipName,
            val registerId: ShipRegisterId
        ) : ShipProperty

        enum class ShipType() : ShipProperty {
            BULK_FREIGHT_SHIP,
            RORO_SHIP,
            LIQUID_FREIGHT_SHIP,
        }

        enum class ShipTypeHazardCategory() : ShipProperty {
            HAZARDEOUS_BULK_FREIGHT_SHIP,
            HAZARDEOUS_LIQUID_FREIGHT_SHIP,
        }
    }

}

sealed interface Vehicle {

    data class Ship(
        val shipRegister: ShipRegister,
        val type: ShipType
    ) : Vehicle
}
