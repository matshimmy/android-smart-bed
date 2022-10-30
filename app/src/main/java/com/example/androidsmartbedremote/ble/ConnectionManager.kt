package com.example.androidsmartbedremote.ble

import android.annotation.SuppressLint
import android.app.PendingIntent.getService
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

// Maximum ble MTU size as defined in gatt_api.h and minimum ble packet size
private const val GATT_MIN_MTU_SIZE = 23
private const val GATT_MAX_MTU_SIZE = 517

object ConnectionManager {

    var gattReference: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    private fun readCornerLamp() { //testing corner lamp at home
        Log.w("readCornerLamp", "reading from corner lamp characteristics and service")
        val lampServiceUuid =
            UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb") //allocated for device information
        val lampNameCharUuid =
            UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb") //allocated forward manufacturer name string
        val lampNameChar =
            gattReference?.getService(lampServiceUuid)?.getCharacteristic(lampNameCharUuid)
        if (lampNameChar?.isReadable() == true) {
            gattReference?.readCharacteristic(lampNameChar)
        }
    }

    val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    gattReference = gatt
                    // gatt?.requestMtu(GATT_MAX_MTU_SIZE)
                    Handler(Looper.getMainLooper()).post { // forces discovery service to run on main thread prevents deadlock
                        gatt?.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt?.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $ encountered for $deviceAddress! Disconnecting..."
                )
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.w(
                "BluetoothGattCallback",
                "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}"
            )
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }
    }
}