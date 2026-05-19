package com.koreatarot.tarot.repository;

import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarotCardRepository extends JpaRepository<TarotCard, Long> {

    List<TarotCard> findAllByArcanaOrderByCardNumberAsc(Arcana arcana);
}
