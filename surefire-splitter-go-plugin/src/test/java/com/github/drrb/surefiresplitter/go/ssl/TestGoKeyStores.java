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
package com.github.drrb.surefiresplitter.go.ssl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;

import static com.github.drrb.surefiresplitter.go.ssl.Entity.Builder.newEntity;
import static com.github.drrb.surefiresplitter.go.ssl.KeyStoreBuilder.newKeyStore;

public class TestGoKeyStores {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String SERVER_KEY_STORE_PASSWORD = "serverKeystorepa55w0rd";
    private static final String AGENT_CERT_SIGNING_PASSWORD = "Crui3CertSigningPassword";
    private static final String AGENT_KEY_STORE_PASSWORD = "agent5s0repa55w0rd";
    private static final String AGENT_TRUST_STORE_PASSWORD = AGENT_KEY_STORE_PASSWORD;

    private interface Holder {
        TestGoKeyStores INSTANCE = TestGoKeyStores.build();
    }

    public static TestGoKeyStores get() {
        return Holder.INSTANCE;
    }

    private static TestGoKeyStores build() {
        try {
            Entity ca = newEntity()
                    .selfSigned()
                    .withCn("go.example.com")
                    .withOu("Cruise Server primary certificate")
                    .withRandomSerialNumber()
                    .build();
            Entity agentSigner = newEntity()
                    .signedBy(ca)
                    .withOu("Cruise intermediate certificate")
                    .withEmailAddress("support@thoughtworks.com")
                    .withRandomSerialNumber()
                    .withSubjectKeyIdExtension()
                    .withAuthorityKeyIdExtension()
                    .withBasicConstraintsExtension()
                    .build();
            Entity server = newEntity()
                    .selfSigned()
                    .withCn("go.example.com")
                    .withOu("Cruise server webserver certificate")
                    .withRandomSerialNumber()
                    .build();
            Entity agent = newEntity()
                    .signedBy(agentSigner)
                    .withOu("Cruise agent certificate")
                    .withCn("agent.example.com")
                    .withSerialNumber(3)
                    .withEmailAddress("support@thoughtworks.com")
                    .withSubjectKeyIdExtension()
                    .withAuthorityKeyIdExtension()
                    .build();

            KeyStore serverKeyStore = newKeyStore()
                    .identifiedBy("cruise", server)
                    .withPassword(SERVER_KEY_STORE_PASSWORD)
                    .build();
            KeyStore serverTrustStore = newKeyStore()
                    .withPassword(AGENT_CERT_SIGNING_PASSWORD)
                    .identifiedBy("ca-intermediate", agentSigner)
                    .trusting("ca-cert", ca)
                    .build();
            KeyStore agentKeyStore = newKeyStore()
                    .withPassword(AGENT_KEY_STORE_PASSWORD)
                    .identifiedBy("agent", agent)
                    .build();
            KeyStore agentTrustStore = newKeyStore()
                    .withPassword(AGENT_TRUST_STORE_PASSWORD)
                    .trusting("cruise-server", server)
                    .build();
            return new TestGoKeyStores(serverKeyStore, serverTrustStore, agentKeyStore, agentTrustStore);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final KeyStore serverKeyStore;
    private final KeyStore serverTrustStore;
    private final KeyStore agentKeyStore;
    private final KeyStore agentTrustStore;

    private TestGoKeyStores(KeyStore serverKeyStore, KeyStore serverTrustStore, KeyStore agentKeyStore, KeyStore agentTrustStore) {
        this.serverKeyStore = serverKeyStore;
        this.serverTrustStore = serverTrustStore;
        this.agentKeyStore = agentKeyStore;
        this.agentTrustStore = agentTrustStore;
    }

    public SSLSocketFactory getServerSslSocketFactory() {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(serverKeyStore, SERVER_KEY_STORE_PASSWORD.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(serverTrustStore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setUpAgentKeyStores(Path agentConfigDir) {
        writeKeyStore(agentConfigDir.resolve("agent.jks"), agentKeyStore, AGENT_KEY_STORE_PASSWORD);
        writeKeyStore(agentConfigDir.resolve("trust.jks"), agentTrustStore, AGENT_TRUST_STORE_PASSWORD);
    }

    private void writeKeyStore(Path path, KeyStore keyStore, String password) {
        try (FileOutputStream agentKeyStoreWriter = new FileOutputStream(path.toFile())) {
            keyStore.store(agentKeyStoreWriter, password.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
