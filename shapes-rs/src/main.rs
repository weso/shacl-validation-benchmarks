use std::path::Path;
use std::time::Instant;

use clap::Parser;
use shacl_validation::validate::{GraphValidator, Mode, Validator};
use srdf::RDFFormat;

type Result<T> = std::result::Result<T, &'static str>;

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Cli {
    /// Path to the Data file
    #[arg(
        short = 'd',
        long = "data",
        value_name = "Data file (.ttl)",
        num_args = 1..,
        value_delimiter = ' ',
        default_value = "../data/10-lubm.ttl"
    )]
    data: Vec<String>,
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
    let shapes = Path::new(&cli.shapes);
    let mut times = Vec::new();

    for data in &cli.data {
        let data = Path::new(data);

        let validator = match GraphValidator::new(data, RDFFormat::Turtle, None, Mode::Default) {
            Ok(validator) => validator,
            Err(_) => return Err("Error creating the Validator"),
        };

        let _ = validator.validate(shapes, RDFFormat::Turtle); // avoid cold starts
        for _ in 0..cli.iterations {
            let before = Instant::now();
            let _ = validator.validate(shapes, RDFFormat::Turtle);
            times.push(before.elapsed());
        }

        println!("{:?}", times);
    }

    Ok(())
}
