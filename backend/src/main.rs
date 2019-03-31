
use std::io;

use actix_web::{middleware, web, App, Error, HttpResponse, HttpServer};
use futures::Future;
use r2d2::Pool;
use r2d2_sqlite::SqliteConnectionManager;
use serde_json::json;

fn health_check(
    db: web::Data<Pool<SqliteConnectionManager>>
) -> impl Future<Item = HttpResponse, Error = Error> {

    web::block(move || {
        let conn = db.get().unwrap();
        println!("Checking db status");

        conn.query_row("SELECT name FROM users WHERE id=$1", &[1], |row| {
            row.get::<_, String>(0)
        })
    })
    .then(|res| match res {
        Ok(user) => Ok(HttpResponse::Ok().json(user)),
        Err(err) => {
            println!("DB connection failed: {:?}", err);
            let json = json!({
                "status": "error",
                "message": "Failed to connect to the database",
            });
            Ok(HttpResponse::BadRequest().json(json))
        }
    })
}

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
            .route("/health", web::get().to_async(health_check))
    })
    .bind("127.0.0.1:9000")?
    .start();

    sys.run()
}
