use std::path::Path;
use std::time::Instant;
use std::{fs::File, io::BufReader};

use clap::Parser;
use oxigraph::io::GraphFormat;
use oxigraph::model::GraphNameRef;
use oxigraph::store::Store;
use shacl_ast::Schema;
use shacl_ast::ShaclParser;
use shacl_validation::store::graph::Graph;
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
    let data = Path::new(&cli.data);
    let shapes = Path::new(&cli.shapes);
    let validator = GraphValidator::new(data, GraphFormat::Turtle);

    let mut times = Vec::new();
    for _ in 0..cli.iterations {
        let before = Instant::now();
        let _ = validator.validate(shapes, GraphFormat::Turtle);
        times.push(before.elapsed());
    }

    println!("{:?}", times);

    Ok(())
}
