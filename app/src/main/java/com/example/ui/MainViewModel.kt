package com.vodafone.smc.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vodafone.smc.AlarmScheduler
import com.vodafone.smc.AlertSoundTester
import com.vodafone.smc.data.AppDatabase
import com.vodafone.smc.data.AppRepository
import com.vodafone.smc.data.AuditLog
import com.vodafone.smc.data.ITARRequest
import com.vodafone.smc.data.ITTask
import com.vodafone.smc.data.NTCreation
import com.vodafone.smc.data.PCProblem
import com.vodafone.smc.data.Reminder
import com.vodafone.smc.data.VPNRequest
import com.vodafone.smc.data.Note
import com.vodafone.smc.data.User
import com.vodafone.smc.data.BackupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("smc_pref", Context.MODE_PRIVATE)
    private val repository: AppRepository

    // Logged in States
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUsername = MutableStateFlow(prefs.getString("logged_in_username", "") ?: "")
    val loggedInUsername: StateFlow<String> = _loggedInUsername.asStateFlow()

    private val _currentUserRole = MutableStateFlow(prefs.getString("logged_in_user_role", "Viewer") ?: "Viewer")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _currentUserAvatar = MutableStateFlow(prefs.getString("avatar_${prefs.getString("logged_in_username", "") ?: ""}", "letters") ?: "letters")
    val currentUserAvatar: StateFlow<String> = _currentUserAvatar.asStateFlow()

    private fun getCurrentAvatarFromPrefs(username: String): String {
        if (username.isBlank()) return "letters"
        return prefs.getString("avatar_$username", "letters") ?: "letters"
    }

    fun updateLoggedInUserAvatar(newAvatar: String) {
        val currentUsername = _loggedInUsername.value
        if (currentUsername.isNotBlank()) {
            prefs.edit().putString("avatar_$currentUsername", newAvatar).apply()
            _currentUserAvatar.value = newAvatar
            logAction("Profile Update", "Updated user avatar profile photo configuration to: $newAvatar")
        }
    }

    val rawUsers: Flow<List<User>> by lazy { repository.allUsers }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database.appDao())
        
        // Dynamic safeguard to ensure default Admin user exists in DB
        viewModelScope.launch {
            try {
                val admin = repository.getUserByUsername("v22aali168")
                if (admin == null) {
                    repository.insertUser(User(username = "v22aali168", passwordHash = "Voda@2002", role = "Admin"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Start perpetual checking for unfinished SLA alerts
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            while (true) {
                checkAndSyncAutomaticHourlyReminders()
                kotlinx.coroutines.delay(12000) // check every 12 seconds
            }
        }
    }

    fun updateRole(newRole: String) {
        _currentUserRole.value = newRole
        prefs.edit().putString("logged_in_user_role", newRole).apply()
        logAction("User Switch", "Switched current application view scope to $newRole")
    }

    fun authenticateUser(usernameInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(usernameInput.trim())
            if (user != null && user.passwordHash == passwordInput) {
                _isLoggedIn.value = true
                _loggedInUsername.value = user.username
                _currentUserRole.value = user.role
                _currentUserAvatar.value = getCurrentAvatarFromPrefs(user.username)
                
                prefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("logged_in_username", user.username)
                    putString("logged_in_user_role", user.role)
                }.apply()
                
                logAction("Login", "Successfully logged in as ${user.username} with role ${user.role}")
                onResult(true, "Successfully authenticated.")
            } else {
                logAction("Fail Login", "Attempted login failed for username: $usernameInput")
                onResult(false, "Invalid username or password.")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            val prevUser = _loggedInUsername.value
            _isLoggedIn.value = false
            _loggedInUsername.value = ""
            _currentUserRole.value = "Viewer"
            _currentUserAvatar.value = "letters"
            
            prefs.edit().apply {
                putBoolean("is_logged_in", false)
                putString("logged_in_username", "")
                putString("logged_in_user_role", "Viewer")
            }.apply()
            
            logAction("Logout", "User $prevUser requested logout.")
        }
    }

    fun getRememberMe(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }

    fun getRememberedUsername(): String {
        return prefs.getString("remembered_username", "") ?: ""
    }

    fun setRememberMe(enabled: Boolean, username: String) {
        prefs.edit().apply {
            putBoolean("remember_me", enabled)
            if (enabled) {
                putString("remembered_username", username)
            } else {
                putString("remembered_username", "")
            }
        }.apply()
    }

    suspend fun isUsernameRegistered(username: String): Boolean {
        if (username.trim().isBlank()) return false
        return repository.getUserByUsername(username.trim()) != null
    }

    fun authenticateUserBiometric(usernameInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(usernameInput.trim())
            if (user != null) {
                _isLoggedIn.value = true
                _loggedInUsername.value = user.username
                _currentUserRole.value = user.role
                _currentUserAvatar.value = getCurrentAvatarFromPrefs(user.username)
                
                prefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("logged_in_username", user.username)
                    putString("logged_in_user_role", user.role)
                }.apply()
                
                logAction("Biometric Login", "Successfully logged in via Biometric Auth as ${user.username} with role ${user.role}")
                onResult(true, "Successfully authenticated.")
            } else {
                onResult(false, "User does not exist in local SMC cluster database.")
            }
        }
    }

    fun addNewLoginUser(usernameInput: String, passwordInput: String, roleInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanUser = usernameInput.trim()
            if (cleanUser.isBlank() || passwordInput.isBlank()) {
                onResult(false, "Username and password cannot be empty.")
                return@launch
            }
            val existing = repository.getUserByUsername(cleanUser)
            if (existing != null) {
                onResult(false, "Username already exists.")
                return@launch
            }
            val newUser = User(username = cleanUser, passwordHash = passwordInput, role = roleInput)
            repository.insertUser(newUser)
            logAction("Create User", "Created new login user '$cleanUser' with role '$roleInput'")
            onResult(true, "User created successfully.")
        }
    }

    fun updateLoginUser(userId: Int, usernameInput: String, passwordInput: String, roleInput: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanUser = usernameInput.trim()
            if (cleanUser.isBlank() || passwordInput.isBlank()) {
                onResult(false, "Username and password cannot be empty.")
                return@launch
            }
            // Double check if username is admin v22aali168
            if (cleanUser != "v22aali168") {
                val searchForAdminObj = repository.getUserByUsername("v22aali168")
                if (searchForAdminObj != null && searchForAdminObj.id == userId) {
                    onResult(false, "The original admin username 'v22aali168' cannot be modified.")
                    return@launch
                }
            }
            
            val updatedUser = User(id = userId, username = cleanUser, passwordHash = passwordInput, role = roleInput)
            repository.insertUser(updatedUser)
            
            // If current user updated their own role or username, update session values
            if (_loggedInUsername.value == cleanUser) {
                _currentUserRole.value = roleInput
                prefs.edit().putString("logged_in_user_role", roleInput).apply()
            }
            
            logAction("Update User", "Updated user details for '$cleanUser' ($roleInput)")
            onResult(true, "User updated successfully.")
        }
    }

    fun deleteLoginUser(userId: Int, usernameToDelete: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (usernameToDelete == "v22aali168") {
                onResult(false, "The primary admin account is locked and cannot be deleted.")
                return@launch
            }
            if (_loggedInUsername.value == usernameToDelete) {
                onResult(false, "You cannot delete your own active session account.")
                return@launch
            }
            repository.deleteUser(userId)
            logAction("Delete User", "Deleted login user '$usernameToDelete'")
            onResult(true, "User deleted successfully.")
        }
    }

    // Global Settings Variables
    private val prefsAgainPlaceholder = false // simple placeholder since we moved prefs up

    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode = _darkMode.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        logAction("Settings Updated", "Theme dark mode set to $enabled")
    }

    private val _maintenanceMode = MutableStateFlow(prefs.getBoolean("maintenance_mode", false))
    val maintenanceMode = _maintenanceMode.asStateFlow()

    private val _backupFrequency = MutableStateFlow(prefs.getString("backup_frequency", "12 Hours") ?: "12 Hours")
    val backupFrequency = _backupFrequency.asStateFlow()

    private val _telemetryEnabled = MutableStateFlow(prefs.getBoolean("telemetry_enabled", true))
    val telemetryEnabled = _telemetryEnabled.asStateFlow()

    fun toggleMaintenanceMode(enabled: Boolean) {
        _maintenanceMode.value = enabled
        prefs.edit().putBoolean("maintenance_mode", enabled).apply()
        logAction("Settings Updated", "Instance maintenance mode set to $enabled")
    }

    fun setBackupFrequency(freq: String) {
        _backupFrequency.value = freq
        prefs.edit().putString("backup_frequency", freq).apply()
        logAction("Settings Updated", "Automatic backup frequency set to $freq")
    }

    fun toggleTelemetry(enabled: Boolean) {
        _telemetryEnabled.value = enabled
        prefs.edit().putBoolean("telemetry_enabled", enabled).apply()
        logAction("Settings Updated", "Telemetry & Error Reporting payload set to $enabled")
    }

    private val _splashImageUri = MutableStateFlow(prefs.getString("splash_image_uri", "") ?: "")
    val splashImageUri = _splashImageUri.asStateFlow()

    fun setSplashImageUri(uri: String) {
        _splashImageUri.value = uri
        prefs.edit().putString("splash_image_uri", uri).apply()
        logAction("Settings Updated", "Splash screen custom image set to: $uri")
    }

    // Customizable sounds for ALERT, NOTIFICATION and REMINDER
    private val _alertSound = MutableStateFlow(prefs.getString("alert_sound", "Siren") ?: "Siren")
    val alertSound = _alertSound.asStateFlow()

    private val _notificationSound = MutableStateFlow(prefs.getString("notification_sound", "Chime") ?: "Chime")
    val notificationSound = _notificationSound.asStateFlow()

    private val _reminderSound = MutableStateFlow(prefs.getString("reminder_sound", "Default") ?: "Default")
    val reminderSound = _reminderSound.asStateFlow()

    private val _alertVolume = MutableStateFlow(prefs.getFloat("alert_volume", 0.8f))
    val alertVolume = _alertVolume.asStateFlow()

    private val _overrideDnd = MutableStateFlow(prefs.getBoolean("override_dnd", false))
    val overrideDnd = _overrideDnd.asStateFlow()

    private val _vibrationPattern = MutableStateFlow(prefs.getString("vibration_pattern", "Default") ?: "Default")
    val vibrationPattern = _vibrationPattern.asStateFlow()

    fun setAlertSound(sound: String) {
        _alertSound.value = sound
        prefs.edit().putString("alert_sound", sound).apply()
        logAction("Settings Updated", "Incident alert sound customized to: $sound")
    }

    fun setNotificationSound(sound: String) {
        _notificationSound.value = sound
        prefs.edit().putString("notification_sound", sound).apply()
        logAction("Settings Updated", "Notification alert sound customized to: $sound")
    }

    fun setReminderSound(sound: String) {
        _reminderSound.value = sound
        prefs.edit().putString("reminder_sound", sound).apply()
        logAction("Settings Updated", "SLA reminder alert sound customized to: $sound")
    }

    fun setAlertVolume(volume: Float) {
        _alertVolume.value = volume
        prefs.edit().putFloat("alert_volume", volume).apply()
    }

    fun toggleOverrideDnd(enabled: Boolean) {
        _overrideDnd.value = enabled
        prefs.edit().putBoolean("override_dnd", enabled).apply()
        logAction("Settings Updated", "Override DND setting changed to: $enabled")
    }

    fun setVibrationPattern(pattern: String) {
        _vibrationPattern.value = pattern
        prefs.edit().putString("vibration_pattern", pattern).apply()
        logAction("Settings Updated", "Vibration pattern updated to: $pattern")
    }

    // Dynamic Filter & Search Terms
    val pcProblemsSearchTerm = MutableStateFlow("")
    val pcProblemsFilter = MutableStateFlow("All") // All, Active, Critical, Resolved
    val pcProblemsSortBy = MutableStateFlow("creation_date_desc") // creation_date_desc, creation_date_asc, status, assigned_team
    val pcProblemsTeamFilter = MutableStateFlow("All") // All, Help Desk, Admin Team, Network Team, Operation Team
    val pcProblemsStatusFilter = MutableStateFlow("All") // All, Open, In Progress, Pending User, Closed

    val itarSearchTerm = MutableStateFlow("")
    val itarFilter = MutableStateFlow("All")

    val vpnSearchTerm = MutableStateFlow("")
    val vpnFilter = MutableStateFlow("All")

    val ntSearchTerm = MutableStateFlow("")
    val auditSearchTerm = MutableStateFlow("")
    val auditFilter = MutableStateFlow("All Entities") // All Entities, Action: Login, Action: Logout, Entity: Asset

    // Raw Room flows
    val rawPCProblems = repository.allPCProblems
    val rawITARRequests = repository.allITARRequests
    val rawVPNRequests = repository.allVPNRequests
    val rawNTCreations = repository.allNTCreations
    val rawReminders = repository.allReminders
    val rawITTasks = repository.allITTasks
    val rawAuditLogs = repository.allAuditLogs
    val rawNotes = repository.allNotes

    val notesList: StateFlow<List<Note>> = rawNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // State Flows compiled with user search criteria
    val filteredPCProblems: StateFlow<List<PCProblem>> = combine(
        combine(rawPCProblems, pcProblemsSearchTerm, pcProblemsFilter) { list, s, f -> Triple(list, s, f) },
        combine(pcProblemsSortBy, pcProblemsTeamFilter, pcProblemsStatusFilter) { sb, tf, sf -> Triple(sb, tf, sf) }
    ) { first, second ->
        val list = first.first
        val search = first.second
        val filter = first.third
        val sortBy = second.first
        val teamFilter = second.second
        val statusFilter = second.third

        var result = list.filter {
            val matchesSearch = it.employeeName.contains(search, ignoreCase = true) ||
                    it.username.contains(search, ignoreCase = true) ||
                    it.detailedSummary.contains(search, ignoreCase = true) ||
                    it.pcName.contains(search, ignoreCase = true) ||
                    it.ticketNumber.contains(search, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Active" -> it.status != "Closed"
                "Critical" -> it.priority == "Critical"
                "Resolved" -> it.status == "Closed"
                else -> true
            }

            val matchesStatusFilter = when (statusFilter) {
                "All" -> true
                else -> it.status.equals(statusFilter, ignoreCase = true)
            }

            val matchesTeamFilter = when (teamFilter) {
                "All" -> true
                else -> it.assignedTeam.equals(teamFilter, ignoreCase = true)
            }

            matchesSearch && matchesFilter && matchesStatusFilter && matchesTeamFilter
        }

        // Sort by chosen criteria
        result = when (sortBy) {
            "creation_date_asc" -> result.sortedBy { it.createdAt }
            "status" -> result.sortedWith(compareBy({ it.status }, { -it.createdAt }))
            "assigned_team" -> result.sortedBy { it.assignedTeam }
            else -> result.sortedByDescending { it.createdAt } // "creation_date_desc" (newest first)
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredITARRequests: StateFlow<List<ITARRequest>> = combine(
        rawITARRequests, itarSearchTerm, itarFilter
    ) { list, search, filter ->
        list.filter {
            val matchesSearch = it.employeeName.contains(search, ignoreCase = true) ||
                    it.username.contains(search, ignoreCase = true) ||
                    it.rId.contains(search, ignoreCase = true) ||
                    it.requestType.contains(search, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Pending" -> it.status == "Pending"
                "Approved" -> it.status == "Approved"
                "Review Needed" -> it.status == "Review Needed"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredVPNRequests: StateFlow<List<VPNRequest>> = combine(
        rawVPNRequests, vpnSearchTerm, vpnFilter
    ) { list, search, filter ->
        list.filter {
            val matchesSearch = it.employeeName.contains(search, ignoreCase = true) ||
                    it.username.contains(search, ignoreCase = true) ||
                    it.vpnType.contains(search, ignoreCase = true) ||
                    it.srNumber.contains(search, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Pending" -> it.status == "Pending" || it.status == "In Review"
                "Approved" -> it.status == "Approved" || it.status == "Completed"
                "Rejected" -> it.status == "Rejected"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredNTCreations: StateFlow<List<NTCreation>> = combine(
        rawNTCreations, ntSearchTerm
    ) { list, search ->
        list.filter {
            it.employeeName.contains(search, ignoreCase = true) ||
                    it.employeeId.contains(search, ignoreCase = true) ||
                    it.site.contains(search, ignoreCase = true) ||
                    it.department.contains(search, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAuditLogs: StateFlow<List<AuditLog>> = combine(
        rawAuditLogs, auditSearchTerm, auditFilter
    ) { list, search, filter ->
        list.filter {
            val matchesSearch = it.actorEmail.contains(search, ignoreCase = true) ||
                    it.actionType.contains(search, ignoreCase = true) ||
                    it.details.contains(search, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Action: Login" -> it.actionType == "Login" || it.actionType == "Fail Login" || it.actionType == "Startup"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ==========================================
    // ACTION DISPATCHERS (INSERT / UPDATE)
    // ==========================================

    fun logAction(action: String, details: String) {
        viewModelScope.launch {
            repository.insertAuditLog(
                AuditLog(
                    actorEmail = "SMC Operator",
                    actionType = action,
                    details = details
                )
            )
        }
    }

    fun addPCProblem(
        name: String,
        username: String,
        site: String,
        zone: String,
        pcName: String,
        assetNumber: String,
        category: String,
        priority: String,
        detailedSummary: String,
        assignedTeam: String = "Help Desk",
        alarmEnabled: Boolean = false,
        alarmStartHour: Int = 7,
        alarmEndHour: Int = 19,
        alarmRepeatHours: Int = 1
    ) {
        viewModelScope.launch {
            val problem = PCProblem(
                employeeName = name,
                username = username,
                site = site,
                zone = zone,
                pcName = pcName,
                assetNumber = assetNumber,
                category = category,
                priority = priority,
                status = "Open",
                detailedSummary = detailedSummary,
                assignedTeam = assignedTeam
            )
            val id = repository.insertPCProblem(problem)
            logAction("Create Ticket", "Created PC Problem Ticket RL-${8000 + id} for $name")
            if (alarmEnabled) {
                addOperationalAlarmEvent(
                    entityType = "PCProblem",
                    entityIdText = "$id",
                    entityTitle = "PC Problem RL-${8000 + id}",
                    startHour = alarmStartHour,
                    endHour = alarmEndHour,
                    repeatIntervalHours = alarmRepeatHours
                )
            } else {
                triggerSlaReminder("PCProblem", "$id", "PC Problem RL-${8000 + id}", priority)
            }
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun updatePCProblemStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updatePCProblemStatus(id, status)
            logAction("Update Ticket", "Updated PC Problem ID $id status to $status")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
            if (status.equals("Closed", ignoreCase = true)) {
                completeRemindersForEntity("PCProblem", "$id")
            }
        }
    }

    fun deletePCProblem(id: Int) {
        viewModelScope.launch {
            repository.deletePCProblem(id)
            logAction("Delete Ticket", "Deleted Workstation Incident Ticket ID $id")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun addITARRequest(
        name: String,
        username: String,
        requestType: String,
        status: String,
        implementer: String,
        systemOwner: String,
        startDate: String,
        endDate: String,
        rejectionReason: String,
        internalNotes: String,
        site: String = "",
        alarmEnabled: Boolean = false,
        alarmStartHour: Int = 7,
        alarmEndHour: Int = 19,
        alarmRepeatHours: Int = 1
    ) {
        viewModelScope.launch {
            val request = ITARRequest(
                employeeName = name,
                username = username,
                requestType = requestType,
                status = status,
                implementer = implementer,
                systemOwner = systemOwner,
                startDate = startDate,
                endDate = endDate,
                rejectionReason = rejectionReason,
                internalNotes = internalNotes,
                site = site
            )
            val id = repository.insertITARRequest(request)
            logAction("Create ITAR", "Created ITAR request ITAR-2024-0${890 + id} for $name at $site")
            if (alarmEnabled) {
                addOperationalAlarmEvent(
                    entityType = "ITARRequest",
                    entityIdText = "$id",
                    entityTitle = "ITAR Request ITAR-2024-0${890 + id}",
                    startHour = alarmStartHour,
                    endHour = alarmEndHour,
                    repeatIntervalHours = alarmRepeatHours
                )
            }
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun deleteITARRequest(id: Int) {
        viewModelScope.launch {
            repository.deleteITARRequest(id)
            logAction("Delete ITAR", "Deleted ITAR Request ID $id")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun approveITARRequest(request: ITARRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "Approved")
            repository.insertITARRequest(updated)
            logAction("Approve ITAR", "Approved ITAR Request ID ${request.id}")
            completeRemindersForEntity("ITARRequest", "${request.id}")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun updateITARRequest(request: ITARRequest) {
        viewModelScope.launch {
            repository.insertITARRequest(request)
            logAction("Edit ITAR", "Updated ITAR Request ID ${request.id}")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun addVPNRequest(
        name: String,
        username: String,
        vpnType: String,
        status: String,
        notes: String,
        alarmEnabled: Boolean = false,
        alarmStartHour: Int = 7,
        alarmEndHour: Int = 19,
        alarmRepeatHours: Int = 1
    ) {
        viewModelScope.launch {
            val request = VPNRequest(
                employeeName = name,
                username = username,
                vpnType = vpnType,
                status = status,
                notes = notes
            )
            val id = repository.insertVPNRequest(request)
            logAction("Create VPN", "Requested secure remote access SR #88${300 + id} for $name")
            if (alarmEnabled) {
                addOperationalAlarmEvent(
                    entityType = "VPNRequest",
                    entityIdText = "$id",
                    entityTitle = "VPN Request SR #88${300 + id}",
                    startHour = alarmStartHour,
                    endHour = alarmEndHour,
                    repeatIntervalHours = alarmRepeatHours
                )
            } else {
                triggerSlaReminder("VPNRequest", "$id", "VPN Request SR #88${300 + id}", status)
            }
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun deleteVPNRequest(id: Int) {
        viewModelScope.launch {
            repository.deleteVPNRequest(id)
            logAction("Delete VPN", "Deleted VPN Request ID $id")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun approveVPNRequest(request: VPNRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "Completed")
            repository.insertVPNRequest(updated)
            logAction("Approve VPN", "Completed VPN Request ID ${request.id}")
            completeRemindersForEntity("VPNRequest", "${request.id}")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun addNTCreation(
        name: String,
        personalNumber: String,
        department: String,
        jobTitle: String,
        manager: String,
        site: String,
        status: String,
        notes: String,
        date: String = "",
        actionDate: String = "",
        alarmEnabled: Boolean = false,
        alarmStartHour: Int = 7,
        alarmEndHour: Int = 19,
        alarmRepeatHours: Int = 1
    ) {
        viewModelScope.launch {
            val creation = NTCreation(
                employeeName = name,
                employeeId = "REQ-88${(200..299).random()}",
                personalNumber = personalNumber,
                department = department,
                jobTitle = jobTitle,
                managerName = manager,
                site = site,
                status = status,
                notes = notes,
                date = date,
                actionDate = actionDate
            )
            val id = repository.insertNTCreation(creation)
            logAction("Create NT", "Logged terminal account request REQ-88${200 + id} for $name ($site)")
            if (alarmEnabled) {
                addOperationalAlarmEvent(
                    entityType = "NTCreation",
                    entityIdText = "$id",
                    entityTitle = "NT Request REQ-88${200 + id}",
                    startHour = alarmStartHour,
                    endHour = alarmEndHour,
                    repeatIntervalHours = alarmRepeatHours
                )
            } else {
                triggerSlaReminder("NTCreation", "$id", "NT Request REQ-88${200 + id}", status)
            }
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun deleteNTCreation(id: Int) {
        viewModelScope.launch {
            repository.deleteNTCreation(id)
            logAction("Delete NT", "Deleted NT Creation ID $id")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun approveNTCreation(nt: NTCreation) {
        viewModelScope.launch {
            val updated = nt.copy(status = "Active")
            repository.insertNTCreation(updated)
            logAction("Approve NT", "Activated NT Creation ID ${nt.id}")
            completeRemindersForEntity("NTCreation", "${nt.id}")
            com.vodafone.smc.PCProblemWidgetProvider.triggerUpdate(getApplication())
        }
    }

    fun getLinkedEntityKey(reminder: Reminder): String? {
        val desc = reminder.description
        val trigger = "Linked to "
        val index = desc.lastIndexOf(trigger)
        if (index != -1) {
            val linkPart = desc.substring(index + trigger.length).trim()
            val key = linkPart.substringBefore(" ").substringBefore(".")
            if (key.isNotEmpty()) {
                return key
            }
        }
        return null
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
            if (reminder.status == "Done") {
                AlarmScheduler.cancelAlarm(getApplication(), reminder.id)
            } else {
                AlarmScheduler.scheduleAlarm(getApplication(), reminder)
            }
            logAction("Update Reminder", "Updated Reminder ID ${reminder.id} - ${reminder.title} individually")
        }
    }

    fun triggerSlaReminder(entityType: String, entityIdText: String, entityTitle: String, priorityOrStatus: String) {
        viewModelScope.launch {
            val isUrgentOrCritical = priorityOrStatus.equals("Critical", ignoreCase = true) || priorityOrStatus.equals("Urgent", ignoreCase = true)
            val isHigh = priorityOrStatus.equals("High", ignoreCase = true)
            
            if (isUrgentOrCritical || isHigh) {
                val minutesToSla = if (isUrgentOrCritical) 15 else 30
                val targetTimeMs = System.currentTimeMillis() + (minutesToSla * 60 * 1000)
                
                val title = "🚨 SLA DEADLINE - $entityTitle"
                val description = "High-priority $entityType ($entityTitle) is approaching its SLA deadline in $minutesToSla minutes! Linked to $entityType:$entityIdText."
                
                val reminder = Reminder(
                    title = title,
                    description = description,
                    dueDate = "TimerTarget:$targetTimeMs",
                    escalateAfterMin = minutesToSla,
                    priority = if (isUrgentOrCritical) "Urgent" else "High",
                    status = "Active"
                )
                val newId = repository.insertReminder(reminder)
                val savedReminder = reminder.copy(id = newId.toInt())
                AlarmScheduler.scheduleAlarm(getApplication(), savedReminder)
                logAction("SLA Reminder", "Automatically scheduled SLA alarm for $entityTitle in $minutesToSla min")
                displayNotification(
                    "SMC SLA Alert",
                    "[$priorityOrStatus] $entityTitle needs action in $minutesToSla minutes!"
                )
            }
        }
    }

    fun checkAndSyncAutomaticHourlyReminders() {
        viewModelScope.launch {
            try {
                val problems = repository.allPCProblems.first()
                val itars = repository.allITARRequests.first()
                val vpns = repository.allVPNRequests.first()
                val nts = repository.allNTCreations.first()
                val reminders = repository.allReminders.first()

                val now = System.currentTimeMillis()

                // Process PC Problems (Unfinished if Not Closed)
                problems.forEach { item ->
                    val isUnfinished = item.status.lowercase() !in setOf("closed", "resolved")
                    val entityType = "PCProblem"
                    val entityId = item.id.toString()
                    val entityTitle = item.ticketNumber

                    syncSingleEntityReminder(entityType, entityId, entityTitle, isUnfinished, reminders, now)
                }

                // Process ITAR Requests (Unfinished if Pending/Review Needed)
                itars.forEach { item ->
                    val isUnfinished = item.status.lowercase() in setOf("pending", "review need", "review needed")
                    val entityType = "ITARRequest"
                    val entityId = item.id.toString()
                    val entityTitle = item.rId

                    syncSingleEntityReminder(entityType, entityId, entityTitle, isUnfinished, reminders, now)
                }

                // Process VPN Requests (Unfinished if not Approved/Completed/Rejected)
                vpns.forEach { item ->
                    val isUnfinished = item.status.lowercase() !in setOf("approved", "completed", "rejected")
                    val entityType = "VPNRequest"
                    val entityId = item.id.toString()
                    val entityTitle = item.srNumber

                    syncSingleEntityReminder(entityType, entityId, entityTitle, isUnfinished, reminders, now)
                }

                // Process NT Creations (Unfinished if not Active/Done)
                nts.forEach { item ->
                    val isUnfinished = item.status.lowercase() !in setOf("active", "done")
                    val entityType = "NTCreation"
                    val entityId = item.id.toString()
                    val entityTitle = item.reqId

                    syncSingleEntityReminder(entityType, entityId, entityTitle, isUnfinished, reminders, now)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun syncSingleEntityReminder(
        entityType: String,
        entityId: String,
        entityTitle: String,
        isUnfinished: Boolean,
        reminders: List<Reminder>,
        now: Long
    ) {
        val linkKey = "Linked to $entityType:$entityId"
        
        val activeReminder = reminders.find {
            (it.status == "Active" || it.status == "Escalated") && it.description.contains(linkKey)
        }

        if (isUnfinished) {
            if (activeReminder == null) {
                // If there's no active reminder, create an hourly alert TimerTarget (due 1 hour from now)
                val nextHourMs = now + (60 * 60 * 1000L) // 1 hour
                val title = "🚨 UNRESOLVED SLA: $entityTitle"
                val description = "High-priority operational $entityType ($entityTitle) is STILL unfinished/pending action. Repeating alarm will beep hourly. Linked to $entityType:$entityId."
                val r = Reminder(
                    title = title,
                    description = description,
                    dueDate = "TimerTarget:$nextHourMs",
                    escalateAfterMin = 60,
                    priority = "High",
                    status = "Active"
                )
                val newId = repository.insertReminder(r)
                val savedReminder = r.copy(id = newId.toInt())
                AlarmScheduler.scheduleAlarm(getApplication(), savedReminder)
                logAction("Auto Alert", "Initiated hourly operational tracking alarm for $entityTitle")
                displayNotification(
                    "SMC SLA Tracker",
                    "[$entityType] $entityTitle remains action-required! Next hourly ping scheduled."
                )
            }
        } else {
            // Soft-resolve matching reminders since ticket is completed
            val activeRemindersToDone = reminders.filter {
                (it.status == "Active" || it.status == "Escalated") && it.description.contains(linkKey)
            }
            activeRemindersToDone.forEach { r ->
                repository.updateReminderStatus(r.id, "Done")
                logAction("Auto Deactivate", "Deactivated SLA tracker for $entityTitle (ticket is completed/approved)")
            }
        }
    }

    fun addOperationalAlarmEvent(
        entityType: String,
        entityIdText: String,
        entityTitle: String,
        startHour: Int,
        endHour: Int,
        repeatIntervalHours: Int
    ) {
        viewModelScope.launch {
            val titleText = "Follow-up: $entityTitle"
            val descText = "SMC Repeat Alarms for $entityTitle. Linked to $entityType:$entityIdText."
            val initialTriggerTime = System.currentTimeMillis() + 5000L
            val r = Reminder(
                title = titleText,
                description = descText,
                dueDate = "TimerTarget:$initialTriggerTime",
                escalateAfterMin = repeatIntervalHours * 60,
                priority = "High",
                status = "Active",
                startHour = startHour,
                endHour = endHour,
                repeatIntervalMin = repeatIntervalHours * 60
            )
            val newId = repository.insertReminder(r)
            val savedReminder = r.copy(id = newId.toInt())
            AlarmScheduler.scheduleAlarm(getApplication(), savedReminder)
            logAction("Auto Alarm Event", "Scheduled dynamic repeating alarm for $entityTitle within window ($startHour to $endHour) every $repeatIntervalHours hour(s)")
        }
    }

    fun completeRemindersForEntity(entityType: String, entityIdText: String) {
        viewModelScope.launch {
            val likePattern = "%Linked to $entityType:$entityIdText.%"
            repository.completeRemindersByPattern(likePattern)
            logAction("Update Reminder", "Completed hourly reminders for $entityType #$entityIdText")
        }
    }

    fun addReminder(
        title: String,
        description: String,
        dueDate: String,
        escalateMin: Int,
        priority: String,
        startHour: Int = 7,
        endHour: Int = 19,
        repeatIntervalMin: Int = 60,
        soundType: String = "Default",
        vibrationEnabled: Boolean = true,
        notificationEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                description = description,
                dueDate = dueDate,
                escalateAfterMin = escalateMin,
                priority = priority,
                status = "Active",
                startHour = startHour,
                endHour = endHour,
                repeatIntervalMin = repeatIntervalMin,
                soundType = soundType,
                vibrationEnabled = vibrationEnabled,
                notificationEnabled = notificationEnabled
            )
            val newId = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = newId.toInt())
            AlarmScheduler.scheduleAlarm(getApplication(), savedReminder)
            logAction("Create Reminder", "Created alarm reminder title: '$title' due $dueDate")
            displayNotification(
                "SMC Reminder: $title",
                "$description | Due: $dueDate [Priority: $priority]"
            )
        }
    }

    fun toggleReminderCompleted(reminder: Reminder) {
        viewModelScope.launch {
            val newStatus = if (reminder.status == "Done") "Active" else "Done"
            repository.updateReminderStatus(reminder.id, newStatus)
            if (newStatus == "Done") {
                AlarmScheduler.cancelAlarm(getApplication(), reminder.id)
            } else {
                AlarmScheduler.scheduleAlarm(getApplication(), reminder.copy(status = newStatus))
            }
            logAction("Update Reminder", "Reminder '${reminder.title}' status updated to $newStatus individually")
            displayNotification(
                "SMC Reminder Updated",
                "'${reminder.title}' status is now $newStatus"
            )
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), id)
            repository.deleteReminder(id)
            logAction("Delete Reminder", "Deleted Reminder ID $id")
        }
    }

    fun snoozeReminder(reminder: Reminder) {
        viewModelScope.launch {
            val targetTimeMs = System.currentTimeMillis() + (15 * 60 * 1000)
            val updated = reminder.copy(dueDate = "TimerTarget:$targetTimeMs")
            repository.insertReminder(updated)
            AlarmScheduler.scheduleAlarm(getApplication(), updated)
            logAction("Reminder Snoozed", "Snoozed alarm '${reminder.title}' for 15 minutes individually")
            displayNotification(
                "SMC Reminder Snoozed",
                "Snoozed '${reminder.title}' alarm for 15 minutes (+15 min)"
            )
        }
    }

    fun snoozeReminderCustom(reminder: Reminder, minutes: Int) {
        viewModelScope.launch {
            val targetTimeMs = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val updated = reminder.copy(dueDate = "TimerTarget:$targetTimeMs")
            repository.insertReminder(updated)
            AlarmScheduler.scheduleAlarm(getApplication(), updated)
            logAction("Reminder Snoozed", "Snoozed alarm '${reminder.title}' for $minutes minutes individually")
            displayNotification(
                "SMC Reminder Snoozed",
                "Snoozed '${reminder.title}' alarm for $minutes minutes (+$minutes min)"
            )
        }
    }

    fun processEscalation(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminderStatus(reminder.id, "Escalated")
            logAction("Reminder Escalated", "SLA escalation event triggered for '${reminder.title}' due to deadline breach.")
            displayNotification(
                "SMC SLA ESCALATION ⚠️",
                "Deadline breached for '${reminder.title}'. SLA escalation event triggered!"
            )
        }
    }

    fun playSystemAlarmSound() {
        try {
            val context = getApplication<Application>()
            val selectedSound = alertSound.value
            AlertSoundTester.playSound(context, selectedSound, "Emergency SMC Alert Triggered")
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val toneG = android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 80)
                toneG.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
            } catch (ex: Exception) {}
        }
    }

    private fun displayNotification(title: String, message: String) {
        val context = getApplication<Application>()
        val channelId = "smc_reminders_channel"
        val notificationId = System.currentTimeMillis().toInt()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMC Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for SMC operational alarm reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.vodafone.smc.R.drawable.ic_no_sim)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            
        try {
            notificationManager.notify(notificationId, builder.build())
            // Play user's customized notification sound!
            val selectedSound = notificationSound.value
            AlertSoundTester.playSound(context, selectedSound, message)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val toneG = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 80)
                toneG.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
            } catch (ex: Exception) {}
        }
    }

    fun addITTask(
        title: String,
        description: String,
        priority: String,
        dueDate: String,
        status: String,
        isRecurring: Boolean
    ) {
        viewModelScope.launch {
            val task = ITTask(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                status = status,
                isRecurring = isRecurring
            )
            repository.insertITTask(task)
            logAction("Create Task", "Assigned new enterprise operation task: '$title'")
        }
    }

    fun updateITTaskStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateITTaskStatus(id, status)
            logAction("Update Task", "Shifted task ID $id stage to: $status")
        }
    }

    fun purgeCacheAndData() {
        viewModelScope.launch {
            repository.clearAuditLogs()
            logAction("Purge Action", "Instance cache purge completed. Local telemetry payload size reset to 0.")
        }
    }

    fun clearCompletedITTasks() {
        viewModelScope.launch {
            repository.clearCompletedITTasks()
            logAction("Purge Tasks", "Cleared all resolved/done IT tasks from database.")
        }
    }

    fun purgeAllReminders() {
        viewModelScope.launch {
            repository.deleteAllReminders()
            logAction("Purge Reminders", "Purged all active and historical alarms & reminders.")
        }
    }

    fun wipeAllDatabases() {
        viewModelScope.launch {
            repository.clearAuditLogs()
            repository.deleteAllReminders()
            repository.clearCompletedITTasks()
            repository.deleteAllPCProblems()
            repository.deleteAllVPNRequests()
            repository.deleteAllITARRequests()
            repository.deleteAllNTCreations()
            repository.deleteAllNotes()
            logAction("Security Purge", "MASTER PURGE SEQUENCE TRIGGERED. All databases safely wiped.")
        }
    }

    fun resetInstanceToDefaults() {
        viewModelScope.launch {
            // Can reload initial datasets by dropping and inserting
            purgeCacheAndData()
            // Add a few baseline entities
            logAction("Reset Instance", "Triggered master reset sequence on cluster.")
        }
    }

    // Notes operations
    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            val note = Note(title = title, content = content, category = category)
            repository.insertNote(note)
            logAction("Create Note", "Created a new operational note: '$title'")
        }
    }

    fun editNote(id: Int, title: String, content: String, category: String) {
        viewModelScope.launch {
            val note = Note(id = id, title = title, content = content, category = category, timestamp = System.currentTimeMillis())
            repository.insertNote(note)
            logAction("Edit Note", "Modified operational note: '$title'")
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
            logAction("Delete Note", "Deleted operational note ID: $id")
        }
    }

    fun backupDatabaseEncrypted(password: String, onResult: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Fetch all database records sequentially via Flow snapshots
                val problems = repository.allPCProblems.first()
                val itars = repository.allITARRequests.first()
                val vpns = repository.allVPNRequests.first()
                val nts = repository.allNTCreations.first()
                val reminders = repository.allReminders.first()
                val tasks = repository.allITTasks.first()
                val logs = repository.allAuditLogs.first()
                val notes = repository.allNotes.first()
                val users = repository.allUsers.first()

                // Serialize to plain JSON
                val serializedPlain = BackupManager.serializeToText(
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

                // Encrypt the JSON data using secure AES
                val encryptedOutput = BackupManager.encryptData(serializedPlain, password)
                
                logAction("Backup DB", "Generated standard secure backup archive.")
                onResult(true, "Gateway configuration safely compiled and encrypted.", encryptedOutput)
            } catch (e: Exception) {
                e.printStackTrace()
                logAction("Backup DB Fail", "Encryption backup compile failed: ${e.message}")
                onResult(false, "System backup execution error: ${e.localizedMessage}", null)
            }
        }
    }

    fun restoreDatabaseEncrypted(encryptedContent: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val decryptedText = BackupManager.decryptData(encryptedContent, password)
                val payload = BackupManager.parseDecrypted(decryptedText)

                // Clear current tables before importing
                repository.deleteAllReminders()
                repository.deleteAllITTasks()
                repository.deleteAllPCProblems()
                repository.deleteAllVPNRequests()
                repository.deleteAllITARRequests()
                repository.deleteAllNTCreations()
                repository.deleteAllNotes()
                repository.deleteAllUsers()

                // Re-insert standard admin account if missing or restored from the payload
                var hasAdmin = false
                for (user in payload.users) {
                    repository.insertUser(user)
                    if (user.username == "v22aali168") {
                        hasAdmin = true
                    }
                }
                if (!hasAdmin) {
                    repository.insertUser(User(username = "v22aali168", passwordHash = "Voda@2002", role = "Admin"))
                }

                // Re-insert other entities
                for (prob in payload.problems) {
                    repository.insertPCProblem(prob)
                }
                for (itar in payload.itars) {
                    repository.insertITARRequest(itar)
                }
                for (vpn in payload.vpns) {
                    repository.insertVPNRequest(vpn)
                }
                for (nt in payload.nts) {
                    repository.insertNTCreation(nt)
                }
                for (reminder in payload.reminders) {
                    repository.insertReminder(reminder)
                    AlarmScheduler.scheduleAlarm(getApplication(), reminder)
                }
                for (task in payload.tasks) {
                    repository.insertITTask(task)
                }
                for (log in payload.logs) {
                    repository.insertAuditLog(log)
                }
                for (note in payload.notes) {
                    repository.insertNote(note)
                }

                logAction("Restore DB", "Imported and decrypted backup state snapshot successfully.")
                
                // Keep login session preferences safe
                val activeUsername = loggedInUsername.value
                val dbUser = repository.getUserByUsername(activeUsername)
                if (dbUser == null) {
                    logoutUser()
                } else {
                    _currentUserRole.value = dbUser.role
                    prefs.edit().putString("logged_in_user_role", dbUser.role).apply()
                }

                onResult(true, "Configurations and databases restored successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                logAction("Restore DB Fail", "Decrypted restore execution failed: ${e.message}")
                onResult(false, "Decryption or restoration failed. Ensure the credential key is correct.")
            }
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
