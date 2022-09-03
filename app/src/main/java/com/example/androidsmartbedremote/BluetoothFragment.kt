package com.example.androidsmartbedremote

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class BluetoothFragment : Fragment() {

    private val isLocationPermissionGranted
        get() = context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val isBluetoothPermissionGranted
        get() = context?.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.scan_button).setOnClickListener { startBleScan() }

        // activity?.let { BluetoothFunctions.getManager(it.application) }
        // if (bluetoothAdapter?.isEnabled == false) {
        //     val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        //     startActivityForResult(enableBtIntent, 0)
        // }
    }

    private fun startBleScan() {
        if (!isLocationPermissionGranted!!) {
            requestLocationPermission()
        } else if (!isBluetoothPermissionGranted!!) {
            requestBluetoothPermission()
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted == false) {
            AlertDialog.Builder(context)
                .setTitle("Location permission required")
                .setMessage("To allow for bluetooth scanning location access must be granted.")
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
                .setMessage("To use bluetooth scanning bluetooth permissions must be granted.")
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

    // Extension functions
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
}