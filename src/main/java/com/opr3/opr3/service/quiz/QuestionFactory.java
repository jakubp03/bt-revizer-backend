package com.opr3.opr3.service.quiz;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.opr3.opr3.dto.quiz.CreateQuizRequest.ChoiceOptionRequest;
import com.opr3.opr3.dto.quiz.CreateQuizRequest.QuestionRequest;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.question.OptionChoice;
import com.opr3.opr3.entity.question.OptionFlashcard;
import com.opr3.opr3.entity.question.OptionMatchPair;
import com.opr3.opr3.entity.question.OptionOrderItem;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.entity.question.TextAnswerConfig;
import com.opr3.opr3.exception.InvalidRequestException;

@Component
public class QuestionFactory {

    public Question create(QuestionRequest request, int order, Quiz quiz) {
        Question question = Question.builder()
                .quiz(quiz)
                .questionOrder(order)
                .type(request.getType())
                .questionText(request.getQuestionText())
                .imagePath(request.getImagePath())
                .points(request.getPoints())
                .build();

        switch (request.getType()) {
            case SINGLE_CHOICE -> buildSingleChoice(question, request);
            case MULTIPLE_CHOICE -> buildMultipleChoice(question, request);
            case TRUE_FALSE -> buildTrueFalse(question, request);
            case TEXT_INPUT -> buildTextInput(question, request);
            case MATCHING -> buildMatching(question, request);
            case ORDERING -> buildOrdering(question, request);
            case FLASHCARD -> buildFlashcard(question, request);
        }

        return question;
    }

    private void buildSingleChoice(Question question, QuestionRequest request) {
        List<ChoiceOptionRequest> options = request.getChoiceOptions();
        if (options == null || options.size() < 2) {
            throw new InvalidRequestException("SINGLE_CHOICE requires at least 2 options");
        }
        long correctCount = options.stream().filter(ChoiceOptionRequest::isCorrect).count();
        if (correctCount != 1) {
            throw new InvalidRequestException("SINGLE_CHOICE must have exactly 1 correct option");
        }
        question.setChoiceOptions(buildChoiceOptions(question, options));
    }

    private void buildMultipleChoice(Question question, QuestionRequest request) {
        List<ChoiceOptionRequest> options = request.getChoiceOptions();
        if (options == null || options.size() < 2) {
            throw new InvalidRequestException("MULTIPLE_CHOICE requires at least 2 options");
        }
        long correctCount = options.stream().filter(ChoiceOptionRequest::isCorrect).count();
        if (correctCount < 1) {
            throw new InvalidRequestException("MULTIPLE_CHOICE must have at least 1 correct option");
        }
        question.setChoiceOptions(buildChoiceOptions(question, options));
    }

    private void buildTrueFalse(Question question, QuestionRequest request) {
        if (request.getCorrectAnswer() == null) {
            throw new InvalidRequestException("TRUE_FALSE requires correctAnswer (true/false)");
        }
        boolean correct = request.getCorrectAnswer();
        question.setChoiceOptions(List.of(
                OptionChoice.builder().question(question).text("True").isCorrect(correct).optionOrder(1).build(),
                OptionChoice.builder().question(question).text("False").isCorrect(!correct).optionOrder(2).build()));
    }

    private void buildTextInput(Question question, QuestionRequest request) {
        if (request.getTextConfig() == null) {
            throw new InvalidRequestException("TEXT_INPUT requires textConfig");
        }
        TextAnswerConfig config = TextAnswerConfig.builder()
                .question(question)
                .correctAnswer(request.getTextConfig().getCorrectAnswer())
                .textReviewType(request.getTextConfig().getReviewType())
                .build();
        question.setTextConfig(config);
    }

    private void buildMatching(Question question, QuestionRequest request) {
        if (request.getMatchPairs() == null || request.getMatchPairs().size() < 2) {
            throw new InvalidRequestException("MATCHING requires at least 2 pairs");
        }
        List<OptionMatchPair> pairs = IntStream.range(0, request.getMatchPairs().size())
                .mapToObj(i -> OptionMatchPair.builder()
                        .question(question)
                        .leftSide(request.getMatchPairs().get(i).getLeftSide())
                        .rightSide(request.getMatchPairs().get(i).getRightSide())
                        .pairOrder(i + 1)
                        .build())
                .toList();
        question.setMatchPairs(pairs);
    }

    private void buildOrdering(Question question, QuestionRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().size() < 2) {
            throw new InvalidRequestException("ORDERING requires at least 2 items");
        }
        List<OptionOrderItem> items = IntStream.range(0, request.getOrderItems().size())
                .mapToObj(i -> OptionOrderItem.builder()
                        .question(question)
                        .text(request.getOrderItems().get(i).getText())
                        .correctPosition(i + 1)
                        .build())
                .toList();
        question.setOrderItems(items);
    }

    private void buildFlashcard(Question question, QuestionRequest request) {
        if (request.getFlashcardBackText() == null || request.getFlashcardBackText().isBlank()) {
            throw new InvalidRequestException("FLASHCARD requires flashcardBackText");
        }
        OptionFlashcard flashcard = OptionFlashcard.builder()
                .question(question)
                .backText(request.getFlashcardBackText())
                .build();
        question.setFlashcardConfig(flashcard);
    }

    private List<OptionChoice> buildChoiceOptions(Question question, List<ChoiceOptionRequest> options) {
        return IntStream.range(0, options.size())
                .mapToObj(i -> OptionChoice.builder()
                        .question(question)
                        .text(options.get(i).getText())
                        .isCorrect(options.get(i).isCorrect())
                        .optionOrder(i + 1)
                        .build())
                .toList();
    }
}
