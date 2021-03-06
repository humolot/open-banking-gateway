package de.adorsys.opba.protocol.hbci.config;

import com.google.common.base.Strings;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.hbci.HbciBanking;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Resources;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIProduct;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HbciAdapterConfig {

    private final HbciAdapterProperties properties;

    @Bean
    @SneakyThrows
    OnlineBankingService onlineBankingService(Optional<HBCIProduct> product) {
        try (InputStream is = Resources.getInputStream("blz.properties")) {
            HBCIUtils.refreshBLZList(is);
        }

        OnlineBankingService hbci = new HbciBanking(
                product.orElse(null),
                properties.getSysIdExpirationTimeMs(),
                properties.getUpdExpirationTimeMs()
        );

        // Initiate MOCK bank, should come after HbciBanking is created
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBlz("10000001");
        bankInfo.setPinTanAddress("http://localhost:8090/hbci-mock/");
        bankInfo.setPinTanVersion(HBCIVersion.HBCI_300);
        bankInfo.setBic("ADORSYS");
        HBCIUtils.addBankInfo(bankInfo);

        return hbci;
    }

    @Bean
    Optional<HBCIProduct> product() {
        if (Strings.isNullOrEmpty(properties.getHbciProduct())) {
            log.warn("No HBCI product defined");
            return Optional.empty();
        }

        return Optional.of(new HBCIProduct(properties.getHbciProduct(), properties.getHbciVersion()));
    }
}
