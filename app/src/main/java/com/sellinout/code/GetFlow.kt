package com.sellinout.code

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun <RESULT, REQUEST> getFlow(
    localFlow: (() -> Flow<RESULT>)? = null,
    local: (() -> RESULT?)? = null,
    remote: suspend () -> Response<REQUEST>,
    saveToDb: suspend (REQUEST) -> Unit,
    forceRefresh: ((RESULT?) -> Boolean)
): Flow<Resource<RESULT>> {
    return object : NetworkResource<RESULT, REQUEST>() {
        override suspend fun saveRemoteData(response: REQUEST) {
            saveToDb.invoke(response)
        }

        override fun fetchLocalFlow(): Flow<RESULT>? {
            return localFlow?.invoke()
        }

        override fun fetchLocal(): RESULT? {
            return local?.invoke()
        }

        override suspend fun fetchFromRemote(): Response<REQUEST> {
            return remote.invoke()
        }

        override fun shouldFetchFromRemote(result: RESULT?): Boolean {
            return forceRefresh.invoke(result)
        }
    }.asFlow().flowOn(Dispatchers.IO)
}

fun <T> getFlow(
    localFlow: (() -> Flow<T>)? = null,
    local: (() -> T?)? = null,
): Flow<Resource<T>> {
    return object : LocalResource<T>() {
        override fun fetchLocalFlow(): Flow<T>? {
            return localFlow?.invoke()
        }

        override fun fetchLocal(): T? {
            return local?.invoke()
        }
    }.asFlow().flowOn(Dispatchers.IO)
}

fun <RESULT, REQUEST> getRemoteFlow(
    remote: suspend () -> Response<REQUEST>,
    mapper: suspend (REQUEST) -> RESULT
): Flow<Resource<RESULT>> {
    return object : NetworkOnlyResource<RESULT, REQUEST>() {
        override suspend fun fetchFromRemote(): Response<REQUEST> {
            return remote.invoke()
        }

        override suspend fun map(response: REQUEST): RESULT {
            return mapper.invoke(response)
        }
    }.asFlow().flowOn(Dispatchers.IO)
}

fun <RESULT> getRemoteFlow(
    remote: suspend () -> Response<RESULT>
): Flow<Resource<RESULT>> {
    return object : NetworkOnlyWithOutMapper<RESULT>() {
        override suspend fun fetchFromRemote(): Response<RESULT> {
            return remote.invoke()
        }
    }.asFlow().flowOn(Dispatchers.IO)
}