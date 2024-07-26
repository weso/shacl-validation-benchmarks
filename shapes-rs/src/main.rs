use std::path::Path;
use std::time::Instant;
use std::{fs::File, io::BufReader};

use clap::Parser;
use oxigraph::io::GraphFormat;
use oxigraph::model::GraphNameRef;
use oxigraph::store::Store;
use shacl_ast::Schema;
use shacl_ast::ShaclParser;
use shacl_validation::validate::validate;
use srdf::{RDFFormat, SRDFGraph};

type Result<T> = std::result::Result<T, &'static str>;

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Cli {
    /// Path to the Data file
    #[arg(
        short = 'd',
        long = "data",
        value_name = "Data file (.ttl)",
        default_value = "../data/1-lubm.ttl"
    )]
    data: String,
    /// Path to the Schema file
    #[arg(
        short = 's',
        long = "shapes",
        value_name = "Shapes file (.ttl)",
        default_value = "../data/lubm.ttl"
    )]
    shapes: String,
    /// Number of iterations to perform
    #[arg(
        short = 'i',
        long = "iters",
        value_name = "Number of iterations to perform",
        default_value = "1"
    )]
    iterations: u8,
}

fn main() -> Result<()> {
    let cli = Cli::parse();
    let benchmark = BenchmarkUtil::new(cli.data, cli.shapes);
    let store = benchmark.load_graph()?;
    let schema = benchmark.load_shapes()?;

    let mut times = Vec::new();
    for _ in 0..cli.iterations {
        let before = Instant::now();
        let _ = validate(&store, schema.clone());
        times.push(before.elapsed());
    }

    println!("{:?}", times);

    Ok(())
}

pub struct BenchmarkUtil {
    data: String,
    shapes: String,
}

impl BenchmarkUtil {
    fn new(data: String, shapes: String) -> Self {
        BenchmarkUtil { data, shapes }
    }

    fn load_graph(&self) -> Result<Store> {
        let data_store = match Store::new() {
            Ok(data_store) => data_store,
            Err(_) => return Err("Error creating the store"),
        };
        let file = match File::open(self.data.to_owned()) {
            Ok(file) => file,
            Err(_) => return Err("Error opening the given file"),
        };
        match data_store.bulk_loader().load_graph(
            BufReader::new(file),
            GraphFormat::Turtle,
            GraphNameRef::DefaultGraph,
            None,
        ) {
            Ok(_) => Ok(data_store),
            Err(_) => Err("Error loading the graph"),
        }
    }

    fn load_shapes(&self) -> Result<Schema> {
        let rdf = match SRDFGraph::from_path(Path::new(&self.shapes), &RDFFormat::Turtle, None) {
            Ok(rdf) => rdf,
            Err(_) => return Err("Error parsing the RDF data from provided path"),
        };

        let schema = match ShaclParser::new(rdf).parse() {
            Ok(shapes_graph) => shapes_graph,
            Err(_) => return Err("Error parsing the Shapes"),
        };

        Ok(schema)
    }
}
