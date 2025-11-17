package org.sid.stockservice;

import org.sid.stockservice.entities.StockMarket;
import org.sid.stockservice.repositories.StockMarketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;

@SpringBootApplication
@EnableDiscoveryClient
public class StockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(StockMarketRepository stockMarketRepository) {
        return args -> {
            Random random = new Random();

            // Create sample stock market data with random company IDs
            StockMarket stock1 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(5))
                    .openValue(BigDecimal.valueOf(170.00))
                    .highValue(BigDecimal.valueOf(172.50))
                    .lowValue(BigDecimal.valueOf(169.75))
                    .closeValue(BigDecimal.valueOf(171.80))
                    .volume(45000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock2 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(4))
                    .openValue(BigDecimal.valueOf(172.00))
                    .highValue(BigDecimal.valueOf(174.00))
                    .lowValue(BigDecimal.valueOf(171.50))
                    .closeValue(BigDecimal.valueOf(173.25))
                    .volume(48000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock3 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(3))
                    .openValue(BigDecimal.valueOf(173.50))
                    .highValue(BigDecimal.valueOf(176.00))
                    .lowValue(BigDecimal.valueOf(173.00))
                    .closeValue(BigDecimal.valueOf(175.50))
                    .volume(52000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock4 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(5))
                    .openValue(BigDecimal.valueOf(375.00))
                    .highValue(BigDecimal.valueOf(378.50))
                    .lowValue(BigDecimal.valueOf(374.00))
                    .closeValue(BigDecimal.valueOf(377.80))
                    .volume(28000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock5 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(4))
                    .openValue(BigDecimal.valueOf(378.00))
                    .highValue(BigDecimal.valueOf(381.00))
                    .lowValue(BigDecimal.valueOf(377.50))
                    .closeValue(BigDecimal.valueOf(380.25))
                    .volume(30000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock6 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(5))
                    .openValue(BigDecimal.valueOf(138.00))
                    .highValue(BigDecimal.valueOf(141.50))
                    .lowValue(BigDecimal.valueOf(137.75))
                    .closeValue(BigDecimal.valueOf(140.75))
                    .volume(35000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock7 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(5))
                    .openValue(BigDecimal.valueOf(240.00))
                    .highValue(BigDecimal.valueOf(248.00))
                    .lowValue(BigDecimal.valueOf(239.50))
                    .closeValue(BigDecimal.valueOf(245.30))
                    .volume(95000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            StockMarket stock8 = StockMarket.builder()
                    .date(LocalDate.now().minusDays(5))
                    .openValue(BigDecimal.valueOf(152.00))
                    .highValue(BigDecimal.valueOf(157.00))
                    .lowValue(BigDecimal.valueOf(151.50))
                    .closeValue(BigDecimal.valueOf(155.80))
                    .volume(42000000L)
                    .companyId(random.nextLong(1, 10000))
                    .build();

            stockMarketRepository.saveAll(Arrays.asList(
                    stock1, stock2, stock3, stock4, stock5, stock6, stock7, stock8
            ));

            System.out.println("✅ Seeded 8 stock market quotations with random company IDs");
            System.out.println("🎉 Data seeding completed successfully!");
        };
    }

}
