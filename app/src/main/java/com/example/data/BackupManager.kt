package com.vodafone.smc.data

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {

    private const val FILE_HEADER = "SMCVF_V1"

    private fun deriveKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plain text string of database data with the password
     */
    fun encryptData(plainText: String, password: String): String {
        val keySpec = deriveKey(password)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        // Prepended IV + Encrypted bytes combined
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        val payloadBase64 = Base64.encodeToString(combined, Base64.NO_WRAP)
        return "$FILE_HEADER\n$payloadBase64"
    }

    /**
     * Decrypts backup string of database data using the password
     */
    fun decryptData(fileContent: String, password: String): String {
        val cleanContent = fileContent.trim()
        if (!cleanContent.startsWith(FILE_HEADER)) {
            throw IllegalArgumentException("Mismatch signature! Not a valid SMC Vodafone backup (.smcvf) file.")
        }
        
        val payloadBase64 = cleanContent.removePrefix(FILE_HEADER).trim()
        val combined = Base64.decode(payloadBase64, Base64.NO_WRAP)
        if (combined.size < 16) {
            throw IllegalArgumentException("Backup package is corrupted or incomplete.")
        }
        
        val iv = ByteArray(16)
        System.arraycopy(combined, 0, iv, 0, 16)
        
        val cipherTextSize = combined.size - 16
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(combined, 16, cipherText, 0, cipherTextSize)
        
        val ivSpec = IvParameterSpec(iv)
        val keySpec = deriveKey(password)
        
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        
        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Serializes all tables into a single plaintext JSON structure
     */
    fun serializeToText(
        problems: List<PCProblem>,
        itars: List<ITARRequest>,
        vpns: List<VPNRequest>,
        nts: List<NTCreation>,
        reminders: List<Reminder>,
        tasks: List<ITTask>,
        logs: List<AuditLog>,
        notes: List<Note>,
        users: List<User>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val pcArr = JSONArray()
        problems.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("employeeName", it.employeeName)
            item.put("username", it.username)
            item.put("site", it.site)
            item.put("zone", it.zone)
            item.put("pcName", it.pcName)
            item.put("assetNumber", it.assetNumber)
            item.put("category", it.category)
            item.put("priority", it.priority)
            item.put("status", it.status)
            item.put("detailedSummary", it.detailedSummary)
            item.put("assignedEngineer", it.assignedEngineer)
            item.put("assignedTeam", it.assignedTeam)
            item.put("resolutionNotes", it.resolutionNotes)
            item.put("createdAt", it.createdAt)
            pcArr.put(item)
        }
        root.put("pc_problems", pcArr)

        val itarArr = JSONArray()
        itars.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("employeeName", it.employeeName)
            item.put("username", it.username)
            item.put("requestType", it.requestType)
            item.put("status", it.status)
            item.put("implementer", it.implementer)
            item.put("systemOwner", it.systemOwner)
            item.put("startDate", it.startDate)
            item.put("endDate", it.endDate)
            item.put("rejectionReason", it.rejectionReason)
            item.put("internalNotes", it.internalNotes)
            item.put("site", it.site)
            item.put("createdAt", it.createdAt)
            itarArr.put(item)
        }
        root.put("itar_requests", itarArr)

        val vpnArr = JSONArray()
        vpns.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("employeeName", it.employeeName)
            item.put("username", it.username)
            item.put("vpnType", it.vpnType)
            item.put("status", it.status)
            item.put("notes", it.notes)
            item.put("createdAt", it.createdAt)
            vpnArr.put(item)
        }
        root.put("vpn_requests", vpnArr)

        val ntArr = JSONArray()
        nts.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("employeeName", it.employeeName)
            item.put("employeeId", it.employeeId)
            item.put("personalNumber", it.personalNumber)
            item.put("department", it.department)
            item.put("jobTitle", it.jobTitle)
            item.put("managerName", it.managerName)
            item.put("site", it.site)
            item.put("status", it.status)
            item.put("notes", it.notes)
            item.put("date", it.date)
            item.put("actionDate", it.actionDate)
            item.put("createdAt", it.createdAt)
            ntArr.put(item)
        }
        root.put("nt_creations", ntArr)

        val remArr = JSONArray()
        reminders.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("title", it.title)
            item.put("description", it.description)
            item.put("dueDate", it.dueDate)
            item.put("escalateAfterMin", it.escalateAfterMin)
            item.put("priority", it.priority)
            item.put("status", it.status)
            item.put("createdAt", it.createdAt)
            item.put("startHour", it.startHour)
            item.put("endHour", it.endHour)
            item.put("repeatIntervalMin", it.repeatIntervalMin)
            item.put("soundType", it.soundType)
            item.put("vibrationEnabled", it.vibrationEnabled)
            item.put("notificationEnabled", it.notificationEnabled)
            remArr.put(item)
        }
        root.put("reminders", remArr)

        val taskArr = JSONArray()
        tasks.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("title", it.title)
            item.put("description", it.description)
            item.put("priority", it.priority)
            item.put("dueDate", it.dueDate)
            item.put("status", it.status)
            item.put("isRecurring", it.isRecurring)
            item.put("createdAt", it.createdAt)
            taskArr.put(item)
        }
        root.put("it_tasks", taskArr)

        val logArr = JSONArray()
        logs.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("actorEmail", it.actorEmail)
            item.put("actionType", it.actionType)
            item.put("details", it.details)
            item.put("timestamp", it.timestamp)
            logArr.put(item)
        }
        root.put("audit_logs", logArr)

        val noteArr = JSONArray()
        notes.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("title", it.title)
            item.put("content", it.content)
            item.put("category", it.category)
            item.put("timestamp", it.timestamp)
            noteArr.put(item)
        }
        root.put("notes", noteArr)

        val userArr = JSONArray()
        users.forEach {
            val item = JSONObject()
            item.put("id", it.id)
            item.put("username", it.username)
            item.put("passwordHash", it.passwordHash)
            item.put("role", it.role)
            userArr.put(item)
        }
        root.put("users", userArr)

        return root.toString()
    }

    /**
     * Parses decrypted text back into individual typed entities
     */
    fun parseDecrypted(plainText: String): DecryptedBackupPayload {
        val root = JSONObject(plainText)
        
        val problems = mutableListOf<PCProblem>()
        val itars = mutableListOf<ITARRequest>()
        val vpns = mutableListOf<VPNRequest>()
        val nts = mutableListOf<NTCreation>()
        val reminders = mutableListOf<Reminder>()
        val tasks = mutableListOf<ITTask>()
        val logs = mutableListOf<AuditLog>()
        val notes = mutableListOf<Note>()
        val users = mutableListOf<User>()

        root.optJSONArray("pc_problems")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                problems.add(
                    PCProblem(
                        id = obj.optInt("id", 0),
                        employeeName = obj.optString("employeeName", ""),
                        username = obj.optString("username", ""),
                        site = obj.optString("site", ""),
                        zone = obj.optString("zone", ""),
                        pcName = obj.optString("pcName", ""),
                        assetNumber = obj.optString("assetNumber", ""),
                        category = obj.optString("category", ""),
                        priority = obj.optString("priority", "Low"),
                        status = obj.optString("status", "Open"),
                        detailedSummary = obj.optString("detailedSummary", ""),
                        assignedEngineer = obj.optString("assignedEngineer", "Unassigned"),
                        assignedTeam = obj.optString("assignedTeam", "Help Desk"),
                        resolutionNotes = obj.optString("resolutionNotes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("itar_requests")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                itars.add(
                    ITARRequest(
                        id = obj.optInt("id", 0),
                        employeeName = obj.optString("employeeName", ""),
                        username = obj.optString("username", ""),
                        requestType = obj.optString("requestType", "New Access"),
                        status = obj.optString("status", "Pending"),
                        implementer = obj.optString("implementer", ""),
                        systemOwner = obj.optString("systemOwner", ""),
                        startDate = obj.optString("startDate", ""),
                        endDate = obj.optString("endDate", ""),
                        rejectionReason = obj.optString("rejectionReason", ""),
                        internalNotes = obj.optString("internalNotes", ""),
                        site = obj.optString("site", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("vpn_requests")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                vpns.add(
                    VPNRequest(
                        id = obj.optInt("id", 0),
                        employeeName = obj.optString("employeeName", ""),
                        username = obj.optString("username", ""),
                        vpnType = obj.optString("vpnType", "GlobalProtect"),
                        status = obj.optString("status", "Open"),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("nt_creations")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                nts.add(
                    NTCreation(
                        id = obj.optInt("id", 0),
                        employeeName = obj.optString("employeeName", ""),
                        employeeId = obj.optString("employeeId", ""),
                        personalNumber = obj.optString("personalNumber", ""),
                        department = obj.optString("department", ""),
                        jobTitle = obj.optString("jobTitle", ""),
                        managerName = obj.optString("managerName", ""),
                        site = obj.optString("site", ""),
                        status = obj.optString("status", "Pending"),
                        notes = obj.optString("notes", ""),
                        date = obj.optString("date", ""),
                        actionDate = obj.optString("actionDate", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("reminders")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                reminders.add(
                    Reminder(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        dueDate = obj.optString("dueDate", ""),
                        escalateAfterMin = obj.optInt("escalateAfterMin", 60),
                        priority = obj.optString("priority", "Medium"),
                        status = obj.optString("status", "Active"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        startHour = obj.optInt("startHour", 7),
                        endHour = obj.optInt("endHour", 19),
                        repeatIntervalMin = obj.optInt("repeatIntervalMin", 60),
                        soundType = obj.optString("soundType", "Default"),
                        vibrationEnabled = obj.optBoolean("vibrationEnabled", true),
                        notificationEnabled = obj.optBoolean("notificationEnabled", true)
                    )
                )
            }
        }

        root.optJSONArray("it_tasks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                tasks.add(
                    ITTask(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        priority = obj.optString("priority", "Medium"),
                        dueDate = obj.optString("dueDate", ""),
                        status = obj.optString("status", "To Do"),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("audit_logs")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                logs.add(
                    AuditLog(
                        id = obj.optInt("id", 0),
                        actorEmail = obj.optString("actorEmail", ""),
                        actionType = obj.optString("actionType", ""),
                        details = obj.optString("details", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("notes")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                notes.add(
                    Note(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        category = obj.optString("category", "Info"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("users")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                users.add(
                    User(
                        id = obj.optInt("id", 0),
                        username = obj.optString("username", ""),
                        passwordHash = obj.optString("passwordHash", ""),
                        role = obj.optString("role", "Viewer")
                    )
                )
            }
        }

        return DecryptedBackupPayload(
            problems = problems,
            itars = itars,
            vpns = vpns,
            nts = nts,
            reminders = reminders,
            tasks = tasks,
            logs = logs,
            notes = notes,
            users = users
        )
    }
}

data class DecryptedBackupPayload(
    val problems: List<PCProblem>,
    val itars: List<ITARRequest>,
    val vpns: List<VPNRequest>,
    val nts: List<NTCreation>,
    val reminders: List<Reminder>,
    val tasks: List<ITTask>,
    val logs: List<AuditLog>,
    val notes: List<Note>,
    val users: List<User>
)
