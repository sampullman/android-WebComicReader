
use actix_web::{Error, HttpResponse};
use serde_json::json;

pub fn api_error<'a>(message: &str) -> Result<HttpResponse, Error> {

    println!("DB query failed: {:?}", message);
    let json = json!({
        "status": "error",
        "message": message,
    });
    Ok(HttpResponse::BadRequest().json(json))
}
