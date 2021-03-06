package de.adorsys.opba.protocol.facade.config.encryption;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static de.adorsys.opba.protocol.facade.config.ConfigConst.FACADE_CONFIG_PREFIX;

@Data
@Validated
@Configuration
@ConfigurationProperties(FACADE_CONFIG_PREFIX + "encryption.psu.key-pair")
public class PsuKeyPairConfig implements CmsEncSpec {

    @NotBlank
    private String keyAlgo;

    @Min(64)
    @SuppressWarnings("checkstyle:MagicNumber") // Magic minimal value - at least 64 bit key
    private int len;

    @NotNull
    private ASN1ObjectIdentifier cipherAlgo;

    @Component
    @ConfigurationPropertiesBinding
    public static class ASN1ObjectIdentifierConverter implements Converter<String, ASN1ObjectIdentifier> {

        private static final Map<String, ASN1ObjectIdentifier> MAPPINGS =
                ImmutableMap.<String, ASN1ObjectIdentifier>builder()
                        .put("AES128_CBC", NISTObjectIdentifiers.id_aes128_CBC)
                        .put("AES192_CBC", NISTObjectIdentifiers.id_aes192_CBC)
                        .put("AES256_CBC", NISTObjectIdentifiers.id_aes256_CBC)
                        .put("AES128_CCM", NISTObjectIdentifiers.id_aes128_CCM)
                        .put("AES192_CCM", NISTObjectIdentifiers.id_aes192_CCM)
                        .put("AES256_CCM", NISTObjectIdentifiers.id_aes256_CCM)
                        .put("AES128_GCM", NISTObjectIdentifiers.id_aes128_GCM)
                        .put("AES192_GCM", NISTObjectIdentifiers.id_aes192_GCM)
                        .put("AES256_GCM", NISTObjectIdentifiers.id_aes256_GCM)
                        .put("AES128_WRAP", NISTObjectIdentifiers.id_aes128_wrap)
                        .put("AES192_WRAP", NISTObjectIdentifiers.id_aes192_wrap)
                        .put("AES256_WRAP", NISTObjectIdentifiers.id_aes256_wrap)
                        .put("CHACHA20_POLY1305", PKCSObjectIdentifiers.id_alg_AEADChaCha20Poly1305) // with CMS should be used data size 64 bytes or more
                        .build();

        @Override
        public ASN1ObjectIdentifier convert(String source) {
            ASN1ObjectIdentifier identifier = MAPPINGS.get(source);
            if (null == identifier) {
                throw new IllegalArgumentException("Unknown mapping: " + source);
            }

            return identifier;
        }
    }
}
