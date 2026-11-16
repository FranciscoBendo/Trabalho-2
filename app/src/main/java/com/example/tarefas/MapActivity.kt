package com.example.tarefas

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tarefas.databinding.ActivityMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Tela 4: mapa (osmdroid / OpenStreetMap) que gira de acordo com a direção
 * para a qual o celular está apontando.
 *
 * Sensor usado: TYPE_ROTATION_VECTOR (fusão de acelerômetro + magnetômetro + giroscópio).
 * O azimute (0° = Norte, 90° = Leste...) é obtido da matriz de rotação e o mapa é
 * girado com -azimute, de modo que a direção apontada fique sempre "para cima" na tela.
 */
class MapActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMapBinding
    private lateinit var sensorManager: SensorManager

    private var rotationSensor: Sensor? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    // Se true, o mapa gira junto com o celular; se false, fica com o norte para cima.
    private var rotateWithPhone = true

    // Azimute magnético suavizado (graus, 0..360)
    private var smoothedAzimuth = 0f
    private var hasAzimuth = false

    // Declinação magnética: diferença entre o norte magnético (bússola) e o norte geográfico (mapa)
    private var declination = 0f
    private var lastDeclinationUpdate = 0L

    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableLocation()
            } else {
                Toast.makeText(this, R.string.msg_location_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração do osmdroid: precisa vir ANTES de inflar o layout com o MapView.
        // O user agent é obrigatório, senão o servidor de tiles bloqueia o app.
        val config = Configuration.getInstance()
        val basePath = File(cacheDir, "osmdroid")
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = File(basePath, "tiles")
        config.userAgentValue = packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            Toast.makeText(this, R.string.msg_no_sensor, Toast.LENGTH_LONG).show()
            rotateWithPhone = false
            binding.btnToggleRotation.isEnabled = false
        }

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)       // zoom com dois dedos (a rotação é feita só pelo sensor)
            controller.setZoom(18.0)
            controller.setCenter(GeoPoint(-15.7939, -47.8828)) // centro padrão até o GPS responder
        }

        binding.tvHeading.setText(R.string.map_heading_waiting)
        updateToggleButton()

        binding.btnToggleRotation.setOnClickListener {
            rotateWithPhone = !rotateWithPhone
            if (!rotateWithPhone) {
                binding.mapView.setMapOrientation(0f) // volta com o norte para cima
            }
            updateToggleButton()
        }

        binding.btnRecenter.setOnClickListener {
            locationOverlay?.enableFollowLocation()
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            enableLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** Adiciona a camada de localização do usuário e faz o mapa seguir a posição. */
    private fun enableLocation() {
        if (locationOverlay != null) return

        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.mapView)
        overlay.enableMyLocation()
        overlay.enableFollowLocation()
        binding.mapView.overlays.add(overlay)
        locationOverlay = overlay
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        locationOverlay?.enableMyLocation()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        locationOverlay?.disableMyLocation()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val pitchDeg = Math.toDegrees(orientation[1].toDouble())

        // Celular deitado (tela para cima): a direção apontada é a da parte de cima do aparelho.
        // Celular em pé (como ao tirar uma foto): a direção apontada é a da câmera traseira,
        // então trocamos os eixos para o azimute não ficar instável.
        val azimuthRad: Float = if (abs(pitchDeg) > 45.0) {
            SensorManager.remapCoordinateSystem(
                rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix
            )
            SensorManager.getOrientation(remappedMatrix, orientation)
            orientation[0]
        } else {
            orientation[0]
        }

        val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0).toFloat()

        // Filtro passa-baixa circular: suaviza o tremor do sensor sem "pular" em 359° -> 0°
        if (!hasAzimuth) {
            smoothedAzimuth = azimuthDeg
            hasAzimuth = true
        } else {
            var diff = azimuthDeg - smoothedAzimuth
            while (diff > 180f) diff -= 360f
            while (diff < -180f) diff += 360f
            smoothedAzimuth = (smoothedAzimuth + SMOOTHING * diff + 360f) % 360f
        }

        updateDeclination()
        val heading = (smoothedAzimuth + declination + 360f) % 360f

        binding.tvHeading.text =
            getString(R.string.map_heading_format, heading.roundToInt() % 360, cardinal(heading))

        if (rotateWithPhone) {
            // Girar o mapa no sentido contrário faz a direção apontada ficar para cima
            binding.mapView.setMapOrientation(-heading)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não utilizado
    }

    /** Corrige norte magnético -> norte geográfico usando a última posição conhecida. */
    private fun updateDeclination() {
        val now = System.currentTimeMillis()
        if (now - lastDeclinationUpdate < DECLINATION_REFRESH_MS) return

        val location = locationOverlay?.myLocation ?: return
        declination = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            now
        ).declination
        lastDeclinationUpdate = now
    }

    private fun updateToggleButton() {
        binding.btnToggleRotation.setText(
            if (rotateWithPhone) R.string.btn_map_rotate_on else R.string.btn_map_rotate_off
        )
    }

    /** Converte graus em ponto cardeal (N, NE, L, SE, S, SO, O, NO). */
    private fun cardinal(heading: Float): String {
        val names = arrayOf("N", "NE", "L", "SE", "S", "SO", "O", "NO")
        return names[((heading + 22.5f) / 45f).toInt() % 8]
    }

    companion object {
        private const val SMOOTHING = 0.15f
        private const val DECLINATION_REFRESH_MS = 5_000L
    }
}
