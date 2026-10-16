# API Documentation — Category, Quiz & Attempt Controllers

Base URL: `/api/v1`

All endpoints require a valid JWT `Authorization: Bearer <token>` header unless noted otherwise.

---

## Authentication

All endpoints are **authenticated**. Include the JWT access token in every request:

```
Authorization: Bearer <access_token>
```

---

## Category Controller

Base path: `/api/v1/category`

### `POST /api/v1/category`

Creates a new category for the authenticated user. Optionally associates the new category with existing quizzes.

**Request body** — `CreateCategoryRequest`

```json
{
  "name": "Mathematics",
  "description": "Quizzes related to math topics",
  "color": "#6366F1",
  "quizIds": [3, 7, 12]
}
```

#### `CreateCategoryRequest`

| Field         | Type      | Required | Description                                                          |
|--------------|-----------|----------|----------------------------------------------------------------------|
| `name`       | `String`  | yes      | Category name. Must be unique (case-insensitive) per user.           |
| `description`| `String`  | no       | Optional description                                                 |
| `color`      | `String`  | no       | Display colour (e.g. hex code)                                       |
| `quizIds`    | `Long[]`  | no       | IDs of existing quizzes to add this category to. All must belong to the authenticated user. |

**Response `201 Created`** — `CategoryResponse`

```json
{
  "id": 5,
  "name": "Mathematics",
  "description": "Quizzes related to math topics",
  "color": "#6366F1",
  "existingQuizIds": [3, 7, 12]
}
```

| Field            | Type      | Description                                                                 |
|-----------------|-----------|-----------------------------------------------------------------------------|
| `id`            | `Long`    | Category ID                                                                 |
| `name`          | `String`  | Category name                                                               |
| `description`   | `String`  | Category description                                                        |
| `color`         | `String`  | Display colour                                                              |
| `existingQuizIds`| `Long[]` | IDs of quizzes associated with this category. `null` if none were provided. |

**Error responses**

| Status | Scenario                                                   |
|--------|------------------------------------------------------------|
| `400`  | `name` is blank                                            |
| `404`  | Any quiz ID in `quizIds` does not exist or belongs to another user |
| `409`  | A category with the same name already exists for this user |

---

### `GET /api/v1/category`

Returns all categories belonging to the authenticated user, sorted alphabetically by name.

**Response `200 OK`**

```json
[
  {
    "id": 5,
    "name": "Mathematics",
    "description": "Quizzes related to math topics",
    "color": "#6366F1"
  }
]
```

---

## Quiz Controller

Base path: `/api/v1/quiz`

### `POST /api/v1/quiz`

Creates a new quiz with questions for the authenticated user.

**Request body** — `CreateQuizRequest`

```json
{
  "title": "Spring Boot Basics",
  "description": "A quiz about Spring Boot fundamentals.",
  "timeLimit": 600,
  "categoryIds": [3],
  "icon": "📘",
  "questions": [
    {
      "type": "SINGLE_CHOICE",
      "questionText": "What annotation starts a Spring Boot app?",
      "points": 1,
      "choiceOptions": [
        { "text": "@SpringBootApplication", "isCorrect": true },
        { "text": "@EnableAutoConfiguration", "isCorrect": false }
      ]
    },
    {
      "type": "TRUE_FALSE",
      "questionText": "Spring Boot requires an XML configuration file.",
      "points": 1,
      "correctAnswer": false
    },
    {
      "type": "TEXT_INPUT",
      "questionText": "What does JPA stand for?",
      "points": 1,
      "textConfig": {
        "correctAnswer": "Java Persistence API",
        "reviewType": "AUTOMATIC"
      }
    },
    {
      "type": "MATCHING",
      "questionText": "Match the annotation to its purpose.",
      "points": 2,
      "matchPairs": [
        { "leftSide": "@Entity", "rightSide": "Marks a JPA entity" },
        { "leftSide": "@Service", "rightSide": "Marks a service bean" }
      ]
    },
    {
      "type": "ORDERING",
      "questionText": "Order the HTTP methods by their typical CRUD mapping.",
      "points": 3,
      "orderItems": [
        { "text": "POST" },
        { "text": "GET" },
        { "text": "PUT" },
        { "text": "DELETE" }
      ]
    },
    {
      "type": "FLASHCARD",
      "questionText": "What is the time complexity of binary search?",
      "points": 1,
      "flashcardBackText": "O(log n)"
    }
  ]
}
```

