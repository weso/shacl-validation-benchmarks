import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

def plot(df):
    # Ordenar las etiquetas correctamente (de 10-lubm a 100-lubm)
    df['label'] = pd.Categorical(df['label'], categories=sorted(df['label'].unique(), key=lambda x: int(x.split('-')[0])), ordered=True)
    df = df.sort_values('label')  # Ordenar el DataFrame por la columna 'label'

    labels = df['label'].unique()
    x = np.arange(len(labels))  # Localización de las etiquetas en el eje X
    width = 0.15  # Ancho de las barras

    # Crear la figura y los ejes
    fig, ax = plt.subplots(figsize=(12, 8))

    # Definir colores para cada software
    colors = {
        'jena': 'blue',
        'rdf4j': 'green',
        'shapes-rs': 'red',
        'topquadrant': 'purple'
    }

    # Desplazamiento de barras para cada software
    for i, (software, color) in enumerate(colors.items()):
        # Filtrar los datos para el software actual
        data = df[df['software'] == software]
        
        # Asegurarse de que hay datos correspondientes a las etiquetas y ordenarlos
        data = data.set_index('label').reindex(labels).sort_index().reset_index()
        
        # Si no hay valores para alguna etiqueta, rellenar con NaN
        means = data['mean'].fillna(0)
        errors = data['error'].fillna(0)
        
        # Graficar las barras con sus respectivos errores
        ax.bar(x + i * width, means, width, label=software, color=color, yerr=errors, capsize=5)

    # Añadir etiquetas y título
    ax.set_xlabel('LUBM')
    ax.set_ylabel('Promedio')
    ax.set_title('Comparación de Software SHACL (10-100 LUBM)')
    ax.set_xticks(x + width * 2)
    ax.set_xticklabels(labels)
    ax.legend(title="Software")

    # Mostrar el gráfico
    fig.tight_layout()
    
    plt.savefig('benches.png')

if __name__ == '__main__':
    jena = pd.read_csv(
        '/home/angel/shacl-validation-benchmark/results/jena.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )
    rdf4j = pd.read_csv(
        '/home/angel/shacl-validation-benchmark/results/rdf4j.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )
    shapesrs = pd.read_csv(
        '/home/angel/shacl-validation-benchmark/results/shapesrs.csv',
        header=None, names=['mean', 'error', 'label', 'software']
    )
    topquadrant = pd.read_csv(
        '/home/angel/shacl-validation-benchmark/results/topquadrant.csv',
        header=None,
        names=['mean', 'error', 'label', 'software']
    )

    # Combinar los datasets en un solo DataFrame
    df = pd.concat([jena, rdf4j, shapesrs, topquadrant])

    # Representamos
    plot(df)