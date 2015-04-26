package pkg.order;

/**
 * OrderBook class for CA05
 * Modified on: 4/7/15
 * 
 * Jared Brown & Jonathan Hart (Pair Programming)
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.market.api.PriceSetter;

public class OrderBook {
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		// Populate the buyOrders and sellOrders data structures, whichever
		// appropriate
		if (order instanceof BuyOrder) {
			// get arraylist of orders for stock
			ArrayList<Order> orders;
			if (buyOrders.containsKey(order.getStockSymbol())) {
				orders = buyOrders.get(order.getStockSymbol());
			} else { // if none exists, create a new arraylist
				orders = new ArrayList<Order>();
			}
			
			// add order to orders arraylist for stock then update buyOrders structure
			orders.add(order);
			buyOrders.put(order.getStockSymbol(), orders);
		} else {
			// get arraylist of orders for stock
			ArrayList<Order> orders;
			if (sellOrders.containsKey(order.getStockSymbol())) {
				orders = sellOrders.get(order.getStockSymbol());
			} else { // if none exists, create a new arraylist
				orders = new ArrayList<Order>();
			}
			
			// add order to orders arraylist for stock then update sellOrders structure
			orders.add(order);
			sellOrders.put(order.getStockSymbol(), orders);
		}
		
	}

	public void trade() {
		// Complete the trading.
		// 1. Follow and create the orderbook data representation (see spec)
		// 2. Find the matching price
		// 3. Update the stocks price in the market using the PriceSetter.
		// Note that PriceSetter follows the Observer pattern. Use the pattern.
		// 4. Remove the traded orders from the orderbook
		// 5. Delegate to trader that the trade has been made, so that the
		// trader's orders can be placed to his possession (a trader's position
		// is the stocks he owns)
		// (Add other methods as necessary)
		
		for (String stock : sellOrders.keySet()) {
			if (buyOrders.containsKey(stock)) {
				ArrayList<Order> buying = buyOrders.get(stock);
				ArrayList<Order> selling = sellOrders.get(stock);
				ArrayList<Order> orders;
				
				TreeMap<Double, ArrayList<Order>> sortedOrders = new TreeMap<Double, ArrayList<Order>>();
				
				
				// populate sortedOrders with both buy and sell orders for each price
				for (Order buyOrder : buying) {
					// get arraylist of orders for price
					if (sortedOrders.containsKey(buyOrder.getPrice())) {
						orders = sortedOrders.get(buyOrder.getPrice());
					} else { // if none exists, create a new arraylist
						orders = new ArrayList<Order>();
					}
					orders.add(buyOrder);
					sortedOrders.put(buyOrder.getPrice(), orders);
				}
				for (Order sellOrder : selling) {
					// get arraylist of orders for price
					if (sortedOrders.containsKey(sellOrder.getPrice())) {
						orders = sortedOrders.get(sellOrder.getPrice());
					} else { // if none exists, create a new arraylist
						orders = new ArrayList<Order>();
					}
					orders.add(sellOrder);
					sortedOrders.put(sellOrder.getPrice(), orders);
				}
				
				// handle and remove market orders
				ArrayList<Order> marketOrders = new ArrayList<Order>();
				int runningBuyTotal = 0, runningSellTotal = 0;
				double marketPrice = m.getStockForSymbol(stock).getPrice();
				if (sortedOrders.containsKey(0.0)) {
					marketOrders = sortedOrders.remove(0.0);
					
					for (Order marketOrder : marketOrders) {
						if (marketOrder instanceof BuyOrder) {
							runningBuyTotal += marketOrder.getSize();
						} else {
							runningSellTotal += marketOrder.getSize();
						}
					}
				}
				
				
				// construct cumulative least favorably price list
				int numPrices = sortedOrders.size();
				int[] cumulativeBuysPerPrice = new int[numPrices];
				int[] cumulativeSellsPerPrice = new int[numPrices];
				
				// add up cumulative sell orders at each price
				int i = 0;
				for (double price : sortedOrders.keySet()) {
					ArrayList<Order> ordersAtPrice = sortedOrders.get(price);

					for (Order o : ordersAtPrice) {
						if (o instanceof SellOrder) {
							runningSellTotal += o.getSize();
						}
					}
					
					cumulativeSellsPerPrice[i] = runningSellTotal;
					i++;
				}
				
				// add up cumulative sell orders at each price
				int j = numPrices - 1;
				for (double price : sortedOrders.descendingKeySet()) {
					ArrayList<Order> ordersAtPrice = sortedOrders.get(price);

					for (Order o : ordersAtPrice) {
						if (o instanceof BuyOrder) {
							runningBuyTotal += o.getSize();
						}
					}
					
					cumulativeBuysPerPrice[j] = runningBuyTotal;
					j--;
				}
				
				// find matching price
				int delta = Integer.MAX_VALUE;
				int k = 0, matchingIndex = -1;
				double matchingPrice = marketPrice;
				
				while (delta > 0 && k < numPrices) {
					int newDelta = cumulativeBuysPerPrice[k] - cumulativeSellsPerPrice[k];
					if (newDelta < delta) {
						delta = newDelta;
						if (newDelta >= 0) {
							matchingIndex = k;
						}
					}
					k++;
				}
				for (double price : sortedOrders.keySet()) {
					if (matchingIndex == 0) {
						matchingPrice = price;
						break;
					}
					matchingIndex--;
				}
			
				// update stock price in market using observer method (PriceSetter)
				PriceSetter priceSetter = new PriceSetter();
				priceSetter.registerObserver(m.getMarketHistory());
				m.getMarketHistory().setSubject(priceSetter);
				if (matchingPrice != marketPrice) {
					priceSetter.setNewPrice(m, stock, matchingPrice);
				}
				
				// remove traded orders from orderbook and delegate to trader
				for (Order marketOrder : marketOrders) { // remove market trades
					if (marketOrder instanceof BuyOrder) {
						buyOrders.get(stock).remove(marketOrder);
					} else {
						sellOrders.get(stock).remove(marketOrder);
					}
					
					try { // delegate to trader
						marketOrder.getTrader().tradePerformed(marketOrder, matchingPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
				}
				
				for (double price : sortedOrders.keySet()) { // remove all other executed trades
					ArrayList<Order> ordersAtPrice = sortedOrders.get(price);

					for (Order o : ordersAtPrice) {
						if (o instanceof BuyOrder && price >= matchingPrice) {
							buyOrders.get(stock).remove(o);
							
							try { // delegate to trader
								o.getTrader().tradePerformed(o, matchingPrice);
							} catch (StockMarketExpection e) {
								e.printStackTrace();
							}
						} else if (o instanceof SellOrder && price <= matchingPrice) {
							sellOrders.get(stock).remove(o);
							
							try { // delegate to trader
								o.getTrader().tradePerformed(o, matchingPrice);
							} catch (StockMarketExpection e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

}
