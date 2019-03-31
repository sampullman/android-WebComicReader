
use failure::Error;
use r2d2::Pool;
use r2d2_sqlite::{SqliteConnectionManager};
use rusqlite::{NO_PARAMS};

pub type Connection = r2d2::PooledConnection<r2d2_sqlite::SqliteConnectionManager>;

pub fn init() -> Pool<SqliteConnectionManager> {
    let manager = SqliteConnectionManager::file("db/development.db");
    let pool = r2d2::Pool::new(manager).unwrap();

    let conn = pool.clone().get().unwrap();

    match conn.execute(
        "CREATE TABLE comics (\
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
        "SELECT COUNT(*) FROM comics",
        NO_PARAMS,
        |r| Ok(r.get(0)),
    )?
}
