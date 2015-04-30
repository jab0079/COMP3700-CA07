package pkg.client;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.api.IPO;
import pkg.order.OrderType;
import pkg.trader.Trader;

public class Client {
	public static void main(String[] args) {
		Market nasdaq = new Market("NASDAQ");
		IPO.enterNewStock(nasdaq, "SBUX", "Starbucks Corp.", 92.86);
		IPO.enterNewStock(nasdaq, "TWTR", "Twitter Inc.", 47.88);
		IPO.enterNewStock(nasdaq, "VSLR", "Vivint Solar", 16.44);
		IPO.enterNewStock(nasdaq, "GILD", "Gilead Sciences", 93.33);

		nasdaq.printStocks();

		Market nikkei = new Market("Nikkei");
		IPO.enterNewStock(nikkei, "BABA", "Alibaba", 84.88);
		IPO.enterNewStock(nikkei, "BDU", "Baidu", 253.66);

		nikkei.printStocks();

		Trader trader1 = new Trader("Neda", 200000.00);
		Trader trader2 = new Trader("Scott", 100000.00);
		Trader trader3 = new Trader("Luke", 100000.00);
		Trader trader4 = new Trader("Thomas", 100000.00);
		Trader trader5 = new Trader("Sritika", 100000.00);
		Trader trader6 = new Trader("Meg", 100000.00);
		Trader trader7 = new Trader("Jen", 100000.00);
		Trader trader8 = new Trader("Emory", 100000.00);
		Trader trader9 = new Trader("Justin", 100000.00);
		Trader trader10 = new Trader("Zach", 100000.00);
		Trader trader11 = new Trader("Matt", 100000.00);
		Trader trader12 = new Trader("Angela", 100000.00);
		Trader trader13 = new Trader("Hamza", 100000.00);
		Trader trader14 = new Trader("Ethan", 100000.00);
		Trader trader15 = new Trader("T1", 300000.00);
		Trader trader16 = new Trader("T2", 300000.00);

		try {
			trader1.buyFromBank(nasdaq, "SBUX", 1600);
			trader2.buyFromBank(nasdaq, "SBUX", 300);
			trader3.buyFromBank(nasdaq, "SBUX", 300);
			trader4.buyFromBank(nasdaq, "SBUX", 300);
			trader5.buyFromBank(nasdaq, "SBUX", 600);
			trader6.buyFromBank(nasdaq, "SBUX", 700);
			trader7.buyFromBank(nasdaq, "SBUX", 500);
			trader15.buyFromBank(nasdaq, "SBUX", 1500);
			trader8.buyFromBank(nasdaq, "SBUX", 5000); // should throw exception
		} catch (StockMarketExpection e) {
			e.printStackTrace();
		}
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader8.printTrader();
		trader15.printTrader();

		try {
			trader1.placeNewOrder(nasdaq, "SBUX", 100, 97.0, OrderType.SELL);
			trader2.placeNewOrder(nasdaq, "SBUX", 300, 97.5, OrderType.SELL);
			trader3.placeNewOrder(nasdaq, "SBUX", 300, 98.0, OrderType.SELL);
			trader4.placeNewOrder(nasdaq, "SBUX", 300, 98.5, OrderType.SELL);
			trader5.placeNewOrder(nasdaq, "SBUX", 500, 99.0, OrderType.SELL);
			trader6.placeNewOrder(nasdaq, "SBUX", 700, 99.5, OrderType.SELL);
			trader7.placeNewOrder(nasdaq, "SBUX", 500, 100.0, OrderType.SELL);
			trader15.placeNewMarketOrder(nasdaq, "SBUX", 1500, 0, OrderType.SELL);

		} catch (StockMarketExpection e) {
			e.printStackTrace();
		}

		System.out.println("Printing after the sell orders are placed");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader15.printTrader();

		try {
			trader8.placeNewOrder(nasdaq, "SBUX", 200, 101.0, OrderType.BUY);
			trader9.placeNewOrder(nasdaq, "SBUX", 300, 100.5, OrderType.BUY);
			trader10.placeNewOrder(nasdaq, "SBUX", 400, 100.0, OrderType.BUY);
			trader11.placeNewOrder(nasdaq, "SBUX", 500, 99.5, OrderType.BUY);
			trader12.placeNewOrder(nasdaq, "SBUX", 900, 99.0, OrderType.BUY);
			trader13.placeNewOrder(nasdaq, "SBUX", 1000, 98.5, OrderType.BUY);
			trader14.placeNewOrder(nasdaq, "SBUX", 900, 98.0, OrderType.BUY);
			trader16.placeNewMarketOrder(nasdaq, "SBUX", 700, 0, OrderType.BUY);
		} catch (StockMarketExpection e) {
			e.printStackTrace();
		}

		System.out.println("Printing after the buy orders are placed");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader8.printTrader();
		trader9.printTrader();
		trader10.printTrader();
		trader11.printTrader();
		trader12.printTrader();
		trader13.printTrader();
		trader14.printTrader();
		trader16.printTrader();

		nasdaq.triggerTrade();

		System.out.println("Printing after the tradings are done");
		System.out.println("&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^&^");
		trader1.printTrader();
		trader2.printTrader();
		trader3.printTrader();
		trader4.printTrader();
		trader5.printTrader();
		trader6.printTrader();
		trader7.printTrader();
		trader8.printTrader();
		trader9.printTrader();
		trader10.printTrader();
		trader11.printTrader();
		trader12.printTrader();
		trader13.printTrader();
		trader14.printTrader();
		trader15.printTrader();
		trader16.printTrader();

		nasdaq.printHistoryFor("SBUX");
	}
}
