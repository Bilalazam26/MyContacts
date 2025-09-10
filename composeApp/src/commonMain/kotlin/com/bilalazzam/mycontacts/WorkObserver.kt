package com.bilalazzam.mycontacts


sealed class WorkStatus {
    data object Idle : WorkStatus()
    data object Succeeded : WorkStatus()
    data object Failed : WorkStatus()
    data object Running : WorkStatus()
}
