package com.example.laba1

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_movie_info.*
import kotlinx.android.synthetic.main.activity_movie_info_content.*
import org.jsoup.Jsoup
import java.lang.ref.WeakReference

data class MovieInfoData(
    val title: String,
    val description: String,
    val imdbRating: String,
    val ganres: String,
    val duration: String,
    val releaseData: String,
    val imageUtl: String
)

class MovieInfoActivity : AppCompatActivity() {
    var db: AppDatabase? = null

    fun loadCommonPageContentFromCache(movieId: Int, clbk: (MovieCommonInfo?) -> Unit) {
        class LoadFromDbTask(val wSelf: WeakReference<MovieInfoActivity>) : AsyncTask<Unit, Unit, MovieCommonInfo?>()
        {
            override fun doInBackground(vararg params: Unit?): MovieCommonInfo? {
                return db!!.movies_common.findByMovieId(movieId)
            }

            override fun onPostExecute(result: MovieCommonInfo?) {
                wSelf.get()?.apply {
                    if(!isFinishing) {
                        assert(result != null)
                        clbk(result)
                    }
                }
                super.onPostExecute(result)
            }
        }
        LoadFromDbTask(WeakReference(this)).execute()
    }

    fun loadFromCache(movieId: Int, clbk: (MoviePageInfo?) -> Unit) {
        class LoadFromDbTask(val wSelf: WeakReference<MovieInfoActivity>) : AsyncTask<Unit, Unit, MoviePageInfo?>()
        {
            override fun doInBackground(vararg params: Unit?): MoviePageInfo? {
                return db!!.movies_page.findByMovieId(movieId)
            }

            override fun onPostExecute(result: MoviePageInfo?) {
                wSelf.get()?.apply {
                    if(!isFinishing)
                        clbk(result)
                }
                super.onPostExecute(result)
            }
        }
        LoadFromDbTask(WeakReference(this)).execute()
    }

    fun loadFromInternet(movieCommonInfo: MovieCommonInfo, clbk: (MoviePageInfo?) -> Unit) {
        class LoadFromInternetTask(val wSelf: WeakReference<MovieInfoActivity>) : AsyncTask<Unit, Unit, MoviePageInfo?>()
        {
            override fun doInBackground(vararg params: Unit?): MoviePageInfo? {
                val imdbPage = Jsoup.connect(movieCommonInfo.pageUrl).get()

                val titleOverviewWidget = imdbPage.select("div.title-overview")

                val titleSubtext = titleOverviewWidget.select("div.subtext").text().split("|").map {
                    it.trim()
                }

                return MoviePageInfo(
                    movieId = movieCommonInfo.movieId,
                    description = titleOverviewWidget.select("div.summary_text").text(),
                    ganres = titleSubtext[2],
                    duration = titleSubtext[1],
                    releaseData =titleSubtext[3],
                    imageUrl = titleOverviewWidget.select("div.poster").select("a").select("img").attr("src")
                )
            }

            override fun onPostExecute(result: MoviePageInfo?) {
                wSelf.get()?.apply {
                    if(!isFinishing)
                        clbk(result)
                }
                super.onPostExecute(result)
            }
        }
        LoadFromInternetTask(WeakReference(this)).execute()
    }

    @SuppressLint("SetTextI18n")
    fun updatePageInfo(movieCommonInfo: MovieCommonInfo, moviePageInfo: MoviePageInfo) {
        title = movieCommonInfo.title
        movieInfoTitle.text = movieCommonInfo.title
        movieInfoDescription.text = moviePageInfo.description
        movieInfoRating.text = movieCommonInfo.imdbRating + "/10"
        movieInfoGanres.text = moviePageInfo.ganres
        movieInfoDuration.text = moviePageInfo.duration
        movieInfoReleaseDate.text = moviePageInfo.releaseData

        Picasso.get().load(moviePageInfo.imageUrl).into(movieInfoImage)
    }

    fun updateCache(moviePageInfo: MoviePageInfo) {
        class UpdateCacheTask : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit) {
                db!!.movies_page.insert(moviePageInfo)
            }
        }
        UpdateCacheTask().execute()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_info)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db").build()
        assert(db != null)

        val movieId = intent.getIntExtra("movieId", -1)

        title = "Loading..."

        loadCommonPageContentFromCache(movieId) { itMovieCommonInfo ->
            if(itMovieCommonInfo != null)
            {
                loadFromCache(itMovieCommonInfo.movieId) { itMoviePageInfoCache ->
                    if (itMoviePageInfoCache != null) {
                        updatePageInfo(itMovieCommonInfo, itMoviePageInfoCache)
                    }
                    loadFromInternet(itMovieCommonInfo) { itMoviePageInfoInternet ->
                        if (itMoviePageInfoInternet != null) {
                            updateCache(itMoviePageInfoInternet)
                            updatePageInfo(itMovieCommonInfo, itMoviePageInfoInternet)
                        }
                    }
                }
            }
        }
    }
}