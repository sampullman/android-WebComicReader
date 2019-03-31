use actix_web::{web, Error, HttpResponse};
use futures::Future;
use r2d2::Pool;
use serde_json::json;
use r2d2_sqlite::SqliteConnectionManager;
use crate::db;

pub fn get_web_comics(
    db: web::Data<Pool<SqliteConnectionManager>>
) -> impl Future<Item = HttpResponse, Error = Error> {

    web::block(move || {
        let conn = db.get().unwrap();
        db::get_web_comics(conn)
    })
    .then(|res| match res {
        Ok(comics) => {
            let json = json!({
                "status": "ok",
                "comics": comics,
            });
            Ok(HttpResponse::Ok().json(json))
        },
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