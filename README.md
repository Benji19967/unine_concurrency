# Concurrency at Uni Neuchatel
Concurrency: Multi-core Programming and Data Processing at Uni Neuchatel

## Folder Structure

    .
    │
    ├── assets                  # Assets comparing CPU usage for different primes calculation algorithms
    │   
    ├── benchmarks              # Benchmarks for different counter implementations
    │   
    ├── reports                 # Exercises reports
    │
    ├── src
    │   └── main / java / concurrency / synchronization
    │       ├── Counter*.java                   # Concurrent algorithms for implementing counters
    │       ├── DiningPhilosophers*.java        # Various solutions to the dining philosophers problem
    │       ├── LockFreeConsensus.java          # Implementation of a consensus algorithm using compare-and-set
    │       ├── ProducerConsumer*.java          # Various solutions to the producer consumer problem
    │       ├── ReadWriteLock*.java              # Various implementations of a read-write lock
    │       └── Savages*.java                   # Various solutions to the savages problem
    │
    └── README.md