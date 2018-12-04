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

package nl.nuts.consent.contract

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.nuts.consent.state.ConsentState
import org.junit.Test

// this file includes some example tests, they do not really have something yet to do with consent functionality
class ConsentContractTests {
    private val ledgerServices = MockServices()
    private val homeCare = TestIdentity(CordaX500Name("homeCare", "Groenlo", "NL"))
    private val generalCare = TestIdentity(CordaX500Name("GP", "Groenlo", "NL"))

    val bsn = "123456782"
    val agb = "01000100"
    val cc = "NE0001"
    val source = "DOCUMENT"

    @Test
    fun `happy flow`() {
        ledgerServices.ledger {
            transaction {
                output(
                        ConsentContract.CONTRACT_ID,
                        ConsentState(bsn, agb, cc, "share information with GP", source, listOf(homeCare.party, generalCare.party))
                )
                command(
                        listOf(homeCare.publicKey, generalCare.publicKey),
                        ConsentContract.Commands.Create()
                )
                verifies()
            }
        }
    }

    @Test
    fun `create transaction must have no inputs`() {
        ledgerServices.ledger {
            transaction {
                input(
                        ConsentContract.CONTRACT_ID,
                        ConsentState(bsn, agb, cc, "share information with GP", source, listOf(homeCare.party, generalCare.party))
                )
                output(
                        ConsentContract.CONTRACT_ID,
                        ConsentState(bsn, agb, cc, "share information with GP", source, listOf(homeCare.party, generalCare.party))
                )
                command(
                        listOf(homeCare.publicKey, generalCare.publicKey),
                        ConsentContract.Commands.Create()
                )
                `fails with`("No input states are consumed.")
            }
        }
    }

    @Test
    fun `create transaction must have one output`() {
        ledgerServices.ledger {
            transaction {
                output(
                        ConsentContract.CONTRACT_ID,
                        ConsentState(bsn, agb, cc, "share information with GP", source, listOf(homeCare.party, generalCare.party))
                )
                output(
                        ConsentContract.CONTRACT_ID,
                        ConsentState(bsn, agb, cc, "share information with GP", source, listOf(homeCare.party, generalCare.party))
                )
                command(
                        listOf(homeCare.publicKey, generalCare.publicKey),
                        ConsentContract.Commands.Create()
                )
                `fails with`("Only one output state should be created.")
            }
        }
    }
}