#### `CreateQuizRequest`

| Field           | Type                | Required | Description                                              |
|----------------|---------------------|----------|----------------------------------------------------------|
| `title`        | `String`            | yes      | Quiz title                                               |
| `description`  | `String`            | no       | Quiz description                                         |
| `timeLimit`    | `Integer`           | no       | Time limit in seconds, omit for no limit                 |
| `categoryIds`  | `Long[]`            | no       | IDs of existing categories to associate with the quiz    |
| `icon`         | `String`            | no       | Icon string (emoji or URL)                               |
| `questions`    | `QuestionRequest[]` | yes      | Ordered list of questions (at least 1)                   |

#### `QuestionRequest`

| Field               | Type                    | Required              | Description                                                    |
|--------------------|-------------------------|-----------------------|----------------------------------------------------------------|
| `type`             | `QuestionType`          | yes                   | See [Question Types](#question-types)                          |
| `questionText`     | `String`                | yes                   | Question body text                                             |
| `imagePath`        | `String`                | no                    | Optional image URL/path                                        |
| `points`           | `int`                   | yes (> 0)             | Points this question is worth                                  |
| `choiceOptions`    | `ChoiceOptionRequest[]` | `SINGLE_CHOICE`, `MULTIPLE_CHOICE` | At least 2 options required        |
| `correctAnswer`    | `Boolean`               | `TRUE_FALSE`          | `true` if "True" is correct, `false` if "False" is correct. Options are auto-generated. |
| `textConfig`       | `TextConfigRequest`     | `TEXT_INPUT`          | Review type and optional correct answer                        |
| `matchPairs`       | `MatchPairRequest[]`    | `MATCHING`            | At least 2 pairs required                                      |
| `orderItems`       | `OrderItemRequest[]`    | `ORDERING`            | At least 2 items required. Correct order = submission order.   |
| `flashcardBackText`| `String`                | `FLASHCARD`           | Back side of the flashcard                                     |

#### `ChoiceOptionRequest`

| Field      | Type      | Description                            |
|-----------|-----------|----------------------------------------|
| `text`    | `String`  | Option label                           |
| `isCorrect`| `boolean`| Whether this option is the correct one |

> `SINGLE_CHOICE` must have exactly 1 correct option. `MULTIPLE_CHOICE` must have at least 1.

#### `TextConfigRequest`

| Field           | Type             | Description                                                         |
|----------------|------------------|---------------------------------------------------------------------|
| `reviewType`   | `TextReviewType` | `AUTOMATIC` (compare text) or `MANUAL` (user self-assesses)        |
| `correctAnswer`| `String`         | Required for `AUTOMATIC` review; optional for `MANUAL`             |

#### `MatchPairRequest`

| Field      | Type     | Description          |
|-----------|----------|----------------------|
| `leftSide` | `String` | Left column text     |
| `rightSide`| `String` | Right column text    |

#### `OrderItemRequest`

| Field  | Type     | Description                                              |
|--------|----------|----------------------------------------------------------|
| `text` | `String` | Item label. Correct position = index in the request list.|

**Response `201 Created`** — `QuizBasicResponse`

```json
{
  "id": 1,
  "title": "Spring Boot Basics",
  "icon": "📘",
  "timeLimit": true,
  "categories": [
    { "id": 3 }
  ],
  "questionCount": 6
}
```

---

### `GET /api/v1/quiz/allQuizzes`

Returns a summary list of all quizzes belonging to the authenticated user.

**Aliases:** `/api/v1/quiz/allQuizes` (legacy spelling also accepted)

**Response `200 OK`**

```json
[
  {
    "id": 1,
    "title": "Spring Boot Basics",
    "icon": "📘",
    "timeLimit": true,
    "categories": [
      { "id": 3 }
    ],
    "questionCount": 10
  }
]
```

| Field           | Type              | Description                                           |
|----------------|-------------------|-------------------------------------------------------|
| `id`           | `Long`            | Quiz ID                                               |
| `title`        | `String`          | Quiz title                                            |
| `icon`         | `String`          | Icon string (emoji or URL)                            |
| `timeLimit`    | `Boolean`         | `true` if a time limit is set, `false` otherwise      |
| `categories`   | `CategoryInfo[]`  | List of category references (id only)                 |
| `questionCount`| `int`             | Total number of questions                             |

---

### `GET /api/v1/quiz/{id}`

Returns the full details of a quiz, including all questions and their options. Also includes the authenticated user's previous attempt score and average score if available.

**Path parameters**

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | `Long` | Quiz ID     |

**Response `200 OK`**

```json
{
  "id": 1,
  "title": "Spring Boot Basics",
  "description": "A quiz about Spring Boot fundamentals.",
  "timeLimit": 600,
  "shuffleQuestions": false,
  "totalPoints": 20,
  "questionCount": 4,
  "categories": [{ "id": 3 }],
  "createdAt": "2025-01-10T12:00:00",
  "updatedAt": "2025-03-01T09:30:00",
  "icon": "📘",
  "questions": [
    {
      "id": 101,
      "questionOrder": 1,
      "type": "SINGLE_CHOICE",
      "questionText": "What annotation starts a Spring Boot app?",
      "imagePath": null,
      "points": 5,
      "choiceOptions": [
        { "id": 201, "text": "@SpringBootApplication", "optionOrder": 1 },
        { "id": 202, "text": "@EnableAutoConfiguration", "optionOrder": 2 }
      ],
      "matchPairs": null,
      "orderItems": null,
      "textConfig": null,
      "flashcard": null
    }
  ]
}
```

#### `QuizDetailedResponse`

| Field                          | Type             | Description                                                   |
|-------------------------------|------------------|---------------------------------------------------------------|
| `id`                          | `Long`           | Quiz ID                                                       |
| `title`                       | `String`         | Quiz title                                                    |
| `description`                 | `String`         | Quiz description                                              |
| `timeLimit`                   | `Integer`        | Time limit in seconds, `null` if no limit                     |
| `shuffleQuestions`            | `boolean`        | Whether questions are randomised                              |
| `totalPoints`                 | `int`            | Sum of all question points                                    |
| `questionCount`               | `int`            | Number of questions                                           |
| `categories`                  | `CategoryInfo[]` | Category references                                           |
| `createdAt`                   | `LocalDateTime`  | ISO-8601 creation timestamp                                   |
| `updatedAt`                   | `LocalDateTime`  | ISO-8601 last-updated timestamp                               |
| `icon`                        | `String`         | Icon string                                                   |
| `questions`                   | `QuestionInfo[]` | Ordered list of questions                                     |

#### `QuestionInfo`

| Field           | Type                  | Description                                                   |
|----------------|-----------------------|---------------------------------------------------------------|
| `id`           | `Long`                | Question ID                                                   |
| `questionOrder`| `int`                 | Display order (1-based)                                       |
| `type`         | `QuestionType`        | See [Question Types](#question-types)                         |
| `questionText` | `String`              | Question body text                                            |
| `imagePath`    | `String`              | Optional image URL/path, `null` if none                       |
| `points`       | `int`                 | Points this question is worth                                 |
| `choiceOptions`| `ChoiceOptionInfo[]`  | Populated for `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`|
| `matchPairs`   | `MatchPairInfo[]`     | Populated for `MATCHING`                                      |
| `orderItems`   | `OrderItemInfo[]`     | Populated for `ORDERING`                                      |
| `textConfig`   | `TextConfigInfo`      | Populated for `TEXT_INPUT`                                    |
| `flashcard`    | `FlashcardInfo`       | Populated for `FLASHCARD`                                     |

> All type-specific fields (`choiceOptions`, `matchPairs`, etc.) are `null` for other question types.

#### `ChoiceOptionInfo`

| Field         | Type     | Description                    |
|--------------|----------|--------------------------------|
| `id`         | `Long`   | Option ID                      |
| `text`       | `String` | Option label                   |
| `optionOrder`| `int`    | Display order                  |

> Correct answers are **not** exposed in this DTO (they are only revealed in the review endpoint).

#### `MatchPairInfo`

| Field       | Type     | Description              |
|------------|----------|--------------------------|
| `id`       | `Long`   | Pair ID                  |
| `leftSide` | `String` | Left column text         |
| `rightSide`| `String` | Right column text        |
| `pairOrder`| `int`    | Display order            |

#### `OrderItemInfo`

| Field  | Type     | Description       |
|--------|----------|-------------------|
| `id`   | `Long`   | Item ID           |
| `text` | `String` | Item label        |

> Correct positions are **not** exposed here.

#### `TextConfigInfo`

| Field           | Type             | Description                                                   |
|----------------|------------------|---------------------------------------------------------------|
| `review`       | `TextReviewType` | `MANUAL` or `AUTOMATIC`                                       |
| `correctAnswer`| `String`         | Correct answer (exposed so the client can do client-side auto-review) |

#### `FlashcardInfo`

| Field      | Type     | Description         |
|-----------|----------|---------------------|
| `backText` | `String` | Back side of the flashcard |

---

## Image Controller

Base path: `/api/v1/images`

### `POST /api/v1/images`

Uploads an image file and returns the stored filename. Use the returned filename as the `imagePath` value when creating a quiz question.

**Request** — `multipart/form-data`

| Field  | Type   | Required | Description          |
|--------|--------|----------|----------------------|
| `file` | `File` | yes      | Image file to upload |

**Accepted MIME types:** `image/jpeg`, `image/png`, `image/gif`, `image/webp`

**Max file size:** 10 MB

**Response `201 Created`**

```json
{
  "filename": "550e8400-e29b-41d4-a716-446655440000.jpg"
}
```

| Field      | Type     | Description                                              |
|-----------|----------|----------------------------------------------------------|
| `filename` | `String` | Stored filename. Pass this as `imagePath` in question requests. |

**Error responses**

| Status | Scenario                                        |
|--------|-------------------------------------------------|
| `400`  | File is empty or MIME type is not an image type |

---

### `GET /api/v1/images/{filename}`

Returns the raw image binary for the given filename.

**Path parameters**

| Parameter  | Type     | Description                   |
|-----------|----------|-------------------------------|
| `filename` | `String` | Filename returned by the upload endpoint |

**Response `200 OK`**

Raw image bytes with the appropriate `Content-Type` header (`image/jpeg`, `image/png`, `image/gif`, or `image/webp`).

**Error responses**

| Status | Scenario                             |
|--------|--------------------------------------|
| `400`  | Filename contains illegal characters |
| `404`  | Image not found                      |

---

## Attempt Controller

Base path: `/api/v1/attempt`

### `POST /api/v1/attempt/submitAttempt`

Submits a completed quiz attempt and returns the score result.

**Request body** — `SubmitQuizRequest`

```json
{
  "quizId": 1,
  "timeSpent": 243,
  "idBasedAnswers": [
    {
      "questionId": 101,
      "selectedOptionIds": [201],
      "timeSpent": 12
    },
    {
      "questionId": 102,
      "selectedOptionIds": [205, 207],
      "timeSpent": 18
    }
  ],
  "textBasedAnswers": [
    {
      "questionId": 103,
      "submittedAnswer": "Spring Boot",
      "userMarkedCorrect": null,
      "timeSpent": 15
    }
  ],
  "matchBasedAttemptAnswer": [
    {
      "questionId": 104,
      "matchedPairs": [
        { "leftId": 301, "rightId": 401 },
        { "leftId": 302, "rightId": 402 }
      ],
      "timeSpent": 20
    }
  ]
}
```

#### `SubmitQuizRequest`

| Field                    | Type                              | Description                                                   |
|-------------------------|-----------------------------------|---------------------------------------------------------------|
| `quizId`                | `Long`                            | ID of the quiz being submitted                                |
| `timeSpent`             | `Integer`                         | Seconds elapsed during the attempt                            |
| `idBasedAnswers`        | `IdBasedAnswerSubmission[]`       | Answers for choice/ordering/flashcard questions               |
| `textBasedAnswers`      | `TextBasedAnswerSubmission[]`     | Answers for text-input questions                              |
| `matchBasedAttemptAnswer` | `MatchBasedAnswerSubmission[]`  | Answers for matching questions                                |

**Unanswered submission contract**

| Field | Unanswered value | Invalid value |
|-------|------------------|---------------|
| `selectedOptionIds` | empty array | `null` |
| `submittedAnswer` | empty string | `null` |

For id-based answers, an empty `selectedOptionIds` array means the user skipped that question and the attempt scores `0` for it. This applies to `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`, `ORDERING`, and `FLASHCARD`.

For `TEXT_INPUT`, an empty `submittedAnswer` string means the user skipped that question and the attempt scores `0` for it. Blank text is accepted and is not sent to the automatic reviewer.

`MATCHING` remains required and must contain at least one matched pair.

#### `IdBasedAnswerSubmission`

Used for `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`, `ORDERING`, and `FLASHCARD` questions.

| Field               | Type     | Description                                                            |
|--------------------|----------|------------------------------------------------------------------------|
| `questionId`       | `Long`   | ID of the question                                                     |
| `selectedOptionIds`| `Long[]` | IDs of selected/ordered option(s). For `ORDERING`, provide option IDs in the user's chosen order. For `FLASHCARD`, provide the option ID to indicate recall. An empty array means the question was skipped. `null` is invalid. |
| `timeSpent`        | `Integer`| Seconds spent on this question (optional)                              |

#### `TextBasedAnswerSubmission`

Used for `TEXT_INPUT` questions.

| Field               | Type      | Description                                                            |
|--------------------|-----------|------------------------------------------------------------------------|
| `questionId`       | `Long`    | ID of the question                                                     |
| `submittedAnswer`  | `String`  | The user's typed answer. An empty string means the question was skipped. `null` is invalid. |
| `userMarkedCorrect`| `Boolean` | For `MANUAL` review: `true`/`false` as self-assessed by user. `null` for `AUTOMATIC`. |
| `timeSpent`        | `Integer` | Seconds spent on this question (optional)                              |

#### `MatchBasedAnswerSubmission`

Used for `MATCHING` questions.

| Field          | Type                | Description                        |
|---------------|---------------------|------------------------------------|
| `questionId`  | `Long`              | ID of the question                 |
| `matchedPairs`| `MatchedPairEntry[]`| The user's matched pairs           || `timeSpent`   | `Integer`           | Seconds spent on this question (optional) |
#### `MatchedPairEntry`

| Field     | Type   | Description                              |
|---------|--------|------------------------------------------|
| `leftId` | `Long` | ID of the left-side option               |
| `rightId`| `Long` | ID of the right-side option paired to it |

**Response `200 OK`** — `QuizResultResponse`

```json
{
  "attemptId": 55,
  "scorePercentage": 80.0,
  "previousAttemptScorePercentage": 60.0,
  "averageScorePercentage": 70.0
}
```

#### `QuizResultResponse`

| Field                          | Type     | Description                                             |
|-------------------------------|----------|---------------------------------------------------------|
| `attemptId`                   | `Long`   | ID of the created attempt                               |
| `scorePercentage`             | `double` | Score as a percentage (0–100)                           |
| `previousAttemptScorePercentage` | `Double`| Score of the attempt before this one, `null` if first  |
| `averageScorePercentage`      | `Double` | Average of all previous attempts, `null` if first      |

---

### `GET /api/v1/attempt/all`

Returns a paginated list of all submitted attempts by the **authenticated user** across all quizzes, ordered by `submittedAt` descending (newest first).

**Query parameters**

| Parameter | Type      | Default | Description              |
|-----------|-----------|---------|--------------------------|
| `page`    | `integer` | `0`     | Zero-based page index    |
| `size`    | `integer` | `20`    | Number of items per page |
| `sort`    | `string`  | —       | Sort field and direction |

**Response `200 OK`** — `Page<AttemptSummaryResponse>`

```json
{
  "content": [
    {
      "id": 55,
      "submittedAt": "2025-04-17T14:22:00",
      "scorePercentage": 80.0,
      "timeSpent": 243,
      "quizId": 1,
      "quizTitle": "Spring Boot Basics"
    }
  ],
  "totalElements": 57,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

#### `AttemptSummaryResponse`

| Field              | Type            | Description                            |
|-------------------|-----------------|----------------------------------------|
| `id`              | `Long`          | Attempt ID                             |
| `submittedAt`     | `LocalDateTime` | ISO-8601 submission timestamp          |
| `scorePercentage` | `double`        | Score as a percentage (0–100)          |
| `timeSpent`       | `Integer`       | Seconds spent on the attempt           |
| `quizId`          | `Long`          | ID of the quiz this attempt belongs to |
| `quizTitle`       | `String`        | Title of the quiz                      |

---

### `GET /api/v1/attempt/byQuiz/{quizId}/all`

Returns a list of all submitted attempts on a given quiz across **all users**, ordered by `submittedAt` descending.

**Path parameters**

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `quizId`  | `Long` | Quiz ID     |

**Response `200 OK`**

```json
[
  {
    "id": 55,
    "submittedAt": "2025-04-17T14:22:00",
    "scorePercentage": 80.0,
    "timeSpent": 243
  }
]
```

Returns `AttemptBasicResponse` — see schema below.

---

### `GET /api/v1/attempt/byQuiz/{quizId}`

Returns a list of all attempts the **authenticated user** has made on a given quiz.

**Path parameters**

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `quizId`  | `Long` | Quiz ID     |

**Response `200 OK`**

```json
[
  {
    "id": 55,
    "submittedAt": "2025-04-17T14:22:00",
    "scorePercentage": 80.0,
    "timeSpent": 243
  }
]
```

#### `AttemptBasicResponse`

| Field              | Type            | Description                            |
|-------------------|-----------------|----------------------------------------|
| `id`              | `Long`          | Attempt ID                             |
| `submittedAt`     | `LocalDateTime` | ISO-8601 submission timestamp          |
| `scorePercentage` | `double`        | Score as a percentage (0–100)          |
| `timeSpent`       | `Integer`       | Seconds spent on the attempt           |

---

### `GET /api/v1/attempt/{attemptId}/review`

Returns a detailed per-question review of a specific attempt, including correct answers and what the user submitted.

**Path parameters**

| Parameter   | Type   | Description  |
|-------------|--------|--------------|
| `attemptId` | `Long` | Attempt ID   |

**Response `200 OK`** — `AttemptReviewResponse`

```json
{
  "attemptId": 55,
  "quizId": 1,
  "quizTitle": "Spring Boot Basics",
  "scorePercentage": 80.0,
  "score": 16.0,
  "maxScore": 20,
  "timeSpent": 243,
  "submittedAt": "2025-04-17T14:22:00",
  "questions": [
    {
      "questionId": 101,
      "questionOrder": 1,
      "type": "SINGLE_CHOICE",
      "questionText": "What annotation starts a Spring Boot app?",
      "imagePath": null,
      "points": 5,
      "pointsAwarded": 5,
      "scorePercentage": 100.0,
      "choiceOptions": [
        { "id": 201, "text": "@SpringBootApplication", "optionOrder": 1, "correct": true, "selected": true },
        { "id": 202, "text": "@EnableAutoConfiguration", "optionOrder": 2, "correct": false, "selected": false }
      ],
      "matchPairs": null,
      "orderItems": null,
      "textAnswer": null,
      "flashcard": null
    }
  ]
}
```

#### `AttemptReviewResponse`

| Field              | Type              | Description                            |
|-------------------|-------------------|----------------------------------------|
| `attemptId`       | `Long`            | Attempt ID                             |
| `quizId`          | `Long`            | Quiz ID                                |
| `quizTitle`       | `String`          | Quiz title                             |
| `scorePercentage` | `double`          | Score as a percentage (0–100)          |
| `score`           | `double`          | Raw points scored                      |
| `maxScore`        | `int`             | Maximum possible points                |
| `timeSpent`       | `Integer`         | Seconds spent on the attempt           |
| `submittedAt`     | `LocalDateTime`   | ISO-8601 submission timestamp          |
| `questions`       | `QuestionReview[]`| Per-question review, sorted by order   |

#### `QuestionReview`

| Field           | Type                    | Description                                                         |
|----------------|-------------------------|---------------------------------------------------------------------|
| `questionId`   | `Long`                  | Question ID                                                         |
| `questionOrder`| `int`                   | Display order                                                       |
| `type`         | `QuestionType`          | See [Question Types](#question-types)                               |
| `questionText` | `String`                | Question body text                                                  |
| `imagePath`    | `String`                | Optional image path, `null` if none                                 |
| `points`       | `int`                   | Points the question was worth                                       |
| `pointsAwarded`| `double`                | Points the user actually received                                   |
| `scorePercentage`| `double`              | Score percentage for this question (0–100)                          |
| `choiceOptions`| `ChoiceOptionReview[]`  | Populated for `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`      |
| `matchPairs`   | `MatchPairReview[]`     | Populated for `MATCHING`                                            |
| `orderItems`   | `OrderItemReview[]`     | Populated for `ORDERING`                                            |
| `textAnswer`   | `TextAnswerReview`      | Populated for `TEXT_INPUT`                                          |
| `flashcard`    | `FlashcardReview`       | Populated for `FLASHCARD`                                           |

#### `ChoiceOptionReview`

| Field         | Type      | Description                          |
|--------------|-----------|--------------------------------------|
| `id`         | `Long`    | Option ID                            |
| `text`       | `String`  | Option label                         |
| `optionOrder`| `int`     | Display order                        |
| `correct`    | `boolean` | Whether this option is correct       |
| `selected`   | `boolean` | Whether the user selected this option|

#### `MatchPairReview`

| Field              | Type      | Description                                      |
|-------------------|-----------|--------------------------------------------------|
| `id`              | `Long`    | Left-side option ID                              |
| `leftSide`        | `String`  | Left column text                                 |
| `correctRightSide`| `String`  | The correct right-side text                      |
| `userRightId`     | `Long`    | The right-side option ID the user chose          |
| `userRightSide`   | `String`  | The right-side text the user chose               |
| `correct`         | `boolean` | Whether this pair was matched correctly          |

#### `OrderItemReview`

| Field            | Type      | Description                        |
|-----------------|-----------|------------------------------------|
| `id`            | `Long`    | Item ID                            |
| `text`          | `String`  | Item label                         |
| `correctPosition`| `int`    | The correct 0-based position       |
| `userPosition`  | `int`     | The position the user placed it at |
| `correct`       | `boolean` | Whether this item was placed correctly |

#### `TextAnswerReview`

| Field           | Type             | Description                                          |
|----------------|------------------|------------------------------------------------------|
| `reviewType`   | `TextReviewType` | `MANUAL` or `AUTOMATIC`                              |
| `correctAnswer`| `String`         | The expected correct answer                          |
| `userAnswer`   | `String`         | The answer the user submitted                        |
| `scorePercentage`| `double`       | Score percentage for this answer (0–100)             |

#### `FlashcardReview`

| Field           | Type      | Description                                             |
|----------------|-----------|---------------------------------------------------------|
| `backText`     | `String`  | The back side (answer) of the flashcard                 |
| `markedCorrect`| `boolean` | Whether the user marked the flashcard as recalled       |

---

### `GET /api/v1/attempt/statistics/byQuizId/{quizId}`

Returns aggregated statistics for all of the authenticated user's submitted attempts on a given quiz. Results are in chronological order (oldest first).

**Path parameters**

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `quizId`  | `Long` | Quiz ID     |

**Response `200 OK`** — `QuizStatsAnswer`

```json
{
  "attemptTimes": [180, 210, 195],
  "scorePercentages": [60.0, 75.0, 85.0],
  "previousAttemptScorePercentage": 75.0,
  "questionAttempts": [
    {
      "questionId": 101,
      "averageQuestionAttemptTime": 45,
      "averageQuestionScorePercentage": 80.0
    }
  ]
}
```

#### `QuizStatsAnswer`

| Field                             | Type                  | Description                                                              |
|----------------------------------|-----------------------|--------------------------------------------------------------------------|
| `attemptTimes`                   | `Integer[]`           | Seconds spent per attempt in chronological order                         |
| `scorePercentages`               | `double[]`            | Score percentage per attempt in chronological order                      |
| `previousAttemptScorePercentage` | `Double`              | Score of the second-to-last attempt, `null` if fewer than 2 attempts     |
| `questionAttempts`               | `AttemptAnswerInfo[]` | Per-question averages across all attempts                                |

#### `AttemptAnswerInfo`

| Field                             | Type      | Description                                                        |
|----------------------------------|-----------|--------------------------------------------------------------------|
| `questionId`                     | `Long`    | Question ID                                                        |
| `averageQuestionAttemptTime`     | `Integer` | Average seconds spent on this question, `null` if no time data     |
| `averageQuestionScorePercentage` | `double`  | Average score percentage for this question across all attempts     |

---

### `GET /api/v1/attempt/statistics/byCategory/{categoryId}`

Returns aggregated statistics for all of the authenticated user's submitted attempts across every quiz in a given category. Results are combined from all quizzes belonging to the category and ordered chronologically (oldest first).

**Path parameters**

| Parameter    | Type   | Description  |
|-------------|--------|--------------|
| `categoryId` | `Long` | Category ID  |

**Response `200 OK`** — `CategoryStatsResponse`

```json
{
  "attemptTimes": [180, 210, 195, 240],
  "scorePercentages": [60.0, 75.0, 85.0, 70.0],
  "averageScorePercentage": 72.5,
  "medianScorePercentage": 72.5,
  "averageAttemptTime": 206.25,
  "medianAttemptTime": 202.5
}
```

#### `CategoryStatsResponse`

| Field                    | Type        | Description                                                                              |
|-------------------------|-------------|------------------------------------------------------------------------------------------|
| `attemptTimes`          | `Integer[]` | Seconds spent per attempt in chronological order, across all quizzes in the category     |
| `scorePercentages`      | `double[]`  | Score percentage per attempt in chronological order, across all quizzes in the category  |
| `averageScorePercentage`| `Double`    | Mean score percentage across all combined attempts. `null` if no attempts.               |
| `medianScorePercentage` | `Double`    | Median score percentage across all combined attempts. `null` if no attempts.             |
| `averageAttemptTime`    | `Double`    | Mean attempt time in seconds. `null` if no attempts with recorded time.                  |
| `medianAttemptTime`     | `Double`    | Median attempt time in seconds. `null` if no attempts with recorded time.                |

**Error responses**

| Status | Scenario                                                              |
|--------|-----------------------------------------------------------------------|
| `404`  | Category not found or does not belong to the authenticated user       |

---
### `GET /api/v1/attempt/dashboard`

Returns dashboard statistics for the authenticated user. Data covers the **past 7 days** for the summary metrics and recent quizzes, and the **current calendar month** for the activity heatmap.

**Response `200 OK`** — `DashboardResponse`

```json
{
  "totalTimeSpent": 1840,
  "submittedQuizzesCount": 12,
  "averageScorePercentage": 73.5,
  "recentQuizzes": [
    {
      "id": 1,
      "title": "Spring Boot Basics",
      "icon": "📘",
      "timeLimit": true,
      "categories": [{ "id": 3 }],
      "questionCount": 6
    }
  ],
  "monthlyActivity": {
    "2026-06-01": 3,
    "2026-06-14": 1,
    "2026-06-16": 5
  }
}
```

#### `DashboardResponse`

| Field                    | Type                        | Description                                                                                       |
|-------------------------|-----------------------------|---------------------------------------------------------------------------------------------------|
| `totalTimeSpent`        | `int`                       | Total seconds spent on quizzes in the past 7 days. `0` if no attempts.                           |
| `submittedQuizzesCount` | `int`                       | Number of quiz attempts submitted in the past 7 days.                                             |
| `averageScorePercentage`| `Double`                    | Average score percentage across all attempts in the past 7 days. `null` if no attempts.           |
| `recentQuizzes`         | `QuizBasicResponse[]`       | Up to 5 distinct quizzes submitted in the past 7 days, deduplicated by quiz ID, most recent first.|
| `monthlyActivity`       | `Map<LocalDate, Integer>`   | Date → submission count for every day in the current calendar month that had at least one attempt. Keys are ISO-8601 date strings (e.g. `"2026-06-14"`), sorted ascending. Days with no submissions are omitted. |

---
## Shared Types

### `CategoryInfo`

| Field | Type   | Description   |
|-------|--------|---------------|
| `id`  | `Long` | Category ID   |

### Question Types

| Value             | Description                              | Answer submission type   |
|------------------|------------------------------------------|--------------------------|
| `SINGLE_CHOICE`  | One correct option from a list           | `IdBasedAnswerSubmission` |
| `MULTIPLE_CHOICE`| Multiple correct options from a list     | `IdBasedAnswerSubmission` |
| `TRUE_FALSE`     | Binary choice (true/false option IDs)    | `IdBasedAnswerSubmission` |
| `ORDERING`       | Place items in the correct sequence      | `IdBasedAnswerSubmission` (IDs in user's chosen order) |
| `FLASHCARD`      | Self-assessed recall card                | `IdBasedAnswerSubmission` |
| `TEXT_INPUT`     | Free-text answer                         | `TextBasedAnswerSubmission` |
| `MATCHING`       | Match left items to right items          | `MatchBasedAnswerSubmission` |

### `TextReviewType`

| Value       | Description                                                        |
|------------|---------------------------------------------------------------------|
| `AUTOMATIC`| Graded automatically by comparing the submitted text to `correctAnswer` |
| `MANUAL`   | User self-assesses correctness via `userMarkedCorrect`             |

---

## Error Responses

All endpoints may return the following error shapes:

| Status | Scenario                                      |
|--------|-----------------------------------------------|
| `401`  | Missing, expired, or invalid JWT token        |
| `403`  | Authenticated but not authorised for resource |
| `404`  | Quiz or attempt not found                     |
| `400`  | Malformed request body                        |
