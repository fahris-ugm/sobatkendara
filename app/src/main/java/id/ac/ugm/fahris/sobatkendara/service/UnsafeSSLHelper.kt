package id.ac.ugm.fahris.sobatkendara.service

import java.security.cert.X509Certificate
import javax.net.ssl.*

object UnsafeSSLHelper {

    // Create a trust manager that does not validate certificate chains
    fun getTrustAllCertsManager(): Array<TrustManager> {
        return arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
    }

    // Create an SSL context that uses the trust manager to trust all certificates
    fun getUnsafeSSLContext(): SSLContext {
        val trustAllCerts = getTrustAllCertsManager()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        return sslContext
    }

    // Get an SSLSocketFactory that trusts all certificates
    fun getUnsafeSSLSocketFactory(): SSLSocketFactory {
        return getUnsafeSSLContext().socketFactory
    }

    // Get an HostnameVerifier that does not verify hostname
    fun getUnsafeHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }
}
