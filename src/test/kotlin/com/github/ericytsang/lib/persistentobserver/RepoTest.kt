package com.github.ericytsang.lib.persistentobserver

import org.junit.Test
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Eric Tsang on 12/7/2015.
 */
val executorService = ThreadPoolExecutor(1,1,1,TimeUnit.SECONDS,LinkedBlockingQueue())
val transactionFactory = object:TransactionAdapterFactory<SerializedPrintTransactionAdapter>
{
    override fun make():SerializedPrintTransactionAdapter
    {
        return SerializedPrintTransactionAdapter()
    }
}

val persistenceStrategy = object:ObservableManager.PersistenceStrategy<SerializedPrintTransactionAdapter>
{
    override fun enqueue(transaction:SerializedPrintTransactionAdapter,functionName:String,argument:Serializable)
    {
        println("enqueue($functionName)")
    }

    override fun delete(transaction:SerializedPrintTransactionAdapter,functionName:String)
    {
        println("delete($functionName)")
    }

    override fun selectAll(transaction:SerializedPrintTransactionAdapter):List<ObservableManager.PersistenceStrategy.Entry>
    {
        println("selectAll()")
        return listOf(
            ObservableManager.PersistenceStrategy.Entry("function1",noArgument),
            ObservableManager.PersistenceStrategy.Entry("function2",noArgument),
            ObservableManager.PersistenceStrategy.Entry("function3",noArgument))
    }
}

class RepoTest
{
    @Test
    fun mainTest()
    {
        val observableManager = ObservableManager(executorService,transactionFactory,persistenceStrategy)
        val observable1 = Observable<SerializedPrintTransactionAdapter,NoArgument>(observableManager)
        val observable2 = Observable<SerializedPrintTransactionAdapter,NoArgument>(observableManager)
        val observable3 = Observable<SerializedPrintTransactionAdapter,NoArgument>(observableManager)
        val observable4 = Observable<SerializedPrintTransactionAdapter,NoArgument>(observableManager)
        val observable5 = Observable<SerializedPrintTransactionAdapter,NoArgument>(observableManager)

        val function1 = StaticNamedFunction<SerializedPrintTransactionAdapter,NoArgument>("function1") {transaction,args -> println("invoked function1")}
        val function2 = SimpleNamedFunction<SerializedPrintTransactionAdapter,NoArgument>("function2") {transaction,args -> println("invoked function2")}
        val function3 = StaticNamedFunction<SerializedPrintTransactionAdapter,NoArgument>("function3") {transaction,args -> println("invoked function3")}
        val function4 = SimpleNamedFunction<SerializedPrintTransactionAdapter,NoArgument>("function4") {transaction,args -> println("invoked function4")}
        val function5 = SimpleNamedFunction<SerializedPrintTransactionAdapter,NoArgument>("function5") {transaction,args -> println("invoked function5")}

        observable1.observers.add(function1)
        observable2.observers.add(function2)
        observable3.observers.add(function3)
        observable4.observers.add(function4)
        observable5.observers.add(function5)

        transactionFactory.make().doTransaction()
        {
            observableManager.start(it)
        }

        transactionFactory.make().doTransaction()
        {
            observable1.notifyObservers(it,noArgument)
            observable2.notifyObservers(it,noArgument)
            observable3.notifyObservers(it,noArgument)
            observable4.notifyObservers(it,noArgument)
            observable5.notifyObservers(it,noArgument)
        }
        Thread.sleep(1000)
    }
}

class SerializedPrintTransactionAdapter:TransactionAdapter
{
    companion object
    {
        val lock = ReentrantLock()
    }

    override fun begin()
    {
        lock.lock()
        println("begin")
    }

    override fun commit()
    {
        println("commit")
    }

    override fun end()
    {
        println("end")
        lock.unlock()
    }
}
