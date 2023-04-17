/*
 * Copyright 2020 RethinkDNS and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celzero.bravedns.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import com.celzero.bravedns.database.DnsLog
import com.celzero.bravedns.database.DnsLogDAO
import com.celzero.bravedns.ui.DnsLogFragment
import com.celzero.bravedns.util.Constants.Companion.LIVEDATA_PAGE_SIZE

class DnsLogViewModel(private val dnsLogDAO: DnsLogDAO) : ViewModel() {

    private var filteredList: MutableLiveData<String> = MutableLiveData()
    private var filterType = DnsLogFragment.DnsLogFilter.ALL
    private val pagingConfig =
            PagingConfig(
                enablePlaceholders = true,
                prefetchDistance = 3,
                initialLoadSize = LIVEDATA_PAGE_SIZE * 2,
                maxSize = LIVEDATA_PAGE_SIZE * 2,
                pageSize = LIVEDATA_PAGE_SIZE,
                jumpThreshold = 5
            )

    init {
        filteredList.value = ""
    }

    val dnsLogsList = filteredList.switchMap { input -> fetchDnsLogs(input) }

    private fun fetchDnsLogs(filter: String): LiveData<PagingData<DnsLog>> {
        return when (filterType) {
            DnsLogFragment.DnsLogFilter.ALL -> {
                getAllDnsLogs(filter)
            }
            DnsLogFragment.DnsLogFilter.ALLOWED -> {
                getAllowedDnsLogs(filter)
            }
            DnsLogFragment.DnsLogFilter.BLOCKED -> {
                getBlockedDnsLogs(filter)
            }
            DnsLogFragment.DnsLogFilter.MAYBE_BLOCKED -> {
                getMaybeBlockedDnsLogs(filter)
            }
        }
    }

    private fun getAllDnsLogs(filter: String): LiveData<PagingData<DnsLog>> {
        return Pager(pagingConfig) {
                if (filter.isEmpty()) {
                    dnsLogDAO.getAllDnsLogs()
                } else {
                    dnsLogDAO.getDnsLogsByName("%$filter%")
                }
            }
            .liveData
            .cachedIn(viewModelScope)
    }

    private fun getAllowedDnsLogs(filter: String): LiveData<PagingData<DnsLog>> {
        return Pager(pagingConfig) {
                if (filter.isEmpty()) {
                    dnsLogDAO.getAllowedDnsLogs()
                } else {
                    dnsLogDAO.getAllowedDnsLogsByName("%$filter%")
                }
            }
            .liveData
            .cachedIn(viewModelScope)
    }

    private fun getBlockedDnsLogs(filter: String): LiveData<PagingData<DnsLog>> {
        return Pager(pagingConfig) {
                if (filter.isEmpty()) {
                    dnsLogDAO.getBlockedDnsLogs()
                } else {
                    dnsLogDAO.getBlockedDnsLogsByName("%$filter%")
                }
            }
            .liveData
            .cachedIn(viewModelScope)
    }

    private fun getMaybeBlockedDnsLogs(filter: String): LiveData<PagingData<DnsLog>> {
        return Pager(pagingConfig) {
                if (filter.isEmpty()) {
                    dnsLogDAO.getMaybeBlockedDnsLogs()
                } else {
                    dnsLogDAO.getMaybeBlockedDnsLogsByName("%$filter%")
                }
            }
            .liveData
            .cachedIn(viewModelScope)
    }

    fun setFilter(searchString: String, type: DnsLogFragment.DnsLogFilter) {
        filterType = type

        if (searchString.isNotBlank()) filteredList.value = searchString
        else filteredList.value = ""
    }
}
