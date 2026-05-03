package com.virt92.consolecollector.ui.navigation

object NavRoutes {
    const val Auth = "auth"
    const val Login = "auth/login"
    const val Register = "auth/register"
    const val Main = "main"
    const val ScanCamera = "scan/camera"
    const val ScanConfirm = "scan/confirm"
    const val ItemDetail = "item/{id}"
    fun itemDetail(id: String) = "item/$id"
}
