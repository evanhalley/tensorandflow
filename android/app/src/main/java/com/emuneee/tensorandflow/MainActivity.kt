/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emuneee.tensorandflow

import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.Bitmap
import com.emuneee.tensorandflow.classifier.Classifier
import com.emuneee.tensorandflow.classifier.TFMobileClassifier
import com.emuneee.tensorandflow.view.CanvasView
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val classifier: Classifier by lazy {
        TFMobileClassifier(this,
                modelFilename = "file:///android_asset/optimized_graph.pb",
                inputName = "input",
                inputDimensions = Pair(28, 28),
                outputName = "output",
                outputSize = 100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        canvas.drawListener = object: CanvasView.DrawListener {
            override fun onNewBitmap(bitmap: Bitmap) {

                Thread(Runnable {

                    // convert the drawing to a 28x28 monochrome image
                    val monochrome = toMonochrome(bitmap)

                    // set the nn input image
                    runOnUiThread { scaledCanvas.setImageBitmap(monochrome) }

                    // convert the data to something that resembles the MNIST training data set
                    val inputData = toIntArray(monochrome)

                    // predict
                    val pred = classifier.predict(inputData)
                    runOnUiThread { prediction.text = pred.toString() }

                }).start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }

    /**
     * Converts a Bitmap to a 28 x 28 monochrome bitmap
     */
    private fun toMonochrome(bitmap: Bitmap): Bitmap {
        // scale bitmap to 28 by 28
        val scaled = Bitmap.createScaledBitmap(bitmap, 28, 28, false)

        // convert bitmap to monochrome
        val monochrome = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(monochrome)
        val ma = ColorMatrix()
        ma.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(ma)
        canvas.drawBitmap(scaled, 0f, 0f, paint)

        val width = monochrome.width
        val height = monochrome.height

        val pixels = IntArray(width * height)
        monochrome.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {

            for (x in 0 until width) {
                val pixel = monochrome.getPixel(x, y)
                val lowestBit = pixel and 0xff

                if (lowestBit < 128) {
                    monochrome.setPixel(x, y, Color.BLACK)
                }
                else {
                    monochrome.setPixel(x, y, Color.WHITE)
                }
            }
        }
        return monochrome
    }

    /**
     * Converts a bitmap to a flattened integer array
     */
    private fun toIntArray(bitmap: Bitmap): IntArray {
        val pixels = IntArray(bitmap.width * bitmap.height)
        var i = 0

        for (y in 0 until bitmap.height) {

            for (x in 0 until bitmap.width) {
                pixels[i++] = if (bitmap.getPixel(x, y) == Color.BLACK) 255 else 0
            }
        }
        return pixels
    }
}
