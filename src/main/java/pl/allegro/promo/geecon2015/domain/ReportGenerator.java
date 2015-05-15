package pl.allegro.promo.geecon2015.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStats;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransactions;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;
import pl.allegro.promo.geecon2015.domain.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class ReportGenerator {
    
    private final FinancialStatisticsRepository financialStatisticsRepository;
    
    private final UserRepository userRepository;
    
    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        Report report = new Report();
        FinancialStats stats = financialStatisticsRepository.listUsersWithMinimalIncome(request.getMinimalIncome(), request.getUsersToCheck());
        for (UUID id : stats.getUserIds()) {
            User user = userRepository.detailsOf(id);
            report.add(new ReportedUser(id, user.getName(), calculateAmount(id)));
        }
        return report;
    }

    private BigDecimal calculateAmount(UUID id) {
        UserTransactions userTransactions = null;
        try {
            userTransactions = transactionRepository.transactionsOf(id);
        } catch (Exception ex) {
            return null;
        }
        return userTransactions.getTransactions().stream().map((x) -> x.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
}
