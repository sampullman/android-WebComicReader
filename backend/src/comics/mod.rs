use actix_web::error::BlockingError;
use actix_web::{web, Error, HttpResponse};
use futures::Future;
use r2d2::Pool;
use serde::Deserialize;
use serde_json::json;
use r2d2_sqlite::SqliteConnectionManager;
use rusqlite::Error::QueryReturnedNoRows;
use crate::db;
use db::{Comic};

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
                        QueryReturnedNoRows => {
                            let json = json!({
                                "status": "ok",
                                "comic": Vec::new() as Vec<Comic>
                            });
                            Ok(HttpResponse::Ok().json(json))
                        },
                        err => {
                            println!("DB query failed: {:?}", err);
                            let json = json!({
                                "status": "error",
                                "message": "Failed to connect to the database",
                            });
                            Ok(HttpResponse::BadRequest().json(json))
                        }
                    }
                },
                _ => {
                    println!("DB connection failed: {:?}", err);
                    let json = json!({
                        "status": "error",
                        "message": "Failed to connect to the database",
                    });
                    Ok(HttpResponse::BadRequest().json(json))
                }
            }
        },
    })
}