package ir.gchat

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.gchat.ui.theme.GChatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

private fun showNotification(
    context: Context,
    id: String,
    displayName: String,
    text: String,
    notificationId: Int = id.hashCode()
) {
    val channelId = "sms_messages"

    val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "SMS Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Incoming SMS messages"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )
        }
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP

        putExtra("open_sms_chat", true)
        putExtra("id", id)
        putExtra("displayName", displayName)
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_CANCEL_CURRENT
    )

    val person = Person.Builder()
        .setName(displayName)
        .build()

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.announcement_sms)
        .setContentTitle(displayName)
        .setContentText(text)
        .setStyle(
            NotificationCompat.MessagingStyle(person)
                .addMessage(text, System.currentTimeMillis(), person)
        )
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setShowWhen(true)
        .setWhen(System.currentTimeMillis())
        .setGroup("sms_$id")
        .build()

    notificationManager.notify(notificationId, notification)
}

// ======================== SMS Database Helper ========================

object SmsDbHelper {

    fun insertOutgoingSms(
        context: Context,
        address: String,
        body: String,
        date: Long = System.currentTimeMillis()
    ): Uri? {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, date)
            put(Telephony.Sms.DATE_SENT, date)
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_OUTBOX)
            put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_PENDING)
        }
        return try {
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateSmsStatus(
        context: Context,
        uri: Uri?,
        type: Int? = null,
        status: Int? = null
    ) {
        if (uri == null) return
        val values = ContentValues()
        type?.let { values.put(Telephony.Sms.TYPE, it) }
        status?.let { values.put(Telephony.Sms.STATUS, it) }
        if (values.size() == 0) return
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun getChatList(context: Context): List<Contact> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }

    val chatMap = linkedMapOf<String, Contact>()

    val projection = arrayOf(
        Telephony.Sms._ID,
        Telephony.Sms.ADDRESS,
        Telephony.Sms.BODY,
        Telephony.Sms.DATE,
        Telephony.Sms.TYPE,
        Telephony.Sms.READ
    )

    val cursor: Cursor? = try {
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    cursor?.use {
        val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
        val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
        val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
        val readIdx = it.getColumnIndex(Telephony.Sms.READ)

        while (it.moveToNext()) {
            val address = it.getString(addressIdx) ?: continue
            val normalized = normalizePhoneNumber(address)
            if (normalized.isBlank()) continue

            // فقط اولین (جدیدترین) پیام هر شماره را نگه می‌داریم
            if (chatMap.containsKey(normalized)) {
                // فقط unread را جمع می‌زنیم
                val existing = chatMap[normalized]!!
                val isInbox = it.getInt(typeIdx) == Telephony.Sms.MESSAGE_TYPE_INBOX
                val isUnread = it.getInt(readIdx) == 0
                if (isInbox && isUnread) {
                    chatMap[normalized] = existing.copy(
                        unreadMessages = existing.unreadMessages + 1
                    )
                }
                continue
            }

            val body = it.getString(bodyIdx) ?: ""
            val dateMillis = it.getLong(dateIdx)
            val isInbox = it.getInt(typeIdx) == Telephony.Sms.MESSAGE_TYPE_INBOX
            val isUnread = it.getInt(readIdx) == 0

            val name = getContactName(context, address) ?: address
            val photoUri = getContactPhotoUri(context, address)

            chatMap[normalized] = Contact(
                id = normalized,
                name = name,
                profilePicture = photoUri ?: "",
                lastMessageText = body,
                lastMessageDate = formatSmsDate(dateMillis),
                unreadMessages = if (isInbox && isUnread) 1 else 0,
                connectionStatus = false
            )
        }
    }

    return chatMap.values.toList()
}

/**
 * همه پیام‌های مربوط به یک شماره خاص را برمی‌گرداند
 * مناسب برای صفحه چت
 */
fun getMessagesForNumber(context: Context, phoneNumber: String): List<MessageItem> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }

    val normalizedTarget = normalizePhoneNumber(phoneNumber)
    if (normalizedTarget.isBlank()) return emptyList()

    val messages = mutableListOf<MessageItem>()

    val projection = arrayOf(
        Telephony.Sms._ID,
        Telephony.Sms.ADDRESS,
        Telephony.Sms.BODY,
        Telephony.Sms.DATE,
        Telephony.Sms.TYPE,
        Telephony.Sms.READ,
        Telephony.Sms.STATUS
    )

    val cursor: Cursor? = try {
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} ASC"   // قدیمی‌ترین اول
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    cursor?.use {
        val idIdx = it.getColumnIndex(Telephony.Sms._ID)
        val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
        val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
        val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
        val readIdx = it.getColumnIndex(Telephony.Sms.READ)
        val statusIdx = it.getColumnIndex(Telephony.Sms.STATUS)

        while (it.moveToNext()) {
            val address = it.getString(addressIdx) ?: continue
            if (normalizePhoneNumber(address) != normalizedTarget) continue

            val id = it.getInt(idIdx)
            val body = it.getString(bodyIdx) ?: ""
            val dateMillis = it.getLong(dateIdx)
            val type = it.getInt(typeIdx)
            val read = it.getInt(readIdx)
            val status = if (statusIdx >= 0) it.getInt(statusIdx) else -1

            val isMyMessage = type == Telephony.Sms.MESSAGE_TYPE_SENT ||
                    type == Telephony.Sms.MESSAGE_TYPE_OUTBOX ||
                    type == Telephony.Sms.MESSAGE_TYPE_FAILED

            // seen = true اگر خوانده شده یا وضعیت کامل باشد
            val seen = when {
                isMyMessage -> status == Telephony.Sms.STATUS_COMPLETE
                else -> read == 1
            }

            messages.add(
                MessageItem(
                    text = body,
                    id = id,
                    date = formatSmsDate(dateMillis),
                    myMessage = isMyMessage,
                    seen = seen
                )
            )
        }
    }

    return messages
}

