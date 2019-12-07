package com.example.laba1

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
    val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "AppDatabase"
    ).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_info)

        val moviePageUrl = intent.getStringExtra("pageUrl")

        title = "Loading..."

        if(moviePageUrl != null)
        {
            LoadPageTask(WeakReference(this))
            {
                title = it.title
                movieInfoTitle.text = it.title
                movieInfoDescription.text = it.description
                movieInfoRating.text = it.imdbRating
                movieInfoGanres.text = it.ganres
                movieInfoDuration.text = it.duration
                movieInfoReleaseDate.text = it.releaseData

                Picasso.get().load(it.imageUtl).into(movieInfoImage)
            }.execute(moviePageUrl)
        }
    }
}

class LoadPageTask(val fr: WeakReference<MovieInfoActivity>, val onEnd: (MovieInfoData) -> Unit) : AsyncTask<String, Unit, MovieInfoData>()
{
    override fun doInBackground(vararg params: String): MovieInfoData {
        val imdbPage = Jsoup.connect(params[0]).get()

        val titleOverviewWidget = imdbPage.select("div.title-overview")

        val titleSubtext = titleOverviewWidget.select("div.subtext").text().split("|").map {
            it.trim()
        }

        return MovieInfoData(
            title = titleOverviewWidget.select("div.title_wrapper").select("h1").text(),
            description = titleOverviewWidget.select("div.summary_text").text(),
            imdbRating = titleOverviewWidget.select("div.ratingValue").text(),
            ganres = titleSubtext[2],
            duration = titleSubtext[1],
            releaseData = titleSubtext[3],
            imageUtl = titleOverviewWidget.select("div.poster").select("a").select("img").attr("src")
        )
    }

    override fun onPostExecute(result: MovieInfoData) {
        fr.get()?.let { activity ->
            if(!activity.isFinishing)
                onEnd(result)
        }
        super.onPostExecute(result)
    }
}