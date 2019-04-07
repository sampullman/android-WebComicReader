use failure::Error;
use r2d2::Pool;
use r2d2_sqlite::{SqliteConnectionManager};
use crate::db::Comic;

mod smbc;
use smbc::SMBCTrawler;

pub enum ComicType {
    SMBC,
}

#[derive(Debug)]
pub struct TrawlResult {
    next_url: Option<String>,
    comic: Comic,
}

trait ComicTrawler {
    fn first_url(&self) -> String;
    fn get_comic(&self, url: String) -> Result<TrawlResult, TrawlError>;
}

#[derive(Debug, Fail)]
pub enum TrawlError {
    #[fail(display = "Url not found: {}", url)]
    InvalidUrl {
        url: String,
    },
    #[fail(display = "No comic found at: {}", url)]
    NoComicFound {
        url: String,
    },
}

fn from_type(comic_type: ComicType) -> impl ComicTrawler {
    match comic_type {
        ComicType::SMBC => SMBCTrawler {},
    }
}

pub fn trawl_full(db: Pool<SqliteConnectionManager>, comic_type: ComicType) -> Result<TrawlResult, TrawlError> {
    let trawler = from_type(comic_type);
    trawler.get_comic(trawler.first_url())
}
