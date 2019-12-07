package com.example.laba1

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_log_in.*

import com.example.laba1.AppDatabase
import java.lang.ref.WeakReference

abstract class AsyncTaskImpl<Params, Progress, Result>
{
    open fun onPreExecute()
        { /* DO NOTHING */ }
    abstract fun doInBackground(vararg params: Params): Result
    open fun onProgressUpdate(vararg values: Progress)
        { /* DO NOTHING */ }
    open fun onPostExecute(result: Result)
        { /* DO NOTHING */ }
    open fun onCancelled()
        { /* DO NOTHING */ }
    open fun onCancelled(result: Result)
        { /* DO NOTHING */ }
}

fun <Params, Progress, Result> runAsync(impl: AsyncTaskImpl<Params, Progress, Result>, vararg params: Params)
{
    class RealAsyncTask(val impl: AsyncTaskImpl<Params, Progress, Result>) : AsyncTask<Params, Progress, Result>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            impl.onPreExecute()
        }

        override fun doInBackground(vararg params: Params): Result {
            return impl.doInBackground(*params)
        }

        override fun onProgressUpdate(vararg values: Progress) {
            super.onProgressUpdate(*values)
            impl.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            impl.onPostExecute(result)
        }

        override fun onCancelled() {
            super.onCancelled()
            impl.onCancelled()
        }

        override fun onCancelled(result: Result) {
            super.onCancelled(result)
            impl.onCancelled(result)
        }
    }

    RealAsyncTask(impl).execute(*params)
}

class LogInActivity : AppCompatActivity() {
    var db: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        mainBtLogIn.setOnClickListener{
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra("name", mainEdLogin.text.toString())
                }
            )
        }

        getDatabasePath("db")?.let {
            deleteDatabase("db")
        }
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db").build()

        class InsertUser(val weakActivity: WeakReference<LogInActivity>) : AsyncTask<Unit, Unit, List<User>>()
        {
            override fun doInBackground(vararg params: Unit?): List<User> {
                return db!!.run{
                    users.insertUser(User("root", "root"))
                    users.insertUser(User("root", "root"))

                    users.getAll()
                }
            }

            override fun onPostExecute(result: List<User>) {
                super.onPostExecute(result)

                weakActivity.get()?.apply {
                    Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
        InsertUser(WeakReference(this)).execute()
    }
}
