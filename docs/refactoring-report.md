# Refactoring Report

## Step 1: Characterization Tests

### Why tests first

Writing tests before any refactoring was the first priority. Without them, there is no way to know if a refactoring step silently breaks existing behavior. The tests act as a safety net for every change that follows.

### What we characterized

- Color, number, and action-type matching rules
- Wild and wild draw four (always legal)
- Skip, reverse, draw two effects
- Drawing from deck, reshuffle from discard, fallback wild when both empty
- Scoring: opponent hand values summed on win
- Bot card priority: draw two > skip > number > wild
- Bot color choice: picks most common color in hand

### Quirks captured

- **Bot color defaults to R when hand has no colored cards.** All color counts are 0, and the first condition `r >= y && r >= g && r >= b` evaluates true, so R wins by tie-break order. This is the current behavior, not a bug fix.
- **Invalid index input gives a penalty card and skips the turn.** The main loop checks `chosen >= hand.size()` and draws a penalty card without playing anything.

### Worst design problems found so far

- The legal-play check is written out three times: once in `isLegal()`, once in `chooseBotCard()`, and once inline in the main turn loop. `isLegal()` exists but is not used everywhere.
- The entire game — state, rules, console I/O, bot logic, scoring — lives in one 535-line class with global static fields.

## Step 2: Eliminate Duplicated Legal-Play Logic in chooseBotCard()

### What changed

`chooseBotCard()` had the 5-line legality check copy-pasted three times across its four loops. Each copy was identical to the existing `isLegal()` method but was not calling it. Each block was replaced with a single call to `isLegal(card, upCard, calledColor)`.

### Why this step first

This was the lowest-risk refactoring available: `isLegal()` already existed with the correct logic, the tests already covered bot card selection, and the change was purely mechanical substitution. No behavior could change because the logic is identical — only the duplication is removed.

### Result

`chooseBotCard()` went from 37 lines to 20. There is now one authoritative place for legality rules.
