package com.example.laba1

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import androidx.room.Room
import kotlinx.android.synthetic.main.activity_log_in.*
import java.lang.ref.WeakReference

class LogInActivity : AppCompatActivity() {
    var db: AppDatabase? = null

    fun tryLogIn(login: String, password: String, clbk: (Boolean) -> Unit) {
        class TryLogInTask(val wSelf: WeakReference<LogInActivity>) : AsyncTask<Unit, Unit, Boolean>() {
            override fun doInBackground(vararg params: Unit?): Boolean {
                val user = db!!.users.findByName(login)

                return user != null && user.password == password
            }

            override fun onPostExecute(result: Boolean) {
                if(wSelf.get() != null && !isFinishing)
                    clbk(result)

                super.onPostExecute(result)
            }
        }
        TryLogInTask(WeakReference(this)).execute()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db").build()

        mainBtLogIn.setOnClickListener{
            tryLogIn(mainEdLogin.text.toString(), mainEdPassword.text.toString()) { bSuccess ->
                if(bSuccess) {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            putExtra("name", mainEdLogin.text.toString())
                        }
                    )
                } else {
                    Toast.makeText(this, "Wrong login or password", Toast.LENGTH_LONG).show()
                }
            }
        }

        getDatabasePath("db")?.let {
            //deleteDatabase("db")
        }

        class InsertUser : AsyncTask<Unit, Unit, List<User>>()
        {
            override fun doInBackground(vararg params: Unit?): List<User> {
                return db!!.run{
                    users.insert(User("root", "root"))
                    users.insert(User("root", "root"))

                    users.selectAll()
                }
            }
        }
        InsertUser().execute()
    }
}
