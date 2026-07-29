package ir.gchat

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

fun getIccidsFromSubscriptionManager(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }
    return try {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val activeList = subscriptionManager.activeSubscriptionInfoList
        if (activeList.isNullOrEmpty()) return emptyList()
        val maxSlotIndex = activeList.maxOfOrNull { it.simSlotIndex } ?: -1
        if (maxSlotIndex < 0) return emptyList()
        val iccids = MutableList(maxSlotIndex + 1) { "" }
        for (subscription in activeList) {
            try {
                val iccid = subscription.iccId
                val slotIndex = subscription.simSlotIndex
                if (!iccid.isNullOrEmpty() && slotIndex in iccids.indices) {
                    iccids[slotIndex] = iccid
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
        iccids
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun getICCIDList(context: Context): List<String> = getIccidsFromSubscriptionManager(context)

fun checkPhonePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED
}

fun checkSmsAppRole(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_SMS)
    } else {
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }
}

fun isDefaultSmsApp(context: Context): Boolean {
    return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
}

fun requestSmsDefaultRole(context: Context) {
    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
        putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
    }
    context.startActivity(intent)
}