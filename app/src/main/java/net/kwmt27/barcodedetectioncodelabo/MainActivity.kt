package net.kwmt27.barcodedetectioncodelabo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val button: Button = findViewById(R.id.button)
        button.setOnClickListener({ v -> Log.d("tag", "onclicklistener") })

        val textView: TextView = findViewById(R.id.txtContent)


        val imageView: ImageView = findViewById(R.id.imgview)
        val bitmap = BitmapFactory.decodeResource(applicationContext.resources,
                R.drawable.puppy)
        imageView.setImageBitmap(bitmap)


        val detector = BarcodeDetector.Builder(applicationContext)
                .setBarcodeFormats(Barcode.DATA_MATRIX.or(Barcode.QR_CODE)).build()
        if (!detector.isOperational) {
            textView.setText("Could not set up the dector!")
            return
        }

        
    }
}
