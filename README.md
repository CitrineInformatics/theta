# theta
Non-dimensional timers for the jvm.

Theta combines simple a simple timer API with a registry of benchmarks that characterize different components of machine performance.
The timer results are normalized (non-dimensionalzed) by appropriate benchmark times to create portable explicit thresholds.
For example, a numerically intensive code section could be normalized by a compute intensive benchmark.
That way, if the underlying machine the test was running on has half the clock frequency as the machine the test was callibrated on, it would still pass.

Theta also performs multiple evaluations of the code block to estimate the error and confidence in the timing.
The default will re-run the block until there is 95% confidence that the relative error is less than 5%.
Both of these thresholds are adjustable.

## Usage examples

```scala
def heavyFunction(a: Vector[Double], b: Vector[Double]): Vector[Double] {
  < some expensive stuff >
}

assert(Stopwatch.time({heavyFunction(testA, testB)}, benchmark = "Compute") < 13.8)
```

## Available benchmarks
 * Random number generation benchmark: generate 2^20 random numbers
 * STREAM benchmark: standard memory bandwidth benchmark
