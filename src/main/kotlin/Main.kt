import java.time.LocalTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/*
	val unSequence = File("src/main/resources/Un.txt").bufferedReader().readLines().map { it.toLong() }
	unSequence.forEachIndexed { i, it ->
		println("$it \t\t\t\t ${i+1}th")
//		println(primeFactors(it))
		val powers = indexForm(primeFactors(it)).map { x ->
			x.second
		}
		println(powers.joinToString(" "))
		println(powers.sum())
//		indexForm(primeFactors(it)).forEach { x ->
//			print((x.second + 1).toString() + " ")
//		}
//		println()

		println()
	}

	bruteForceSequence()
 */

fun main() {
	val factorSequences = generateAllFactorSequences(210)
	factorSequences.forEach {
		println(it)
	}

	println()
	println()
	println()

	factorSequences.forEach {
		println(it.reduce { acc, i -> acc * i })
	}

//	efficientBruteForce(1)
}

//==========================
//fun generateAllFactorSequences(n: Long): List<List<Int>> {
//	val factorSequences = mutableListOf<List<Int>>()
//	val primeFactors = primeFactors(n)
//
//	var lastSet = mutableListOf(primeFactors)
//	var nextSet = mutableListOf<List<Int>>()
//
//	for (i in 1 until primeFactors.size - 1) {
//		for (set in lastSet) {
//			nextSet.addAll(multiplyOutSet(set))
//		}
//
//		factorSequences.addAll(lastSet.toMutableList())
//		lastSet = nextSet.toMutableList()
//		nextSet = mutableListOf()
//	}
//	factorSequences.addAll(lastSet.toMutableList())
//
//	return factorSequences.toList()
//}
//
//fun multiplyOutSet(set:List<Int>): List<List<Int>> {
//	val sets = mutableListOf<MutableList<Int>>()
//	for (i in 0 until set.size - 1) {
//		val subset = set.toMutableList()
//		subset.removeAt(i)
//		for (j in 0 until subset.size - i) {
//			val subSubset = subset.toMutableList()
//			subSubset[j + i] = subSubset[j + i] * set[i]
//			sets.add(subSubset)
//		}
//	}
//	return sets
//}
fun generateAllFactorSequences(n: Long): List<List<Int>> {
	val factorSequences = HashSet<List<Int>>()
	val primeFactors = primeFactors(n)

	var lastSet = mutableListOf(primeFactors)
	var nextSet = mutableListOf<List<Int>>()

	for (i in 1 until primeFactors.size - 1) {
		for (set in lastSet) {
			nextSet.addAll(multiplyOutSet(set))
		}

		factorSequences.addAll(lastSet.toMutableList())
		lastSet = nextSet.toMutableList()
		nextSet = mutableListOf()
	}
	factorSequences.addAll(lastSet.toMutableList())

	for (sequence in factorSequences) {
		sequence.toMutableList().sort()
	}

	return factorSequences.map {
		x -> run {
			val sequence = x.toMutableList()
			sequence.sort()
			sequence.toList()
		}
	}
}

fun multiplyOutSet(set:List<Int>): List<List<Int>> {
	val sets = mutableListOf<MutableList<Int>>()
	for (i in 0 until set.size - 1) {
		val subset = set.toMutableList()
		subset.removeAt(i)
		for (j in 0 until subset.size - i) {
			val subSubset = subset.toMutableList()
			subSubset[j + i] = subSubset[j + i] * set[i]
			sets.add(subSubset)
		}
	}
	return sets
}
//=========================

/*
fun efficientBruteForce(startingN: Int) {
	val previousValues = hashMapOf<Long, Int>()
	var i = startingN
	while (true) {
		val iFactorNumber = countFactors(i.toLong())
		if (iFactorNumber <= 2) {
			// RULE #1 | for prime n: Un = 2^{n-1}
			println("$i \t 2^${i - 1} \t\t\t (${2.0.pow (i - 1)})")
			i++
			continue
		} else if (iFactorNumber == 4) {
			// RULE #2 | for n with 4 factors (1, n, a, b), Un = 2^{b-1} * 3^{a-1}
			val iFactors = factorise(i.toLong()).drop(2)
			println("$i \t   ${2.0.pow(iFactors[1].toInt() - 1) * 3.0.pow(iFactors[0].toInt() - 1)}")
			i++
			continue
		}

		var n = 0L
		do {
			n += 2
			val factors = if (previousValues.containsKey(n)) {
				previousValues[n]
			} else {
					countFactors(n)
			}
			previousValues[n] = factors ?: 0
		} while (factors != i)

		println("$i \t   $n")
		i++
	}
}
*/

