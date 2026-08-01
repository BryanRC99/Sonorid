// app/src/main/java/com/example/sonorid/util/NetworkStatus.kt
package com.example.sonorid.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Chequeo simple y puntual de conectividad, suficiente para decidir si
 * arrancar o no una operación que depende de internet (ej. descarga masiva
 * de letras). No es un observador continuo: se consulta bajo demanda. */
object NetworkStatus {
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}