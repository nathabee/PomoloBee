package de.nathabee.pomolobee.utils

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc




fun detectApple(image: Mat): Mat {
    val gray = Mat()
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGBA2GRAY)
    Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
    val edges = Mat()
    Imgproc.Canny(gray, edges, 50.0, 150.0)
    return edges
}
