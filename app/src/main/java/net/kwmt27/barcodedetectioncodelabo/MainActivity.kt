package net.kwmt27.barcodedetectioncodelabo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Processor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val TAG = "Barcode-reader"
    private val RC_HANDLE_CAMERA_PERM = 2

    private lateinit var _surfaceView: SurfaceView
    private var _cameraSource: CameraSource? = null
    private var _surfaceCreated = false


    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            // no-op
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            _cameraSource?.apply {
                stop()
                release()
            }
            _cameraSource = null
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                val hasCameraPermission = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
                    _cameraSource?.start(_surfaceView.holder)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            setupCameraSource()
        } else {
            requestCameraPermission()
        }

    }

    override fun onResume() {
        super.onResume()

        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            if (_cameraSource == null) {
                setupCameraSource()
            }
            // SurfaceViewが準備できていたらキャプチャを開始
            if (_surfaceCreated) {
                startCameraSource(_surfaceView.holder)
            }
        } else {
            requestCameraPermission()
        }

    }


    override fun onPause() {
        super.onPause()
        // キャプチャを停止
        _cameraSource?.stop()
    }

    private fun setupCameraSource(): Boolean {

        // QRコードを認識させるためのBarcodeDetectorを作成
        val barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.EAN_13)
                .build()

        // DetectorにProcessorというコールバックを設定
        barcodeDetector.setProcessor(object : Processor<Barcode> {
            override fun release() {
            }

            // バーコードを受け取ったときの処理
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val detectedItems = detections.detectedItems
                if (detectedItems.size() > 0) {
                    this@MainActivity.runOnUiThread({
                        Toast.makeText(this@MainActivity, detectedItems.valueAt(0).displayValue, Toast.LENGTH_LONG).show()
                    })
                }
            }
        })

        _surfaceView = findViewById(R.id.camera_preview);

        // CameraSourceを作成
        _cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build()


        _surfaceView.getHolder().addCallback(SurfaceCallback());


        return true
    }

    @SuppressLint("MissingPermission")
    private fun startCameraSource(holder: SurfaceHolder) {
        try {
            _cameraSource?.start(_surfaceView.holder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, permissions,
                    RC_HANDLE_CAMERA_PERM)
        }

        val layout: RelativeLayout = findViewById(R.id.topLayout)
        layout.setOnClickListener(listener)
        Snackbar.make(layout, "camera",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", listener)
                .show()
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // we have permission, so create the camerasource
            _surfaceCreated = setupCameraSource()
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multitracker sample")
                .setPositiveButton("ok", listener)
                .show()
    }


}
