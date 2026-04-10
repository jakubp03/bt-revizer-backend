package com.opr3.opr3.service.quiz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final OllamaChatModel ollamaChatModel;

    /**
     * Asks the LLM whether the student's answer is correct for the given question
     * and expected answer. Returns true if the LLM responds with "YES", false
     * otherwise. Falls back to false on any error.
     */
    public boolean isAnswerCorrect(String questionText, String expectedAnswer, String studentAnswer) {
        String prompt = buildPrompt(questionText, expectedAnswer, studentAnswer);
        try {
            String response = ollamaChatModel
                    .call(new Prompt(new UserMessage(prompt)))
                    .getResult()
                    .getOutput()
                    .getText()
                    .trim()
                    .toUpperCase();

            boolean correct = response.startsWith("YES");
            log.info("[LLM] question=\"{}\" expected=\"{}\" student=\"{}\" → {} (raw: \"{}\")",
                    questionText, expectedAnswer, studentAnswer, correct ? "CORRECT" : "INCORRECT", response);
            return correct;

        } catch (Exception e) {
            log.error("[LLM] Failed to evaluate answer for question=\"{}\", falling back to incorrect. Error: {}",
                    questionText, e.getMessage());
            return false;
        }
    }

    private String buildPrompt(String questionText, String expectedAnswer, String studentAnswer) {
        return """
                You are an answer evaluator. Your only job is to check if the student answer contains the correct factual information.
                Ignore uncertainty phrases like "i think", "maybe", "i believe", "i guess".
                Focus only on whether the core fact is correct.

                Question: %s
                Correct answer: %s
                Student answer: %s

                Is the student answer factually correct? Reply with only YES or NO."""
                .formatted(questionText, expectedAnswer, studentAnswer);
    }
}
