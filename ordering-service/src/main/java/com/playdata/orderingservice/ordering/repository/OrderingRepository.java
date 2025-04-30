package com.playdata.orderingservice.ordering.repository;

import com.playdata.orderingservice.ordering.entity.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderingRepository
        extends JpaRepository<Ordering, Long> {

}