// ======================== Helperهای کمکی ========================

//private fun normalizePhoneNumber(number: String): String {
//
//    var cleaned = number.replace(Regex("[^0-9]"), "")
//
//    if (cleaned.startsWith("98") && cleaned.length >= 12) {
//        // 989123456789
//    } else if (cleaned.startsWith("0") && cleaned.length == 11) {
//        cleaned = "98" + cleaned.substring(1) // 989123456789
//    } else if (cleaned.startsWith("9") && cleaned.length == 10) {
//        cleaned = "98$cleaned" // 989123456789
//    }
//
//    return cleaned
//}

private fun normalizePhoneNumber(number: String): String {
    if (number.any { it.isLetter() }) {
        return number
    }

    var cleaned = number.replace(Regex("[^0-9]"), "")

    if (cleaned.length <= 6) {
        return number
    }

    if (cleaned.startsWith("98") && cleaned.length >= 12) {
        // 989123456789
    } else if (cleaned.startsWith("0") && cleaned.length == 11) {
        cleaned = "98" + cleaned.substring(1) // 989123456789
    } else if (cleaned.startsWith("9") && cleaned.length == 10) {
        cleaned = "98$cleaned" // 989123456789
    }

    return cleaned
}

private fun formatSmsDate(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(millis))
}

private fun getContactName(context: Context, phoneNumber: String): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) return null

    return try {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                it.getString(0)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

private fun getContactPhotoUri(context: Context, phoneNumber: String): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) return null

    return try {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.PHOTO_URI),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                it.getString(0)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

// ======================== SMS Receiver ========================

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        if (!isDefaultSmsApp(context)) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        for (sms in messages) {
            val address = sms.originatingAddress ?: continue
            val body = sms.messageBody.orEmpty()
            val timestamp =
                if (sms.timestampMillis > 0) sms.timestampMillis
                else System.currentTimeMillis()

            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, timestamp)
                put(Telephony.Sms.DATE_SENT, timestamp)
                put(Telephony.Sms.READ, 0)
                put(Telephony.Sms.SEEN, 0)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
            }

            try {
                context.contentResolver.insert(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    values
                )
            } catch (e: Exception) {
            }

            val displayName = getContactName(context, address) ?: address

            showNotification(
                context = context,
                id = address,
                displayName = displayName,
                text = body,
                notificationId = address.hashCode()
            )
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }

        return null
    }
}

// ======================== MMS Receiver ========================

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) return
        if (!isDefaultSmsApp(context)) return

        if (intent.type != "application/vnd.wap.mms-message") return

        handleMms(context)
    }

    private fun handleMms(context: Context) {
        try {
            val cursor = context.contentResolver.query(
                Telephony.Mms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Mms._ID,
                    Telephony.Mms.DATE
                ),
                null,
                null,
                "${Telephony.Mms.DATE} DESC LIMIT 1"
            )

            cursor?.use {
                if (!it.moveToFirst()) return

                val mmsId = it.getLong(0)

                showNotification(
                    context = context,
                    id = mmsId.toString(),
                    displayName = "MMS",
                    text = "New multimedia message received",
                    notificationId = mmsId.toInt()
                )
            }
        } catch (e: Exception) {
        }
    }
}

