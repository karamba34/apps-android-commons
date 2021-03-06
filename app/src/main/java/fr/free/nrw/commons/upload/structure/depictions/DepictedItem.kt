package fr.free.nrw.commons.upload.structure.depictions

import fr.free.nrw.commons.explore.depictions.DepictsClient.Companion.getImageUrl
import fr.free.nrw.commons.explore.depictions.THUMB_IMAGE_SIZE
import fr.free.nrw.commons.nearby.Place
import fr.free.nrw.commons.upload.WikidataItem
import fr.free.nrw.commons.wikidata.WikidataProperties
import org.wikipedia.wikidata.DataValue
import org.wikipedia.wikidata.Entities
import org.wikipedia.wikidata.Statement_partial

/**
 * Model class for Depicted Item in Upload and Explore
 */
data class DepictedItem constructor(
    override val name: String,
    val description: String?,
    val imageUrl: String?,
    val instanceOfs: List<String>,
    var isSelected: Boolean,
    override val id: String
) : WikidataItem {

    constructor(entity: Entities.Entity) : this(
        entity,
        entity.labels().values.firstOrNull()?.value() ?: "",
        entity.descriptions().values.firstOrNull()?.value() ?: ""
    )

    constructor(entity: Entities.Entity, place: Place) : this(
        entity,
        place.name,
        place.longDescription
    )

    constructor(entity: Entities.Entity, name: String, description: String) : this(
        name,
        description,
        entity[WikidataProperties.IMAGE].primaryImageValue?.let {
            getImageUrl(it.value, THUMB_IMAGE_SIZE)
        },
        entity[WikidataProperties.INSTANCE_OF].toIds(),
        false,
        entity.id()
    )

    override fun equals(other: Any?) = when {
        this === other -> true
        other is DepictedItem -> name == other.name
        else -> false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

private fun List<Statement_partial>?.toIds(): List<String> {
   return this?.map { it.mainSnak.dataValue }
        ?.filterIsInstance<DataValue.EntityId>()
        ?.map { it.value.id }
        ?: emptyList()
}

private val List<Statement_partial>?.primaryImageValue: DataValue.ValueString?
    get() = this?.first()?.mainSnak?.dataValue as? DataValue.ValueString

operator fun Entities.Entity.get(property: WikidataProperties) =
    statements?.get(property.propertyName)

