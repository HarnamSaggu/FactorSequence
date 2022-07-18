import java.io.File
import java.time.LocalTime
import kotlin.math.*

fun main() {
	search(endingN = 1_000_000, path = "src/main/resources/millionUn.txt")
	parseIntoCSV(path = "src/main/resources/millionUn.txt")
}

// Used getU() to find all terms startingN <= n <= endingN
fun search(startingN: Int = 1, endingN: Int = -1, path: String = "src/main/resources/Un.txt", backupConstant: Int = 5_000) {
	var log = ""
	var n = startingN
	while (n <= endingN || endingN == -1) {
		val record = "$n\t\t${beatifyIndexForm(getU(n))}"
		log += "$record\n"
		println(record.padEnd(50, ' ') + "\t" + LocalTime.now())
		n++

		if (n % backupConstant == 0) {
			File(path).writeText(log)
			parseIntoCSV(path)
		}
	}
	File(path).writeText(log)
}

// List of primes and their natural logs
val primes = listOf(
	2, 3, 5, 7, 11, 13, 17,
	19, 23, 29, 31, 37, 41,
	43, 47, 53, 59, 61, 67,
	71, 73, 79, 83, 89, 97
)
val primeWeights = primes.map { ln(it.toDouble()) }
fun getU(n: Int): List<Pair<Int, Int>> {
	return if (countFactors(n) == 2) {
		// If n is prime then Un = 2^{n-1}
		listOf(Pair(2, n - 1))
	} else {
		// Every factor sequence is weighted and the lowest is the powers of Un's prime factorization
		generateAllFactorSequences(n).minByOrNull { x ->
			run {
				var weight = 0.0
				for (i in x.indices) {
					weight += (x[i] - 1) * primeWeights[i]
				}
				weight
			}
		}!!.mapIndexed { index, i -> Pair(primes[index], i - 1) }
		// This is a way to compare the size of powers
		// of different bases without crazy huge numbers
		// If we had to compare [3, 2, 2] and [17, 1]
		// (2^3 * 3^2 * 5^2) vs (2^17 * 3^1)
		// Take the log of both:
		// ln(2^3 * 3^2 * 5^2) = 3ln(2) + 2ln(3) + 2ln(5) = 7.49
		// ln(2^17 * 3^1) = 17ln(2) + 3ln(1) = 11.78
		// (Therefore [3, 2, 2] is smaller than [17, 2])
		// which are the powers multiplied by the log of the next prime
		// The lowest value yields the lowest exp() therefore it's Un
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
			// if sqrt(n) = i it only counts as 1 factor
		}
	}
	return factors
}

// https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
fun primeFactors(number: Int): List<Int> {
	if (number == 1) {
		return listOf(1)
	}
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

// Puts the abstract list of pairs into a string which more resembles index notation
fun beatifyIndexForm(factors: List<Pair<Int, Int>>): String {
	return factors.joinToString(" * ") { "${it.first}^${it.second}" }
}

// Produces every set of number whose product is n
// It takes the prime factors, then reduces them using the function below
// Then those new sets are further
fun generateAllFactorSequences(n: Int): List<List<Int>> {
	val factorSequences = mutableSetOf<List<Int>>()
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
		nextSet = nextSet.distinct().toMutableList()

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
	}.distinct().toList()
	// Factors put in descending order and removes duplicates
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

// Takes the file output of search and parses it into a csv,
// which can then be imported to replace the values in the spreadsheet
fun parseIntoCSV(rawTextPath: String = "src/main/resources/Un.txt", path: String = "src/main/resources/un_spreadsheet.csv") {
	val lines = File(rawTextPath).readLines().map {
		it.split("\t\t").joinToString(",=") + "," + it.split("\t\t")[1]
	}
	File(path).writeText(lines.joinToString("\n"))
}
