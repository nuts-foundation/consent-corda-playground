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

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.nuts.consent.state.ConsentState

class ConsentContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val CONTRACT_ID = "nl.nuts.consent.contract.ConsentContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output state
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        requireThat {
            // generic contract constraints
            "No input states are consumed." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<ConsentState>().single()
            "All parties are unique." using (out.parties.toSet().size == out.parties.size)
            "All participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

            // no specific contstraints yet
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
    }
}