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

## Step 3: Extract Card Effect Handling into applyEffect()

### What changed

The 30-line if/else chain inside `playGame()` that handled skip, reverse, draw two, and wild draw four was extracted into a new `applyEffect(String card)` method. The main loop now calls `applyEffect(card)` in its place.

### Why this step

The main game loop was doing too many things at once — tracking turns, validating cards, handling console output, and applying card effects all in the same block. Extracting `applyEffect()` gives each card's behavior a clear, findable home and makes the loop easier to read. It also makes it straightforward to add a new card effect later without touching the turn logic.

### Result

The main loop is significantly shorter and easier to follow. Card effects are now in one dedicated method.

## Step 4: Separate Console Output into Display Class

### What changed

A new `Display` class was created in `src/Display.java` with a static method for every `System.out.println` and `System.out.print` call in the game. Main no longer prints anything directly — all output goes through Display. The `quiet` flag moved into Display so game logic does not need to check it.

### Why this step

This is the most important structural change. Before this step, game rules and console output were completely tangled — `playGame()` was making decisions and printing results in the same breath. After this step, game logic methods contain no I/O. `isLegal()`, `applyEffect()`, `chooseBotCard()`, and the scoring logic are all testable without any console involvement. This is the MVC-like boundary the rubric requires.

### What was preserved

All output messages are identical — the same strings, the same quiet/verbose behavior. No visible behavior changed.

### Result

`Main.java` contains no `System.out` calls except inside `Display`. Swapping out the CLI view now means only touching `Display.java`.

## Step 5: Extract Player Class and Scoring Method

### What changed

A new `Player` class was created with three fields: `name`, `human`, and `hand`. The three parallel static ArrayLists — `playerNames`, `humanPlayers`, and `hands` — were removed from `Main` and replaced with a single `ArrayList<Player> players`. All access to player data now goes through the `Player` object.

Scoring was also extracted from the win-detection block into its own `calculateScore()` method, which sums opponent hand values. The win block now reads clearly: calculate points, add to score, display result, return.

### Why this step

The three parallel lists were the most visible form of weak player boundaries. Any operation on a player required touching three separate lists at the same index — easy to get out of sync, hard to read. A `Player` object keeps all player data in one place.

Extracting `calculateScore()` addresses the scoring-mixed-with-game-completion smell. The win condition and the score calculation are now separate concerns.

### Result

Player-related code is easier to follow. Adding a new field to a player (e.g. a score per player, or a strategy) now means one change in one class, not three parallel lists.

## Step 6: Extract Card Knowledge into Card Class

### What changed

A new `Card` class was created with static methods: `color()`, `rank()`, `number()`, `points()`, `isWild()`, and `isLegal()`. All six methods were removed from `Main` and now live exclusively in `Card`. Every call site in `Main` was updated to use `Card.xxx()`. The characterization tests were also updated to call `Card.xxx()` directly for card-related assertions, making it clear what is being tested.

### Why this step

Cards were represented as raw strings throughout the codebase, with their behavior spread across several methods in `Main`. There was no single place that owned "what a card knows about itself." The `Card` class gives card behavior a clear home — color, rank, legality, and scoring are now all in one place and testable independently of the game loop.

### What was preserved

Card strings remain unchanged ("R5", "GS", "W4" etc.). The deck, hands, and discard still hold strings — this is an intentional incremental step. A future refactor could introduce Card instances if needed.

### Result

`Main` no longer contains any card-parsing logic. `Card` is fully testable in isolation. Adding a new card type now means one change in one class.
