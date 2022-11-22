package com.example.androidsmartbedremote.ble

import android.annotation.SuppressLint
import android.bluetooth.*
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
    fun readCornerLamp() { //testing corner lamp at home
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

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        gattReference?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
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

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }
    }

    // createBond() might need to be used, or connect to an encrypted characteristic
    //https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createBond()
//    fun listenToBondStateChanges(context: Context) {
//        context.applicationContext.registerReceiver(
//            broadcastReceiver,
//            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//        )
//    }
//
//    private val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            with(intent) {
//                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
//                    val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//                    val previousBondState = getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
//                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
//                    val bondTransition = "${previousBondState.toBondStateDescription()} to " +
//                            bondState.toBondStateDescription()
//                    Log.w("Bond state change", "${device?.address} bond state changed | $bondTransition")
//                }
//            }
//        }
//
//        private fun Int.toBondStateDescription() = when(this) {
//            BluetoothDevice.BOND_BONDED -> "BONDED"
//            BluetoothDevice.BOND_BONDING -> "BONDING"
//            BluetoothDevice.BOND_NONE -> "NOT BONDED"
//            else -> "ERROR: $this"
//        }
//    }
}