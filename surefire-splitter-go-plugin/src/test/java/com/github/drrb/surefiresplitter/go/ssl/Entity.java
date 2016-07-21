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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

public class Entity {
    private static final long TEN_YEARS = 10L * 365 * 24 * 60 * 60 * 1000;
    private static final Date TEN_YEARS_FROM_NOW = new Date(new Date().getTime() + TEN_YEARS);
    private static final Date EPOCH = new Date(0);

    public static class Builder {
        private final KeyPair keyPair;
        private final Builder.PrincipalBuilder principalBuilder = new Builder.PrincipalBuilder();
        private Entity signer;
        private boolean hasSubjectKeyIdExtension;
        private boolean hasAuthorityKeyIdExtension;
        private boolean hasBasicConstraintsExtension;
        private BigInteger serialNumber;

        public static Builder newEntity() throws Exception {
            return new Builder(createKeyPair());
        }

        private Builder(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        public Builder withCn(String cn) {
            principalBuilder.withCn(cn);
            return this;
        }

        public Builder withOu(String ou) {
            principalBuilder.withOu(ou);
            return this;
        }

        public Builder withEmailAddress(String emailAddress) {
            principalBuilder.withEmailAddress(emailAddress);
            return this;
        }

        public Builder signedBy(Entity signer) {
            this.signer = signer;
            return this;
        }

        public Builder selfSigned() {
            return signedBy(null);
        }

        public Builder withSubjectKeyIdExtension() {
            this.hasSubjectKeyIdExtension = true;
            return this;
        }

        public Builder withAuthorityKeyIdExtension() {
            this.hasAuthorityKeyIdExtension = true;
            return this;
        }

        public Builder withBasicConstraintsExtension() {
            this.hasBasicConstraintsExtension = true;
            return this;
        }

        public Builder withRandomSerialNumber() {
            return withSerialNumber(Math.round(Math.random() * 11234455544545L));
        }

        public Builder withSerialNumber(long serialNumber) {
            this.serialNumber = BigInteger.valueOf(serialNumber);
            return this;
        }

        public Entity build() throws Exception {
            X509Principal principal = principalBuilder.build();
            if (isSelfSigned()) {
                X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
                certGen.setSerialNumber(serialNumber);
                certGen.setIssuerDN(principal);
                certGen.setNotBefore(EPOCH);
                certGen.setNotAfter(TEN_YEARS_FROM_NOW);
                certGen.setSubjectDN(principal);
                certGen.setPublicKey(keyPair.getPublic());
                certGen.setSignatureAlgorithm("SHA1WITHRSA");
                X509Certificate certificate = certGen.generate(keyPair.getPrivate(), "BC");
                certificate.checkValidity();
                return new Entity(keyPair, certificate);
            } else {
                X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
                certificateGenerator.reset();
                certificateGenerator.setSignatureAlgorithm("SHA1WithRSAEncryption");
                certificateGenerator.setNotBefore(EPOCH);
                certificateGenerator.setNotAfter(TEN_YEARS_FROM_NOW);
                certificateGenerator.setIssuerDN(signer.getPrincipal());
                certificateGenerator.setSubjectDN(principal);
                certificateGenerator.setPublicKey(keyPair.getPublic());
                certificateGenerator.setSerialNumber(serialNumber);
                if (hasSubjectKeyIdExtension) {
                    certificateGenerator.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(keyPair.getPublic()));
                }
                if (hasAuthorityKeyIdExtension) {
                    certificateGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(signer.getCertificate()));
                }
                if (hasBasicConstraintsExtension) {
                    certificateGenerator.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
                }
                X509Certificate certificate = certificateGenerator.generate(signer.getPrivateKey(), "BC");

                certificate.checkValidity();
                certificate.verify(signer.getPublicKey());
                return new Entity(keyPair, certificate, signer.getCertificateAndChain());
            }
        }

        private boolean isSelfSigned() {
            return signer == null;
        }

        private static KeyPair createKeyPair() throws Exception {
            KeyPair seed = KeyPairGenerator.getInstance("RSA", "BC").generateKeyPair();
            RSAPrivateKey privateSeed = (RSAPrivateKey) seed.getPrivate();
            RSAPublicKey publicSeed = (RSAPublicKey) seed.getPublic();
            KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
            RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(
                    privateSeed.getModulus(),
                    privateSeed.getPrivateExponent()
            );
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    publicSeed.getModulus(),
                    publicSeed.getPublicExponent()
            );
            return new KeyPair(fact.generatePublic(publicKeySpec), fact.generatePrivate(privateKeySpec));
        }

        private static class PrincipalBuilder {
            private final List<Builder.PrincipalBuilder.PrincipalIdentifier> identifiers = new LinkedList<>();

            public Builder.PrincipalBuilder withCn(String cn) {
                return with(identifier(X509Principal.CN, cn));
            }

            public Builder.PrincipalBuilder withOu(String ou) {
                return with(identifier(X509Principal.OU, ou));
            }

            public Builder.PrincipalBuilder withEmailAddress(String emailAddress) {
                return with(identifier(X509Principal.EmailAddress, emailAddress));
            }

            private Builder.PrincipalBuilder with(Builder.PrincipalBuilder.PrincipalIdentifier identifier) {
                identifiers.add(identifier);
                return this;
            }

            private static Builder.PrincipalBuilder.PrincipalIdentifier identifier(ASN1ObjectIdentifier key, String value) {
                return new Builder.PrincipalBuilder.PrincipalIdentifier(key, value);
            }

            public X509Principal build() {
                Vector<DERObjectIdentifier> order = new Vector<>();
                Hashtable<DERObjectIdentifier, String> attrs = new Hashtable<>();
                for (Builder.PrincipalBuilder.PrincipalIdentifier identifier : identifiers) {
                    order.addElement(identifier.identifier);
                    attrs.put(identifier.identifier, identifier.value);
                }
                return new X509Principal(order, attrs);
            }

            private static class PrincipalIdentifier {
                private final DERObjectIdentifier identifier;
                private final String value;

                private PrincipalIdentifier(DERObjectIdentifier identifier, String value) {
                    this.identifier = identifier;
                    this.value = value;
                }
            }
        }
    }

    private final List<Certificate> chain;
    private final KeyPair keyPair;
    private final X509Certificate certificate;
    private final X509Principal principal;

    private Entity(KeyPair keyPair, X509Certificate certificate) throws Exception {
        this(keyPair, certificate, Collections.<Certificate>emptyList());
    }

    private Entity(KeyPair keyPair, X509Certificate certificate, List<Certificate> chain) throws Exception {
        this.keyPair = keyPair;
        this.certificate = certificate;
        this.chain = chain;
        this.principal = PrincipalUtil.getSubjectX509Principal(certificate);
    }

    private X509Principal getPrincipal() {
        return principal;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    private PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public Certificate[] getCertificateChain() {
        List<Certificate> certificateAndChain = getCertificateAndChain();
        return certificateAndChain.toArray(new Certificate[certificateAndChain.size()]);
    }

    private List<Certificate> getCertificateAndChain() {
        List<Certificate> certificateAndChain = new LinkedList<>();
        certificateAndChain.add(certificate);
        certificateAndChain.addAll(chain);
        return certificateAndChain;
    }
}
