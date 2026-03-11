# Exercise 1

## Ex 1.1

In milliseconds:

| threads / elements | 10'000'000 | 100'000'000 |
|--------------------|------|----------|
| 1                  | 794  | 19320  |
| 2                  | 478  | 12317  |
| 4                  | 272  | 7291  |
| 8                  | 217  | 5074  |
| 16                 | 222  | 4675  |

## Ex 1.2

In milliseconds:

|  threads / elements  | 10'000'000 | 100'000'000 |
|----|------|----------|
| 1  | 803  | 19433  |
| 2  | 519  | 10636  |
| 4  | 541  | 7769  |
| 8  | 496  | 7413  |
| 16 | 497  | 8055  |

## Ex 1.4

Choose from these 2 options for a particular application:

1. 1 uniprocessor, 5 zillion instructions per second
2. 10 processor multiprocessor, each processor one zillion instructions per second

Amdahl's law:

Speedup =$\frac{1}{1 - p + \frac{p}{n}}$

- p: portion of program that is parallelizable
- n: num processors

We need to solve for p:

$\frac{1}{1 - p + \frac{p}{10}} = 5$

sol: p = 0.889

So for p > 0.889 we choose option 2, otherwise option 1.
