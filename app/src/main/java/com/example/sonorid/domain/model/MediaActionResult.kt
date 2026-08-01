package com.example.sonorid.domain.model

import android.content.IntentSender

/** Resultado de una operación sobre MediaStore (eliminar/editar) que puede
 * requerir consentimiento explícito del usuario en Android 10+. */
sealed class MediaActionResult {
    object Success : MediaActionResult()
    data class RequiresPermission(val intentSender: IntentSender) : MediaActionResult()
    data class Error(val message: String) : MediaActionResult()
}