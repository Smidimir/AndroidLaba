package com.example.laba1.ui.movies

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.example.laba1.AppDatabase
import com.example.laba1.MovieInfoActivity
import com.example.laba1.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.fragment_movies.*

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.InputStream
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class ValueWrapper<T>(val value: T)
class MutableValueWrapper<T>(var value: T)

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

class MoviesFragment : Fragment() {
//    val db = Room.databaseBuilder(
//        activity!!.applicationContext,
//        AppDatabase::class.java, "AppDatabase"
//    ).build()

    var loadInProgress = false
    set(value)
    {
        field = value
    }

    private lateinit var moviesViewModel: MoviesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        moviesViewModel =
            ViewModelProviders.of(this).get(MoviesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_movies, container, false)

        moviesViewModel.text.observe(this, Observer {
            text_movies.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadInProgress = true
        LoadPageTask(WeakReference(this)) { imdbListElements ->
            imdbListElements.forEach{
                createCard(it)
            }
            loadInProgress = false
        }.execute()

        super.onViewCreated(view, savedInstanceState)
    }

    fun createCard(imdbElementInfo: ImdbListElement)
    {
        val cardWidget = layoutInflater.inflate(R.layout.card_layout, frMoviesVll, false)

        cardWidget.cardTitle.text = imdbElementInfo.title
        cardWidget.cardImdbRating.text = imdbElementInfo.ratingImdb
        //Picasso.get().load(imdbElementInfo.imagePreviewUrl).into(cardWidget.cardImage)
        DownloadImageTask(WeakReference(cardWidget.cardImage)){ image, width, height ->
            image.layoutParams.height = (image.layoutParams.width.toFloat() / width.toFloat() * height.toFloat()).toInt()
        }.execute(imdbElementInfo.imagePreviewUrl)

        cardWidget.setOnClickListener {
            startActivity(
                Intent(context, MovieInfoActivity::class.java).apply {
                    putExtra("pageUrl", imdbElementInfo.pageUrl)
                }
            )
        }

        frMoviesVll.addView(cardWidget)
    }
}

class LoadPageTask(val fr: WeakReference<MoviesFragment>, val onEnd: (List<ImdbListElement>) -> Unit) : AsyncTask<Unit, Unit, List<ImdbListElement>>()
{
    override fun doInBackground(vararg params: Unit?): List<ImdbListElement> {
        val imdbMainAddress = "https://www.imdb.com"
        val imdbPageAddress = "${imdbMainAddress}/chart/top?ref_=nv_mv_250"
        val imdbPage = Jsoup.connect(imdbPageAddress).get()

        val imdbElements = imdbPage.select("tbody.lister-list").first().children()

        return imdbElements.map {
            val titleColumn = it.select("td.titleColumn")
            ImdbListElement(
                title = titleColumn.text(),
                pageUrl = imdbMainAddress + titleColumn.select("a").attr("href"),
                imagePreviewUrl = it.select("td.posterColumn").select("a").select("img").attr("src"),
                ratingImdb = it.select("td.ratingColumn.imdbRating").text()
            )
        }
    }

    override fun onPostExecute(result: List<ImdbListElement>) {
        fr.get()?.activity?.let { activity ->
            if(!activity.isFinishing)
                onEnd(result)
        }
        super.onPostExecute(result)
    }
}

class DownloadImageTask(private val bmImage: WeakReference<ImageView>, val onEnd: (ImageView, Int, Int) -> Unit) : AsyncTask<String, Unit, Bitmap?>()
{
    override fun doInBackground(vararg urls: String?): Bitmap? {
        val urldisplay = urls[0]
        var mIcon11 = null as Bitmap?

        try {
            val inStream = java.net.URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(inStream)
        } catch (_: Exception) {

        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        if(result != null)
            bmImage.get()?.apply {
                setImageBitmap(result)
                onEnd(this, result.width, result.height)
            }

        super.onPostExecute(result)
    }
}

class LoadImdbElementTask(
    private val fr: WeakReference<MoviesFragment>,
    private val imdbElements: List<MutableValueWrapper<ImdbElement?>>,
    private val url: String,
    private val imdbElement: MutableValueWrapper<ImdbElement?>,
    private val counter: MutableValueWrapper<Int>,
    private val onEnd: (List<ImdbElement>) -> Unit
) : AsyncTask<Unit, Unit, Unit>()
{
    fun parseImdbElementPage(pageUrl: String): ImdbElement
    {
        val imdbElementPage = Jsoup.connect(pageUrl).get()
        val titleOverviewWidget = imdbElementPage.select("div#title-overview-widget.heroic-overview")

        return ImdbElement(
            title       = titleOverviewWidget.select("div.title_wrapper").select("h1").text(),
            description = titleOverviewWidget.select("div.summary_text").text(),
            imageUrl    = titleOverviewWidget.select("div.poster").select("a").select("img").attr("src")
        )
    }

    override fun doInBackground(vararg params: Unit?) {
        imdbElement.value = parseImdbElementPage(url)
    }

    override fun onPostExecute(result: Unit?) {
        --counter.value
        if(counter.value == 0)
        {
            fr.get()?.activity?.let { activity ->
                if (!activity.isFinishing)
                    onEnd(
                        (imdbElements as List<MutableValueWrapper<ImdbElement>>).map {
                            it.value
                        }
                    )
            }
        }
        super.onPostExecute(result)
    }
}