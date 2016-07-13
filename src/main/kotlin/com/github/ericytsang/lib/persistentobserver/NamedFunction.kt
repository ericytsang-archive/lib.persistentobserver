package com.github.ericytsang.lib.persistentobserver

import java.io.Serializable

/**
 * Created by surpl on 5/23/2016.
 */
interface NamedFunction<Transaction:TransactionAdapter,Argument:Serializable>
{
    val name:String?
    val block:(transaction:Transaction,argument:Argument) -> Unit
    val shouldEnqueue:(list:List<ObservableManager.Command<Transaction>>,command:ObservableManager.Command<Transaction>,argument:Argument) -> Boolean
}

class SimpleNamedFunction<Transaction:TransactionAdapter,Argument:Serializable>(
    override val name:String?,
    override val block:(transaction:Transaction,argument:Argument) -> Unit)
:NamedFunction<Transaction,Argument>
{
    override val shouldEnqueue = fun(list:List<ObservableManager.Command<Transaction>>,command:ObservableManager.Command<Transaction>,argument:Argument):Boolean
    {
        return true
    }
}

class StaticNamedFunction<Transaction:TransactionAdapter,Argument:Serializable>(
    override val name:String?,
    override val block:(transaction:Transaction,argument:Argument) -> Unit)
:NamedFunction<Transaction,Argument>
{
    override val shouldEnqueue = fun(list:List<ObservableManager.Command<Transaction>>,command:ObservableManager.Command<Transaction>,argument:Argument):Boolean
    {
        return !list.contains(command)
    }
}

class NoArgument private constructor():Serializable
{
    companion object
    {
        val instance = NoArgument()
    }
}

val noArgument = NoArgument.instance
