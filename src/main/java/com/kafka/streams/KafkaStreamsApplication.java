package com.kafka.streams;

import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kafka.streams.configuration.KafkaConfiguration;
import com.kafka.streams.model.CreditCard;
import com.kafka.streams.repository.Repository;

@SpringBootApplication
@EnableScheduling
public class KafkaStreamsApplication {

    private final Repository repository;

    public KafkaStreamsApplication(Repository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsApplication.class, args);
    }

    @Scheduled(fixedRate = 2000)
    public void randomCards() {
        CreditCard card = new CreditCard(UUID.randomUUID());
        card.assignLimit(new BigDecimal(2000));
        card.withdraw(BigDecimal.TEN);
        card.repay(BigDecimal.TEN);
        repository.save(card);
    }

}

@RestController
class CreditCardsController {

    private final StreamsBuilderFactoryBean kStreamBuilderFactoryBean;

    CreditCardsController(StreamsBuilderFactoryBean kStreamBuilderFactoryBean) {
        this.kStreamBuilderFactoryBean = kStreamBuilderFactoryBean;
    }

    @GetMapping("/cards")
    List<CreditCard> creditCardList() {
        List<CreditCard> cards = new ArrayList<>();
        ReadOnlyKeyValueStore<String, CreditCard> store = kStreamBuilderFactoryBean.getKafkaStreams()
                .store(KafkaConfiguration.SNAPSHOTS_FOR_CARDS, keyValueStore());

        store.all().forEachRemaining(stringCreditCardKeyValue -> cards.add(stringCreditCardKeyValue.value));
        return cards;
    }
}