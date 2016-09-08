package com.github.ericytsang.lib.persistentobserver

import com.github.ericytsang.lib.observe.KeylessChange
import com.github.ericytsang.lib.observe.ObservableSet
import com.github.ericytsang.lib.observe.SimpleObservableSet
import java.io.Serializable
import java.util.LinkedHashSet

/**
 * Created by surpl on 5/23/2016.
 */
// todo add a setting somehow that will allow a function to be only actually executed if the execution token is the last one in the execution queue
class Observable<Transaction:TransactionAdapter,Argument:Serializable>(val observableManager:ObservableManager<Transaction>)
{
    /**
     * set of function to invoke when [notifyObservers] is called.
     */
    val observers:MutableSet<NamedFunction<Transaction,Argument>> = run()
    {
        // create observable set...
        val observableSet:ObservableSet<NamedFunction<Transaction,Serializable>> = SimpleObservableSet(LinkedHashSet())

        // add listener to observable set so that if a value is added, it is
        // also registered with the observable manager's map of named functions
        observableSet.observers += KeylessChange.Observer.new()
        {
            change ->
            change.added.forEach()
            {
                namedFunction ->
                namedFunction.name?.let()
                {
                    observableManager.namedFunctions.put(it,namedFunction)
                }
            }
            Unit
        }
        @Suppress("UNCHECKED_CAST")
        return@run observableSet as MutableSet<NamedFunction<Transaction,Argument>>
    }

    fun notifyObservers(transaction:Transaction,argument:Argument)
    {
        @Suppress("UNCHECKED_CAST")
        (observers as Set<NamedFunction<Transaction,Serializable>>).forEach()
        {
            namedFunction ->
            observableManager.postForExecution(transaction,namedFunction,argument)
        }
    }
}
