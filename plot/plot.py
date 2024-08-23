import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def plot(df, output):
    df['label'] = pd.Categorical(
        df['label'],
        categories = sorted(df['label'].unique(), key=lambda x: int(x.split('-')[0])), 
        ordered=True
    )

    df = df.sort_values(by=['label', 'mean'])

    labels = df['label'].unique()
    x = np.arange(len(labels))
    width = 0.8 / len(df['software'].unique())

    fig, ax = plt.subplots(figsize=(12, 8))

    for i, (software, group) in enumerate(df.groupby('software', sort = False)):
        ax.bar(
            x       = x + i * width,
            height  = group['mean'] * 10e-9,
            yerr    = group['error'] * 10e-9,
            width   = width,
            label   = software,
            capsize = 5
        )

    ax.set_xlabel('Dataset')
    ax.set_ylabel('Execution time (s)')
    ax.set_title(f'Validation of LUBM datasets against { output } SHACL shapes')
    ax.set_xticks(x + width * (len(df['software'].unique()) - 1) / 2)
    ax.set_xticklabels(labels)
    ax.legend(title="Software")

    fig.tight_layout()
    
    plt.savefig(f'{output}.pdf', backend='pgf')


if __name__ == '__main__':
    SHACL = 'conformant'

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
    # rdf4j = pd.read_csv(
    #     f'/home/angel/shacl-validation-benchmark/results/{SHACL}/rdf4j.csv',
    #     header=None,
    #     names=['mean', 'error', 'label', 'software']
    # )
    # pyshacl = pd.read_csv(
    #     f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyshacl.csv',
    #     header=None,
    #     names=['mean', 'error', 'label', 'software']
    # )
    # pyrudof = pd.read_csv(
    #     f'/home/angel/shacl-validation-benchmark/results/{SHACL}/pyrudof.csv',
    #     header=None,
    #     names=['mean', 'error', 'label', 'software']
    # )

    df1 = pd.concat([shapesrs, jena, topquadrant])
    # df2 = pd.concat([pyshacl, rdf4j, pyrudof])

    plot(df1, SHACL)
    # plot(df2, SHACL)