package com.chetantuteja.pocketbarcode

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.actions.getActionButton

class MainActivity : AppCompatActivity() {
    private val GET_BARCODE_DATA = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getStartedClick(view: View) {
        val intent = Intent(this,BarcodeActivity::class.java)
        //startActivity(intent)
        startActivityForResult(intent, GET_BARCODE_DATA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GET_BARCODE_DATA && resultCode == Activity.RESULT_OK) {
            if(data !=null && data.hasExtra("barcode")) {
                val value = data.getStringExtra("barcode")
                if(value != null) {
                    showDialog(value)
                }
            }
        }
    }

    private fun showDialog(textVal: String) {
        MaterialDialog(this).show {
            title(text = "The Scanned Result is:")
            message(text = textVal)
            positiveButton(text = "OK") { dialog ->
                dialog.dismiss()
            }
            negativeButton(text = "COPY") {dialog ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", textVal)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@MainActivity, "Text Copied to Clipboard.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            cornerRadius(16f)
        }
    }
}
