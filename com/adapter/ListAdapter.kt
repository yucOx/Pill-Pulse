import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.yucox.pillpulse.R
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListAdapter(
    var context: Context,
    var pillDetails: ArrayList<PillTime>,
    var reference: DatabaseReference
) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.list_taken_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pillDetails.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deleteBtn.visibility = View.GONE
        val pillInfo = pillDetails[position]
        holder.pillName.text = pillInfo.drugName
        holder.noteText.text = pillInfo.note
        holder.takenTime.text = pillInfo.whenYouTook

        showDeleteBtn(holder.bg,holder.deleteBtn)
        deleteItem(holder.deleteBtn,pillInfo,pillDetails)

    }

    private fun deleteItem(
        deleteBtn: ImageView,
        pillInfo: PillTime,
        pillDetails: ArrayList<PillTime>
    ) {
        deleteBtn.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Silmek istediğinize emin misiniz?")
                .setMessage("Evet'e basarsanız bu işlem geri alınamaz.")
                .setNegativeButton("Evet"){dialog,which ->
                    reference.child(pillInfo.key).removeValue()
                        .addOnCompleteListener{task ->
                            if(task.isSuccessful){
                                CoroutineScope(Dispatchers.Main).launch {
                                    pillDetails.remove(pillInfo)
                                    notifyDataSetChanged()
                                }
                            }
                        }
                }
                .setPositiveButton("Hayır"){dialog,which ->}
                .show()
        }
    }

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        var pillName = view.findViewById<TextView>(R.id.pill_name_tv)
        var takenTime = view.findViewById<TextView>(R.id.taken_time_tv)
        var noteText = view.findViewById<TextView>(R.id.note_tv)
        var bg = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
        var deleteBtn = view.findViewById<ImageView>(R.id.deleteBtn)
    }

    private fun showDeleteBtn(bg: ConstraintLayout, deleteBtn: ImageView) {
        bg.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                if(deleteBtn.visibility == View.GONE){
                    deleteBtn.visibility = View.VISIBLE
                }else{
                    deleteBtn.visibility = View.GONE
                }
                return true
            }
        })
    }
}