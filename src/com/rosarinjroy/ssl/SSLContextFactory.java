package com.rosarinjroy.ssl;

/**
 * Copyright (c) 2013, Rosarin Roy (roarinjroy at hotmail dot com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public class SSLContextFactory {
    private final Logger logger = Logger.getLogger(SSLContextFactory.class.getName());

    private String securityProvider;
    private String keyStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String protocol = "TLS";
    private boolean performStrictNameMatching = true;
    private SSLContext sslContext = null;

    @PostConstruct
    public void init() {
        if(keyStorePath == null || keyStorePassword == null) {
            throw new RuntimeException("Expected both the keyStorePath and the keyStorePassword to be set.");
        }

        if(keyStoreType == null) {
            keyStoreType = guessKeyStoreType(keyStorePath);
        }

        createSSLContext();
    }

    private String guessKeyStoreType(String keyStorePath) {
        keyStorePath = keyStorePath.toLowerCase().trim();
        if(keyStorePath.endsWith("jks")) {
            return "JKS";
        } else if (keyStorePath.endsWith("p12") || keyStorePath.endsWith("pkcs12")){
            return "PKCS12";
        } else {
            return "PKCS12";
        }
    }

    private void createSSLContext() {
        String normalizedKeyStorePath = (new File(keyStorePath)).getAbsolutePath();
        logger.info("Attempting to load the keystore from [" + normalizedKeyStorePath + "] (key store type [" + keyStoreType + "]).");
        KeyStore keyStore;

        try (FileInputStream fis = new FileInputStream(normalizedKeyStorePath)) {
            if (securityProvider == null) {
                keyStore = KeyStore.getInstance(keyStoreType);
            } else {
                keyStore = KeyStore.getInstance(keyStoreType, securityProvider);
            }
            keyStore.load(fis, keyStorePassword.toCharArray());

        } catch (Exception e) {
            throw new RuntimeException("Cannot load key store from [" + normalizedKeyStorePath + "] (type [" + keyStoreType + "], provider ["
                    + (securityProvider == null ? "DEFAULT" : securityProvider) + "])", e);
        }

        KeyManagerFactory keyManagerFactory;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            sslContext = SSLContext.getInstance(getProtocol());
            TrustManager[] trustManager;

            if (performStrictNameMatching) {
                throw new RuntimeException(
                        "Strict name matching is not supported yet. To continue to use, please disable strict name matching and retry.");
            } else {
                trustManager = new TrustManager[] { new UnsafeAllTrustingTrustManager() };
            }
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManager, null);
            logger.info("SSLContext successfully initialized (provider [" + sslContext.getProvider().toString() + "]).");
        } catch (Exception e) {
            throw new RuntimeException("Creating KeyManagerFactory failed for key store [" + normalizedKeyStorePath + "] (type [" + keyStoreType
                    + "], provider [" + (securityProvider == null ? "DEFAULT" : securityProvider) + "])", e);
        }
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
        this.securityProvider = securityProvider;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        if(protocol != null) {
            this.protocol = protocol;
        }
    }

    public boolean isPerformStrictNameMatching() {
        return performStrictNameMatching;
    }

    public void setPerformStrictNameMatching(boolean performStrictNameMatching) {
        this.performStrictNameMatching = performStrictNameMatching;
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SSLContextFactory [keyStoreType=");
        builder.append(keyStoreType);
        builder.append(", keyStorePath=");
        builder.append(keyStorePath);
        builder.append(", keyStorePassword=");
        builder.append(keyStorePassword != null ? "****" : "null");
        builder.append(", securityProvider=");
        builder.append(securityProvider);
        builder.append(", protocol=");
        builder.append(protocol);
        builder.append(", performStrictNameMatching=");
        builder.append(performStrictNameMatching);
        builder.append("]");
        return builder.toString();
    }

    /**
     * WARNING: Use this trust manager at your own risk. This doesn't perform any check on the certificate presented by the server.
     * You should be able to confirm which servers it is trusting by looking at the logs. You will see a log message like this:
     * Trusting server with SubjectDN: [CN=blahblah.com, OU=blahgroup, O="Blah, Inc.", L=Sunnyvale, ST=California, C=US] (auth type [DHE_RSA]).
     **/
    public static class UnsafeAllTrustingTrustManager implements X509TrustManager {
        private final Logger logger = Logger.getLogger(UnsafeAllTrustingTrustManager.class.getName());

        @Override
        public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {
            // This method is relevant only when we are operating in server mode.
            logger.info("Trusting client with SubjectDN: [" + cert[0].getSubjectDN().getName() + "] (auth type [" + authType + "]).");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] cert, String authType) throws CertificateException {
            X500Principal subjectDN = cert[0].getSubjectX500Principal();
            logger.info("Trusting server with SubjectDN: [" + subjectDN.getName() + "] (auth type [" + authType + "]).");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // This method is relevant only when we are operating in server mode.
            return null;
        }
    }
}
