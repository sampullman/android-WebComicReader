use crate::trawl::{ComicTrawler, TrawlError, TrawlResult};

pub struct SMBCTrawler {

}

impl ComicTrawler for SMBCTrawler {
    fn first_url(&self) -> String {
        "".to_string()
    }

    fn get_comic(&self, url: String) -> Result<TrawlResult, TrawlError> {
        Err(TrawlError::InvalidUrl {url})
    }

}