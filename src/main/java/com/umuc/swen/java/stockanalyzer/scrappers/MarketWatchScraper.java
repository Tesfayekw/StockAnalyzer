/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umuc.swen.java.stockanalyzer.scrappers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.umuc.swen.java.stockanalyzer.Constants;
import com.umuc.swen.java.stockanalyzer.StockReporter;
import com.umuc.swen.java.stockanalyzer.Utility;
import com.umuc.swen.java.stockanalyzer.daomodels.StockSummary;
import com.umuc.swen.java.stockanalyzer.daomodels.StockTicker;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


//Scraps MarketWatch Financial data
public class MarketWatchScraper  extends StockScraper{

    private boolean test = false;
    private Document document;
    private StockSummary summaryData;
//    private List<StockSummary> summaryDataList;
   // private StockService stockService;
 //   private List<StockTicker> stockTickers;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    //Default constructor
    public MarketWatchScraper() {
        super("Marketwatch");
     
    }

    public List scrapeAllSummaryData() throws Exception {
       List<String> exceptionLogs = new ArrayList<String>();
        int tickerCount = 0;
        
           for(StockTicker stockTicker: stockTickers){
               try{
                scrapeSingleSummaryData(stockTicker);
                tickerCount++;
                }catch(Exception e) {
                    exceptionLogs.add(stockTickers.get(tickerCount).getSymbol() + ": " + e.getMessage());
                    tickerCount++;
                }
            }
        return exceptionLogs;
    }

    /**
     * Scrap historical data
     * @param stockTicker
     */
    public void scrapeSingleSummaryData(StockTicker stockTicker) throws Exception {
        logger.log(Level.INFO, "Scrapping: {MarketWatch}", stockTicker.getSymbol());
        System.out.println("Scrapping: " + stockTicker.getSymbol());
        String url = "https://www.marketwatch.com/investing/stock/" + stockTicker.getSymbol().toLowerCase();

        try {
            if (!test) {
                Connection jsoupConn = Jsoup.connect(url)
                
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
                
                document = jsoupConn.referrer("http://www.google.com").timeout(1000 * 20).get();
            }
            Date stockDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            if((latestScrappedDate!=null && stockDate.compareTo(latestScrappedDate) > 0) || latestScrappedDate == null){
         
            summaryData = new StockSummary();
            summaryData.setSource(dao.getStockSourceByName(Constants.SCRAP_DATA_FROM_MARKETWATCH));
            summaryData.setTicker_symbol(stockTicker.getSymbol());
            summaryData.setTicker_name(stockTicker.getTicker_name());
            summaryData.setStock_record_date(new SimpleDateFormat("yyyy-MM-dd").format(stockDate));

// Elements marketWatchdiv=document.select("div.element.element--list");
            Elements list = document.select("ul.list.list--kv.list--col50 li"); 
            String openPrice = list.select("li:nth-of-type(1) span.kv__primary").text().substring(1).trim();
            summaryData.setOpenPrice(Utility.convertStringCurrency(Utility.isBlank(openPrice) ? "0" : openPrice));

//Getting closing stock price
            Elements row = document.select("table.table--primary.align--right tr");
            String prevClosingPrice = row.select(".u-semi.table__cell").text().substring(1).trim();
            summaryData.setPrevClosePrice(Utility.convertStringCurrency(Utility.isBlank(prevClosingPrice) ? "0" : prevClosingPrice));

// Getting daily max and min stock string values
            String daysRangeMaxAndMin = list.select("li.kv__item:nth-of-type(2) > .kv__primary.kv__value").text();
               
//Splitting daily range max and min stock value
            String[] arrayOfDailySplit = daysRangeMaxAndMin.split("-");

//Days range minimum value
            String daysRangeMin = arrayOfDailySplit[0].trim();
            summaryData.setDaysRangeMin(Utility.convertStringCurrency(Utility.isBlank(daysRangeMin) ? "0" : daysRangeMin));

//Days range maximum value
            String daysRangeMax = arrayOfDailySplit[1].trim();
            summaryData.setDaysRangeMax(Utility.convertStringCurrency(Utility.isBlank(daysRangeMax) ? "0" : daysRangeMax));

//Getting 52 week max and min stock value                   
            String fiftyTwoWeekRangeMaxAndMin = list.select("li.kv__item:nth-of-type(3) > .kv__primary.kv__value").text();
           
//Splitting 52 week max and min stock value
            String[] arrayOfFiftyTwoWeekSplit = fiftyTwoWeekRangeMaxAndMin.split("-");

//Days range minimum value
            String fiftyTwoWeeksMin = arrayOfFiftyTwoWeekSplit[0].trim();
            summaryData.setFiftyTwoWeeksMin(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMin) ? "0" : fiftyTwoWeeksMin));

//Days range maximum value
            String fiftyTwoWeeksMax = arrayOfFiftyTwoWeekSplit[1].trim();
            summaryData.setFiftyTwoWeeksMax(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMax) ? "0" : fiftyTwoWeeksMax));

//Volume
            Elements div = document.select("div.range__details span");
            String volume = div.select("span.last-value.volume").text();
            summaryData.setVolume(Utility.convertStringCurrency(Utility.isBlank(volume) ? "0" : volume).longValue());

//Average Volume
            String avgVolume = list.select("li.kv__item:nth-of-type(16) > .kv__primary.kv__value").text();
            summaryData.setAvgVolume(Utility.convertStringCurrency(Utility.isBlank(avgVolume) ? "0" : avgVolume).longValue());

//Market Cap
            String marketCap = list.select("li.kv__item:nth-of-type(4) > .kv__primary.kv__value").text().substring(1).trim();
            summaryData.setMarketCap(Utility.convertStringCurrency(Utility.isBlank(marketCap) ? "0" : marketCap));

//Beta Coefficient
            String betaCoefficient = list.select("li.kv__item:nth-of-type(7) > .kv__primary.kv__value").text();
            summaryData.setBetaCoefficient(Utility.convertStringCurrency(Utility.isBlank(betaCoefficient) ? "0" : betaCoefficient));

//P/E Ratio
            String peRatio = list.select("li.kv__item:nth-of-type(9) > .kv__primary.kv__value").text();
            if (peRatio.matches("[^0-9]+$")) {
                peRatio = "0";
            }
            summaryData.setPeRatio(Utility.convertStringCurrency(Utility.isBlank(peRatio) ? "0" : peRatio));

//EPS
            String eps = list.select("li.kv__item:nth-of-type(10) > .kv__primary.kv__value").text().substring(1).trim();
            summaryData.setEps(Utility.convertStringCurrency(Utility.isBlank(eps) ? "0" : eps));
//Dividend

            String dividend = list.select("li.kv__item:nth-of-type(12) > .kv__primary.kv__value").text().substring(1).trim();
            if (dividend.matches("[^0-9]+$")) {
                dividend = "0";
            }
            summaryData.setDividentYield(Utility.convertStringCurrency(Utility.isBlank(dividend) ? "0" : dividend));

//EX-DIVIDEND DATE                             
            String exDividendDate = list.select("li.kv__item:nth-of-type(13) > .kv__primary.kv__value").text();
            summaryData.setExDividentDate(exDividendDate);
            }
            
            dao.insertStockSummaryData(summaryData);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            throw ex;
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
        }catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }



  
}
