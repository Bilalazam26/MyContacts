package com.bilalazzam.mycontacts

import androidx.paging.PagingSource
import androidx.paging.PagingState

class BasePagingSource<T : Any>(private val fetchItems: suspend (page: Int, pageSize: Int) -> PagedData<T>) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: STARTING_PAGE_INDEX
        val pageSize = params.loadSize
        return try {
            val pagedData = fetchItems(page, pageSize)
            LoadResult.Page(
                data = pagedData.data,
                prevKey = if (page == STARTING_PAGE_INDEX) null else page.minus(1),
                nextKey = if (pagedData.isLastPage) null else page.plus(1)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }
}