package com.apexgarage.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val chooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileCallback?.onReceiveValue(uris)
        fileCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)

        // Plein écran : on réserve nous-mêmes la place des barres système et du clavier.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        updateBarIcons()

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            setSupportZoom(false)
        }
        web.addJavascriptInterface(Bridge(), "AndroidBridge")

        // Sert les fichiers du dossier assets sous une adresse https locale (stockage des données stable).
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.host == "appassets.androidplatform.net") return false
                startActivity(Intent(Intent.ACTION_VIEW, request.url))
                return true
            }
        }

        // Sélecteur de fichier pour « Importer une sauvegarde ».
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: WebChromeClient.FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    chooser.launch(params.createIntent())
                    true
                } catch (e: Exception) {
                    fileCallback = null
                    false
                }
            }
        }

        // Bouton retour : ferme d'abord la fenêtre ouverte, puis revient au Cockpit, puis quitte.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("(window.__back&&window.__back())?'1':'0'") { r ->
                    if (r != "\"1\"") {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })

        if (savedInstanceState == null) {
            web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        } else {
            web.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateBarIcons()
    }

    private fun updateBarIcons() {
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = !night
        controller.isAppearanceLightNavigationBars = !night
    }

    /** Appelé depuis la page pour enregistrer la sauvegarde JSON dans Téléchargements. */
    inner class Bridge {
        @JavascriptInterface
        fun saveFile(name: String, content: String): String {
            return try {
                val safe = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
                if (Build.VERSION.SDK_INT >= 29) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safe)
                        put(MediaStore.Downloads.MIME_TYPE, "application/json")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: return ""
                    contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                        ?: return ""
                    "Téléchargements/$safe"
                } else {
                    val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return ""
                    File(dir, safe).writeText(content, Charsets.UTF_8)
                    "Android/data/$packageName/files/Download/$safe"
                }
            } catch (e: Exception) {
                ""
            }
        }
    }
}
