package br.com.controlplus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.print.PrintManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.webkit.WebViewAssetLoader

/**
 * Control+ para Android.
 * Abre o aplicativo (Google Apps Script) em tela cheia, a partir de uma página local que não mostra a faixa do Google.
 * - links externos (WhatsApp, comprovantes no Drive) abrem no app certo do celular;
 * - "Imprimir / Salvar PDF" usa a impressão do Android;
 * - "Anexar foto ou PDF" abre a galeria/arquivos.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private var escolhaArquivo: ValueCallback<Array<Uri>>? = null
    private val pedidoArquivo = 1001
    private var ultimoVoltar = 0L

    private val carregador by lazy {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            userAgentString = "$userAgentString ControlPlusApp"
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        // Ponte para impressão (disponível para a página do app)
        web.addJavascriptInterface(Ponte(), "ControlPlus")

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                carregador.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (!request.isForMainFrame) return false
                return if (url.host == "appassets.androidplatform.net") false else { abrirFora(url); true }
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            // Links com target="_blank" / window.open: abre fora (WhatsApp, Drive...)
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val temporario = WebView(this@MainActivity)
                temporario.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                        abrirFora(r.url); v.destroy(); return true
                    }
                }
                (resultMsg.obj as WebView.WebViewTransport).webView = temporario
                resultMsg.sendToTarget()
                return true
            }

            // Campo "Anexar foto ou PDF"
            override fun onShowFileChooser(view: WebView, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                escolhaArquivo?.onReceiveValue(null)
                escolhaArquivo = callback
                return try {
                    startActivityForResult(params.createIntent(), pedidoArquivo); true
                } catch (e: ActivityNotFoundException) {
                    escolhaArquivo = null; false
                }
            }
        }

        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    private fun abrirFora(url: Uri) {
        try { startActivity(Intent(Intent.ACTION_VIEW, url)) }
        catch (e: Exception) { Toast.makeText(this, "Não foi possível abrir o link.", Toast.LENGTH_SHORT).show() }
    }

    inner class Ponte {
        @JavascriptInterface
        fun imprimir() {
            runOnUiThread {
                val pm = getSystemService(PRINT_SERVICE) as PrintManager
                pm.print("Control+", web.createPrintDocumentAdapter("Control+"), null)
            }
        }
    }

    @Deprecated("Usado para receber o arquivo escolhido")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == pedidoArquivo) {
            escolhaArquivo?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            escolhaArquivo = null
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    @Deprecated("Botão voltar: toque duas vezes para sair")
    override fun onBackPressed() {
        val agora = System.currentTimeMillis()
        if (agora - ultimoVoltar < 2000) { super.onBackPressed() }
        else { ultimoVoltar = agora; Toast.makeText(this, "Toque de novo para sair", Toast.LENGTH_SHORT).show() }
    }

    override fun onPause() { super.onPause(); CookieManager.getInstance().flush() }
}
