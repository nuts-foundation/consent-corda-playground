/*
 *     Copyright (c) 2018.
 *
 *     This file is part of Nuts: corda-playground.
 *
 *     corda-playground is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     corda-playground is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with corda-playground.  If not, see <https://www.gnu.org/licenses/>.
 */

package nl.nuts.consent.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import nl.nuts.consent.schema.ConsentSchemaV1

/**
 * The concrete values below give a better view on how things will work. (Before we switch to the abstract id's)
 *
 * patientId: currently we use bsn
 * professionalId: currently we use agb
 * organisationId: currently a custom identifier for the POC
 * purpose: why is this consent given?
 * source: the type of proof that has the signed consent (document probably)
 */
data class ConsentState(val patientId: String = "",
                        val professionalId: String = "",
                        val organisationId: String = "",
                        val purpose: String = "",
                        val source: String = "",
                        val parties: List<Party> = ArrayList()) : LinearState, QueryableState {

    // A good unique identifier would be ?????, The ConsentResourceProvider party should be presented in the externalId
    override val linearId: UniqueIdentifier get() = UniqueIdentifier("${professionalId}_${patientId}_${organisationId}")
    override val participants: List<AbstractParty> get() = parties

    override fun toString() = linearId.toString()

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ConsentSchemaV1 -> ConsentSchemaV1.PersistentConsent(
                    this.patientId,
                    this.professionalId,
                    this.organisationId,
                    this.purpose,
                    this.source
            )
            else -> throw IllegalArgumentException("Unrecognized schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ConsentSchemaV1)
}