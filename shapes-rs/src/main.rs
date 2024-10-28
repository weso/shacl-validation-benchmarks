use std::io::BufReader;
use std::time::Instant;
use std::{fs::File, path::Path};

use clap::Parser;
use csv::Writer;
use shacl_validation::shacl_processor::{GraphValidation, ShaclProcessor, ShaclValidationMode};
use shacl_validation::store::ShaclDataManager;
use srdf::RDFFormat;
use statistical::{mean, standard_deviation};

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

    let f = match File::open(&cli.shapes) {
        Ok(f) => f,
        Err(_) => todo!(),
    };

    let reader = BufReader::new(f);

    let schema = match ShaclDataManager::load(reader, RDFFormat::Turtle, None) {
        Ok(schema) => schema,
        Err(_) => return Err("Error parsing the SHACL shapes"),
    };

    let mut ans = Vec::new();
    let mut num_non_conformant_shapes = 0;

    for data in &cli.data {
        let mut times = Vec::new();
        let data = Path::new(data);

        let validator = match GraphValidation::new(
            data,
            RDFFormat::Turtle,
            None,
            ShaclValidationMode::Native,
        ) {
            Ok(validator) => validator,
            Err(error) => {
                eprintln!("{}", error);
                return Err("Error creating the Validator, {}");
            }
        };

        let _ = validator.validate(&schema); // avoid cold starts
        for _ in 0..cli.iterations {
            let before = Instant::now();
            let report = validator.validate(&schema);
            times.push(before.elapsed().as_nanos() as f64);

            num_non_conformant_shapes = match report {
                Ok(report) => report.results().len(),
                Err(_) => todo!(),
            };
        }

        let average = mean(&times);
        let std = standard_deviation(&times, None);

        ans.push([
            average.to_string(),
            std.to_string(),
            data.to_str()
                .unwrap()
                .replace("../data/", "")
                .replace(".ttl", "")
                .to_uppercase(),
            // (num_non_conformant_shapes == 0).to_string(),
            // num_non_conformant_shapes.to_string(),
            "rudof".to_string(),
        ])
    }

    let writer_result = Writer::from_path("../results/rudof-dev.csv");
    let mut writer = match writer_result {
        Ok(writer) => writer,
        Err(_) => return Err("Error creating the Writer"),
    };

    for e in ans {
        let _ = writer.write_record(e);
    }

    Ok(())
}
