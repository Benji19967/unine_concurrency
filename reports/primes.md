# Primes

## Brute force, with threads

1.) Each thread calculates primes in a range:
![img](assets/primes_threads_by_range.png)

2.) Global counter, threads take a value from the counter and check if prime:
![img](assets/primes_threads_from_counter.png))

We would expect 2.) to be faster since the load is more balanced (each thread takes the same amount of time to finish).
But in 2.) the threads spend a lot of time waiting.

## Sieve of Erathostenes

Using 1 thread, ~100% CPU usage.

Computing all primes up to 500'000'000: ~5'550ms
