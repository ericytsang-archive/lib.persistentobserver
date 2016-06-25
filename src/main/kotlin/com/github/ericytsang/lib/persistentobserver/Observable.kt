package com.github.ericytsang.lib.persistentobserver

import com.github.ericytsang.lib.collections.ObservableSet
import java.io.Serializable
import java.util.LinkedHashSet

/**
 * Created by surpl on 5/23/2016.
 */
class Observable<Transaction:TransactionAdapter,Argument:Serializable>(val observableManager:ObservableManager<Transaction>)
{
    /**
     * set of function to invoke when [notifyObservers] is called.
     */
    val observers:MutableSet<NamedFunction<Transaction,Argument>> = run()
    {
        // create observable set...
        val observableSet = ObservableSet<NamedFunction<Transaction,Serializable>>(LinkedHashSet())

        // add listener to observable set so that if a value is added, it is
        // also registered with the observable manager's map of named functions
        observableSet.observers.add()
        {
            change ->

            if (change.wasAdded)
            {
                val namedFunction = change.valueAdded!!
                namedFunction.name?.let()
                {
                    observableManager.namedFunctions.put(it,namedFunction)
                }
            }
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
            observableManager.execute(transaction,namedFunction,argument)
        }
    }
}
