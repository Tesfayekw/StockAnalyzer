/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umuc.swen.java.stockanalyzer.scrappers;

import com.umuc.swen.java.stockanalyzer.scrappers.*;
import java.io.IOException;
import java.text.ParseException;
import com.umuc.swen.java.stockanalyzer.daomodels.StockSummary;
import com.umuc.swen.java.stockanalyzer.daomodels.StockTicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.umuc.swen.java.stockanalyzer.Constants;
import com.umuc.swen.java.stockanalyzer.StockReporter;
import com.umuc.swen.java.stockanalyzer.Utility;
import com.umuc.swen.java.stockanalyzer.daomodels.StockDateMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author craigscheiderer
 */
public class FidelityScraper extends StockScraper {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    /**
     * default constructor
     */
    private boolean test = false;
    private Document document;
    private StockSummary summaryData;

    public FidelityScraper (){
      super("Fidelity");  
    }
     

    /**
     * Scrap summary data
     */
    
    public List scrapeAllSummaryData() {
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
    

    public void scrapeSingleSummaryData(StockTicker stockTicker) throws IOException, ParseException, Exception {
        logger.log(Level.INFO, "Scrapping: {0}", stockTicker.getSymbol());
        System.out.println("Scraping: " + stockTicker.getSymbol());
        String url = "https://eresearch.fidelity.com/eresearch/goto/evaluate/snapshot.jhtml?symbols=" + stockTicker.getSymbol().toLowerCase();
        try {
            if (!test) {
                Connection jsoupConn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
            document = jsoupConn.referrer("http://www.google.com").timeout(1000 * 20).get();
            }

            Date stockDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            if((latestScrappedDate!=null && stockDate.compareTo(latestScrappedDate) > 0) || latestScrappedDate == null){
                Element table1 = document.select("table tr td:has(table)").get(0);
               // Elements table11 = table1.select("td");
                Elements rows = table1.select("tr");    
                summaryData = new StockSummary();
                
                summaryData.setSource(dao.getStockSourceByName(Constants.SCRAP_DATA_FROM_FIDELITY));
                summaryData.setTicker_symbol(stockTicker.getSymbol());
                summaryData.setTicker_name(stockTicker.getTicker_name());
                summaryData.setStock_record_date(new SimpleDateFormat("yyyy-MM-dd").format(stockDate).toString());

       
    //Getting bid price            
            String bidPrice = rows.get(1).select("td").get(0).text();
            summaryData.setBidPrice(Utility.convertStringCurrency(Utility.isBlank(bidPrice) ? "0" : bidPrice));
              
              
    //Getting ask price          
            String askPrice = rows.get(3).select("td").get(0).text();
            summaryData.setAskPrice(Utility.convertStringCurrency(Utility.isBlank(askPrice) ? "0" : askPrice));
              
              
    //Getting open price            
            String openPrice = rows.get(5).select("td").get(0).text();
            summaryData.setOpenPrice(Utility.convertStringCurrency(Utility.isBlank(openPrice) ? "0" : openPrice));
              
              
    //Getting day's range high/max        
            String daysRangeMax = rows.get(6).select("td").get(0).text();
            summaryData.setDaysRangeMax(Utility.convertStringCurrency(Utility.isBlank(daysRangeMax) ? "0" : daysRangeMax));
              
    
    //Getting day's range low/min          
            String daysRangeMin = rows.get(7).select("td").get(0).text();
            summaryData.setDaysRangeMin(Utility.convertStringCurrency(Utility.isBlank(daysRangeMin) ? "0" : daysRangeMin));
              
              
    //Getting previous close price        
            String prevClosePrice = rows.get(8).select("td").get(0).text();
            summaryData.setPrevClosePrice(Utility.convertStringCurrency(Utility.isBlank(prevClosePrice) ? "0" : prevClosePrice));
              
              
    //Getting 52 weeks max          
            String fiftyTwoWeeksMax = rows.get(9).select("td").get(0).text();
            summaryData.setFiftyTwoWeeksMax(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMax) ? "0" : fiftyTwoWeeksMax));
              
              
    //Getting 52 weeks max           
            String fiftyTwoWeeksMin = rows.get(10).select("td").get(0).text();
            summaryData.setFiftyTwoWeeksMin(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMin) ? "0" : fiftyTwoWeeksMin));
              
              
    //Getting volume          
            String volume = rows.get(13).select("td").get(0).text();
            summaryData.setVolume(Utility.convertStringCurrency(Utility.isBlank(volume) ? "0" : volume).longValue());
              
    
    //Getting average  90-Day Average Volume (not similar to  avarage volume on other sites)          
//            String avgVolume = rows.get(10).select("td").get(0).text().substring(1, 6);
//            summaryData.setAvgVolume(Utility.convertStringCurrency(Utility.isBlank(avgVolume) ? "0" : avgVolume).longValue());
//              
           
            Element table2 = document.select("table").get(19);
            rows = table2.select("tr");
            String avgVolume = null; //Fidelity does not provide this data on their website  
    //Getting market cap        
            String marketCap = rows.get(1).select("td").get(0).text();
            String marketCapFormatted = marketCap.substring(1);
            summaryData.setMarketCap(Utility.convertStringCurrency(Utility.isBlank(marketCapFormatted) ? "0" : marketCapFormatted));
              
              
    //Getting beta Coefficient         
            String betaCoefficient = rows.get(3).select("td").get(0).text();
            summaryData.setBetaCoefficient(Utility.convertStringCurrency(Utility.isBlank(betaCoefficient) ? "0" : betaCoefficient));
              
              
    //Getting EPS        
            String eps = rows.get(4).select("td").get(0).text().substring(1);
            summaryData.setEps(Utility.convertStringCurrency(Utility.isBlank(eps) ? "0" : eps));
              
              
    //Getting PE Ratio      
            String peRatio = rows.get(7).select("td").get(0).text();

            if (peRatio.matches("[^0-9]+$")) {
                peRatio = "0";
            }

            summaryData.setPeRatio(Utility.convertStringCurrency(Utility.isBlank(peRatio) ? "0" : peRatio));
              
    //Getting EX dividend          
//            String exDividendDate = rows.get(8).select("th").get(0).text().substring(27);
//            summaryData.setExDividentDate(exDividendDate);
            String exDividendDate = null; //Fidelity does not provide this data on their website
              
              
    //Getting dividend yield           
            String dividend = rows.get(8).select("td").get(0).text();
            if (dividend.matches("[^0-9]+$")) {
                dividend = "0";
            }
         
            summaryData.setDividentYield(Utility.convertStringCurrency(Utility.isBlank(dividend) ? "0" : dividend));
              
              
            String earningDate = null;//Fidelity does not provide this data on their website

            String onYearTargetEst = null;//Fidelity does not provide this data on their website

             dao.insertStockSummaryData(summaryData);
            }
        
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            throw ex;
        } catch (ParseException px) {
            logger.log(Level.SEVERE, px.getLocalizedMessage());
            throw px;
        }catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            throw e;
        }
    }
 
}
