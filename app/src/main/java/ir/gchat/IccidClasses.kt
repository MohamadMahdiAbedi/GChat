package ir.gchat

import android.Manifest
import android.app.Service
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList

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

        if (activeList.isNullOrEmpty()) {
            return emptyList()
        }

        val maxSlotIndex = activeList.maxOfOrNull { it.simSlotIndex } ?: -1
        if (maxSlotIndex < 0) return emptyList()

        val iccids = MutableList(maxSlotIndex + 1) { "" }

        for (subscription in activeList) {
            try {
                val iccid = subscription.iccId
                val slotIndex = subscription.simSlotIndex

                if (!iccid.isNullOrEmpty() && slotIndex >= 0 && slotIndex < iccids.size) {
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
//class IccidCollector(private val context: Context) {
//    fun getIccidsFromSubscriptionManager(): List<String> {
//        val iccids = mutableListOf("", "")
//        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//
//        try {
//            val activeList = subscriptionManager.activeSubscriptionInfoList
//            activeList?.forEach { subscription ->
//                try {
//                    val iccid = subscription.iccId
//                    val slotIndex = subscription.simSlotIndex
//
//                    if (!iccid.isNullOrEmpty() && slotIndex in 0..1) {
//                        iccids[slotIndex] = iccid
//                    }
//                } catch (e: SecurityException) {
//                }
//            }
//        } catch (e: SecurityException) {
//        }
//
//        return iccids
//    }
//}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // بررسی امنیتی برای Broadcast
        if (!isValidBroadcast(intent)) {
            return
        }

        // بررسی اینکه اپلیکیشن پیش‌فرض SMS هست
        if (!isDefaultSmsApp(context)) {
            return
        }

        // کد اصلی شما اینجا
        handleSms(context, intent)
    }

    private fun isValidBroadcast(intent: Intent): Boolean {
        // بررسی اینکه intent از سیستم هست نه از اپلیکیشن دیگه
        return intent.action == "android.provider.Telephony.SMS_DELIVER"
    }

    private fun isDefaultSmsApp(context: Context): Boolean {
        val packageName = context.packageName
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
        return packageName == defaultSms
    }

    private fun handleSms(context: Context, intent: Intent) {
        // منطق دریافت SMS
    }
}

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isValidBroadcast(intent)) {
            return
        }

        if (!isDefaultSmsApp(context)) {
            return
        }

        handleMms(context, intent)
    }

    private fun isValidBroadcast(intent: Intent): Boolean {
        return intent.action == "android.provider.Telephony.WAP_PUSH_DELIVER"
    }

    private fun isDefaultSmsApp(context: Context): Boolean {
        val packageName = context.packageName
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
        return packageName == defaultSms
    }

    private fun handleMms(context: Context, intent: Intent) {
        // منطق دریافت MMS
    }
}

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // بررسی اینکه اپلیکیشن پیش‌فرض SMS هست
        if (!isDefaultSmsApp()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // بررسی مجوزها
        if (!checkPhonePermission() || !checkSmsAppRole()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // پردازش intent
        intent?.let {
            handleSendSms(it)
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun isDefaultSmsApp(): Boolean {
        val packageName = packageName
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(this)
        return packageName == defaultSms
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkSmsAppRole(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        } else {
            true
        }
    }

    private fun handleSendSms(intent: Intent) {
        // منطق ارسال SMS با محدودیت‌های امنیتی
        // فقط به مخاطبین اجازه بدید یا شماره‌های خاص
    }
}

fun getICCIDList(context: Context): List<String> {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }

    return try {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        if (subscriptionInfoList.isNullOrEmpty()) {
            return emptyList()
        }

        val maxSlotIndex = subscriptionInfoList.maxOfOrNull { it.simSlotIndex } ?: -1
        if (maxSlotIndex < 0) return emptyList()

        val iccids = MutableList(maxSlotIndex + 1) { "" }

        for (subscriptionInfo in subscriptionInfoList) {
            val iccid = subscriptionInfo.iccId ?: ""
            val slotIndex = subscriptionInfo.simSlotIndex

            if (slotIndex in iccids.indices) {
                iccids[slotIndex] = iccid
            }
        }

        iccids
    } catch (_: SecurityException) {
        emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

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
        true
    }
}