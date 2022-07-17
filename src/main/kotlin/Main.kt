import java.io.File
import java.time.LocalTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

fun main() {
	search(1)
}

// Looks for successive values for Un via the use of rules and bruteforce
fun search(startingN: Int) {
	var previousNumbers = mutableListOf<Int>()
	var previousFactorNumbers = mutableListOf<Int>()
	val maxListSize = 30_000_000
	// These lists keep track of numbers factored as factoring is inefficient

	var n = startingN
	while (true) {
		// Rules
		val iFactorNumber = countFactors(n)
		if (iFactorNumber <= 2) {
			// RULE #1 | for prime n: Un = 2^{n-1}
			printWithTimestamp("$n  #1  2^${n - 1}")
			n++
			continue
		} else if (iFactorNumber == 4) {
			// RULE #2 | for n with 4 factors (1, n, a, b), Un = 2^{b-1} * 3^{a-1}  (b > a)
			val iFactors = factorise(n).drop(2)
			printWithTimestamp("$n  #2  2^${iFactors[1] - 1} * 3^${iFactors[0] - 1}")
			n++
			continue
		} else if (iFactorNumber == 3) {
			// RULE #3 | for n being a square of a prime with factors (1, n, a) (a is prime) the Un = 2^{a-1} * 3^{a-1}
			val iFactor = factorise(n).drop(2)[0] - 1
			printWithTimestamp("$n  #3  2^$iFactor * 3^$iFactor")
			n++
			continue
		}

		// Bruteforce
		// Checks every even number until Un is found
		var num = 0
		do {
			num += 2
			val index = previousNumbers.indexOf(num)
			var factors: Int
			if (index == -1) {
				factors = countFactors(num)
				previousNumbers.add(num)
				previousFactorNumbers.add(factors)
			} else {
				factors = previousFactorNumbers[index]
			}
			// If the number has been checked the number of factors is retrieved
			// If the number is unchecked it adds it the list
		} while (factors != n)

		printWithTimestamp("$n      $num")
		// Prints brute-forced number

		if (previousNumbers.size >= maxListSize) {
			previousNumbers = previousNumbers.takeLast(maxListSize).toMutableList()
			previousFactorNumbers = previousFactorNumbers.takeLast(maxListSize).toMutableList()
		}
		// If the size of the previousNumbers is too large
		// the first maxListSize elements are removed

		n++
	}
}

fun countFactors(n: Int): Int {
	if (n == 1) {
		return 1
	}
	var factors = 2
	// 2 representing 1 and n
	val upperBound = floor(sqrt(n.toDouble())).toInt()
	// The largest factor of n is always <= sqrt(n)
	for (i in 2..upperBound) {
		if (n % i == 0) {
			factors += if (i * i == n) 1 else 2
			// if i is the sqrt(n) it only counts as 1 factor
		}
	}
	return factors
}

fun factorise(n: Int): List<Int> {
	val factors = mutableListOf(1)
	if (n == 1) {
		return factors
	}
	factors.add(n)
	val upperBound = ceil(sqrt(n.toDouble())).toInt()
	for (i in 2..upperBound) {
		if (n % i == 0 && !factors.contains(i)) {
			factors.add(i)
			if (i * i != n) {
				factors.add(n / i)
			}
		}
	}
	return factors
}

// https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
fun primeFactors(number: Int): List<Int> {
	var n = number
	val factors = mutableListOf<Int>()
	var i = 2
	while (i <= n / i) {
		while (n % i == 0) {
			factors.add(i)
			n /= i
		}
		i++
	}
	if (n > 1) {
		factors.add(n)
	}
	return factors
}

// Produces every set of number whose product is n
// It takes the prime factors, then reduces them using the function below
// Then those new sets are further
fun generateAllFactorSequences(n: Int): List<List<Int>> {
	val factorSequences = HashSet<List<Int>>()
	val primeFactors = primeFactors(n)
	// The first set is the prime factorisation (not in index form)
	// E.g. for n=24, primeFactors(n) => [2, 2, 2, 3]

	var lastSet = mutableListOf(primeFactors)
	var nextSet = mutableListOf<List<Int>>()
	// The lastSet refers to the set which will have every subset 'reduced'
	// The nextSet is where these 'reduced' sets are stored
	// They are kept separate from factorSequences to avoid duplicate sets
	for (i in 1 until primeFactors.size - 1) {
		for (set in lastSet) {
			nextSet.addAll(multiplyOutSet(set))
			// This where sets are 'reduced'
		}

		factorSequences.addAll(lastSet.toMutableList())
		lastSet = nextSet.toMutableList()
		nextSet = mutableListOf()
		// The lastSet is moved to factorSequences
		// The nextSet is moved to lastSet to be reduced next loop
		// The nextSet is cleared
	}
	factorSequences.addAll(lastSet.toMutableList())
	factorSequences.add(listOf(n))
	// The last batch is transferred
	// [n] is added as the product of that list is n

	return factorSequences.map { x ->
		run {
			val sequence = x.toMutableList()
			sequence.sortDescending()
			sequence.toList()
		}
	}
	// Factors put in descending order
}

// Given a set of numbers each number is multiplied with another to provide a new set
// E.g. 2 2 3 7 =>
//      4 3 7
//      2 6 7
//      etc...
// (It reduces the set to each set of size n-1 which multiply to the product of the original set)
fun multiplyOutSet(set: List<Int>): List<List<Int>> {
	val sets = mutableListOf<MutableList<Int>>()
	for (i in 0 until set.size - 1) {
		// Once you reach the last element all 'new' sets are duplicates
		// Hence the loop only repeats n-1 times
		val subset = set.toMutableList()
		subset.removeAt(i)
		for (j in 0 until subset.size - i) {
			val subSubset = subset.toMutableList()
			subSubset[j + i] = subSubset[j + i] * set[i]
			sets.add(subSubset)
			// Multiplies set[i] to every factor after it
			// This avoids duplicates as multiplication is commutative
		}
	}
	return sets
}

// Prints with a timestamp attached
fun printWithTimestamp(str: String) {
	println("${str.padEnd(30, ' ')}${LocalTime.now()}")
}

// THIS FUNCTION IS HORRIBLY WRITTEN - I'm not bothered
// Takes the console output of the search and parses it into a csv,
// which can then be imported to replace the values in the spreadsheet
fun parseIntoCSV(rawTextPath: String, path: String) {
	val lines = File(rawTextPath).readLines().map {
		if (it.contains("#")) {
			"Rule " + it.replace("^.*#".toRegex(), "").replace("\\s+.*$".toRegex(), "") + ",=" + it.replace("^\\d+\\s{2}#\\d+\\s{2}".toRegex(), "").replace("\\s+\\d+:\\d+:\\d+\\.\\d+\$".toRegex(), "").replace("\\s*".toRegex(), "")
		} else {
			",=" + it.replace("^\\d+\\s+".toRegex(), "").replace("\\s+.*".toRegex(), "")
		}
	}
	File(path).writeText(lines.joinToString("\n"))
}
