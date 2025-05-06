package com.example.rescueme.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rescueme.EmergencyActivity
import com.example.rescueme.R

class EmergencyAdapter(
    private val context: EmergencyActivity,
    private var contactList: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<EmergencyAdapter.ContactViewHolder>() {

    fun updateContacts(newContacts: MutableList<Contact>) {
        contactList = newContacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.bind(contact)

        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contactList.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val relationTextView: TextView = itemView.findViewById(R.id.relationTextView)
        private val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        private val serviceTypeTextView: TextView = itemView.findViewById(R.id.serviceTypeTextView)
        private val arrowImageView: ImageView = itemView.findViewById(R.id.arrowImageView)

        fun bind(contact: Contact) {
            nameTextView.text = contact.name
            relationTextView.text = contact.relation
            phoneNumberTextView.text = contact.phoneNumber
            profileImageView.setImageResource(contact.profileImageResourceId)

            if (contact.isEmergencyService) {
                serviceTypeTextView.visibility = View.VISIBLE
                serviceTypeTextView.text = contact.serviceType
            } else {
                serviceTypeTextView.visibility = View.GONE
            }
        }
    }
}