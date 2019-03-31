
use std::io;

use actix_web::{middleware, web, App, HttpServer};
use actix_web::web::{resource, get};

mod health;
use health::{db_check, server_check};

mod comics;
use comics::{get_web_comics};

mod db;

fn main() -> io::Result<()> {
    let sys = actix_rt::System::new("r2d2-example");
    env_logger::init();
    println!("Starting WebComicReader backend...");

    let pool = db::init();

    HttpServer::new(move || {
        App::new()
            .data(pool.clone())
            .wrap(middleware::Logger::default())
            .service(
                web::scope("/health")
                    .service(resource("/db").route(get().to_async(db_check)))
                    .service(resource("/server").route(get().to(server_check)))
            )
            .service(
                web::scope("/comics")
                    .route("", get().to_async(get_web_comics))   
            )
    })
    .bind("127.0.0.1:9000")?
    .start();

    sys.run()
}
