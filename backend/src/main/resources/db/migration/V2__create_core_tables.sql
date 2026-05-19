CREATE TABLE tarot_cards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(100) NOT NULL,
  name_ko VARCHAR(100) NOT NULL,
  arcana VARCHAR(20) NOT NULL,
  suit VARCHAR(20) NULL,
  card_number INT NULL,
  image_url VARCHAR(500) NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uq_tarot_cards_arcana_number (arcana, card_number)
);

CREATE TABLE card_interpretations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  card_id BIGINT NOT NULL,
  orientation VARCHAR(20) NOT NULL,
  position_code VARCHAR(20) NOT NULL,
  keywords JSON NOT NULL,
  interpretation TEXT NOT NULL,
  document_version VARCHAR(20) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_card_interpretations_card
    FOREIGN KEY (card_id) REFERENCES tarot_cards(id),
  UNIQUE KEY uq_card_interpretation_version
    (card_id, orientation, position_code, document_version),
  INDEX idx_card_interpretations_lookup
    (card_id, orientation, position_code, is_active)
);

CREATE TABLE consultations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  concern TEXT NOT NULL,
  spread_type VARCHAR(50) NOT NULL,
  category_code VARCHAR(50) NULL,
  status VARCHAR(30) NOT NULL,
  result_summary VARCHAR(300) NULL,
  result_detail JSON NULL,
  retrieved_doc_ids JSON NULL,
  model_name VARCHAR(100) NULL,
  model_provider VARCHAR(50) NULL,
  prompt_version VARCHAR(50) NULL,
  document_version VARCHAR(50) NULL,
  idempotency_key VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  completed_at DATETIME NULL,
  deleted_at DATETIME NULL,
  CONSTRAINT fk_consultations_user
    FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY uq_consultations_user_idempotency (user_id, idempotency_key),
  INDEX idx_consultations_user_created (user_id, created_at DESC),
  INDEX idx_consultations_status (status),
  INDEX idx_consultations_deleted_at (deleted_at)
);

CREATE TABLE consultation_cards (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  consultation_id BIGINT NOT NULL,
  card_id BIGINT NOT NULL,
  position_order INT NOT NULL,
  position_code VARCHAR(20) NOT NULL,
  orientation VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_consultation_cards_consultation
    FOREIGN KEY (consultation_id) REFERENCES consultations(id),
  CONSTRAINT fk_consultation_cards_card
    FOREIGN KEY (card_id) REFERENCES tarot_cards(id),
  UNIQUE KEY uq_consultation_position (consultation_id, position_order),
  UNIQUE KEY uq_consultation_position_code (consultation_id, position_code),
  INDEX idx_consultation_cards_consultation (consultation_id)
);

CREATE TABLE context_documents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  embedding JSON NULL,
  document_version VARCHAR(20) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  INDEX idx_context_documents_category (category_code, is_active)
);

CREATE TABLE ai_request_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  consultation_id BIGINT NULL,
  request_id VARCHAR(100) NOT NULL,
  input_tokens INT NULL,
  output_tokens INT NULL,
  latency_ms INT NULL,
  status VARCHAR(50) NOT NULL,
  error_category VARCHAR(100) NULL,
  error_message TEXT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_ai_request_logs_consultation
    FOREIGN KEY (consultation_id) REFERENCES consultations(id),
  INDEX idx_ai_logs_consultation (consultation_id),
  INDEX idx_ai_logs_status_created (status, created_at)
);
