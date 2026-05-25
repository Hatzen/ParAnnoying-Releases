package de.hartz.software.parannoying.offline.helper.guard.facedetection

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import de.hartz.software.parannoying.core.helper.ui.UiHelper

// https://github.com/asmaamirkhan/MLKitDemo/blob/master/app/src/main/java/com/asmaamir/mlkitdemo/FaceTracking/FaceTrackingAnalyzer.java
class FaceTrackingAnalyzer internal constructor(val callback: Runnable, val context: Context) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "MLKitFacesAnalyzer"
    }

    private var fbImage: FirebaseVisionImage? = null

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        fbImage = FirebaseVisionImage.fromMediaImage(image.image!!, FirebaseVisionImageMetadata.ROTATION_0) // TODO: Do we need to calculate this?
        initDetector()
    }

    private fun initDetector() {
        val detectorOptions = FirebaseVisionFaceDetectorOptions.Builder()
                .enableTracking()
                .build()
        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(detectorOptions)
        faceDetector.detectInImage(fbImage!!).addOnSuccessListener { firebaseVisionFaces: List<FirebaseVisionFace> ->
            if (!firebaseVisionFaces.isEmpty()) {
                processFaces(firebaseVisionFaces)
            }
        }.addOnFailureListener { e: Exception -> Log.i(TAG, e.toString()) }
    }

    private fun processFaces(faces: List<FirebaseVisionFace>) {
        Log.i(TAG, "Number of faces: " + faces.size)
        if (faces.size > 1) {
            callback.run()
            UiHelper.showToastFromBackgroundTask(context, "" + faces.size + " Faces detected. Screensaver activated.")
        }
    }

}