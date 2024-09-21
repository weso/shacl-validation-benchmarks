import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def plot(df, colors, ymax, output):
    df['label'] = pd.Categorical(
        df['label'],
        categories=sorted(df['label'].unique(), key=lambda x: int(x.split('-')[0])), 
        ordered=True
    )

    df = df.sort_values(by=['label', 'mean'])

    labels = df['label'].unique()
    x = np.arange(len(labels))
    width = 0.9 / len(df['software'].unique())

    fig, ax = plt.subplots(figsize=(14, 8))

    for i, (software, group) in enumerate(df.groupby('software', sort=False)):
        bar_container = ax.bar(
            x = x + i * width,
            height=group['mean'] * 10e-9,
            yerr=group['error'] * 10e-9,
            width=width,
            label=software,
            capsize=5,
            color=colors[software]
        )
        ax.bar_label(
            bar_container,
            fmt='{:,.3f}',
            padding=6,
            fontsize=7
        )

    ax.set_xlabel('Dataset', fontsize=14)
    ax.set_ylabel('Execution time (s)', fontsize=14)
    ax.set_ylim([0, ymax])
    ax.set_xticks(x + width * (len(df['software'].unique()) - 1) / 2)
    ax.set_xticklabels(labels, fontsize=12)
    ax.legend(title="Software", fontsize=12, title_fontsize=14)

    fig.tight_layout()

    plt.yticks(fontsize=12)
    plt.margins(x=0.015)
    plt.savefig(f'{output}.pdf', backend='pgf')


if __name__ == '__main__':
    # SHACL = 'non-conformant'
    SHACL = 'conformant'

    colors = {
        'rudof': 'purple',
        'pyrudof': 'purple',
        'Apache Jena': 'blue',
        'TopQuadrant': 'green',
        'RDF4J': 'orange',
        'pySHACL': 'red'
    }

    names = ['mean', 'error', 'label', 'software']

    jena = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/jena.csv',
        header=None,
        names=names
    )
    shapesrs = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/rudof.csv',
        header=None, 
        names=names
    )
    topquadrant = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/topquadrant.csv',
        header=None,
        names=names
    )
    rdf4j = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/rdf4j.csv',
        header=None,
        names=names
    )
    pyshacl = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyshacl.csv',
        header=None,
        names=names
    )

    pyshacl_full = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyshacl-full.csv',
        header=None,
        names=names
    )
    pyrudof = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyrudof.csv',
        header=None,
        names=names
    )

    df1 = pd.concat([shapesrs, jena, topquadrant, rdf4j, pyshacl])
    df2 = pd.concat([pyshacl_full, pyrudof])

    plot(df1, colors,  70, SHACL)
    plot(df2, colors, 9000, f'py-{SHACL}')
