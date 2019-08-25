package com.chetantuteja.pocketbarcode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_barcode.*

class BarcodeActivity : AppCompatActivity() {
    private val TAG = "BarcodeActivity"
    private val PERMISSION_REQUEST_CAMERA = 1024

    private lateinit var bDetector:BarcodeDetector
    private lateinit var cameraSource: CameraSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)

        bDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        if(bDetector.isOperational) {
            setupScanner()
        } else {
            Log.w(TAG, "Detector dependencies are not yet available.")

            if(cacheDir.usableSpace * 100 / cacheDir.totalSpace <=10) {
                Toast.makeText(this, R.string.low_storage, Toast.LENGTH_LONG).show()
                Log.w(TAG, "Low Storage")
            }
        }

    }

    private fun setupScanner(){
        cameraSource = CameraSource.Builder(this,bDetector)
            .setRequestedFps(15.0f)
            .setRequestedPreviewSize(1024,768)
            .setAutoFocusEnabled(true)
            .build()

        scannerView.holder.addCallback(object: SurfaceHolder.Callback2{
            override fun surfaceRedrawNeeded(p0: SurfaceHolder?) {
            }

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    if(isPermissionGranted()) {
                        cameraSource.start(scannerView.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@BarcodeActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        })
        setupDetector()
    }

    private fun setupDetector() {
        bDetector.setProcessor(object: Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                val items = detections?.detectedItems
                if(items!= null) {
                    if (items.size() <= 0) {
                        return
                    }
                    val sBuilder = StringBuilder()
                    for(i in 0 until items.size()) {
                        val itemVal = items.valueAt(i)
                        sBuilder.append(itemVal.rawValue)
                        sBuilder.append("\n")
                    }
                    displayTV.post {

                        displayTV.text = items.valueAt(0).rawValue

                        /*val intent = Intent()
                        intent.putExtra("barcode",items.valueAt(0).rawValue)
                        setResult(Activity.RESULT_OK, intent)*/
                    }
                    val intent = Intent()
                    intent.putExtra("barcode",sBuilder.toString())
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }
            }

        })

    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty()) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(isPermissionGranted()) {
                    cameraSource.start(scannerView.holder)
                }
                else {
                    Toast.makeText(this, R.string.need_permission, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun isPermissionGranted(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),PERMISSION_REQUEST_CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        bDetector.release()
        cameraSource.stop()
        cameraSource.release()
    }
}
