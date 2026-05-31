# Extension Readiness

## Which Extension The Design Supports Best

**Adding a smarter bot strategy** is the most natural extension for the current design.

`BotStrategy` is a standalone class with two static methods:

```
chooseCard(ArrayList<String> hand, String upCard, String calledColor) → int
chooseColor(ArrayList<String> hand) → String
```

Both methods take all their inputs as parameters and touch no global state. A smarter strategy — one that counts cards the opponent draws, avoids giving opponents a +4, or prefers playing action cards late in the game — can be implemented entirely inside a new class or by adding a second implementation of these two methods. `Main` calls `BotStrategy` by name at exactly two call sites; pointing those at a different class requires changing two lines.

## Where The Change Would Be Implemented

1. **Create a new strategy class** (e.g. `AggressiveBotStrategy.java`) with the same two method signatures as `BotStrategy`.
2. **Update the two call sites in `Main.runTurnLoop()`** that currently read `BotStrategy.chooseCard(...)` and `BotStrategy.chooseColor(...)`.
3. **No changes needed** in `Card`, `GameState`, `Display`, or `Player`. The strategy is fully isolated from the rest of the game.

If per-player strategies were needed (some bots easy, some hard), the `Player` class could hold a strategy reference and the call site in `Main` would dispatch through it.

## What Still Makes Change Difficult

- **No strategy interface exists.** Both `BotStrategy` methods are static, so there is no type to program against. To support multiple strategies, an interface with `chooseCard` and `chooseColor` would need to be introduced first, and `Player` would need a strategy field. This is a small but real structural step before per-player strategies become clean.
- **`applyEffect()` is still in `Main`.** Adding a new card effect (e.g. a Swap Hands card) means editing the main controller. If the effect also requires new card types, `Card.rank()` and `Card.points()` must be updated too. There is no plugin point for new effects; each one is a manual addition in multiple places.
- **`Display` cannot be swapped without a source change.** All output goes through `Display` static methods, which is a large improvement over the original. But because there is no interface, replacing the CLI view with a different output format means either modifying `Display` or copying and renaming it. An interface or injectable view object would make this cleaner.
- **`GameState` is not injectable.** `Main.state` is a static field. Tests reset it directly in `@BeforeEach`. Code that relies on a single shared static state is harder to run in parallel or to test with multiple independent game instances.
