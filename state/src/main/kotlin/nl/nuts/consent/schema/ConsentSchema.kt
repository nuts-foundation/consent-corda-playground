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

package nl.nuts.consent.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object ConsentSchema

object ConsentSchemaV1 : MappedSchema(
        schemaFamily = ConsentSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentConsent::class.java)) {

    @Entity
    @Table(name = "example_states")
    class PersistentConsent(
            @Column(name = "patient_id")
            var patientId: String,

            @Column(name = "professional_id")
            var professionalId: String,

            @Column(name = "organisation_id")
            var organisationId:String,

            @Column(name = "purpose")
            var purpose:String,

            @Column(name = "source")
            var source:String
    ) : PersistentState() {
        constructor() : this("", "", "", "", "")
    }
}