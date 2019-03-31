
use std::io;

use actix_web::{middleware, web, App, HttpServer};
use actix_web::web::{resource, get};
use r2d2_sqlite::SqliteConnectionManager;

mod health;
use health::{db_check, server_check};

fn main() -> io::Result<()> {
    let sys = actix_rt::System::new("r2d2-example");
    env_logger::init();
    println!("Starting WebComicReader backend...");

    let manager = SqliteConnectionManager::file("db/development.db");
    let pool = r2d2::Pool::new(manager).unwrap();

    HttpServer::new(move || {
        App::new()
            .data(pool.clone())
            .wrap(middleware::Logger::default())
            .service(
                web::scope("/health")
                    .service(resource("/db").route(get().to_async(db_check)))
                    .service(resource("/server").route(get().to(server_check)))
            )
    })
    .bind("127.0.0.1:9000")?
    .start();

    sys.run()
}
