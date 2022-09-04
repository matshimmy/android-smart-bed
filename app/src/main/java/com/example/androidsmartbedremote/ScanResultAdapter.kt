package com.example.androidsmartbedremote

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanResultAdapter(
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    class ViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {
        @SuppressLint("MissingPermission", "SetTextI18n")
        fun bind(result: ScanResult) {
            view.findViewById<TextView>(R.id.device_name).text = result.device.name ?: "Unnamed"
            view.findViewById<TextView>(R.id.mac_address).text = result.device.address
            view.findViewById<TextView>(R.id.signal_strength).text = "${result.rssi} dBm"
            view.setOnClickListener { onClickListener.invoke(result) }
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_scan_result, parent, false)
        return ViewHolder(adapterLayout, onClickListener)
    }
}