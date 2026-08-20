package com.cardbilling.seed;

import com.cardbilling.domain.Account;
import com.cardbilling.domain.Card;
import com.cardbilling.domain.CardTransaction;
import com.cardbilling.domain.Customer;
import com.cardbilling.domain.repository.AccountRepository;
import com.cardbilling.domain.repository.CardRepository;
import com.cardbilling.domain.repository.CardTransactionRepository;
import com.cardbilling.domain.repository.CustomerRepository;
import java.time.LocalDateTime;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Populates an empty database with a synthetic but realistically-shaped card issuer dataset:
 * customers, one account and one card each, and several months of transaction history per
 * card - enough for invoice-closing to have real cycles to close and for the later jobs to have
 * genuinely overdue invoices to work through. A fixed random seed keeps the dataset identical
 * across fresh runs, which matters for reproducible benchmarks later.
 *
 * <p>A no-op if the database already has customers - this only ever seeds once.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final long RANDOM_SEED = 42L;
    private static final int CUSTOMER_COUNT = 150;
    private static final int MONTHS_OF_HISTORY = 4;

    private static final String[] FIRST_NAMES = {
            "Ana", "Bruno", "Carla", "Diego", "Elisa", "Fabio", "Gabriela", "Heitor", "Isabela", "Joao",
            "Larissa", "Marcos", "Natalia", "Otavio", "Patricia", "Rafael", "Sabrina", "Thiago", "Vanessa", "Wesley"
    };
    private static final String[] LAST_NAMES = {
            "Almeida", "Barbosa", "Cardoso", "Duarte", "Ferreira", "Goncalves", "Henriques", "Lima",
            "Martins", "Nogueira", "Oliveira", "Pereira", "Ribeiro", "Santos", "Teixeira", "Vieira"
    };
    private static final String[] MERCHANTS = {
            "Mercado Bom Preco", "Farmacia Popular", "Posto Ipiranga", "Restaurante Sabor Caseiro",
            "Livraria Cultura", "Loja de Roupas Vestir Bem", "Assinatura Streaming", "Padaria Pao Quente",
            "Academia Corpo Ativo", "Pet Shop Amigo Fiel"
    };

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;

    public DataSeeder(CustomerRepository customerRepository, AccountRepository accountRepository,
            CardRepository cardRepository, CardTransactionRepository cardTransactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Database already seeded - skipping");
            return;
        }

        Random random = new Random(RANDOM_SEED);
        int transactionCount = 0;
        for (int i = 0; i < CUSTOMER_COUNT; i++) {
            Customer customer = seedCustomer(random, i);
            Account account = seedAccount(customer, i);
            Card card = seedCard(random, account, i);
            transactionCount += seedTransactions(random, card);
        }

        log.info("Seeded {} customers, each with one account and one card, {} transactions total",
                CUSTOMER_COUNT, transactionCount);
    }

    private Customer seedCustomer(Random random, int index) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String fullName = firstName + " " + lastName;
        String email = (firstName + "." + lastName + index).toLowerCase() + "@example.com";
        String documentNumber = String.format("%011d", 10000000000L + index);
        String phoneNumber = String.format("+55119%08d", random.nextInt(100_000_000));
        return customerRepository.save(new Customer(fullName, documentNumber, email, phoneNumber));
    }

    private Account seedAccount(Customer customer, int index) {
        String accountNumber = String.format("ACC-%06d", index);
        return accountRepository.save(new Account(customer, accountNumber));
    }

    private Card seedCard(Random random, Account account, int index) {
        String cardNumberMasked = String.format("**** **** **** %04d", 1000 + (index % 9000));
        long creditLimitCents = (200 + random.nextInt(800)) * 100L * 10; // R$200-R$1000 limit, in cents
        int billingCycleDay = 1 + random.nextInt(28);
        return cardRepository.save(new Card(account, cardNumberMasked, creditLimitCents, billingCycleDay));
    }

    private int seedTransactions(Random random, Card card) {
        int count = 0;
        LocalDateTime cursor = LocalDateTime.now().minusMonths(MONTHS_OF_HISTORY);
        while (cursor.isBefore(LocalDateTime.now())) {
            int transactionsThisWeek = random.nextInt(3);
            for (int i = 0; i < transactionsThisWeek; i++) {
                String merchant = MERCHANTS[random.nextInt(MERCHANTS.length)];
                long amountCents = (10 + random.nextInt(490)) * 100L;
                cardTransactionRepository.save(new CardTransaction(card, merchant, amountCents, cursor));
                count++;
            }
            cursor = cursor.plusDays(7);
        }
        return count;
    }
}
