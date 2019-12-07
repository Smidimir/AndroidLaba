package com.example.laba1.ui.movies

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.laba1.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.fragment_movies.*

import org.jsoup.Jsoup
import java.lang.ref.WeakReference

class MoviesFragment : Fragment() {
    var db: AppDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movies, container, false)
    }

    fun loadFromCache(clbk:(List<MoviePreviewFullInfo>) -> Unit)
    {
        class LoadFromDbTask(val wSelf: WeakReference<MoviesFragment>, val clbk:(List<MoviePreviewFullInfo>) -> Unit) : AsyncTask<Unit, Unit, List<MoviePreviewFullInfo>>()
        {
            override fun doInBackground(vararg params: Unit?): List<MoviePreviewFullInfo> {
                return db!!.movies_preview_full.selectTop()
            }

            override fun onPostExecute(result: List<MoviePreviewFullInfo>) {
                wSelf.get()?.activity?.apply {
                    if(!isFinishing)
                        clbk(result)
                }
                super.onPostExecute(result)
            }
        }
        LoadFromDbTask(WeakReference(this), clbk).execute()
    }

    fun loadFromInternet(clbk:(List<MoviePreviewFullInfo>) -> Unit)
    {
        class LoadFromInternetTask(val wSelf: WeakReference<MoviesFragment>, val clbk:(List<MoviePreviewFullInfo>) -> Unit) : AsyncTask<Unit, Unit, List<MoviePreviewFullInfo>>()
        {
            override fun doInBackground(vararg params: Unit?): List<MoviePreviewFullInfo> {
                val imdbMainAddress = "https://www.imdb.com"
                val imdbPageAddress = "${imdbMainAddress}/chart/top?ref_=nv_mv_250"
                val imdbPage = Jsoup.connect(imdbPageAddress).get()

                val imdbElements = imdbPage.select("tbody.lister-list").first().children()

                var counter = 0
                return imdbElements.map {
                    val titleColumn = it.select("td.titleColumn")

                    val localUrl = titleColumn.select("a").attr("href")
                    val movieId = Regex("/title/tt([0-9]*)/.*").find(localUrl)!!.destructured.component1().toInt()

                    MoviePreviewFullInfo(
                        movieId     = movieId,
                        title       = titleColumn.text(),
                        imdbRating  = it.select("td.ratingColumn.imdbRating").text(),
                        pageUrl     = imdbMainAddress + localUrl,
                        topPosition = counter++,
                        imageUrl    = it.select("td.posterColumn").select("a").select("img").attr("src")
                    )
                }
            }

            override fun onPostExecute(result: List<MoviePreviewFullInfo>) {
                wSelf.get()?.activity?.apply {
                    if(!isFinishing)
                        clbk(result)
                }
                super.onPostExecute(result)
            }
        }
        LoadFromInternetTask(WeakReference(this), clbk).execute()
    }

    fun reloadList(list: List<MoviePreviewFullInfo>)
    {
        frMoviesVll.removeAllViews()

        list.forEach{
            createCard(it)
        }
    }

    fun updateCache(list: List<MoviePreviewFullInfo>)
    {
        class UpdateCacheTask : AsyncTask<Unit, Unit, Unit>()
        {
            override fun doInBackground(vararg params: Unit) {
                db!!.movies_common.insertMultiple(list.map{ it.asCommon })
                db!!.movies_preview.insertMultiple(list.map{ it.asPreview })
            }
        }
        UpdateCacheTask().execute()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.apply{
            db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db").build()
        }
        assert(db != null)

        loadFromCache { cacheList ->
            if(cacheList.isNotEmpty()) {
                reloadList(cacheList)
            }

            loadFromInternet { internetList ->
                if(internetList.isNotEmpty()) {
                    updateCache(internetList)
                    reloadList(internetList)
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    fun createCard(imdbElementInfo: MoviePreviewFullInfo)
    {
        val cardWidget = layoutInflater.inflate(R.layout.card_layout, frMoviesVll, false)

        cardWidget.cardTitle.text = imdbElementInfo.title
        cardWidget.cardImdbRating.text = imdbElementInfo.imdbRating

        Picasso.get().load(imdbElementInfo.imageUrl).into(cardWidget.cardImage)

        cardWidget.setOnClickListener {
            startActivity(
                Intent(context, MovieInfoActivity::class.java).apply {
                    putExtra("movieId", imdbElementInfo.movieId)
                }
            )
        }

        frMoviesVll.addView(cardWidget)
    }
}
