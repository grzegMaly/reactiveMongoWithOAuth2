package com.myproject.reactivemongo.listeners;

import com.myproject.reactivemongo.domain.Beer;
import com.myproject.reactivemongo.domain.Customer;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MongoEntityListener extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof Beer beer) {

            if (beer.getCreatedDate() == null) {
                beer.setCreatedDate(Instant.now());
            }
            beer.setLastModifiedDate(Instant.now());

        } else if (source instanceof Customer customer) {

            if (customer.getCreatedDate() == null) {
                customer.setCreatedDate(Instant.now());
            }
            customer.setLastModifiedDate(Instant.now());
        }
    }
}
