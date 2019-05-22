use reqwest;
use crate::trawl::{ComicTrawler, TrawlError, TrawlResult};

const URL_BASE: &str = "http://www.smbc-comics.com/comic/";

pub struct SMBCTrawler;

impl ComicTrawler for SMBCTrawler {
    fn first_url(&self) -> String {
        format!("{}{}", URL_BASE, "2002-09-05")
    }

    fn get_comic(&self, url: String) -> Result<TrawlResult, TrawlError> {
        match reqwest::get(&url) {
            Ok(mut response) => {
                match response.text() {
                    Ok(page) => {
                        println!("{}", &page[0..100]);
                        Err(TrawlError::PageParseFail {url})
                    }
                    _ => Err(TrawlError::PageParseFail {url})
                }
            }
            _ => Err(TrawlError::PageLoadFail {url})
        }
    }

}