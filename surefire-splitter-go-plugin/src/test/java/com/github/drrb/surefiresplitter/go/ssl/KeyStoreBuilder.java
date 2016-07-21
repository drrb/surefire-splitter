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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.LinkedList;
import java.util.List;

public class KeyStoreBuilder {


    public static KeyStoreBuilder newKeyStore() {
        return new KeyStoreBuilder();
    }

    private AliasedEntity identity;
    private List<AliasedEntity> trusted = new LinkedList<>();
    private String password;

    public KeyStoreBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public KeyStoreBuilder identifiedBy(String identityAlias, Entity identity) {
        this.identity = new AliasedEntity(identityAlias, identity);
        return this;
    }

    public KeyStoreBuilder trusting(String trustedEntityAlias, Entity trustedEntity) {
        this.trusted.add(new AliasedEntity(trustedEntityAlias, trustedEntity));
        return this;
    }

    public KeyStore build() throws Exception {
        KeyStore keyStore = newKeyStore(password);
        if (identity != null) {
            identity.addPrivateKeyEntryTo(keyStore, password);
        }
        for (AliasedEntity trustedEntity : trusted) {
            trustedEntity.addCertificateEntryTo(keyStore);
        }
        return keyStore;
    }

    private static KeyStore newKeyStore(String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, password.toCharArray());
        return keyStore;
    }

    private class AliasedEntity {
        private final String alias;
        private final Entity entity;

        public AliasedEntity(String alias, Entity entity) {
            this.alias = alias;
            this.entity = entity;
        }

        public void addPrivateKeyEntryTo(KeyStore keyStore, String password) throws KeyStoreException {
            KeyStore.Entry privateKeyEntry = new KeyStore.PrivateKeyEntry(entity.getPrivateKey(), entity.getCertificateChain());
            keyStore.setEntry(alias, privateKeyEntry, new KeyStore.PasswordProtection(password.toCharArray()));
        }

        public void addCertificateEntryTo(KeyStore keyStore) throws KeyStoreException {
            keyStore.setCertificateEntry(alias, entity.getCertificate());
        }
    }
}
