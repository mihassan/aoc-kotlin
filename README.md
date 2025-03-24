# Advent of Code Kotlin

[Advent of Code][aoc] – an annual event in December since 2015.
This repository contains partial solutions to the problems in [kotlin].

## How to setup input data

The input data is not included in the repository to respect the [AoC][aoc] terms of service.
So, you need to download the input data from the AoC website and save it in the `src/data` directory.
The input data should be saved in a file named `DayXX.txt`, where `XX` is the day number.
It should be saved `src/data/aocXX` folder, where `XX` is the last 2 digits of the year.
For example, `src/data/aoc24/Day01.txt` contains the input data for day 1 of the year 2024.

Alternatively, you can run the following command to download the input data for a specific day:

```shell
./gradlew fetchInput --args="YY DD"
```

Make sure to set the `AOC_SESSION` environment variable to your AoC session cookie.
You can retrieve the session cookie from your browser and export it as an environment variable.

## How to run the solutions

Preferred approach is to load the project in IntelliJ IDEA and run the solutions from there.
For each day, there is a `main` function that reads the input data and prints the solution.
You can run the `main` function from inside the IDE and see the output in the console.

Alternatively, you can run the solutions from the command line using the following command:

```shell
./gradlew solve --args="YY DD"
```

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
Also, in some cases, it may be necessary to use low-level optimizations for performance.

5. **Shared code**: Avoid duplicating code by extracting common functionality into shared functions.
The `lib` package contains utility functions that are used across multiple days.

[aoc]: https://adventofcode.com
[kotlin]: https://kotlinlang.org
