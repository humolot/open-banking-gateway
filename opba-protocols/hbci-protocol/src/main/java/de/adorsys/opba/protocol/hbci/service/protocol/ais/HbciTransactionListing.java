package de.adorsys.opba.protocol.hbci.service.protocol.ais;

import de.adorsys.multibanking.domain.Bank;
import de.adorsys.multibanking.domain.BankAccess;
import de.adorsys.multibanking.domain.BankAccount;
import de.adorsys.multibanking.domain.BankApiUser;
import de.adorsys.multibanking.domain.request.TransactionRequest;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.domain.spi.OnlineBankingService;
import de.adorsys.multibanking.domain.transaction.AbstractTransaction;
import de.adorsys.multibanking.domain.transaction.LoadTransactions;
import de.adorsys.multibanking.hbci.model.HbciConsent;
import de.adorsys.opba.protocol.bpmnshared.service.context.ContextUtil;
import de.adorsys.opba.protocol.bpmnshared.service.exec.ValidatedExecution;
import de.adorsys.opba.protocol.hbci.context.HbciContext;
import de.adorsys.opba.protocol.hbci.context.TransactionListHbciContext;
import de.adorsys.opba.protocol.hbci.service.protocol.ais.dto.AisListTransactionsResult;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

import static de.adorsys.multibanking.domain.transaction.LoadTransactions.BookingStatus.BOOKED;

@Service("hbciTransactionListing")
@RequiredArgsConstructor
public class HbciTransactionListing extends ValidatedExecution<TransactionListHbciContext> {

    private final OnlineBankingService onlineBankingService;

    @Override
    protected void doRealExecution(DelegateExecution execution, TransactionListHbciContext context) {
        HbciConsent consent = context.getHbciDialogConsent();
        BankAccount account = new BankAccount();
        account.setIban(context.getAccountIban());
        TransactionRequest<LoadTransactions> request = create(
                createLoadTransactions(account),
                new BankApiUser(),
                new BankAccess(),
                context.getBank(),
                consent
        );
        TransactionsResponse response = onlineBankingService.loadTransactions(request);

        if (null == response.getAuthorisationCodeResponse()) {
            ContextUtil.getAndUpdateContext(
                    execution,
                    (TransactionListHbciContext ctx) -> {
                        ctx.setResponse(
                                new AisListTransactionsResult(
                                        response.getBookings(),
                                        response.getBalancesReport(),
                                        Instant.now()
                                )
                        );
                        ctx.setTanChallengeRequired(false);
                    }
            );

            return;
        }

        onlineBankingService.getStrongCustomerAuthorisation().afterExecute(consent, response.getAuthorisationCodeResponse());
        ContextUtil.getAndUpdateContext(
                execution,
                (HbciContext ctx) -> {
                    ctx.setHbciDialogConsent((HbciConsent) response.getBankApiConsentData());
                    ctx.setTanChallengeRequired(true);
                }
        );
    }

    private static LoadTransactions createLoadTransactions(BankAccount account) {
        LoadTransactions loadBookings = new LoadTransactions();
        loadBookings.setBookingStatus(BOOKED);
        loadBookings.setPsuAccount(account);
        loadBookings.setDateFrom(null);
        loadBookings.setDateTo(LocalDate.now());
        loadBookings.setWithBalance(true);
        return loadBookings;
    }

    public static <T extends AbstractTransaction> TransactionRequest<T> create(T transaction,
                                                                               BankApiUser bankApiUser,
                                                                               BankAccess bankAccess,
                                                                               Bank bank,
                                                                               Object bankApiConsentData) {
        TransactionRequest<T> transactionRequest = new TransactionRequest<>(transaction);
        transactionRequest.setBankApiUser(bankApiUser);
        transactionRequest.setBankAccess(bankAccess);
        transactionRequest.setBankApiConsentData(bankApiConsentData);
        transactionRequest.setBank(bank);

        return transactionRequest;
    }
}
