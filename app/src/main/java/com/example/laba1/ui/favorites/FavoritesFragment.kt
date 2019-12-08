package com.example.laba1.ui.favorites

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.laba1.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_layout.view.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import java.lang.ref.WeakReference

class FavoritesFragment : Fragment() {
    var db: AppDatabase? = null
    var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    fun loadFavorites(clbk:(List<MoviePreviewFullInfo>) -> Unit)
    {
        class LoadFromDbTask(val wSelf: WeakReference<FavoritesFragment>) : AsyncTask<Unit, Unit, List<MoviePreviewFullInfo>>()
        {
            override fun doInBackground(vararg params: Unit?): List<MoviePreviewFullInfo> {
                return db!!.movies_preview_full.selectFavoritesForCurrentUser(userId)
            }

            override fun onPostExecute(result: List<MoviePreviewFullInfo>) {
                wSelf.get()?.activity?.apply {
                    if(!isFinishing)
                        clbk(result)
                }
                super.onPostExecute(result)
            }
        }
        LoadFromDbTask(WeakReference(this)).execute()
    }

    fun reloadList(list: List<MoviePreviewFullInfo>)
    {
        createCards(list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply{
            db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db").build()
            userId = intent.getIntExtra("userId", -1)
        }

        loadFavorites { favoritesList ->
            reloadList(favoritesList)
        }

        frFavoritesRecycler.layoutManager = LinearLayoutManager(activity)
    }

    fun removeFavorite(userId: Int, movieId: Int) {
        class AddFavoriteTask(val wActivity: WeakReference<Activity>) : AsyncTask<Unit, Unit, Unit>()
        {
            override fun doInBackground(vararg params: Unit?) {
                db!!.favorites.deleteByUserAndMovieId(userId, movieId)
            }

            override fun onPostExecute(result: Unit?) {
                wActivity.get()?.apply {
                    if (!isFinishing) {
                        Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_LONG).show()
                    }
                }
                super.onPostExecute(result)
            }
        }
        AddFavoriteTask(WeakReference(requireActivity())).execute()
    }

    fun createCards(list: List<MoviePreviewFullInfo>) {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class MoviesRecyclerAdapter(val listInfo: List<MoviePreviewFullInfo>) : RecyclerView.Adapter<ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(layoutInflater.inflate(R.layout.card_layout, parent, false))
            }

            override fun getItemCount(): Int {
                return listInfo.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val cardWidget = holder.itemView
                val imdbElementInfo = listInfo[position]

                cardWidget.cardTitle.text = imdbElementInfo.title
                cardWidget.cardImdbRating.text = imdbElementInfo.imdbRating

                Picasso.get().load(imdbElementInfo.imageUrl).into(cardWidget.cardImage)

                cardWidget.setOnClickListener {
                    startActivity(
                        Intent(context, MovieInfoActivity::class.java).apply {
                            putExtra("userId", imdbElementInfo.movieId) // TODO
                            putExtra("movieId", imdbElementInfo.movieId)
                        }
                    )
                }

                val showMovieInfo = {
                    startActivity(
                        Intent(context, MovieInfoActivity::class.java).apply {
                            putExtra("userId", userId)
                            putExtra("movieId", imdbElementInfo.movieId)
                        }
                    )
                }

                cardWidget.setOnClickListener {
                    showMovieInfo()
                }

                registerForContextMenu(cardWidget)
                cardWidget.setOnCreateContextMenuListener { menu, v, menuInfo ->
                    //menu.setHeaderTitle("Context Menu");
                    menu.add(0, v.id, 0, "Info").setOnMenuItemClickListener {
                        showMovieInfo()
                        true
                    }
                    menu.add(0, v.id, 0, "Remove From Favorite").setOnMenuItemClickListener {
                        removeFavorite(userId, imdbElementInfo.movieId)

                        loadFavorites { favoritesList ->
                            reloadList(favoritesList)
                        }
                        true
                    }
                }
            }
        }

        frFavoritesRecycler.adapter = MoviesRecyclerAdapter(list)
    }
}