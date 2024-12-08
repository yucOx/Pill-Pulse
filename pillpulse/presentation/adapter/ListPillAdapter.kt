import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.R
import com.yucox.pillpulse.domain.model.Pill
import org.mongodb.kbson.ObjectId

class ListPillAdapter(
    private val context: Context,
    var pillList: MutableList<Pill>,
    private val removePill: (id: String) -> Unit
) :
    RecyclerView.Adapter<ListPillAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_medicine_item,
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pillList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pill = pillList[position]

        holder.pillName.text = pill.drugName
        holder.takenTime.text = pill.whenYouTookHour
        holder.takenDate.text = pill.whenYouTookDate

        holder.deleteBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Silmek istediğinize emin misiniz?")
                .setMessage("Evet'e basarsanız bu işlem geri alınamaz.")
                .setNegativeButton("Evet") { dialog, which ->
                    val position = pillList.indexOf(pill)
                    if (position != -1) {
                        removePill(pill._id)
                        pillList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                .setPositiveButton("Hayır") { dialog, which -> }
                .show()
        }
    }

    fun submitList(pills: List<Pill>) {
        try {
            pillList.clear()
            pillList.addAll(pills)
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pillName = view.findViewById<TextView>(R.id.pill_name_tv)
        val takenTime: TextView = view.findViewById<TextView>(R.id.taken_time_tv)
        val takenDate: TextView = view.findViewById<TextView>(R.id.taken_date_tv)
        val bg = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
        val deleteBtn = view.findViewById<ImageView>(R.id.deleteBtn)
    }
}