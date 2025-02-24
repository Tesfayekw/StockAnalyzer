-- Check to see if the view exist and if so drop it before creating the new VIEW
DROP VIEW IF EXISTS summary_view;
-- CREATES a VIEW that presents a summary by combining and averaging all data across all sources for each ticker for every time that the scrapper ran and logged to the database
CREATE VIEW summary_view(ticker_symbol, ticker_name, summary_date, prev_close_price, open_price, bid_price, ask_price, days_range_min, days_range_max, fifty_two_week_min, fifty_two_week_max, volume, avg_volume, market_cap, beta_coefficient, pe_ratio, eps, earning_date, dividend_yield, ex_dividend_date, one_year_target_est)
AS SELECT DISTINCT ticker_symbol, ticker_name, stock_record_date, avg(prev_close_price), avg(open_price), avg(bid_price), avg(ask_price), avg(days_range_min), avg(days_range_max), avg(fifty_two_week_min), avg(fifty_two_week_max), avg(volume), avg(avg_volume), avg(market_cap), avg(beta_coefficient), avg(pe_ratio), avg(eps), earning_date, avg(dividend_yield), ex_dividend_date, avg(one_year_target_est)
FROM STOCK_SUMMARY
GROUP BY ticker_symbol, ticker_name, stock_record_date;