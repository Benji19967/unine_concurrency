import subprocess
import time
from pathlib import Path

import polars as pl

HERE = Path(__file__)
JAVA_PACKAGE_NAME = "synchronization"

PROGRAMS = [
    "Counter",
    "CounterAtomic",
    "CounterBakery",
    "CounterFilter",
    "CounterLock",
    "CounterMonitor",
]
THREAD_COUNTS = [2, 4, 8, 16]


def main():

    results = []
    for prog in PROGRAMS:
        for t in THREAD_COUNTS:

            # Takes too long
            if prog in ["CounterBakery", "CounterFilter"] and t == 16:
                continue

            start = time.perf_counter()
            subprocess.run(
                ["java", "-cp", "bin", f"{JAVA_PACKAGE_NAME}.{prog}", str(t)],
                check=True,
            )
            end = time.perf_counter()

            results.append({"Program": prog, "Threads": t, "Duration": end - start})

    # Save to CSV for analysis
    df = pl.DataFrame(results)
    df.write_csv("benchmarks/results/counters.csv")


if __name__ == "__main__":
    main()
