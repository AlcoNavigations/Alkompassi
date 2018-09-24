package fi.metropolia.alkompassi.util

import android.os.AsyncTask

abstract class AsyncTasker(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

/*

    Run any task async in the project:

        AsyncTasker.doAsync {
            yourTask()
        }.execute()

 */