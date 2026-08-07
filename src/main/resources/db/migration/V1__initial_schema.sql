-- Initial MySQL schema for OPR3.
-- This migration mirrors the current JPA model so Flyway can own schema evolution.

CREATE TABLE IF NOT EXISTS `user` (
    `uid` VARCHAR(36) NOT NULL,
    `name` VARCHAR(255),
    `password` VARCHAR(255),
    `email` VARCHAR(255),
    PRIMARY KEY (`uid`),
    UNIQUE KEY `uk_user_name` (`name`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `quiz` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author_uid` VARCHAR(36) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    `time_limit` INT,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `icon` VARCHAR(10) CHARACTER SET utf8mb4,
    PRIMARY KEY (`id`),
    KEY `idx_quiz_author_uid` (`author_uid`),
    CONSTRAINT `fk_quiz_author` FOREIGN KEY (`author_uid`) REFERENCES `user` (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_uid` VARCHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    `color` VARCHAR(255),
    PRIMARY KEY (`id`),
    KEY `idx_category_user_uid` (`user_uid`),
    CONSTRAINT `fk_category_user` FOREIGN KEY (`user_uid`) REFERENCES `user` (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `quiz_category` (
    `quiz_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    PRIMARY KEY (`quiz_id`, `category_id`),
    KEY `idx_quiz_category_category_id` (`category_id`),
    CONSTRAINT `fk_quiz_category_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`),
    CONSTRAINT `fk_quiz_category_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quiz_id` BIGINT NOT NULL,
    `question_order` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `question_text` TEXT,
    `image_path` VARCHAR(255),
    `points` INT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_question_quiz_id` (`quiz_id`),
    CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_choice_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `text` VARCHAR(255) NOT NULL,
    `is_correct` BIT(1) NOT NULL,
    `option_order` INT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_question_choice_option_question_id` (`question_id`),
    CONSTRAINT `fk_question_choice_option_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_match_pair` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `left_side` VARCHAR(255) NOT NULL,
    `right_side` VARCHAR(255) NOT NULL,
    `pair_order` INT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_question_match_pair_question_id` (`question_id`),
    CONSTRAINT `fk_question_match_pair_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `text` VARCHAR(255) NOT NULL,
    `correct_position` INT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_question_order_item_question_id` (`question_id`),
    CONSTRAINT `fk_question_order_item_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_text_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `correct_answer` VARCHAR(255),
    `review` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_text_config_question_id` (`question_id`),
    CONSTRAINT `fk_question_text_config_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question_flashcard` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL,
    `back_text` VARCHAR(255) NOT NULL,
    `was_recalled` BIT(1),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_flashcard_question_id` (`question_id`),
    CONSTRAINT `fk_question_flashcard_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `quiz_attempt` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quiz_id` BIGINT NOT NULL,
    `user_uid` VARCHAR(36),
    `submitted_at` DATETIME(6),
    `score` DOUBLE,
    `max_score` INT NOT NULL,
    `time_spent` INT,
    PRIMARY KEY (`id`),
    KEY `idx_quiz_attempt_quiz_id` (`quiz_id`),
    KEY `idx_quiz_attempt_user_uid` (`user_uid`),
    CONSTRAINT `fk_quiz_attempt_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`),
    CONSTRAINT `fk_quiz_attempt_user` FOREIGN KEY (`user_uid`) REFERENCES `user` (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `attempt_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `attempt_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `answer_type` VARCHAR(31) NOT NULL,
    `max_points` DOUBLE,
    `time_spent` INT,
    `points_awarded` DOUBLE,
    `submitted_answer` TEXT,
    `user_marked_correct` BIT(1),
    PRIMARY KEY (`id`),
    KEY `idx_attempt_answer_attempt_id` (`attempt_id`),
    KEY `idx_attempt_answer_question_id` (`question_id`),
    CONSTRAINT `fk_attempt_answer_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `quiz_attempt` (`id`),
    CONSTRAINT `fk_attempt_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `id_based_attempt_answer_selected_option_ids` (
    `id_based_attempt_answer_id` BIGINT NOT NULL,
    `selected_option_ids` BIGINT,
    KEY `idx_id_based_attempt_answer_id` (`id_based_attempt_answer_id`),
    CONSTRAINT `fk_id_based_attempt_answer_selected_option_ids` FOREIGN KEY (`id_based_attempt_answer_id`) REFERENCES `attempt_answer` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `attempt_answer_matched_pair` (
    `match_based_attempt_answer_id` BIGINT NOT NULL,
    `left_id` BIGINT,
    `right_id` BIGINT,
    KEY `idx_match_based_attempt_answer_id` (
        `match_based_attempt_answer_id`
    ),
    CONSTRAINT `fk_attempt_answer_matched_pair_answer` FOREIGN KEY (
        `match_based_attempt_answer_id`
    ) REFERENCES `attempt_answer` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `token` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(255),
    `revoked` BIT(1) NOT NULL,
    `user_uid` VARCHAR(36),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_token` (`token`),
    KEY `idx_token_user_uid` (`user_uid`),
    CONSTRAINT `fk_token_user` FOREIGN KEY (`user_uid`) REFERENCES `user` (`uid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;