package fi.metropolia.alkompassi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.datamodels.Alko
import fi.metropolia.alkompassi.utils.LocationUtility
import fi.metropolia.alkompassi.utils.MapHolder
import kotlinx.android.synthetic.main.alkolist_item.view.*

class AlkoListAdapter(private var dataset: List<Alko>, private val mapHolder: MapHolder) : RecyclerView.Adapter<AlkoListAdapter.AlkoListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlkoListItemViewHolder {
       val listItem = LayoutInflater.from(parent.context)
               .inflate(R.layout.alkolist_item, parent, false) as ConstraintLayout
        return AlkoListItemViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: AlkoListItemViewHolder, position: Int) {
        holder.listItem.textView_alko_name.text = dataset[position].name
        holder.listItem.textView_alko_distance.text = "${LocationUtility.distToAlko(dataset[position], mapHolder.getLocation()).toInt()} m"
    }

    class AlkoListItemViewHolder(val listItem: View) : RecyclerView.ViewHolder(listItem)
}