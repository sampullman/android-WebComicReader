
use actix_web::{web, Error, HttpRequest, HttpResponse};
use futures::Future;
use r2d2::Pool;
use serde_json::json;
use r2d2_sqlite::SqliteConnectionManager;

use crate::db::{get_comics_count};
use crate::util::{api_error};

pub fn db_check(
    db: web::Data<Pool<SqliteConnectionManager>>
) -> impl Future<Item = HttpResponse, Error = Error> {

    web::block(move || {
        let conn = db.get().unwrap();
        get_comics_count(conn)
    })
    .then(|res| match res {
        Ok(count) => {
            let json = json!({
                "status": "ok",
                "message": format!("Database running with {} comics", count),
            });
            Ok(HttpResponse::Ok().json(json))
        },
        _ => api_error("Failed to connect to the database")
    })
}

pub fn server_check(_: HttpRequest) -> HttpResponse {
    let json = json!({
        "status": "ok",
        "message": "Server is running",
    });
    HttpResponse::Ok().json(json)
}