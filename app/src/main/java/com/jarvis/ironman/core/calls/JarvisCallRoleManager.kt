package com.jarvis.ironman.core.calls

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build

class JarvisCallRoleManager(context: Context) {

    private val appContext = context.applicationContext

    fun isCallScreeningAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val roleManager =
            appContext.getSystemService(RoleManager::class.java)
                ?: return false

        return roleManager.isRoleAvailable(
            RoleManager.ROLE_CALL_SCREENING
        )
    }

    fun isCallScreeningEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val roleManager =
            appContext.getSystemService(RoleManager::class.java)
                ?: return false

        return roleManager.isRoleHeld(
            RoleManager.ROLE_CALL_SCREENING
        )
    }

    fun createPermissionRequest(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val roleManager =
            appContext.getSystemService(RoleManager::class.java)
                ?: return null

        if (!roleManager.isRoleAvailable(
                RoleManager.ROLE_CALL_SCREENING
            )
        ) return null

        return roleManager.createRequestRoleIntent(
            RoleManager.ROLE_CALL_SCREENING
        )
    }
}
