use actix_web::error::BlockingError;
use actix_web::{web, Error, HttpResponse};
use futures::Future;
use r2d2::Pool;
use serde::Deserialize;
use serde_json::json;
use r2d2_sqlite::SqliteConnectionManager;
use rusqlite::Error::QueryReturnedNoRows;

use crate::db;
use crate::util::{api_error};

#[derive(Deserialize)]
pub struct UpdateParams {
    id: i32,
}

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
        _ => api_error("Failed to query the database")
    })
}

pub fn get_comic(
    db: web::Data<Pool<SqliteConnectionManager>>,
    params: web::Path<UpdateParams>,
) -> impl Future<Item = HttpResponse, Error = Error> {

    web::block(move || {
        let conn = db.get().unwrap();
        db::get_comic(conn, params.id)
    })
    .then(|res| match res {
        Ok(comic) => {
            let json = json!({
                "status": "ok",
                "comic": comic,
            });
            Ok(HttpResponse::Ok().json(json))
        },
        Err(err) => {
            match err {
                BlockingError::Error(err) => {
                    match err {
                        QueryReturnedNoRows => api_error("Comic not found"),
                        _ => api_error("Failed to query the database")
                    }
                },
                _ => api_error("Failed to connect to the database")
            }
        },
    })
}