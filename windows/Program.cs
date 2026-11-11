using System.Diagnostics;
using System.Reflection;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace ControlPlus;

// Control+ para Windows: abre o aplicativo em janela própria (WebView2 / Edge), sem barra de navegador
// e sem a faixa do Google. Links externos (WhatsApp, Drive) abrem no navegador padrão.
static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new Janela());
    }
}

class Janela : Form
{
    readonly WebView2 web = new WebView2 { Dock = DockStyle.Fill, DefaultBackgroundColor = System.Drawing.Color.FromArgb(31, 78, 120) };

    public Janela()
    {
        Text = "Control+";
        using (var s = Assembly.GetExecutingAssembly().GetManifestResourceStream("ControlPlus.app.ico"))
            if (s != null) Icon = new Icon(s);
        StartPosition = FormStartPosition.CenterScreen;
        Size = new Size(1280, 820);
        MinimumSize = new Size(420, 600);
        WindowState = FormWindowState.Maximized;
        Controls.Add(web);
        Load += async (_, _) => await Iniciar();
    }

    async Task Iniciar()
    {
        string pasta = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ControlPlus");
        string site = Path.Combine(pasta, "site");
        Directory.CreateDirectory(site);
        using (var s = Assembly.GetExecutingAssembly().GetManifestResourceStream("ControlPlus.index.html"))
        using (var r = new StreamReader(s))
            File.WriteAllText(Path.Combine(site, "index.html"), r.ReadToEnd());

        try
        {
            var env = await CoreWebView2Environment.CreateAsync(null, Path.Combine(pasta, "dados"));
            await web.EnsureCoreWebView2Async(env);
        }
        catch (WebView2RuntimeNotFoundException)
        {
            MessageBox.Show("Para abrir o Control+ é preciso o \"Microsoft Edge WebView2\" (gratuito, da Microsoft).\n\nA página de download vai abrir agora. Depois de instalar, abra o Control+ de novo.",
                "Control+", MessageBoxButtons.OK, MessageBoxIcon.Information);
            AbrirFora("https://go.microsoft.com/fwlink/p/?LinkId=2124703");
            Close();
            return;
        }

        var cw = web.CoreWebView2;
        cw.Settings.UserAgent += " ControlPlusApp";
        cw.Settings.AreDevToolsEnabled = false;
        cw.Settings.IsStatusBarEnabled = false;
        cw.Settings.AreBrowserAcceleratorKeysEnabled = true; // Ctrl+P, Ctrl+F etc.
        cw.SetVirtualHostNameToFolderMapping("app.controlplus", site, CoreWebView2HostResourceAccessKind.Allow);

        // Links que pedem nova janela (WhatsApp, comprovantes): abre no navegador padrão
        cw.NewWindowRequested += (_, e) =>
        {
            e.Handled = true;
            if (e.Uri.StartsWith("http", StringComparison.OrdinalIgnoreCase)) AbrirFora(e.Uri);
        };

        web.Source = new Uri("https://app.controlplus/index.html");
    }

    static void AbrirFora(string url)
    {
        try { Process.Start(new ProcessStartInfo(url) { UseShellExecute = true }); } catch { }
    }
}