// ======================== Headless SMS Send Service ========================

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isDefaultSmsApp(this) || !checkSmsAppRole(this)) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        intent?.let { handleSendSms(it) }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun handleSendSms(intent: Intent) {
        val phoneNumber = when {
            intent.data?.scheme == "smsto" || intent.data?.scheme == "sms" ->
                intent.data?.schemeSpecificPart

            intent.hasExtra("address") ->
                intent.getStringExtra("address")

            else -> null
        } ?: return

        val message = when {
            intent.hasExtra("sms_body") -> intent.getStringExtra("sms_body")
            intent.hasExtra(Intent.EXTRA_TEXT) -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return

        if (phoneNumber.isBlank() || message.isBlank()) return

        try {
            val smsManager = getSmsManager()
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSmsManager(): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SmsManager.getSmsManagerForSubscriptionId(
                SubscriptionManager.getDefaultSmsSubscriptionId()
            )
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }
}

// ======================== Sent / Delivered Receivers (Static) ========================

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uriString = intent.getStringExtra("sms_uri")
        val uri = uriString?.let { Uri.parse(it) }

        when (resultCode) {
            Activity.RESULT_OK -> {
                SmsDbHelper.updateSmsStatus(
                    context = context,
                    uri = uri,
                    type = Telephony.Sms.MESSAGE_TYPE_SENT,
                    status = Telephony.Sms.STATUS_PENDING
                )
            }

            SmsManager.RESULT_ERROR_GENERIC_FAILURE,
            SmsManager.RESULT_ERROR_NO_SERVICE,
            SmsManager.RESULT_ERROR_NULL_PDU,
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                SmsDbHelper.updateSmsStatus(
                    context = context,
                    uri = uri,
                    type = Telephony.Sms.MESSAGE_TYPE_FAILED,
                    status = Telephony.Sms.STATUS_FAILED
                )
            }

            else -> {
                SmsDbHelper.updateSmsStatus(
                    context = context,
                    uri = uri,
                    type = Telephony.Sms.MESSAGE_TYPE_FAILED,
                    status = Telephony.Sms.STATUS_FAILED
                )
            }
        }
    }
}

//class SmsDeliveredReceiver : BroadcastReceiver() {
//
//    override fun onReceive(context: Context, intent: Intent) {
//        val uri = intent.getStringExtra("sms_uri")?.let(Uri::parse)
//
//        when (resultCode) {
//            Activity.RESULT_OK -> {
//                SmsDbHelper.updateSmsStatus(
//                    context = context,
//                    uri = uri,
//                    status = Telephony.Sms.STATUS_COMPLETE
//                )
//
//                showNotification(
//                    context = context,
//                    id = "delivery",
//                    displayName = "Message delivered",
//                    text = "Your message has been delivered.",
//                    notificationId = uri?.hashCode() ?: 1
//                )
//            }
//
//            else -> {
//                SmsDbHelper.updateSmsStatus(
//                    context = context,
//                    uri = uri,
//                    status = Telephony.Sms.STATUS_FAILED
//                )
//
//                showNotification(
//                    context = context,
//                    id = "delivery_error",
//                    displayName = "Message not delivered",
//                    text = "The message could not be delivered.",
//                    notificationId = (uri?.hashCode() ?: 0) + 1
//                )
//            }
//        }
//    }
//}

class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uri = intent.getStringExtra("sms_uri")?.let(Uri::parse)
        val phoneNumber = intent.getStringExtra("phone_number") ?: ""

        // دریافت نام مخاطب برای نمایش بهتر
        val displayName = if (phoneNumber.isNotEmpty()) {
            getContactName(context, phoneNumber) ?: phoneNumber
        } else {
            "Message"
        }

        when (resultCode) {
            Activity.RESULT_OK -> {
                SmsDbHelper.updateSmsStatus(
                    context = context,
                    uri = uri,
                    status = Telephony.Sms.STATUS_COMPLETE
                )
                showNotification(
                    context = context,
                    id = phoneNumber.ifEmpty { "delivery" },
                    displayName = displayName,
                    text = "Message delivered successfully",
                    notificationId = if (phoneNumber.isNotEmpty()) {
                        phoneNumber.hashCode() + 1000
                    } else {
                        9999
                    }
                )
            }
            else -> {
                SmsDbHelper.updateSmsStatus(
                    context = context,
                    uri = uri,
                    status = Telephony.Sms.STATUS_FAILED
                )
                showNotification(
                    context = context,
                    id = phoneNumber.ifEmpty { "delivery_error" },
                    displayName = displayName,
                    text = "Message delivery failed",
                    notificationId = if (phoneNumber.isNotEmpty()) {
                        phoneNumber.hashCode() + 2000 // offset متفاوت برای خطاها
                    } else {
                        9998
                    }
                )
            }
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return null

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}

class SendSmsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.data?.schemeSpecificPart.orEmpty()
        val body = when {
            intent.hasExtra("sms_body") -> intent.getStringExtra("sms_body").orEmpty()
            intent.hasExtra(Intent.EXTRA_TEXT) -> intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            else -> ""
        }

        val viewModel: MainViewModel by viewModels()
        val smsViewModel: SmsChatViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            val theme by viewModel.theme.collectAsState()
            val darkTheme = when (theme) {
                0 -> isSystemInDarkTheme()
                1 -> true
                2 -> false
                else -> isSystemInDarkTheme()
            }
            // sms setup
            val context = LocalContext.current
            if (checkSmsAppRole(context)) {
                DisposableEffect(Unit) {
                    smsViewModel.init(context)
                    smsViewModel.refreshChatList()

                    onDispose {

                    }
                }
            }
            GChatTheme(dynamicColor = false, darkTheme = darkTheme) {
                val context = LocalContext.current
                SMSChatScreen(
                    back = {
                        (context as? Activity)?.finish()
                        true
                    },
                    id = phone,
                    draft = body,
                    displayName = getContactName(context, phone) ?: phone,
                    smsViewModel = smsViewModel
                )
                SetUpSystemBars(darkTheme)
            }
        }
    }

    private fun sendSms(context: Context, contact: String, msg: String) {
        if (contact.isBlank() || msg.isBlank()) return

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SmsManager.getSmsManagerForSubscriptionId(
                SubscriptionManager.getDefaultSmsSubscriptionId()
            )
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        // ۱. پیام را در دیتابیس سیستم ثبت می‌کنیم
        val messageUri = SmsDbHelper.insertOutgoingSms(context, contact, msg)

        val sentAction = "ir.gchat.SMS_SENT"
        val deliveredAction = "ir.gchat.SMS_DELIVERED"

        val sentIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            Intent(sentAction).apply {
                putExtra("sms_uri", messageUri?.toString())
                setPackage(context.packageName) // مهم برای Android 8+
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt() + 1,
            Intent(deliveredAction).apply {
                putExtra("sms_uri", messageUri?.toString())
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val parts = smsManager.divideMessage(msg)
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>()
                val deliveredIntents = ArrayList<PendingIntent>()

                parts.forEachIndexed { index, _ ->
                    sentIntents.add(
                        PendingIntent.getBroadcast(
                            context,
                            200 + index,
                            Intent(sentAction).apply {
                                putExtra("sms_uri", messageUri?.toString())
                                setPackage(context.packageName)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    deliveredIntents.add(
                        PendingIntent.getBroadcast(
                            context,
                            300 + index,
                            Intent(deliveredAction).apply {
                                putExtra("sms_uri", messageUri?.toString())
                                setPackage(context.packageName)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }

                smsManager.sendMultipartTextMessage(
                    contact, null, parts, sentIntents, deliveredIntents
                )
            } else {
                smsManager.sendTextMessage(
                    contact, null, msg, sentIntent, deliveredIntent
                )
            }
        } catch (e: Exception) {
            SmsDbHelper.updateSmsStatus(
                context = context,
                uri = messageUri,
                type = Telephony.Sms.MESSAGE_TYPE_FAILED,
                status = Telephony.Sms.STATUS_FAILED
            )
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

class SmsChatViewModel : ViewModel() {

    companion object {
        private val pendingIntentCounter = AtomicInteger(0)
    }

    private val _chatList = MutableStateFlow<List<Contact>>(emptyList())
    val chatList: StateFlow<List<Contact>> = _chatList

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages

    private var currentPhoneNumber: String = ""
    private var smsObserver: ContentObserver? = null
    private var context: Context? = null

    private var lastRefreshTime = 0L
    private val REFRESH_DEBOUNCE = 600L

    fun init(context: Context) {
        this.context = context.applicationContext
        startObservingSmsChanges()
        refreshChatList()
    }

    fun refreshChatList() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_DEBOUNCE) return
        lastRefreshTime = now

        viewModelScope.launch(Dispatchers.IO) {
            val ctx = context ?: return@launch
            val chats = getChatList(ctx)
            withContext(Dispatchers.Main) {
                _chatList.value = chats
            }
        }
    }

    fun loadMessages(phoneNumber: String) {
        currentPhoneNumber = phoneNumber
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = context ?: return@launch
            val list = getMessagesForNumber(ctx, phoneNumber)
            withContext(Dispatchers.Main) {
                _messages.value = list
            }
        }
    }

    fun sendMessage(context: Context, phoneNumber: String, message: String): Boolean {
        if (phoneNumber.isBlank() || message.isBlank()) return false

        return try {
            sendSmsInternal(context, phoneNumber, message)
            // بعد از ارسال فوری آپدیت می‌کنیم
            refreshChatList()
            if (normalizePhoneNumber(currentPhoneNumber) == normalizePhoneNumber(phoneNumber)) {
                loadMessages(phoneNumber)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun markMessageAsRead(context: Context, phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                markSmsAsReadInternal(context, phoneNumber)
                withContext(Dispatchers.Main) {
                    refreshChatList()
                    if (normalizePhoneNumber(currentPhoneNumber) == normalizePhoneNumber(phoneNumber)) {
                        loadMessages(phoneNumber)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startObservingSmsChanges() {
        val ctx = context ?: return

        smsObserver?.let {
            try {
                ctx.contentResolver.unregisterContentObserver(it)
            } catch (_: Exception) {
            }
        }

        smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                // هر تغییری در SMS → آپدیت
                refreshChatList()
                if (currentPhoneNumber.isNotEmpty()) {
                    loadMessages(currentPhoneNumber)
                }
            }
        }

        try {
            ctx.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                smsObserver!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendSmsInternal(context: Context, phoneNumber: String, message: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SmsManager.getSmsManagerForSubscriptionId(
                SubscriptionManager.getDefaultSmsSubscriptionId()
            )
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        val messageUri = SmsDbHelper.insertOutgoingSms(context, phoneNumber, message)

        val sentAction = "ir.gchat.SMS_SENT"
        val deliveredAction = "ir.gchat.SMS_DELIVERED"

        val baseId = pendingIntentCounter.incrementAndGet()

        val sentIntent = PendingIntent.getBroadcast(
            context,
            baseId,
            Intent(sentAction).apply {
                putExtra("sms_uri", messageUri?.toString())
                putExtra("phone_number", phoneNumber)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            baseId + 1_000_000,
            Intent(deliveredAction).apply {
                putExtra("sms_uri", messageUri?.toString())
                putExtra("phone_number", phoneNumber)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val parts = smsManager.divideMessage(message)
        if (parts.size > 1) {
            val sentIntents = ArrayList<PendingIntent>()
            val deliveredIntents = ArrayList<PendingIntent>()

            parts.forEachIndexed { index, _ ->
                sentIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        baseId + index,
                        Intent(sentAction).apply {
                            putExtra("sms_uri", messageUri?.toString())
                            setPackage(context.packageName)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                deliveredIntents.add(
                    PendingIntent.getBroadcast(
                        context,
                        baseId + 1_000_000 + index,
                        Intent(deliveredAction).apply {
                            putExtra("sms_uri", messageUri?.toString())
                            setPackage(context.packageName)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            smsManager.sendMultipartTextMessage(
                phoneNumber, null, parts, sentIntents, deliveredIntents
            )
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
        }
    }

    private fun markSmsAsReadInternal(context: Context, phoneNumber: String) {
        val normalized = normalizePhoneNumber(phoneNumber)
        if (normalized.isBlank()) return

        val values = ContentValues().apply {
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.SEEN, 1)
        }

        // استفاده از LIKE برای تطبیق بهتر شماره‌ها
        val where = "${Telephony.Sms.ADDRESS} LIKE ? AND ${Telephony.Sms.READ} = 0"
        val whereArgs = arrayOf("%$normalized%")

        try {
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                where,
                whereArgs
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        smsObserver?.let {
            try {
                context?.contentResolver?.unregisterContentObserver(it)
            } catch (_: Exception) {
            }
        }
        smsObserver = null
        context = null
    }
}

fun openContact(context: Context, phoneNumber: String) {
    try {
        // ابتدا شماره را نرمالایز می‌کنیم
        val normalizedNumber = normalizePhoneNumber(phoneNumber)

        // جستجوی مخاطب با شماره
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME
            ),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val contactId = it.getString(0)
                val displayName = it.getString(1)

                // باز کردن مخاطب در اپ Contacts
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_URI,
                        contactId
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                // اگر مخاطب در Contacts نبود، شماره را به صورت مستقیم باز کن
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback: باز کردن شماره در dialer
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}