fun timestamp(): String {
	return "  \t\t\t ${LocalTime.now()}"
}

fun addTimestamp(str: String): String {
	return "$${str.padEnd(30, ' ')}${LocalTime.now()}"
}

fun efficientBruteForce(startingN: Int) {
	var previousNumbers = mutableListOf<Long>()
	var previousFactorNumbers = mutableListOf<Int>()
	val maxListSize = 30_000_000
	var i = startingN
	while (true) {
		// RULES
		val iFactorNumber = countFactors(i.toLong())
		if (iFactorNumber <= 2) {
			// RULE #1 | for prime n: Un = 2^{n-1}

			println(addTimestamp("$i  #1  2^${i - 1}"))
			i++
			continue
		} else if (iFactorNumber == 4) {
			// RULE #2 | for n with 4 factors (1, n, a, b), Un = 2^{b-1} * 3^{a-1}  (b > a)
			val iFactors = factorise(i.toLong()).drop(2)
			println(addTimestamp("$i  #2  2^${iFactors[1] - 1} * 3^${iFactors[0] - 1}"))
			i++
			continue
		} else if (iFactorNumber == 3) {
			// RULE #3 | for n being a square of a prime with factors (1, n, a) (a is prime) the Un = 2^{a-1} * 3^{a-1}
			val iFactor = factorise(i.toLong()).drop(2)[0].toInt() - 1
			println(addTimestamp("$i  #3  2^$iFactor * 3^$iFactor"))
			i++
			continue
		}

		// BRUTE FORCE
		var n = 0L
		do {
			n += 2
			val index = previousNumbers.indexOf(n)
			val factors = if (index != -1) {
				previousFactorNumbers[index]
			} else {
				countFactors(n)
			}
			if (index != -1) {
				previousNumbers.add(n)
				previousFactorNumbers.add(factors)
			}
		} while (factors != i)

		println(addTimestamp("$i      $n"))
		i++

		if (previousNumbers.size >= maxListSize) {
			previousNumbers = previousNumbers.takeLast(maxListSize).toMutableList()
			previousFactorNumbers = previousFactorNumbers.takeLast(maxListSize).toMutableList()
		}
	}
}

fun bruteForceSequence() {
	var i = 1
	while (true) {
		if (countFactors(i.toLong()) <= 2) {
			println("$i \t 2^${i - 1} \t\t\t (${2.0.pow(i - 1)})")
			i++
			continue
		}

		var n = 2L
		while (countFactors(n) != i) {
			n += 2
		}
		println("$i \t   $n")
		i++
	}
}

fun countFactors(n: Long): Int {
	if (n == 1L) {
		return 1
	}
	var factors = 2
	val upperBound = floor(sqrt(n.toDouble())).toLong()
	for (i in 2..upperBound) {
		if (n % i == 0L) {
			factors += if (i * i == n) 1 else 2
		}
	}
	return factors
}

fun factorise(n: Long): List<Long> {
	val factors = mutableListOf(1L)
	if (n == 1L) {
		return factors
	}
	factors.add(n)
	val upperBound = ceil(sqrt(n.toDouble())).toLong()
	for (i in 2..upperBound) {
		if (n % i == 0L && !factors.contains(i)) {
			factors.add(i)
			if (i * i != n) {
				factors.add(n / i)
			}
		}
	}
	return factors
}

fun primeFactors(number: Long): List<Int> {
	var n = number
	val factors = mutableListOf<Int>()
	var i = 2
	while (i <= n / i) {
		while (n % i.toLong() == 0L) {
			factors.add(i)
			n /= i
		}
		i++
	}
	if (n > 1) {
		factors.add(n.toInt())
	}
	return factors
}

fun indexForm(factors: List<Long>): List<Pair<Long, Long>> {
	val indexPairs = mutableListOf<Pair<Long, Long>>()
	var lastFactor = 0L
	factors.forEach {
		if (it != lastFactor) {
			lastFactor = it
			indexPairs.add(Pair(it, 1))
		} else {
			indexPairs[indexPairs.size - 1] = Pair(it, indexPairs.last().second + 1)
		}
	}
	return indexPairs.toList()
}
