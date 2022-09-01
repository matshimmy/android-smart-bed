package com.example.androidsmartbedremote

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat

class RemoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_remote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btnRequestPermissions).setOnClickListener {requestPermissions() }
    }

    private fun hasBluetoothConnectionPermission() =
        getContext()?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED }

    private fun hasBluetoothScanPermission() =
        getContext()?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED }

    private fun hasFineLocationPermission() =
        getContext()?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED }

    private fun hasCoarseLocationPermission() =
        getContext()?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED }

    private fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if (!hasBluetoothConnectionPermission()!!) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (!hasBluetoothScanPermission()!!) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (!hasFineLocationPermission()!!) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasCoarseLocationPermission()!!) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(getContext() as Activity, permissionsToRequest.toTypedArray(), 0)
        }
    }

}