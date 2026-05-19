package com.koreatarot.tarot.service;

import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TarotCardService {

    private final TarotCardRepository tarotCardRepository;

    public TarotCardService(TarotCardRepository tarotCardRepository) {
        this.tarotCardRepository = tarotCardRepository;
    }

    @Transactional(readOnly = true)
    public List<TarotCard> listMajorCards() {
        return tarotCardRepository.findAllByArcanaOrderByCardNumberAsc(Arcana.MAJOR);
    }
}
