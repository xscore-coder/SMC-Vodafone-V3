package com.vodafone.smc.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ==========================================
// ENTITIES
// ==========================================

@Entity(tableName = "pc_problems")
data class PCProblem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeName: String,
    val username: String,
    val site: String,
    val zone: String,
    val pcName: String,
    val assetNumber: String,
    val category: String,
    val priority: String, // Low, Medium, High, Critical
    val status: String,   // Open, In Progress, Pending User, Closed
    val detailedSummary: String,
    val assignedEngineer: String = "Unassigned",
    val assignedTeam: String = "Help Desk",
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val ticketNumber: String
        get() = "RL-${8000 + id}"
}

@Entity(tableName = "itar_requests")
data class ITARRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeName: String,
    val username: String,
    val requestType: String, // New Access, Software Export, Technical Data, Physical Access, Remote Support, Export License
    val status: String,     // Approved, Pending, Review Needed, Expired, Archived
    val implementer: String,
    val systemOwner: String,
    val startDate: String,
    val endDate: String,
    val rejectionReason: String,
    val internalNotes: String,
    val site: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val rId: String
        get() = "ITAR-2024-0${890 + id}"
}

@Entity(tableName = "vpn_requests")
data class VPNRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeName: String,
    val username: String,
    val vpnType: String,    // GlobalProtect, AnyConnect, Zero Trust, Full Tunnel
    val status: String,     // Open, Pending, Approved, Rejected, In Review, Completed, Urgent
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val srNumber: String
        get() = "SR #88${300 + id}"
}

@Entity(tableName = "nt_creations")
data class NTCreation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeName: String,
    val employeeId: String,
    val personalNumber: String,
    val department: String,
    val jobTitle: String,
    val managerName: String,
    val site: String,
    val status: String, // Pending, In Review, Urgent, Active
    val notes: String = "",
    val date: String = "",
    val actionDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val reqId: String
        get() = "REQ-88${200 + id}"
}

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: String,
    val escalateAfterMin: Int = 60,
    val priority: String, // Low, Medium, High, Urgent, Maintenance
    val status: String,   // Active, Escalated, Done
    val createdAt: Long = System.currentTimeMillis(),
    val startHour: Int = 7,  // 7:00 AM daily active window start
    val endHour: Int = 19,   // 7:00 PM daily active window end
    val repeatIntervalMin: Int = 60, // Custom hourly or interval check repeat (minutes)
    val soundType: String = "Default", // "Default", "Siren", "Chime", "Digital Pulse"
    val vibrationEnabled: Boolean = true,
    val notificationEnabled: Boolean = true
)

@Entity(tableName = "it_tasks")
data class ITTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // Low, Medium, High, Critical
    val dueDate: String,
    val status: String,   // To Do, In Progress, Testing, Done
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actorEmail: String,
    val actionType: String, // Login, Logout, Fail Login, Create Ticket, Update Task, Settings Updated
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "Info",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val role: String // Admin, Team Leader, IT Engineer, Viewer
)

// ==========================================
// DAO
// ==========================================

@Dao
interface AppDao {
    // PC Problems
    @Query("SELECT * FROM pc_problems ORDER BY createdAt DESC")
    fun getAllPCProblems(): Flow<List<PCProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPCProblem(problem: PCProblem): Long

    @Query("DELETE FROM pc_problems WHERE id = :id")
    suspend fun deletePCProblem(id: Int)

    @Query("UPDATE pc_problems SET status = :status WHERE id = :id")
    suspend fun updatePCProblemStatus(id: Int, status: String)

    // ITAR Requests
    @Query("SELECT * FROM itar_requests ORDER BY createdAt DESC")
    fun getAllITARRequests(): Flow<List<ITARRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertITARRequest(request: ITARRequest): Long

    @Query("DELETE FROM itar_requests WHERE id = :id")
    suspend fun deleteITARRequest(id: Int)

    // VPN Requests
    @Query("SELECT * FROM vpn_requests ORDER BY createdAt DESC")
    fun getAllVPNRequests(): Flow<List<VPNRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVPNRequest(request: VPNRequest): Long

    @Query("DELETE FROM vpn_requests WHERE id = :id")
    suspend fun deleteVPNRequest(id: Int)

    // NT Creations
    @Query("SELECT * FROM nt_creations ORDER BY createdAt DESC")
    fun getAllNTCreations(): Flow<List<NTCreation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNTCreation(creation: NTCreation): Long

    @Query("DELETE FROM nt_creations WHERE id = :id")
    suspend fun deleteNTCreation(id: Int)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, status: String)

    @Query("UPDATE reminders SET status = 'Done' WHERE description LIKE :likePattern")
    suspend fun completeRemindersByPattern(likePattern: String)

    // IT Tasks
    @Query("SELECT * FROM it_tasks ORDER BY createdAt DESC")
    fun getAllITTasks(): Flow<List<ITTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertITTask(task: ITTask): Long

    @Query("DELETE FROM it_tasks WHERE id = :id")
    suspend fun deleteITTask(id: Int)

    @Query("UPDATE it_tasks SET status = :status WHERE id = :id")
    suspend fun updateITTaskStatus(id: Int, status: String)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()

    // Notes
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Int)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("DELETE FROM it_tasks WHERE status = 'Done'")
    suspend fun clearCompletedITTasks()

    @Query("DELETE FROM it_tasks")
    suspend fun deleteAllITTasks()

    @Query("DELETE FROM pc_problems")
    suspend fun deleteAllPCProblems()

    @Query("DELETE FROM vpn_requests")
    suspend fun deleteAllVPNRequests()

    @Query("DELETE FROM itar_requests")
    suspend fun deleteAllITARRequests()

    @Query("DELETE FROM nt_creations")
    suspend fun deleteAllNTCreations()

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    // Users
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)

    @Query("UPDATE users SET passwordHash = :password, role = :role WHERE id = :id")
    suspend fun updateUser(id: Int, password: String, role: String)
}

