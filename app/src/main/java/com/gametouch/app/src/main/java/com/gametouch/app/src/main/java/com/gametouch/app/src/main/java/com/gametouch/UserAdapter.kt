package com.gametouch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val onDelete: (User) -> Unit,
    private val onReset: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    private val list = mutableListOf<User>()

    fun submit(data: List<User>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvUser: TextView = v.findViewById(R.id.tvItemUser)
        val tvRole: TextView = v.findViewById(R.id.tvItemRole)
        val tvBy: TextView = v.findViewById(R.id.tvItemBy)
        val btnReset: Button = v.findViewById(R.id.btnItemReset)
        val btnDelete: Button = v.findViewById(R.id.btnItemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return VH(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = list[position]
        holder.tvUser.text = u.username
        holder.tvRole.text = "Role: ${u.role.label}"
        holder.tvBy.text = "Dibuat oleh: ${u.createdBy}"
        holder.btnReset.setOnClickListener { onReset(u) }
        holder.btnDelete.setOnClickListener { onDelete(u) }
    }
}
