package com.github.ericytsang.lib.persistentobserver

import java.io.Serializable
import java.util.LinkedHashMap
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * Created by surpl on 5/23/2016.
 */
class ObservableManager<Transaction:TransactionAdapter>(val executor:Executor,val transactionAdapterFactory:TransactionAdapterFactory<Transaction>,val persistenceStrategy:PersistenceStrategy<Transaction>)
{
    internal val namedFunctions = LinkedHashMap<String,NamedFunction<Transaction,Serializable>>()

    /**
     * queue of functions that need to be invoked as a result of
     * [Observable.notifyObservers].
     */
    private val commandQueue = LinkedBlockingQueue<Command>()

    inner class Command(val namedFunction:NamedFunction<Transaction,Serializable>,val argument:Serializable)

    /**
     * invoke named functions from the command queue forever once started
     */
    private val commandArbiterThread = thread(start = false,name = "commandArbiterThread",isDaemon = true)
    {
        while (true)
        {
            val namedFunction = commandQueue.take()
            executor.execute()
            {
                transactionAdapterFactory.make().doTransaction()
                {
                    transaction ->
                    namedFunction.namedFunction.block(transaction,namedFunction.argument)
                    namedFunction.namedFunction.name?.let()
                    {
                        persistenceStrategy.delete(transaction,it)
                    }
                }
            }
        }
    }

    fun start(transaction:Transaction)
    {
        // populate the command queue from persistent memory
        persistenceStrategy
            .selectAll(transaction)
            .forEach()
            {
                val namedFunction = namedFunctions[it.functionName]
                    ?: throw IllegalStateException("missing function for name: $it")
                commandQueue.add(Command(namedFunction,it.argument))
            }

        commandArbiterThread.start()
    }

    internal fun execute(transaction:Transaction,namedFunction:NamedFunction<Transaction,Serializable>,argument:Serializable)
    {
        namedFunction.name?.let()
        {
            persistenceStrategy.enqueue(transaction,it,argument)
        }
        commandQueue.add(Command(namedFunction,argument))
    }

    interface PersistenceStrategy<Transaction:TransactionAdapter>
    {
        fun enqueue(transaction:Transaction,functionName:String,argument:Serializable)
        fun delete(transaction:Transaction,functionName:String)
        fun selectAll(transaction:Transaction):List<Entry>
        data class Entry(val functionName:String,val argument:Serializable)
    }
}