// ==========================================
// DATABASE
// ==========================================

@Database(
    entities = [
        PCProblem::class,
        ITARRequest::class,
        VPNRequest::class,
        NTCreation::class,
        Reminder::class,
        ITTask::class,
        AuditLog::class,
        Note::class,
        User::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "redline_ops_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build().also {
                    INSTANCE = it
                }
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.appDao())
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            // Populate Initial System Setup Log
            dao.insertAuditLog(AuditLog(actorEmail = "SMC Operator", actionType = "Startup", details = "SMC Specialist instance successfully initialized with empty databases."))
        }
    }
}

// ==========================================
// REPOSITORY (Abstracted Data Access)
// ==========================================

class AppRepository(private val appDao: AppDao) {
    val allPCProblems: Flow<List<PCProblem>> = appDao.getAllPCProblems()
    val allITARRequests: Flow<List<ITARRequest>> = appDao.getAllITARRequests()
    val allVPNRequests: Flow<List<VPNRequest>> = appDao.getAllVPNRequests()
    val allNTCreations: Flow<List<NTCreation>> = appDao.getAllNTCreations()
    val allReminders: Flow<List<Reminder>> = appDao.getAllReminders()
    val allITTasks: Flow<List<ITTask>> = appDao.getAllITTasks()
    val allAuditLogs: Flow<List<AuditLog>> = appDao.getAllAuditLogs()

    suspend fun insertPCProblem(problem: PCProblem) = appDao.insertPCProblem(problem)
    suspend fun deletePCProblem(id: Int) = appDao.deletePCProblem(id)
    suspend fun updatePCProblemStatus(id: Int, status: String) = appDao.updatePCProblemStatus(id, status)

    suspend fun insertITARRequest(request: ITARRequest) = appDao.insertITARRequest(request)
    suspend fun deleteITARRequest(id: Int) = appDao.deleteITARRequest(id)

    suspend fun insertVPNRequest(request: VPNRequest) = appDao.insertVPNRequest(request)
    suspend fun deleteVPNRequest(id: Int) = appDao.deleteVPNRequest(id)

    suspend fun insertNTCreation(creation: NTCreation) = appDao.insertNTCreation(creation)
    suspend fun deleteNTCreation(id: Int) = appDao.deleteNTCreation(id)

    suspend fun insertReminder(reminder: Reminder) = appDao.insertReminder(reminder)
    suspend fun deleteReminder(id: Int) = appDao.deleteReminder(id)
    suspend fun updateReminderStatus(id: Int, status: String) = appDao.updateReminderStatus(id, status)
    suspend fun completeRemindersByPattern(likePattern: String) = appDao.completeRemindersByPattern(likePattern)

    suspend fun insertITTask(task: ITTask) = appDao.insertITTask(task)
    suspend fun deleteITTask(id: Int) = appDao.deleteITTask(id)
    suspend fun updateITTaskStatus(id: Int, status: String) = appDao.updateITTaskStatus(id, status)

    suspend fun insertAuditLog(log: AuditLog) = appDao.insertAuditLog(log)
    suspend fun clearAuditLogs() = appDao.clearAuditLogs()

    val allNotes: Flow<List<Note>> = appDao.getAllNotes()
    suspend fun insertNote(note: Note) = appDao.insertNote(note)
    suspend fun deleteNote(id: Int) = appDao.deleteNote(id)

    suspend fun deleteAllReminders() = appDao.deleteAllReminders()
    suspend fun clearCompletedITTasks() = appDao.clearCompletedITTasks()
    suspend fun deleteAllITTasks() = appDao.deleteAllITTasks()
    suspend fun deleteAllPCProblems() = appDao.deleteAllPCProblems()
    suspend fun deleteAllVPNRequests() = appDao.deleteAllVPNRequests()
    suspend fun deleteAllITARRequests() = appDao.deleteAllITARRequests()
    suspend fun deleteAllNTCreations() = appDao.deleteAllNTCreations()
    suspend fun deleteAllNotes() = appDao.deleteAllNotes()
    suspend fun deleteAllUsers() = appDao.deleteAllUsers()

    // Users
    val allUsers: Flow<List<User>> = appDao.getAllUsers()
    suspend fun getUserByUsername(username: String): User? = appDao.getUserByUsername(username)
    suspend fun insertUser(user: User): Long = appDao.insertUser(user)
    suspend fun deleteUser(id: Int) = appDao.deleteUser(id)
    suspend fun updateUser(id: Int, password: String, role: String) = appDao.updateUser(id, password, role)
}
