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
	Market market;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market market) {
		this.market = market;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		if (order instanceof BuyOrder) {
			addToOrders(buyOrders, order);
		} else {
			addToOrders(sellOrders, order);
		}	
	}
  
	public void addToOrders(HashMap<String, ArrayList<Order>> orders, Order order) {
        	ArrayList<Order> stockOrders;
    
		if (orders.containsKey(order.getStockSymbol())) {
			stockOrders = orders.get(order.getStockSymbol());
		} else {
			stockOrders = new ArrayList<Order>();
		}
			
		stockOrders.add(order);
		orders.put(order.getStockSymbol(), stockOrders);
        }

	public void trade() {
		for (String stock : sellOrders.keySet()) {
			if (buyOrders.containsKey(stock)) {
				ArrayList<Order> buying = buyOrders.get(stock);
				ArrayList<Order> selling = sellOrders.get(stock);
				
				TreeMap<Double, ArrayList<Order>> sortedOrders = createSortedOrders(buying, selling);
				
				// Handle and remove market orders
				ArrayList<Order> marketOrders = new ArrayList<Order>();
				int runningBuyTotal = 0, runningSellTotal = 0;
				double marketPrice = market.getStockForSymbol(stock).getPrice();
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
				
				
				// Construct cumulative least favorably price list at each price
				int numPrices = sortedOrders.size();
				int[] cumulativeBuysPerPrice = new int[numPrices];
				int[] cumulativeSellsPerPrice = new int[numPrices];
				

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
				
				// Find the matching price
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
			
				// Update the stocks price in the market using the PriceSetter.
				PriceSetter priceSetter = new PriceSetter();
				priceSetter.registerObserver(market.getMarketHistory());
				market.getMarketHistory().setSubject(priceSetter);
				if (matchingPrice != marketPrice) {
					priceSetter.setNewPrice(market, stock, matchingPrice);
				}
				
				removeDelegateOrders(stock, sortedOrders, marketOrders, matchingPrice);
			}
		}
	}
  
	TreeMap<Double, ArrayList<Order>> createSortedOrders(ArrayList<Order> buying, ArrayList<Order> selling) {
        	TreeMap<Double, ArrayList<Order>> sortedOrders = new TreeMap<Double, ArrayList<Order>>();
        	ArrayList<Order> orders;
          
        	// Populate sortedOrders with both buy and sell orders for each price
        	for (Order buyOrder : buying) {
        		if (sortedOrders.containsKey(buyOrder.getPrice())) {
        			orders = sortedOrders.get(buyOrder.getPrice());
        		} else {
        			orders = new ArrayList<Order>();
        		}
        		orders.add(buyOrder);
        		sortedOrders.put(buyOrder.getPrice(), orders);
        	}
        	for (Order sellOrder : selling) {
        		if (sortedOrders.containsKey(sellOrder.getPrice())) {
        			orders = sortedOrders.get(sellOrder.getPrice());
        		} else {
        			orders = new ArrayList<Order>();
        		}
        		orders.add(sellOrder);
        		sortedOrders.put(sellOrder.getPrice(), orders);
        	}
          
        	return sortedOrders;
        }
  
	void removeDelegateOrders(Stock stock, TreeMap<Double, ArrayList<Order>> sortedOrders, ArrayList<Order> marketOrders, double matchingPrice) {
        	for (Order marketOrder : marketOrders) {
			if (marketOrder instanceof BuyOrder) {
				buyOrders.get(stock).remove(marketOrder);
			} else {
				sellOrders.get(stock).remove(marketOrder);
			}
					
			try {
				marketOrder.getTrader().tradePerformed(marketOrder, matchingPrice);
			} catch (StockMarketExpection e) {
					e.printStackTrace();
			}
		}
				
		for (double price : sortedOrders.keySet()) {
			ArrayList<Order> ordersAtPrice = sortedOrders.get(price);

			for (Order o : ordersAtPrice) {
				if (o instanceof BuyOrder && price >= matchingPrice) {
					buyOrders.get(stock).remove(o);
							
					try {
						o.getTrader().tradePerformed(o, matchingPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
				} else if (o instanceof SellOrder && price <= matchingPrice) {
					sellOrders.get(stock).remove(o);
							
					try {
						o.getTrader().tradePerformed(o, matchingPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
				}
			}
		}
        
        }

}
