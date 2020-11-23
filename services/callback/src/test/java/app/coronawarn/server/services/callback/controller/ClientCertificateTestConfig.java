package app.coronawarn.server.services.callback.controller;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class ClientCertificateTestConfig {

  private static final String CLIENT_CERTIFICATE = "src/test/resources/efgs.p12";
  private static final String CERTIFICATE_PWD = "123456";
  private static final String TLS = "TLS";
  private static final String SUN_X509 = "SunX509";
  private static final String PKCS12 = "PKCS12";

  @Bean
  public RestTemplateBuilder myRestTemplateBuilder() {
    final RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder() {
      @Override
      public ClientHttpRequestFactory buildRequestFactory() {
        try {
          KeyStore ks = KeyStore.getInstance(PKCS12);
          ks.load(new FileInputStream(CLIENT_CERTIFICATE), CERTIFICATE_PWD.toCharArray());
          KeyManagerFactory kmf = KeyManagerFactory.getInstance(SUN_X509);
          kmf.init(ks, CERTIFICATE_PWD.toCharArray());
          SSLContext sc = SSLContext.getInstance(TLS);
          sc.init(kmf.getKeyManagers(), null, null);
          HttpClient httpClient = HttpClients.custom().setSSLContext(sc).build();
          ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
          System.out.println("Loading client certificate for requests from " + CLIENT_CERTIFICATE);
          return requestFactory;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    return restTemplateBuilder.rootUri("https://localhost:7777").detectRequestFactory(true);
  }
}
