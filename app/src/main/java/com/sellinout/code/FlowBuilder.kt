package com.sellinout.code

import com.sellinout.utils.isListAndEmpty
import com.sellzinout.network.AppException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.Response
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
abstract class NetworkResource<RESULT, REQUEST> {

    fun asFlow() = flow<Resource<RESULT>> {
        emit(Resource.loading())
        //if localFlow is not null, it means we are constantly observing database for changes
        val localFlow = fetchLocalFlow()
        try {
            val shouldFetch: Boolean

            if (localFlow != null) {
                val result = localFlow.first()
                val isEmptyList = result.isListAndEmpty()
                shouldFetch = shouldFetchFromRemote(result)
                if (!isEmptyList) emit(Resource.success(result))
            } else {
                val oneTimeData = fetchLocal()
                shouldFetch = shouldFetchFromRemote(oneTimeData)
                if (oneTimeData != null) {
                    emit(Resource.success(oneTimeData))
                }
            }

            if (!shouldFetch) return@flow

            emit(Resource.loading())
            val apiResponse = fetchFromRemote()
            val data = apiResponse.body()
            if (apiResponse.isSuccessful && data != null) {
                saveRemoteData(data)
                if (localFlow == null) {
                    //enough for database to be updated
                    delay(100)
                    //emission is not continuous, so data won't be delivered automatically
                    //so we trigger the data manually after storing it locally
                    emit(Resource.success(fetchLocal()))
                }
            } else {
                val ex = AppException(apiResponse.code())
                emit(Resource.error(ex.message, null, ex, apiResponse))
            }
        } catch (e: Exception) {
            val ex = AppException(e)
            emit(Resource.error(ex.message, null, ex))
        }

        if (localFlow != null) {
            emitAll(localFlow.map { Resource.success<RESULT>(it) })
        }
    }

    protected abstract suspend fun saveRemoteData(response: REQUEST)

    protected abstract fun fetchLocalFlow(): Flow<RESULT>?

    protected abstract fun fetchLocal(): RESULT?

    protected abstract suspend fun fetchFromRemote(): Response<REQUEST>

    protected abstract fun shouldFetchFromRemote(result: RESULT?): Boolean

}

abstract class LocalResource<T> {

    fun asFlow() = flow<Resource<T>> {
        emit(Resource.loading())
        //if localFlow is not null, it means we are constantly observing database for changes
        val localFlow = fetchLocalFlow()
        try {
            if (localFlow != null) {
                emit(Resource.success(localFlow.first()))
            } else {
                val oneTimeData = fetchLocal()
                if (oneTimeData != null) {
                    emit(Resource.success(oneTimeData))
                }
            }
        } catch (e: Exception) {
            val ex = AppException(e)
            emit(Resource.error(ex.message, null, ex))
        }

        if (localFlow != null) {
            emitAll(localFlow.map { Resource.success<T>(it) })
        }
    }

    protected abstract fun fetchLocalFlow(): Flow<T>?

    protected abstract fun fetchLocal(): T?

}

abstract class NetworkOnlyResource<RESULT, REQUEST> {

    fun asFlow() = flow<Resource<RESULT>> {
        emit(Resource.loading())
        try {
            val apiResponse = fetchFromRemote()
            val data = apiResponse.body()
            if (apiResponse.isSuccessful && data != null) {
                val result = map(data)
                emit(Resource.success(result))
            } else {
                val ex = AppException(apiResponse.code())
                emit(Resource.error(apiResponse.message(), null, ex, apiResponse))
            }
        } catch (e: Exception) {
            val ex = AppException(e)
            emit(Resource.error(ex.message, null, ex))
        }
    }

    protected abstract suspend fun fetchFromRemote(): Response<REQUEST>

    protected abstract suspend fun map(response: REQUEST): RESULT

}

abstract class NetworkOnlyWithOutMapper<RESULT> {

    fun asFlow() = flow<Resource<RESULT>> {
        emit(Resource.loading())
        try {
            val apiResponse = fetchFromRemote()
            val data = apiResponse.body()
            if (apiResponse.isSuccessful && data != null) {
                emit(Resource.success(data))
            } else {
                val ex = AppException(apiResponse.code())
                emit(Resource.error(apiResponse.message(), null, ex, apiResponse))
            }
        } catch (e: Exception) {
            val ex = AppException(e)
            emit(Resource.error(ex.message, null, ex))
            e.printStackTrace()
        }
    }

    protected abstract suspend fun fetchFromRemote(): Response<RESULT>


}