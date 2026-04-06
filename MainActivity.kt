package com.moisnostudio.jerutiapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moisnostudio.jerutiapp.model.CategoriaConCanales
import com.moisnostudio.jerutiapp.model.Sample
import com.moisnostudio.jerutiapp.network.RetrofitClient
import com.moisnostudio.jerutiapp.ui.MainVerticalAdapter
import kotlinx.coroutines.launch
import androidx.media3.exoplayer.DefaultLoadControl

data class ServidorIPTV(val nombre: String, val url: String)

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var rvCanales: RecyclerView
    private lateinit var layoutError: LinearLayout
    private lateinit var txtMensajeError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutInfoCanal: LinearLayout
    private lateinit var txtNombreCanalInfo: TextView
    private lateinit var imgLogoCanalInfo: ImageView
    private lateinit var imgFondoInicio: ImageView


    private val listaServidores = listOf(
        ServidorIPTV("Servidor GitHub (Punto Play)", "https://raw.githubusercontent.com/elvioladordemark/cijefcji/refs/heads/main/prueba6.json"),
        ServidorIPTV("Servidor Vimetrix (M+ Plus)", "https://vimetrix.lat/prueba6.json"),
        ServidorIPTV("Servidor Jeruti Oficial", "https://raw.githubusercontent.com/cheroga/cheroga.github.io/refs/heads/master/jeruti.json")
    )

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vincularVistas()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setupRecyclerView()
        setupPlayer()
        mostrarSelectorDeServidor()
        rvCanales.setItemViewCacheSize(50)

    }

    private fun vincularVistas() {
        imgFondoInicio = findViewById(R.id.imgFondoInicio) // Agregado
        layoutInfoCanal = findViewById(R.id.layoutInfoCanal)
        txtNombreCanalInfo = findViewById(R.id.txtNombreCanalInfo)
        imgLogoCanalInfo = findViewById(R.id.imgLogoCanalInfo)
        playerView = findViewById(R.id.playerView)
        rvCanales = findViewById(R.id.rvCanales)
        progressBar = findViewById(R.id.progressBar)
        layoutError = findViewById(R.id.layoutError)
        txtMensajeError = findViewById(R.id.txtMensajeError)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        layoutManager.initialPrefetchItemCount = 4
        rvCanales.layoutManager = layoutManager
        rvCanales.setItemViewCacheSize(20)
        rvCanales.setHasFixedSize(true)
    }

    private fun mostrarSelectorDeServidor() {
        val nombres = listaServidores.map { it.nombre }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Seleccione un Proveedor")
            .setCancelable(false)
            .setItems(nombres) { _, posicion ->
                cargarCanales(listaServidores[posicion].url)
            }
            .setNegativeButton("Cerrar App") { _, _ -> finish() }
            .show()
    }

    private fun cargarCanales(urlFinal: String) {
        // 1. Limpieza y preparación visual
        rvCanales.adapter = null
        progressBar.visibility = View.VISIBLE

        // Aseguramos que el fondo sea visible mientras carga el nuevo servidor
        imgFondoInicio.visibility = View.VISIBLE
        imgFondoInicio.alpha = 1f

        lifecycleScope.launch {
            try {
                // 2. Descarga del JSON
                val responseList = RetrofitClient.instance.getIptvData(urlFinal)

                if (responseList.isNotEmpty()) {
                    // 3. Transformación de datos para el Adapter
                    val filasParaLaUI = responseList.map { servidorData ->
                        CategoriaConCanales(
                            titulo = servidorData.name,
                            canales = servidorData.samples
                        )
                    }

                    // 4. Asignación del adaptador
                    rvCanales.adapter = MainVerticalAdapter(filasParaLaUI) { canal ->
                        reproducirCanal(canal)
                    }

                    // 5. ANIMACIÓN PROFESIONAL: El fondo se desvanece al cargar los canales
                    imgFondoInicio.animate()
                        .alpha(0f)
                        .setDuration(800)
                        .withEndAction {
                            imgFondoInicio.visibility = View.GONE
                        }
                        .start()

                    progressBar.visibility = View.GONE
                    rvCanales.requestFocus()

                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "El servidor está vacío", Toast.LENGTH_SHORT).show()
                    mostrarSelectorDeServidor()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                android.util.Log.e("IPTV_ERROR", "Error: ${e.message}")
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()

                // Si falla, volvemos al selector para que intente con otro
                mostrarSelectorDeServidor()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        // 1. CONFIGURACIÓN DE BUFFER RESISTENTE (MEJORA AÑADIDA)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                25_000, // Min buffer: 25 seg antes de dar error
                60_000, // Max buffer: 60 seg de precarga
                3_000,  // Buffer para iniciar: 3 seg
                5_000   // Buffer para reanudar
            )
            .setBackBuffer(10_000, true)
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()

        playerView.player = player
        playerView.useController = true

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                progressBar.visibility = View.GONE
                layoutError.visibility = View.VISIBLE

                txtMensajeError.text = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Conexión inestable, reintentando..."
                    PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "Error de Licencia DRM"
                    else -> "Buscando señal..."
                }

                // 2. MEJORA: REINTENTO AUTOMÁTICO EN VEZ DE CERRAR
                playerView.postDelayed({
                    if (layoutError.visibility == View.VISIBLE) {
                        player.prepare()
                        player.play()
                        // No llamamos a onBackPressed() para no salir del canal
                    }
                }, 4000) // Esperamos 4 segundos para que el internet respire
            }

            override fun onPlaybackStateChanged(state: Int) {
                progressBar.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                if (state == Player.STATE_READY) {
                    layoutError.visibility = View.GONE
                }
            }
        })
    }

    private fun reproducirCanal(canal: Sample) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        layoutError.visibility = View.GONE

        // Mantenemos tu lógica de foco para Android TV
        rvCanales.isFocusable = false
        rvCanales.isClickable = false
        rvCanales.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS

        playerView.visibility = View.VISIBLE
        playerView.isFocusable = true
        playerView.isFocusableInTouchMode = true
        playerView.requestFocus()

        txtNombreCanalInfo.text = canal.name
        Glide.with(this)
            .load(canal.icono)
            .placeholder(R.mipmap.ic_launcher)
            .into(imgLogoCanalInfo)

        lanzarBanner()

        // 3. CONFIGURACIÓN DE MEDIA CON TU LÓGICA DE DRM
        val mediaItem = MediaItem.Builder()
            .setUri(canal.url)
            .setDrmConfiguration(
                if (canal.type?.uppercase() == "CLEARKEY") {
                    MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                        .setLicenseUri(canal.drm_license_uri)
                        .build()
                } else null
            ).build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        playerView.post { playerView.requestFocus() }
    }


    @OptIn(UnstableApi::class)
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        // Si el reproductor es visible, deja que el PlayerView maneje el evento primero
        if (playerView.visibility == View.VISIBLE) {
            return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }


    private fun lanzarBanner() {
        layoutInfoCanal.visibility = View.VISIBLE
        layoutInfoCanal.alpha = 0f
        layoutInfoCanal.translationY = 100f
        layoutInfoCanal.animate().alpha(1f).translationY(0f).setDuration(500).start()
        layoutInfoCanal.postDelayed({
            layoutInfoCanal.animate().alpha(0f).translationY(100f).setDuration(500).withEndAction {
                layoutInfoCanal.visibility = View.GONE
            }.start()
        }, 5000)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (playerView.visibility == View.VISIBLE) {
            // 1. Limpieza de Sistema y UI
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            layoutInfoCanal.visibility = View.GONE

            // 2. Detener y ocultar el player
            player.stop()
            playerView.visibility = View.GONE

            // 3. RECUPERAR EL FOCO (Optimizado para Android TV)
            rvCanales.postDelayed({
                // Habilitamos de nuevo la interactividad de la lista
// Dentro de onBackPressed, cuando cierras el video:
                rvCanales.isFocusable = true
                rvCanales.isClickable = true
                rvCanales.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                rvCanales.requestFocus()
            }, 150) // Subimos ligeramente a 150ms para mayor estabilidad en TV Box

        } else {
            // 4. Regreso al Selector de Servidores
            imgFondoInicio.visibility = View.VISIBLE
            imgFondoInicio.animate().alpha(1f).setDuration(300).start()
            mostrarSelectorDeServidor()
        }
    }
}
