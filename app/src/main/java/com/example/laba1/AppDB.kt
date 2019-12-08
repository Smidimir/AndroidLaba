package com.example.laba1

import androidx.room.*

@Entity(indices = [Index(value = ["name"], name = "user_unique_index", unique = true)])
data class User(
    val name: String,
    val password: String)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(indices = [Index(value = ["movieId"], name = "movie_common_unique_index", unique = true)])
data class MovieCommonInfo(
    val movieId: Int,
    val title: String,
    val imdbRating: String,
    val pageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(indices = [Index(value = ["movieId"], name = "movie_preview_unique_index", unique = true)])
data class MoviePreviewInfo(
    val movieId: Int,
    val topPosition: Int,
    val imageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(indices = [Index(value = ["movieId"], name = "movie_page_unique_index", unique = true)])
data class MoviePageInfo(
    val movieId: Int,
    val description: String,
    val ganres: String,
    val duration: String,
    val releaseData: String,
    val imageUrl: String)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(indices = [Index(value = ["userId", "movieId"], name = "favorite_unique_index", unique = true)])
data class Favorite(val userId: Int, val movieId: Int)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

data class MoviePreviewFullInfo(
    val movieId: Int,
    val title: String,
    val imdbRating: String,
    val pageUrl: String,
    val topPosition: Int,
    val imageUrl: String)
{
    constructor(movieCommonInfo: MovieCommonInfo, moviePreviewInfo: MoviePreviewInfo): this(
        movieId     = movieCommonInfo.movieId,
        title       = movieCommonInfo.title,
        imdbRating  = movieCommonInfo.imdbRating,
        pageUrl     = movieCommonInfo.pageUrl,
        topPosition = moviePreviewInfo.topPosition,
        imageUrl    = moviePreviewInfo.imageUrl
    )

    val asCommon get() = MovieCommonInfo(
        movieId    = movieId,
        title      = title,
        imdbRating = imdbRating,
        pageUrl    = pageUrl
    )
    val asPreview get() = MoviePreviewInfo(
        movieId     = movieId,
        topPosition = topPosition,
        imageUrl    = imageUrl
    )
}

data class MoviePageFullInfo(
    val movieId: Int,
    val title: String,
    val imdbRating: String,
    val pageUrl: String,
    val description: String,
    val ganres: String,
    val duration: String,
    val releaseData: String,
    val imageUrl: String)
{
    constructor(movieCommonInfo: MovieCommonInfo, moviePageInfo: MoviePageInfo): this(
        movieId     = movieCommonInfo.movieId,
        title       = movieCommonInfo.title,
        imdbRating  = movieCommonInfo.imdbRating,
        pageUrl     = movieCommonInfo.pageUrl,
        description = moviePageInfo.description,
        ganres      = moviePageInfo.ganres,
        duration    = moviePageInfo.duration,
        releaseData = moviePageInfo.releaseData,
        imageUrl    = moviePageInfo.imageUrl
    )

    val asCommon get() = MovieCommonInfo(
        movieId    = movieId,
        title      = title,
        imdbRating = imdbRating,
        pageUrl    = pageUrl
    )
    val asPage get() = MoviePageInfo(
        movieId     = movieId,
        description = description,
        ganres      = ganres,
        duration    = duration,
        releaseData = releaseData,
        imageUrl    = imageUrl
    )
}

@Dao
interface UserDao {
    @Query("SELECT * FROM User WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): User?

    @Query("SELECT * FROM User")
    fun selectAll(): List<User>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(element: User)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMultiple(elements: List<User>)

    @Delete
    fun delete(element: User)

    @Query("DELETE FROM User")
    fun deleteAll()
}

@Dao
interface MovieCommonInfoDao {
    @Query("SELECT * FROM MovieCommonInfo WHERE movieId LIKE :movieId LIMIT 1")
    fun findByMovieId(movieId: Int): MovieCommonInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(element: MovieCommonInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultiple(elements: List<MovieCommonInfo>)

    @Delete
    fun delete(element: MoviePreviewInfo)

    @Query("DELETE FROM MovieCommonInfo")
    fun deleteAll()
}

@Dao
interface MoviePageInfoDao {
    @Query("SELECT * FROM MoviePageInfo WHERE movieId LIKE :movieId LIMIT 1")
    fun findByMovieId(movieId: Int): MoviePageInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(element: MoviePageInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultiple(elements: List<MoviePageInfo>)

    @Delete
    fun delete(element: MoviePageInfo)

    @Query("DELETE FROM MoviePageInfo")
    fun deleteAll()
}

@Dao
interface MoviePreviewInfoDao {
    @Query("SELECT * FROM MoviePreviewInfo WHERE movieId LIKE :movieId LIMIT 1")
    fun findByMovieId(movieId: String): MoviePreviewInfo?

    @Query("SELECT * FROM MoviePreviewInfo ORDER BY topPosition ASC")
    fun selectAll(): List<MoviePreviewInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(element: MoviePreviewInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultiple(elements: List<MoviePreviewInfo>)

    @Delete
    fun delete(element: MoviePreviewInfo)

    @Query("DELETE FROM MoviePreviewInfo")
    fun deleteAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM Favorite ORDER BY movieId ASC")
    fun selectAll(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(element: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultiple(elements: List<Favorite>)

    @Delete
    fun delete(element: Favorite)

    @Query("DELETE FROM Favorite WHERE userId = :userId AND movieId = :movieId")
    fun deleteByUserAndMovieId(userId: Int, movieId: Int)

    @Query("DELETE FROM Favorite")
    fun deleteAll()
}

@Dao
interface MoviePreviewFullInfoDao {
    @Query("SELECT MovieCommonInfo.movieId, MovieCommonInfo.title, MovieCommonInfo.imdbRating, MovieCommonInfo.pageUrl, MoviePreviewInfo.topPosition, MoviePreviewInfo.imageUrl FROM MovieCommonInfo INNER JOIN MoviePreviewInfo ON MovieCommonInfo.movieId = MoviePreviewInfo.movieId ORDER BY topPosition ASC")
    fun selectTop(): List<MoviePreviewFullInfo>

    @Query("SELECT MovieCommonInfo.movieId, MovieCommonInfo.title, MovieCommonInfo.imdbRating, MovieCommonInfo.pageUrl, MoviePreviewInfo.topPosition, MoviePreviewInfo.imageUrl FROM MovieCommonInfo INNER JOIN MoviePreviewInfo ON MovieCommonInfo.movieId = MoviePreviewInfo.movieId INNER JOIN Favorite ON MovieCommonInfo.movieId = Favorite.movieId WHERE Favorite.userId = :userId ORDER BY topPosition ASC")
    fun selectFavoritesForCurrentUser(userId: Int): List<MoviePreviewFullInfo>
}

@Database
    ( entities =
        [   User::class
        ,   MovieCommonInfo::class
        ,   MoviePreviewInfo::class
        ,   MoviePageInfo::class
        ,   Favorite::class
        ]
    ,   version = 1 )
abstract class AppDatabase : RoomDatabase() {
    abstract val users: UserDao
    abstract val movies_common: MovieCommonInfoDao
    abstract val movies_preview: MoviePreviewInfoDao
    abstract val movies_preview_full: MoviePreviewFullInfoDao
    abstract val movies_page: MoviePageInfoDao
    abstract val favorites: FavoriteDao
}
