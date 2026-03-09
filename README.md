# Advent of Code Kotlin

[Advent of Code][aoc] (AoC) is an annual programming challenge held every December since 2015.
Each of the 25 days presents a two-part puzzle; both parts share the same puzzle input but ask different questions.
This repository contains [Kotlin][kotlin] solutions for AoC from 2021 through 2025.

## Table of Contents

- [Overview](#overview)
- [Repository Structure](#repository-structure)
- [Architecture](#architecture)
  - [Solution Template](#solution-template)
  - [Library Utilities](#library-utilities)
  - [CLI Tools](#cli-tools)
- [Coverage](#coverage)
- [Getting Started](#getting-started)
  - [How to Fetch Input Data](#how-to-fetch-input-data)
  - [How to Prepare for a New Year](#how-to-prepare-for-a-new-year)
  - [How to Run the Solutions](#how-to-run-the-solutions)
- [Dependencies](#dependencies)
- [Philosophy](#philosophy)

---

## Overview

Solutions in this repository prioritize **readability**, **domain-specific naming**, and **functional programming** over raw performance.
The codebase is structured so that each day's solution is self-contained while sharing common utilities from the `lib` package.

---

## Repository Structure

```
aoc-kotlin/
├── build.gradle.kts      # Gradle build configuration (Kotlin DSL)
├── settings.gradle.kts   # Project settings (name: "Advent of Code")
├── gradlew / gradlew.bat # Gradle wrapper scripts (Unix/Windows)
└── src/
    ├── aoc21/            # 2021 solutions
    ├── aoc22/            # 2022 solutions
    ├── aoc23/            # 2023 solutions
    ├── aoc24/            # 2024 solutions
    ├── aoc25/            # 2025 solutions
    ├── lib/              # Shared utility library
    ├── tool/             # CLI tools (solve, fetch, prepare)
    └── data/             # Puzzle input files (gitignored)
```

Each year directory (`src/aocYY/`) contains one `DayXX.kt` file per solved day.
Input data lives in `src/data/aocYY/DayXX.txt` and is **not** committed to the repository (see [How to Fetch Input Data](#how-to-fetch-input-data)).

---

## Architecture

### Solution Template

Every day follows the same structure using the abstract `Solution<P, R>` base class from `lib/`:

```kotlin
@file:Suppress("PackageDirectoryMismatch")

package aoc24.day01

import lib.Solution

typealias Input  = List<List<Int>>   // Parsed input type
typealias Output = Int               // Answer type

private val solution = object : Solution<Input, Output>(2024, "Day01") {
  override fun parse(input: String): Input = ...    // Parse raw puzzle text
  override fun format(output: Output): String = "$output"
  override fun part1(input: Input): Output = ...    // Solve part 1
  override fun part2(input: Input): Output = ...    // Solve part 2
}

fun main() = solution.run()
```

When `solution.run()` is called it:
1. Reads the puzzle input from `src/data/aocYY/DayXX.txt`
2. Parses the input once (the result is shared by both parts)
3. Runs and times `part1` and `part2` independently
4. Prints each result and the elapsed time in milliseconds

### Library Utilities

The `lib/` package provides reusable modules that are shared across multiple days and years:

| Module | Description |
|--------|-------------|
| **Grid.kt** | `Point`, `Direction`, `Adjacency`, `Line`, `Path`, and `Grid<T>` — core data structures for 2D grid problems |
| **Parser.kt** | Monadic parser combinators (`Parsers`, `ParserCombinators`) for complex, structured input formats |
| **Bag.kt** | Multiset / bag data structure for efficient element counting (backed by `MutableMap<T, Int>`) |
| **Solution.kt** | Abstract base class defining the `parse → part1 / part2 → format → run` lifecycle |
| **Maths.kt** | GCD, LCM, modular arithmetic (`mod`), integer power (`pow`), prime sieve, and numeric predicates |
| **Collections.kt** | `histogram()`, `prefixes()`, `suffixes()`, `cumulativeSum()`, `transposed()`, `groupContiguousBy()`, and more |
| **Strings.kt** | Extension functions to extract integers from strings (e.g. `String.ints()`) |
| **Tuples.kt** | Extensions and DSL for `Pair` and `Triple`; also defines `Quadruple` and `Quintuple` |
| **Combinatorics.kt** | `permutations()`, `combinations()`, and Cartesian product helpers |
| **Ranges.kt** | Utility extensions for `IntRange` and `LongRange` |
| **IO.kt** | Input-reading utilities supporting both file and stdin sources |
| **Functional.kt** | Function composition helpers (`compose`, `pipe`) |
| **DP.kt** | `memoize {}` helper for dynamic-programming solutions |

#### Key Data Structures

**`Point`** — A 2D coordinate (`x`, `y`) with arithmetic operators, `manhattanDistance()`, directional movement via `Direction`, and adjacency enumeration via `Adjacency`.

**`Grid<T>`** — An immutable 2D grid that supports `get`, `set` (returning a new grid), `map`, `find`, `indices`, rotation, horizontal/vertical reflection, and bounded adjacency queries.

**`Bag<T>`** — A multiset implemented as an inline value class over a mutable map.
Supports `+`, `-`, `*`, `in`, and subset checks (`isSubSetOf`).

**`Parser<T>`** — A functional interface for monadic parsers.
Combinators include `map`, `chain`, `and`, `or`, `many`, `sepBy`, `between`, and `sequenceOf`.

### CLI Tools

The `tool/` package provides three command-line utilities, each registered as a Gradle task:

| Tool | Gradle Task | Description |
|------|-------------|-------------|
| **Solve.kt** | `./gradlew solve` | Runs solutions for a given year/day (or all); uses JVM reflection to locate the `main()` function for each day |
| **FetchInput.kt** | `./gradlew fetchInput` | Downloads puzzle input from the AoC website using an authenticated HTTP request |
| **PrepareYear.kt** | `./gradlew prepareYear` | Creates the year directory and generates solution template files for each day |
| **AocClient.kt** | *(internal)* | OkHttp3-based HTTP client used by `FetchInput` and `Solve` to communicate with the AoC website |

---

## Coverage

| Year | Days Solved | Notes |
|------|-------------|-------|
| 2021 | 23 / 25 | — |
| 2022 | 25 / 25 | Complete ✓ |
| 2023 | 15 / 25 | — |
| 2024 | 25 / 25 | Complete ✓ |
| 2025 | 12 / 12 | New 12-day format ✓ |

---

## Getting Started

### How to Fetch Input Data

Puzzle input is not included in this repository to respect the [AoC terms of service][aoc].
To obtain it, either download it manually from the website and save it as `src/data/aocYY/DayXX.txt`, or use the automated `fetchInput` task.

The `fetchInput` task requires your AoC session cookie.
Retrieve it from your browser's cookies for `adventofcode.com` and provide it in one of two ways:

1. Set the `AOC_SESSION` environment variable:
   ```shell
   export AOC_SESSION=your_session_cookie
   ```
2. Create a `.env` file in the project root:
   ```
   AOC_SESSION=your_session_cookie
   ```

Then run:

```shell
# Fetch input for a specific day
./gradlew fetchInput --args="--year=YY --day=DD"

# Fetch input for all days in a year
./gradlew fetchInput --args="--year=YY"
```

### How to Prepare for a New Year

```shell
./gradlew prepareYear --args="--year=YY"
```

This command:
- Creates `src/aocYY/` for solution files
- Creates `src/data/aocYY/` for input files
- Generates a `DayXX.kt` template for each day (25 days for years before 2025, 12 days for 2025+)

Use the `--overwrite` flag to replace any existing solution files.

### How to Run the Solutions

**From IntelliJ IDEA (recommended):** Open the project and run the `main()` function directly from any `DayXX.kt` file.

**From the command line:**

```shell
# Run a specific day
./gradlew solve --args="--year=YY --day=DD"

# Run all days for a given year
./gradlew solve --args="--year=YY"

# Run all solutions across all years
./gradlew solve
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| [OkHttp3][okhttp] | 4.12.0 | HTTP client for fetching puzzle inputs and verifying solutions |
| [Clikt][clikt] | 5.0.3 | CLI argument parsing for the Gradle tasks |
| [dotenv-kotlin][dotenv] | 6.5.1 | Loading `AOC_SESSION` from a `.env` file |

Build system: **Gradle 8.13** · Kotlin JVM plugin: **2.1.20**

[okhttp]: https://square.github.io/okhttp/
[clikt]: https://ajalt.github.io/clikt/
[dotenv]: https://github.com/cdimascio/dotenv-kotlin

---

## Philosophy

1. **Readability**: The code should be easy to read and understand.
   While performance is important, it should not come at the cost of readability.

2. **Domain-specific names**: Use names that are specific to the domain of the problem.
   Kotlin makes it easy to define domain-specific names using data classes, enums, and type aliases.
   It may seem verbose at first, but it makes the code more readable and maintainable.

3. **Functional style**: Use functional programming constructs like higher-order functions, lambdas, and extension functions.
   Immutable data structures and pure functions make the code easier to reason about and test.

4. **Performance**: While readability is important, performance is also a consideration.
   In general, prefer readability to performance, but optimize when necessary.
   Specifically, prefer algorithms with better time complexity and use efficient data structures.
   In some cases, it may be necessary to use low-level optimizations for performance.

5. **Shared code**: Avoid duplicating code by extracting common functionality into shared functions.
   The `lib` package contains utility functions that are used across multiple days and years.

[aoc]: https://adventofcode.com
[kotlin]: https://kotlinlang.org
