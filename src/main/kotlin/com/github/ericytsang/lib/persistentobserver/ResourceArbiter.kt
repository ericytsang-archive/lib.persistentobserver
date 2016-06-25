package com.github.ericytsang.lib.persistentobserver

import java.util.LinkedHashSet
import java.util.WeakHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * only one thread may be in the expanding phase at a time. the thread that
 * acquires this lock is allowed to enter an expanding phase and acquire its
 * resources. once they are finished acquiring resources (but not
 * necessarily using them), they must release this lock to allow another
 * thread to enter their expanding phase.
 */
private val lockingPhaseLock = ReentrantLock()

private val resourceToReaderThreads = WeakHashMap<Resource,MutableSet<Thread>>()

/**
 * acquires write locks of all [toWrite] [Resource]s, and all read locks
 * of all [toRead] [Resource]s before executing [block].
 */
fun <R> withResources(toRead:Set<Resource>,toWrite:Set<Resource>,block:()->R):R
{
    lockingPhaseLock.withLock()
    {
        toRead.forEach()
        {
            it.readWriteLock.readLock().lock()
            it.readerThreads.add(Thread.currentThread())
        }
        toWrite.forEach {it.readWriteLock.writeLock().lock()}
    }
    try
    {
        return block()
    }
    finally
    {
        toWrite.forEach {it.readWriteLock.writeLock().unlock()}
        toRead.forEach()
        {
            it.readWriteLock.readLock().unlock()
            it.readerThreads.remove(Thread.currentThread())
        }
    }
}

/**
 * represents a resource that can be mutated or read.
 */
interface Resource
{
    val readWriteLock:ReentrantReadWriteLock
}

val Resource.readerThreads:MutableSet<Thread> get() = resourceToReaderThreads.getOrPut(this,{LinkedHashSet()})

fun Resource.assertIsReadOrWriteLockedByCurrentThread()
{
    if (!(readWriteLock.isWriteLockedByCurrentThread || Thread.currentThread() in readerThreads))
    {
        throw IllegalStateException("resource not read or write locked by current thread")
    }
}

fun Resource.assertIsWriteLockedByCurrentThread()
{
    if (!readWriteLock.isWriteLockedByCurrentThread)
    {
        throw IllegalStateException("resource not write locked by current thread")
    }
}
