
use actix_web::{web, Error, HttpRequest, HttpResponse};
use futures::Future;
use r2d2::Pool;
use serde_json::json;
use r2d2_sqlite::SqliteConnectionManager;

pub fn db_check(
    db: web::Data<Pool<SqliteConnectionManager>>
) -> impl Future<Item = HttpResponse, Error = Error> {

    web::block(move || {
        let conn = db.get().unwrap();
        println!("Checking db status");

        conn.query_row("SELECT name FROM comics WHERE id=$1", &[1], |row| {
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

pub fn server_check(_: HttpRequest) -> HttpResponse {
    let json = json!({
        "status": "ok",
        "message": "Server is running",
    });
    HttpResponse::Ok().json(json)
}