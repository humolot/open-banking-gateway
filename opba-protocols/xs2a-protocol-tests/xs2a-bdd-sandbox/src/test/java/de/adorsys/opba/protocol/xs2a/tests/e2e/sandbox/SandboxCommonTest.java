package de.adorsys.opba.protocol.xs2a.tests.e2e.sandbox;

import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest;
import de.adorsys.opba.protocol.xs2a.config.protocol.ProtocolUrlsConfiguration;
import de.adorsys.opba.protocol.xs2a.testsandbox.SandboxAppsStarter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import java.security.Security;
import java.time.LocalDate;
import java.util.UUID;

public class SandboxCommonTest<GIVEN, WHEN, THEN> extends SpringScenarioTest<GIVEN, WHEN, THEN> {

    protected static final LocalDate DATE_FROM = LocalDate.parse("2018-01-01");
    protected static final LocalDate DATE_TO = LocalDate.parse("2020-09-30");
    protected static final String BOTH_BOOKING = "BOTH";

    protected static final SandboxAppsStarter executor = new SandboxAppsStarter();

    protected final String OPBA_LOGIN = UUID.randomUUID().toString();
    protected final String OPBA_PASSWORD = UUID.randomUUID().toString();

    @LocalServerPort
    private int port;

    @Autowired
    private ProtocolUrlsConfiguration urlsConfiguration;

    @BeforeAll
    static void startSandbox() {
        WebDriverManager.firefoxdriver().arch64();

        if (null != System.getenv("NO_SANDBOX_START")) {
            return;
        }

        executor.runAll();
        executor.awaitForAllStarted();
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @AfterAll
    static void stopSandbox() {
        executor.shutdown();
    }

    // See https://github.com/spring-projects/spring-boot/issues/14879 for the 'why setting port'
    @BeforeEach
    void setBaseUrl() {
        ProtocolUrlsConfiguration.WebHooks aisUrls = urlsConfiguration.getAis().getWebHooks();
        aisUrls.setOk(aisUrls.getOk().replaceAll("localhost:\\d+", "localhost:" + port));
        aisUrls.setNok(aisUrls.getNok().replaceAll("localhost:\\d+", "localhost:" + port));

        ProtocolUrlsConfiguration.WebHooks pisUrls = urlsConfiguration.getPis().getWebHooks();
        pisUrls.setOk(pisUrls.getOk().replaceAll("localhost:\\d+", "localhost:" + port));
        pisUrls.setNok(pisUrls.getNok().replaceAll("localhost:\\d+", "localhost:" + port));
    }
}
