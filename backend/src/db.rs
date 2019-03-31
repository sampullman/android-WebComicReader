
use failure::Error;
use r2d2::Pool;
use r2d2_sqlite::{SqliteConnectionManager};
use rusqlite::{NO_PARAMS};
use serde::Serialize;

pub type Connection = r2d2::PooledConnection<r2d2_sqlite::SqliteConnectionManager>;

#[derive(Debug, Serialize)]
pub struct WebComic {
    id: i32,
    name: String,
    count: u32,
    random: bool,
    store: String,
}

pub fn init() -> Pool<SqliteConnectionManager> {
    let manager = SqliteConnectionManager::file("db/development.db");
    let pool = r2d2::Pool::new(manager).unwrap();

    let conn = pool.clone().get().unwrap();

    match conn.execute(
        "CREATE TABLE web_comics (\
            id          INTEGER PRIMARY KEY,\
            name        TEXT NOT NULL,\
            count       INTEGER,
            random      INTEGER,
            store       TEXT)",
        NO_PARAMS
    ) {
        Err(err) => println!("{:?}", err),
        _ => ()
    };

    pool
}

pub fn get_comics_count(conn: Connection) -> Result<i64, Error> {
    conn.query_row(
        "SELECT COUNT(*) FROM web_comics",
        NO_PARAMS,
        |r| Ok(r.get(0)),
    )?
}

pub fn get_web_comics(conn: Connection) -> Result<Vec<WebComic>, Error> {
    let mut prep_stmt = conn.prepare("SELECT id, name, count, random, store FROM web_comics")?;
    let comics = prep_stmt
        .query_map(NO_PARAMS, |row| WebComic {
            id: row.get(0),
            name: row.get(1),
            count: row.get(2),
            random: row.get::<_, i32>(3) == 1,
            store: row.get(4),
        })
        .and_then(|mapped_rows| {
            Ok(mapped_rows
                .map(|row| row.unwrap())
                .collect::<Vec<WebComic>>())
        })?;
    Ok(comics)
}
