package com.syhdzn.amigos_gemastik.process

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.amigos_gemastik.R
import com.syhdzn.amigos_gemastik.dashboard.DashboardActivity
import com.syhdzn.amigos_gemastik.databinding.ActivityProcessBinding
import com.syhdzn.amigos_gemastik.result.ResultActivity
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ProcessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessBinding
    private lateinit var interpreter: Interpreter
    private var imageFile: File? = null

    companion object {
        private const val img_width = 150
        private const val img_height = 150
        private const val NUM_CLASSES = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        interpreter = Interpreter(loadModelFile())

        handleImage()
        setupAction()
    }

    private fun handleImage() {
        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val pictureFile = intent.getSerializableExtra("picture") as? File

        imageFile = when {
            imageUriString != null -> {
                val imageUri = Uri.parse(imageUriString)
                handleGalleryImage(imageUri)
                null
            }
            isBackCamera && pictureFile != null -> {
                val rotatedBitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }
    }

    private fun handleGalleryImage(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
    }

    private fun setupAction() {
        binding.btnProcessImage.setOnClickListener {
            handleImageProses()
        }
        binding.ivBgReplace.setOnClickListener {
            showDialogReplace()
        }
    }

    private fun handleImageProses() {
        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val pictureFile = intent.getSerializableExtra("picture") as? File

        imageFile = when {
            imageUriString != null -> {
                val imageUri = Uri.parse(imageUriString)
                handleGalleryImageProses(imageUri)
                null
            }
            isBackCamera && pictureFile != null -> {
                val rotatedBitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                processImage(rotatedBitmap)
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                processImage(bitmap)
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }
    }

    private fun handleGalleryImageProses(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        processImage(bitmap)
    }


    private fun processImage(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, img_width, img_height, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputScores = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter.run(byteBuffer, outputScores)
        val result = outputScores[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val label = when (maxIndex) {
            0 -> "Sampah Anorganik"
            1 -> "Sampah B3"
            2 -> "Sampah Organik"
            else -> "Unknown"
        }

        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val imageFile = intent.getSerializableExtra("picture") as? File

        setupLoading()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("label", label)
                imageUriString?.let { putExtra("imageUri", it) }
                if (isBackCamera && imageFile != null) {
                    val imageUri = FileProvider.getUriForFile(this@ProcessActivity, "$packageName.fileprovider", imageFile)
                    putExtra("imageUri", imageUri.toString())
                }
            }
            startActivity(intent)
            hideLoading()
        }, 1000)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * img_width * img_height * 3).apply {
            order(ByteOrder.nativeOrder())
        }
        val pixels = IntArray(img_width * img_height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        pixels.forEach { pixel ->
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    private fun loadModelFile(): ByteBuffer {
        return assets.openFd("sampah_detection_model.tflite").run {
            FileInputStream(fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }
    }


    private fun showDialogReplace() {
        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_replace, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                putExtra("switchToFragment", "DetectionFragment")
                putExtra("selectMenuItem", R.id.cam)
            })
            dialog.dismiss()
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupLoading() {
        SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Loading"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Loading"
            setCancelable(false)
            hide()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, DashboardActivity::class.java))
        super.onBackPressed()
    }
}