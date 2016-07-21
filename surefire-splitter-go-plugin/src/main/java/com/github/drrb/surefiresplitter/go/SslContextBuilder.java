/**
 * Surefire Splitter Go Plugin
 * Copyright (C) 2016 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Surefire Splitter Go Plugin. If not, see <http://www.gnu.org/licenses />.
 */
package com.github.drrb.surefiresplitter.go;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class SslContextBuilder {

    private final String purpose;
    private Path trustStorePath;
    private char[] trustStorePassword = null;
    private Path keyStorePath;
    private char[] keyStorePassword = null;

    public SslContextBuilder(String purpose) {
        this.purpose = purpose;
    }

    public SslContextBuilder withTrustStore(Path trustStorePath) {
        this.trustStorePath = trustStorePath;
        return this;
    }

    public SslContextBuilder withTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword.toCharArray();
        return this;
    }

    public SslContextBuilder withKeyStore(Path keyStorePath) {
        this.keyStorePath = keyStorePath;
        return this;
    }

    public SslContextBuilder withKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword.toCharArray();
        return this;
    }

    public SSLContext build() {
        SSLContext tlsContext = getSslContext("TLSv1.2");
        try {
            tlsContext.init(getKeyManagers(), getTrustManagers(), /* use default SecureRandom */ null);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return tlsContext;
    }

    private KeyManager[] getKeyManagers() {
        KeyStore keyStore = loadKeystore(keyStorePath, keyStorePassword);
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword);
            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw failure("Failed to load key manager", e);
        }
    }

    private TrustManager[] getTrustManagers() {
        KeyStore trustStore = loadKeystore(trustStorePath, trustStorePassword);
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            return trustManagerFactory.getTrustManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw failure("Failed to load trust manager", e);
        }
    }

    private SSLContext getSslContext(String scheme) {
        try {
            return SSLContext.getInstance(scheme);
        } catch (NoSuchAlgorithmException e) {
            throw failure("Couldn't create an SSL context of type '" + scheme + "'", e);
        }
    }

    private KeyStore loadKeystore(Path keyStoreFile, char[] keyStorePassword) {
        try (FileInputStream keyStoreInputStream = new FileInputStream(keyStoreFile.toFile())) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreInputStream, keyStorePassword);
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw failure("Failed to load the keystore at '" + keyStoreFile + "'", e);
        }
    }

    private RuntimeException failure(String message, Throwable cause) {
        return new RuntimeException(String.format("%s (I need it %s)", message, purpose), cause);
    }
}
