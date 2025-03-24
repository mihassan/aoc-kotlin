fun main(args: Array<String>) {
    if(args.size != 2) {
        println("Usage: <YY> <DD>")
        return
    }
    val year = args[0].toIntOrNull() ?: error("Invalid year (21-24)")
    val day = args[1].toIntOrNull() ?: error("Invalid day (1-25)")

    when(year to day) {
        21 to 1 -> aoc21.day01.main()
        21 to 2 -> aoc21.day02.main()
        21 to 3 -> aoc21.day03.main()
        22 to 1 -> aoc22.day01.main()
        22 to 2 -> aoc22.day02.main()
        22 to 3 -> aoc22.day03.main()
        22 to 4 -> aoc22.day04.main()
        22 to 5 -> aoc22.day05.main()
        22 to 6 -> aoc22.day06.main()
        22 to 7 -> aoc22.day07.main()
        22 to 8 -> aoc22.day08.main()
        22 to 9 -> aoc22.day09.main()
        22 to 10 -> aoc22.day10.main()
        22 to 11 -> aoc22.day11.main()
        22 to 12 -> aoc22.day12.main()
        22 to 13 -> aoc22.day13.main()
        22 to 14 -> aoc22.day14.main()
        22 to 15 -> aoc22.day15.main()
        22 to 16 -> aoc22.day16.main()
        22 to 17 -> aoc22.day17.main()
        22 to 18 -> aoc22.day18.main()
        22 to 19 -> aoc22.day19.main()
        22 to 20 -> aoc22.day20.main()
        22 to 21 -> aoc22.day21.main()
        22 to 22 -> aoc22.day22.main()
        23 to 1 -> aoc23.day01.main()
        23 to 2 -> aoc23.day02.main()
        23 to 3 -> aoc23.day03.main()
        23 to 4 -> aoc23.day04.main()
        23 to 5 -> aoc23.day05.main()
        23 to 6 -> aoc23.day06.main()
        23 to 7 -> aoc23.day07.main()
        23 to 8 -> aoc23.day08.main()
        23 to 9 -> aoc23.day09.main()
        23 to 10 -> aoc23.day10.main()
        23 to 11 -> aoc23.day11.main()
        23 to 12 -> aoc23.day12.main()
        23 to 13 -> aoc23.day13.main()
        23 to 14 -> aoc23.day14.main()
        23 to 15 -> aoc23.day15.main()
        24 to 1 -> aoc24.day01.main()
        24 to 2 -> aoc24.day02.main()
        24 to 3 -> aoc24.day03.main()
        24 to 4 -> aoc24.day04.main()
        24 to 5 -> aoc24.day05.main()
        24 to 6 -> aoc24.day06.main()
        24 to 7 -> aoc24.day07.main()
        24 to 8 -> aoc24.day08.main()
        24 to 9 -> aoc24.day09.main()
        24 to 10 -> aoc24.day10.main()
        24 to 11 -> aoc24.day11.main()
        24 to 12 -> aoc24.day12.main()
        24 to 13 -> aoc24.day13.main()
        24 to 14 -> aoc24.day14.main()
        24 to 15 -> aoc24.day15.main()
        24 to 16 -> aoc24.day16.main()
        24 to 17 -> aoc24.day17.main()
        24 to 18 -> aoc24.day18.main()
        24 to 19 -> aoc24.day19.main()
        24 to 20 -> aoc24.day20.main()
        24 to 21 -> aoc24.day21.main()
        24 to 22 -> aoc24.day22.main()
        24 to 23 -> aoc24.day23.main()
        24 to 24 -> aoc24.day24.main()
        24 to 25 -> aoc24.day25.main()
        else -> error("The problem for year $year day $day is not solved yet")
    }
}
