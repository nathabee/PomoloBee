package de.nathabee.pomolobee.ui.components

import android.content.Context
import android.view.SurfaceHolder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import de.nathabee.pomolobee.utils.detectApple // ✅ Import image processing function
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.core.Mat

@Composable
fun CameraView(context: Context, modifier: Modifier = Modifier) {
    val cameraView = remember { CameraView(context) }

    AndroidView(
        factory = { cameraView },
        modifier = modifier.fillMaxSize()
    )
}

class CameraView(context: Context) : JavaCameraView(context, CAMERA_ID_BACK), CameraBridgeViewBase.CvCameraViewListener2 {

    init {
        setCvCameraViewListener(this)
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val processedFrame = detectApple(inputFrame.rgba()) // ✅ Apply processing
        return processedFrame
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        enableView()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}
    override fun onCameraViewStopped() {}

}
