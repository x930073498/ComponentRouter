package com.x930073498.component.architecture

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

//abstract class NetworkBoundResource<ResultType, RequestType> {
//   @WorkerThread
//   protected abstract suspend fun saveCallResult(item: RequestType)
//
//   @MainThread
//   protected abstract fun shouldFetch(data: ResultType?): Boolean
//
//   @MainThread
//   protected abstract suspend fun loadFromLocal(): Flow<ResultType>
//
//   @MainThread
//   protected abstract fun createCall(): Flow<ApiResponse<RequestType>>
//
//
//   protected open fun onFetchFailed() {}
//}
