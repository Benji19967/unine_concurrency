from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import polars as pl
import seaborn as sns

HERE = Path(__file__).parent
BENCHMARKS_FILE = HERE / "results" / "counters.csv"
PLOTS_DIR = HERE / "plots"


def main():
    df = pl.read_csv(str(BENCHMARKS_FILE))
    with pl.Config(tbl_rows=-1, tbl_cols=-1):
        print(df)

    g = sns.catplot(df, x="Duration", y="Program", hue="Threads", kind="bar")
    g.set_axis_labels("Duration (s)", "Program")
    plt.savefig(str(PLOTS_DIR / "counters"))
    plt.show()

    df = pd.read_csv(str(BENCHMARKS_FILE))
    print(df.to_latex(float_format="%.4f", index=False))


if __name__ == "__main__":
    main()
