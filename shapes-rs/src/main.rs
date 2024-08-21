use std::path::Path;
use std::time::Instant;

use clap::Parser;
use csv::Writer;
use shacl_validation::{
    store::ShaclDataManager,
    validate::{GraphValidator, ShaclValidationMode, Validator},
};
use srdf::RDFFormat;
use statistical::{mean, standard_deviation};

type Result<T> = std::result::Result<T, &'static str>;

#[global_allocator]
static GLOBAL: jemallocator::Jemalloc = jemallocator::Jemalloc;

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
    let schema = match ShaclDataManager::load(shapes, RDFFormat::Turtle, None) {
        Ok(schema) => schema,
        Err(_) => return Err("Error parsing the SHACL shapes"),
    };

    let mut ans = Vec::new();

    for data in &cli.data {
        let mut times = Vec::new();
        let data = Path::new(data);

        let validator = match GraphValidator::new(
            data,
            RDFFormat::Turtle,
            None,
            ShaclValidationMode::Default,
        ) {
            Ok(validator) => validator,
            Err(_) => return Err("Error creating the Validator"),
        };

        let _ = validator.validate(schema.clone()); // avoid cold starts
        for _ in 0..cli.iterations {
            let schema = schema.clone();
            let before = Instant::now();
            let _ = validator.validate(schema);
            times.push(before.elapsed().as_nanos() as f64);
        }

        let average = mean(&times);
        let std = standard_deviation(&times, None);

        ans.push([
            average.to_string(),
            std.to_string(),
            data.to_str()
                .unwrap()
                .replace("../data/", "")
                .replace(".ttl", ""),
            "shapes-rs".to_string(),
        ])
    }

    let writer_result =
        Writer::from_path("/home/angel/shacl-validation-benchmark/results/shapesrs.csv");
    let mut writer = match writer_result {
        Ok(writer) => writer,
        Err(_) => return Err("Error creating the Writer"),
    };

    for e in ans {
        let _ = writer.write_record(e);
    }

    Ok(())
}
