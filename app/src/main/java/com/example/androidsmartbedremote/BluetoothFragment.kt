package com.example.androidsmartbedremote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidsmartbedremote.ble.ConnectionManager


@SuppressLint("MissingPermission")
class BluetoothFragment : Fragment() {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            activity?.application?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun bluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, 0)
    }

    // Bluetooth functions above these will be refactored to a view model once
    // fragment development is complete

    private lateinit var btnScan: Button

    private var isScanning = false
        set(value) {
            field = value
            activity?.runOnUiThread { btnScan.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            Log.w("scanResultAdapter", "${result.device.name} was pressed")
            if (isScanning) {
                stopBleScan()
            }
            result.device.connectGatt(context, false, ConnectionManager.gattCallback)
        }
    }

    private val isLocationPermissionGranted
        get() = context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val isBluetoothPermissionGranted
        get() = context?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private val isScanPermissionGranted
        get() = context?.hasPermission(Manifest.permission.BLUETOOTH_SCAN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnScan = view.findViewById(R.id.scan_button)
        btnScan.setOnClickListener {
            if (isScanning) {
                stopBleScan()
            } else {
                startBleScan()
            }
        }
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        view.findViewById<RecyclerView>(R.id.scan_results_recycler_view).apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun startBleScan() {
        if (!isLocationPermissionGranted!!) {
            requestLocationPermission()
        } else if (!isBluetoothPermissionGranted!!) {
            requestBluetoothPermission()
        } else if (!isScanPermissionGranted!!) {
            requestScanPermission()
        } else if (!bluetoothAdapter.isEnabled) {
            bluetoothEnable()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // a scan result already exists with the same address
                scanResults[indexQuery] = result // update item
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted == false) {
            AlertDialog.Builder(context)
                .setTitle("Location permission required")
                .setMessage("To allow for bluetooth scanning LOCATION permissions must be granted.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(),
                        0
                    )
                }.create()
                .show()
        }
    }

    private fun requestBluetoothPermission() {
        if (isBluetoothPermissionGranted == false) {
            AlertDialog.Builder(context)
                .setTitle("Bluetooth permission required")
                .setMessage("To use bluetooth scanning BLUETOOTH permissions must be granted.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        listOf(Manifest.permission.BLUETOOTH_CONNECT).toTypedArray(),
                        1
                    )
                }.create()
                .show()
        }
    }

    private fun requestScanPermission() {
        if (isScanPermissionGranted == false) {
            AlertDialog.Builder(context)
                .setTitle("Scan permission required")
                .setMessage("To use bluetooth scanning SCAN permissions must be granted.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        listOf(Manifest.permission.BLUETOOTH_SCAN).toTypedArray(),
                        1
                    )
                }.create()
                .show()
        }
    }

    // Extension functions
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
}