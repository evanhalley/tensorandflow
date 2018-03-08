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
package com.emuneee.tensorandflow.classifier

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.util.*
import kotlin.Comparator


/**
 * Created by evan on 2/28/18.
 */
class TFMobileClassifier(context: Context,
                         modelFilename: String,
                         private val inputName: String,
                         private val inputDimensions: Pair<Long, Long>,
                         private val outputName: String,
                         private val outputSize: Int) : Classifier {

    private val assetManager: AssetManager = context.assets
    private val inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

    override fun predict(input: IntArray): Int {
        val floatInput = input.map { it.toFloat() }
                .toFloatArray()
        // 1) create an array to store our predictions
        val predictions = LongArray(outputSize)

        // 2) feed our data into input layer of our neural network
        inferenceInterface.feed(inputName, floatInput, 1, inputDimensions.first, inputDimensions.second, 1)

        // 3) run inference between the input and specified output nodes
        inferenceInterface.run(arrayOf(outputName))

        // 4) fetch the predictions from the specified output node
        inferenceInterface.fetch(outputName, predictions)

        // 5) tabulate our predictions and return the most probable
        return processPredictions(predictions)
    }

    private fun processPredictions(predictions: LongArray): Int {
        val counts = predictions.toTypedArray()
                .groupingBy { it }
                .eachCount()
        val predictionSet = TreeSet<Pair<Long, Int>>(Comparator<Pair<Long, Int>> { o1, o2 -> o2!!.second.compareTo(o1!!.second) })
        counts.toList()
                .forEach { pair -> predictionSet.add(pair) }
        val pair = predictionSet.first()
        Timber.d("Selecting ${pair.first} @ ${(pair.second / 100.0) * 100}% confidence")
        return pair.first.toInt()
    }

    override fun close() {
        inferenceInterface.close()
    }
}
