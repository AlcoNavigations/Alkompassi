package fi.metropolia.alkompassi.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko
import fi.metropolia.alkompassi.utils.DatabaseManager
import fi.metropolia.alkompassi.utils.LocationUtility
import fi.metropolia.alkompassi.utils.MapHolder
import kotlinx.android.synthetic.main.alkolist_item.view.*

class AlkoListAdapter(private var dataset: List<Alko>, private val mapHolder: MapHolder, context: Context?, private var favoriteAlkos: List<FavoriteAlko>) : RecyclerView.Adapter<AlkoListAdapter.AlkoListItemViewHolder>() {

    private val mDb = DatabaseManager(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlkoListItemViewHolder {
       val listItem = LayoutInflater.from(parent.context)
               .inflate(R.layout.alkolist_item, parent, false) as ConstraintLayout
        return AlkoListItemViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AlkoListItemViewHolder, position: Int) {

       val favorite: Boolean = favoriteAlkos.find { it.placeID == dataset[position].placeID } != null

        holder.listItem.textView_alko_name.text = dataset[position].name

        // Lint suppressed: Only numbers and "m" for meters. No need to translate ever.
        holder.listItem.textView_alko_distance.text = "${LocationUtility.distToAlko(dataset[position], mapHolder.getLocation()).toInt()} m"

        if (favorite) {
            holder.listItem.imageView_fav.setImageResource(R.drawable.ic_round_favorite_24px)
            holder.listItem.imageView_fav.setOnClickListener {
                holder.listItem.imageView_fav.setImageResource(R.drawable.ic_round_favorite_border_24px)
                mDb.doAsyncDeleteFavorites(dataset, position, favoriteAlkos)
                favoriteAlkos = mDb.doAsyncGetFavorites()
                notifyDataSetChanged()
            }
        } else {
            holder.listItem.imageView_fav.setImageResource(R.drawable.ic_round_favorite_border_24px)
            holder.listItem.imageView_fav.setOnClickListener {
                holder.listItem.imageView_fav.setImageResource(R.drawable.ic_round_favorite_24px)
                mDb.doAsyncSaveFavorites(dataset, position)
                favoriteAlkos = mDb.doAsyncGetFavorites()
                notifyDataSetChanged()
            }
        }
    }

    fun updateFavorites(newFavorites: MutableList<FavoriteAlko>) {
        favoriteAlkos = newFavorites
    }

    class AlkoListItemViewHolder(val listItem: View) : RecyclerView.ViewHolder(listItem)
}