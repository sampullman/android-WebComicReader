
use r2d2::Pool;
use r2d2_sqlite::{SqliteConnectionManager};
use rusqlite::{Error, NO_PARAMS};
use serde::Serialize;

pub type Connection = r2d2::PooledConnection<r2d2_sqlite::SqliteConnectionManager>;

#[derive(Debug, Serialize)]
enum AltType {
    NONE = 0,
    TEXT = 1,
    IMAGE = 2,
}

fn int_to_alttype(n: i32) -> AltType {
    match n {
        1 => AltType::TEXT,
        2 => AltType::IMAGE,
        _ => AltType::NONE,
    }
}

#[derive(Debug, Serialize)]
pub struct WebComic {
    id: i32,
    name: String,
    count: u32,
    random: bool,
    alt: AltType,
    store: String,
}

#[derive(Debug, Serialize)]
pub struct Comic {
    url: String,
    random: bool,
    alt: String,
}

pub fn init() -> Pool<SqliteConnectionManager> {
    let manager = SqliteConnectionManager::file("db/development.db");
    let pool = r2d2::Pool::new(manager).unwrap();

    let conn = pool.clone().get().unwrap();

    match conn.execute(
        "CREATE TABLE IF NOT EXISTS web_comics (\
            id          INTEGER PRIMARY KEY,\
            name        TEXT NOT NULL,\
            count       INTEGER,\
            random      INTEGER,\
            alt         INTEGER,\
            store       TEXT)",
        NO_PARAMS
    ) {
        Err(err) => println!("{:?}", err),
        _ => ()
    };

    match conn.execute(
        "CREATE TABLE IF NOT EXISTS comics (\
            id          INTEGER PRIMARY KEY,\
            url         TEXT NOT NULL,\
            random      TEXT,\
            alt         INTEGER)",
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
    let mut prep_stmt = conn.prepare("SELECT id, name, count, random, alt, store FROM web_comics")?;
    let comics = prep_stmt
        .query_map(NO_PARAMS, |row| Ok(WebComic {
            id: row.get(0)?,
            name: row.get(1)?,
            count: row.get(2)?,
            random: row.get::<_, i32>(3)? == 1,
            alt: int_to_alttype(row.get(4)?),
            store: row.get(5)?,
        }))
        .and_then(|mapped_rows| {
            Ok(mapped_rows
                .map(|row| row.unwrap())
                .collect::<Vec<WebComic>>())
        })?;
    Ok(comics)
}

pub fn get_comic(conn: Connection, id: i32) -> Result<Comic, Error> {

    conn.query_row("SELECT url, random, alt FROM comics WHERE id=$1", &[&id], |row| {
        Ok(Comic {
            url: row.get(0)?,
            random: row.get(1)?,
            alt: row.get(2)?,
        })
    })
}
