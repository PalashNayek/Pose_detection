package com.palash.posedetectionusingmlkit

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.palash.posedetectionusingmlkit.databinding.ActivitySingleImageBinding
import com.palash.posedetectionusingmlkit.visionutils.PoseDetectorProcessor
import com.palash.posedetectionusingmlkit.visionutils.VisionImageProcessor

class SingleImageActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySingleImageBinding
    private var imageUri: Uri? = null
    private val REQUESTCODE_CAMERA = 1
    private val REQUESTCODE_GALLERY = 2
    private var imageProcessor: VisionImageProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_single_image)
        initClicks()
        createImageProcessor()
    }

    private fun createImageProcessor() {
        val option =
            PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()

        imageProcessor = PoseDetectorProcessor(
            context = this,
            options = option,
            showInFrameLikelihood = true,
            visualizeZ = true,
            rescaleZForVisualization = true,
            runClassification = false,
            isStreamMode = false
        )
    }

    private fun initClicks() {
        mBinding.btnCamera.setOnClickListener {
            startIntentFromCamera()
        }

        mBinding.btnGallery.setOnClickListener {
            startIntentFromGallery()
        }
    }

    private fun startIntentFromGallery() {
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, REQUESTCODE_GALLERY)
    }

    private fun startIntentFromCamera() {
        imageUri = null
        mBinding.preview.setImageBitmap(null)
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Image")
            values.put(MediaStore.Images.Media.DESCRIPTION, "For Single Mode Pose Detection API")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(pictureIntent, REQUESTCODE_CAMERA)
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUESTCODE_CAMERA && resultCode == Activity.RESULT_OK) {
            mBinding.preview.setImageURI(imageUri)
            //createImageBitmap(imageUri)
            passToImageProcessor(imageUri)
        } else if (requestCode == REQUESTCODE_GALLERY && resultCode == Activity.RESULT_OK) {
            imageUri = data?.let {
                it.data
            } ?: kotlin.run {
                null
            }
            mBinding.preview.setImageURI(imageUri)
            //createImageBitmap(imageUri)
            passToImageProcessor(imageUri)
        }
    }

    private fun passToImageProcessor(imageUri: Uri?) {
        imageUri?.let {
            try {
                val imageBitmap = BitmapUtils.getBitmapFromContentUri(contentResolver, it)
                mBinding.graphicOverlay.clear()
                mBinding.preview.setImageURI(it)

                if (imageProcessor != null) {
                    mBinding.graphicOverlay.setImageSourceInfo(
                        imageBitmap?.width ?: 0,
                        imageBitmap?.height ?: 0,
                        false
                    )

                    imageProcessor?.processBitmap(imageBitmap, mBinding.graphicOverlay)
                }else{
                    Log.e("TAG", "Image Processor is null")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createImageBitmap(imageUri: Uri?) {
        imageUri?.let {
            val bitmap = BitmapUtils.getBitmapFromContentUri(
                contentResolver,
                it
            )//BitmapUtils.getBitmapContentUri(contentResolver, it)
            bitmap?.let { bitmap ->
                val imageObject = InputImage.fromBitmap(bitmap, 0)
                getPoseDetection().process(imageObject).addOnSuccessListener { pose ->
                    val builder = java.lang.StringBuilder()
                    Log.e("Landmark", pose.getPoseLandmark(PoseLandmark.NOSE)!!.position.toString())
                    pose.allPoseLandmarks.forEach { poseLandMark ->
                        builder.appendLine("Landmark type: " + poseLandMark.landmarkType + "Landmark Position: " + poseLandMark.position)
                    }
                    Log.e("TAG", builder.toString())

                }.addOnFailureListener { error ->
                    Log.e("TAG", error.message.toString())
                }
            }
        }
    }

    private fun getPoseDetection(): PoseDetector {
        /*val option =
            PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()*/

        val option = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE).build()

        return PoseDetection.getClient(option)
    }
}