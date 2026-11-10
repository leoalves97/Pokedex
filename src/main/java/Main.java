import ui.PokedexFrame;
import javax.swing.SwingUtilities;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class Main {
    public static void main(String[] args) {
        // Desativa a verificação de hostname do HttpClient moderno
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        
        // Desativa a validação chata de SSL que estava causando o erro
        ignorarCertificadosSSL();

        // Inicia a interface gráfica
        SwingUtilities.invokeLater(() -> {
            PokedexFrame frame = new PokedexFrame();
            frame.setVisible(true);
        });
    }

    private static void ignorarCertificadosSSL() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Aplica a liberação para o HttpClient (que busca os dados)
            SSLContext.setDefault(sc); 
            // Aplica a liberação para o ImageIO (que faz o download da imagem)
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}