package mx.edu.utez.cafeteria.ui.sensores

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import mx.edu.utez.cafeteria.config.Configuracion
import kotlin.math.sqrt

/**
 * Escucha el acelerómetro mientras la pantalla que lo usa está visible
 * y llama a [onAgitar] cuando el teléfono se agita con fuerza.
 */
@Composable
fun DetectorAgitar(onAgitar: () -> Unit) {
    val context = LocalContext.current
    val accionActual by rememberUpdatedState(onAgitar)

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var ultimaAgitada = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val v = event?.values ?: return
                // Cada eje en unidades G (en reposo la fuerza total vale 1)
                val gX = v[0] / SensorManager.GRAVITY_EARTH
                val gY = v[1] / SensorManager.GRAVITY_EARTH
                val gZ = v[2] / SensorManager.GRAVITY_EARTH
                val fuerza = sqrt(gX * gX + gY * gY + gZ * gZ)

                if (fuerza > Configuracion.UMBRAL_AGITAR) {
                    val ahora = System.currentTimeMillis()
                    if (ahora - ultimaAgitada > Configuracion.ESPERA_ENTRE_AGITADAS_MS) {
                        ultimaAgitada = ahora
                        accionActual()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // Algunos equipos no tienen acelerómetro: en ese caso no se registra nada
        acelerometro?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        // Al salir de la pantalla se libera el sensor (si no, sigue gastando batería)
        onDispose { sensorManager.unregisterListener(listener) }
    }
}
