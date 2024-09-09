import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def plot(df, colors, output):
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
    ax.set_xticks(x + width * (len(df['software'].unique()) - 1) / 2)
    ax.set_xticklabels(labels, fontsize=12)
    ax.legend(title="Software", fontsize=12, title_fontsize=14)

    fig.tight_layout()

    plt.yticks(fontsize=12)
    plt.margins(x=0.015)
    plt.savefig(f'{output}.pdf', backend='pgf')


if __name__ == '__main__':
    SHACL = 'non-conformant'

    colors = {
        'rudof': 'purple',
        'pyrudof': 'purple',
        'Apache Jena': 'blue',
        'TopQuadrant': 'green',
        'RDF4J': 'orange',
        'pySHACL': 'red'
    }

    jena = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/jena.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )
    shapesrs = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/shapesrs.csv',
        header=None, 
        names=['mean', 'error', 'label', 'software']
    )
    topquadrant = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/topquadrant.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )
    rdf4j = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/rdf4j.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )

    pyshacl = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyshacl.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )
    pyrudof = pd.read_csv(
        f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyrudof.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )

    df1 = pd.concat([shapesrs, jena, topquadrant, rdf4j])
    df2 = pd.concat([pyshacl, pyrudof])

    plot(df1, colors, SHACL)
    plot(df2, colors, f'py-{SHACL}')
