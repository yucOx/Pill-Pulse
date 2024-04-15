import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yucox.pillpulse.R
import com.yucox.pillpulse.Repository.PillRepository
import com.yucox.pillpulse.Model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ListPillAdapter(
    private val context: Context,
    private val pillList: LiveData<ArrayList<PillTime>>,
) :
    RecyclerView.Adapter<ListPillAdapter.ViewHolder>() {
    private val pillRepository = PillRepository()
    private val _shf = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )
    private val _sdf = SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault()
    )
    val dispatchersIO = CoroutineScope(Dispatchers.IO)

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
        return pillList.value?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sortByTime = pillList.value!!.sortedByDescending { it.whenYouTook }
        val pillInfo = sortByTime[position]
        holder.pillName.text = pillInfo.drugName

        val formattedTime = _shf.format(pillInfo.whenYouTook)
        holder.takenTime.text = formattedTime

        val formattedDate = _sdf.format(pillInfo.whenYouTook)
        holder.takenDate.text = formattedDate


        holder.bg.setOnClickListener {
            val rootView = (context as Activity).findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "Düzenlemek için basılı tutun", Snackbar.LENGTH_SHORT).show()
        }

        showEditOptions(holder.bg, holder.deleteBtn)

        holder.deleteBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Silmek istediğinize emin misiniz?")
                .setMessage("Evet'e basarsanız bu işlem geri alınamaz.")
                .setNegativeButton("Evet") { dialog, which ->
                    dispatchersIO.launch {
                        val (result, exception) = pillRepository.deletePill(pillInfo)
                        if (result) {
                            withContext(Dispatchers.Main) {
                                pillList.value?.let {
                                    it.remove(pillInfo)
                                    notifyItemRemoved(position)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    exception,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                }
                .setPositiveButton("Hayır") { dialog, which -> }
                .show()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pillName = view.findViewById<TextView>(R.id.pill_name_tv)
        val takenTime: TextView = view.findViewById<TextView>(R.id.taken_time_tv)
        val takenDate: TextView = view.findViewById<TextView>(R.id.taken_date_tv)
        val bg = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
        val deleteBtn = view.findViewById<ImageView>(R.id.deleteBtn)
    }

    private fun showEditOptions(
        bg: ConstraintLayout,
        deleteBtn: ImageView,
    ) {
        bg.setOnLongClickListener {
            if (deleteBtn.visibility == View.GONE) {
                deleteBtn.visibility = View.VISIBLE

                val rootView = (context as Activity).findViewById<View>(android.R.id.content)
                Snackbar.make(
                    rootView,
                    "Notu değiştirmek için nota tıklayın",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Tamam") {}
                    .show()

            } else {
                deleteBtn.visibility = View.GONE
            }
            true
        }
    }
}