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
    #[fail(display = "Can't acces url: {}", url)]
    PageLoadFail {
        url: String,
    },
    #[fail(display = "Failed to parse result at: {}", url)]
    PageParseFail {
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

pub fn trawl_full(db: Pool<SqliteConnectionManager>, comic_type: ComicType) -> Result<(), TrawlError> {

    let trawler = from_type(comic_type);
    let mut next_url = Some(trawler.first_url());

    while let Some(url) = next_url {
        let result = trawler.get_comic(url)?;
        next_url = result.next_url;
    }
    Ok(())
}
