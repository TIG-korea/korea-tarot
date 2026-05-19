package com.koreatarot.tarot.repository;

import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarotCardRepository extends JpaRepository<TarotCard, Long> {

    List<TarotCard> findAllByArcanaOrderByCardNumberAsc(Arcana arcana);

    @Query("""
            select tarotCard.id
            from TarotCard tarotCard
            where tarotCard.arcana = :arcana
            order by tarotCard.cardNumber asc
            """)
    List<Long> findIdsByArcanaOrderByCardNumberAsc(@Param("arcana") Arcana arcana);
}
