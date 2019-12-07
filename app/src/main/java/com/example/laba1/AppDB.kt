package com.example.laba1

import androidx.room.*

@Entity(indices = [Index(value = ["name"], name = "name_index", unique = true)])
data class User(
    val name: String,
    val password: String)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity
data class MovieCommonInfo(
    val title: String,
    val imdbRating: String,
    val pageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
}

@Entity
data class MoviePageInfo(
    val movieCommonInfoId: Int,
    val description: String,
    val ganres: String,
    val duration: String,
    val releaseData: String,
    val imageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
}

@Entity
data class MoviePreviewInfo(
    val movieCommonInfoId: Int,
    val imageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
}

data class ImdbElement(
    val title: String,
    val description: String,
    val imageUrl: String
)

data class ImdbListElement(
    val title: String,
    val pageUrl: String,
    val imagePreviewUrl: String,
    val ratingImdb: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE name LIKE :first AND password LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Query("DELETE FROM user")
    fun delteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}

@Dao
interface MovieCommonDao {
    @Query("SELECT * FROM MovieCommonInfo")
    fun getAll(): List<User>

    @Insert
    fun insertAll(vararg movieCommonInfo: MovieCommonInfo)

    @Delete
    fun delete(movieCommonInfo: MovieCommonInfo)
}

@Dao
interface MoviePageDao {
    @Query("SELECT * FROM MoviePageInfo")
    fun getAll(): List<User>

    @Insert
    fun insertAll(vararg moviePageInfo: MoviePageInfo)

    @Delete
    fun delete(moviePageInfo: MoviePageInfo)
}

@Dao
interface MoviePreviewDao {
    @Query("SELECT * FROM MoviePreviewInfo")
    fun getAll(): List<User>

    @Insert
    fun insertAll(vararg moviePreviewInfo: MoviePreviewInfo)

    @Delete
    fun delete(moviePreviewInfo: MoviePreviewInfo)
}

@Database
    ( entities =
        [   User::class
        ]
    ,   version = 1 )
abstract class AppDatabase : RoomDatabase() {
    abstract val users: UserDao


//    abstract fun getUsers(): UserDao
}
