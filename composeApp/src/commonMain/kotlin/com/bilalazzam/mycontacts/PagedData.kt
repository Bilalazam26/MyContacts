package com.bilalazzam.mycontacts

data class PagedData<T>(
    val data: List<T>,
    val totalItems: Int,
    val isLastPage: Boolean,
)