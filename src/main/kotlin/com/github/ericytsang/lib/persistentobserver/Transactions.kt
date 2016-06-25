package com.github.ericytsang.lib.persistentobserver

/**
 * Created by Eric Tsang on 12/31/2015.
 */

interface TransactionAdapter
{
    fun begin()
    fun commit()
    fun end()
}

interface TransactionAdapterFactory<Transaction:TransactionAdapter>
{
    fun make():Transaction
}

inline fun <Transaction:TransactionAdapter,R> Transaction.doTransaction(transaction:(Transaction)->R):R
{
    begin()
    try
    {
        val result = transaction(this)
        commit()
        return result
    }
    finally
    {
        end()
    }
}
