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
package com.emuneee.tensorandflow.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by evan on 2/28/18.
 * Based on https://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
 */

class CanvasView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val path: Path = Path()
    private val paint: Paint = Paint()
    private val bitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
    private val circlePaint: Paint = Paint()
    private val circlePath: Path = Path()
    var drawListener: DrawListener? = null

    private var x1: Float = 0.toFloat()
    private var y1: Float = 0.toFloat()

    init {
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeJoin = Paint.Join.MITER
        circlePaint.strokeWidth = 4f

        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 55f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, bitmapPaint)
        canvas.drawPath(path, paint)
        canvas.drawPath(circlePath, circlePaint)
    }

    private fun fingerDown(x: Float, y: Float) {
        canvas!!.drawColor(Color.WHITE)
        path.reset()
        path.moveTo(x, y)
        x1 = x
        y1 = y
    }

    private fun fingerMove(x: Float, y: Float) {
        val dx = Math.abs(x - x1)
        val dy = Math.abs(y - y1)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(x1, y1, (x + x1) / 2, (y + y1) / 2)
            x1 = x
            y1 = y

            circlePath.reset()
            circlePath.addCircle(x1, y1, 30f, Path.Direction.CW)
        }
    }

    private fun fingerUp() {
        path.lineTo(x1, y1)
        circlePath.reset()
        canvas!!.drawPath(path, paint)
        path.reset()
        drawListener?.onNewBitmap(bitmap!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                fingerDown(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                fingerMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                fingerUp()
                invalidate()
            }
        }
        return true
    }

    interface DrawListener {
        fun onNewBitmap(bitmap: Bitmap)
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}
