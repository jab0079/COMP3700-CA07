package pkg.trader;

/**
 * Trader class for CA05
 * Modified on: 4/2/15
 * 
 * Jared Brown & Jonathan Hart (Pair Programming)
 */

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.BuyOrder;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.order.SellOrder;
import pkg.util.OrderUtility;

public class Trader {
	// Name of the trader
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		// Buy stock straight from the bank
		// Need not place the stock in the order list
		// Add it straight to the user's position
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// Adjust cash possessed since the trader spent money to purchase a
		// stock.
		
		// Get total stock price
		double orderPrice = m.getStockForSymbol(symbol).getPrice();
		
		// Check if stock order price is more than cashInHand
		if (orderPrice * volume > cashInHand) {
			throw new StockMarketExpection("Cannot place buy order for stock: " + symbol 
					+ " since there is not enough money. Trader: " + this.name);
		}
		
		// Create the order
		BuyOrder theOrder = new BuyOrder(symbol, volume, orderPrice, this);
		
		// Add the order to the trader's position and update cashInHand
		position.add(theOrder);
		this.cashInHand -= orderPrice * volume;
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Place a new order and add to the orderlist
		// Also enter the order into the orderbook of the market.
		// Note that no trade has been made yet. The order is in suspension
		// until a trade is triggered.
		//
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// A trader cannot place two orders for the same stock, throw an
		// exception if there are multiple orders for the same stock.
		// Also a person cannot place a sell order for a stock that he does not
		// own. Or he cannot sell more stocks than he possesses. Throw an
		// exception in these cases.
		
		// Get total stock price
		double totalPrice = price * volume;
		
		// Check if stock is more than cashInHand
		if (orderType == OrderType.BUY && totalPrice > cashInHand) {
			throw new StockMarketExpection("Cannot place buy order for stock: " + symbol 
					+ " since there is not enough money. Trader: " + this.name);
		}
		
		Order theOrder;
		if (orderType == OrderType.BUY) {
			theOrder = new BuyOrder(symbol, volume, price, this);
		} else {
			theOrder = new SellOrder(symbol, volume, price, this);
		}
		
		// Check if there is an outstanding order for stock
		if (OrderUtility.isAlreadyPresent(ordersPlaced, theOrder)) {
			throw new StockMarketExpection("Cannot place order for stock: " + symbol 
					+ " since there already one in place. Trader: " + this.name);
		}
		
		// Check if trader owns the stock if selling 
		if (orderType == OrderType.SELL 
				&& !OrderUtility.owns(position, symbol)) {
			throw new StockMarketExpection("Cannot place sell order for stock: " + symbol 
					+ " since no stock is currently owned. Trader: " + this.name);
		}
		
		// Check if trader owns enough of the stock if selling
		if (orderType == OrderType.SELL 
				&& volume > OrderUtility.ownedQuantity(position, symbol)) {
			throw new StockMarketExpection("Cannot place sell order for stock: " + symbol 
					+ " since not enough stock is currently owned. Trader: " + this.name);
		}
		
		// Enter the order into the orderbook of the market
		m.addOrder(theOrder);
		
		// Add the order to the orders placed 
		this.ordersPlaced.add(theOrder);
	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Similar to the other method, except the order is a market order
		
		// Get total stock price
		double totalPrice = m.getStockForSymbol(symbol).getPrice() * volume;
		
		// Check if stock is more than cashInHand
		if (orderType == OrderType.BUY && totalPrice > cashInHand) {
			throw new StockMarketExpection("Cannot place buy order for stock: " + symbol 
					+ " since there is not enough money. Trader: " + this.name);
		}
		
		Order theOrder;
		if (orderType == OrderType.BUY) {
			theOrder = new BuyOrder(symbol, volume, true, this);
		} else {
			theOrder = new SellOrder(symbol, volume, true, this);
		}
		
		// Check if there is an outstanding order for stock
		if (OrderUtility.isAlreadyPresent(ordersPlaced, theOrder)) {
			throw new StockMarketExpection("Cannot place order for stock: " + symbol 
					+ " since there already one in place. Trader: " + this.name);
		}
		
		// Check if trader owns the stock if selling 
		if (orderType == OrderType.SELL 
				&& !OrderUtility.owns(position, symbol)) {
			throw new StockMarketExpection("Cannot place sell order for stock: " + symbol 
					+ " since no stock is currently owned. Trader: " + this.name);
		}
		
		// Check if trader owns enough of the stock if selling
		if (orderType == OrderType.SELL 
				&& volume > OrderUtility.ownedQuantity(position, symbol)) {
			throw new StockMarketExpection("Cannot place sell order for stock: " + symbol 
					+ " since not enough stock is currently owned. Trader: " + this.name);
		}
		
		// Enter the order into the orderbook of the market
		m.addOrder(theOrder);
		
		// Add the order to the orders placed 
		this.ordersPlaced.add(theOrder);
	}

	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		
		// Update orderPlaced
		//if (ordersPlaced.remove(o) == false) {
		//	throw new StockMarketExpection("Order does not exist in ordersPlaced");
		//}
		
		if (!OrderUtility.isAlreadyPresent(ordersPlaced, o)) {
			throw new StockMarketExpection("Order does not exist in ordersPlaced");
		}
		
		if (SellOrder.class.isInstance(o)) {
			// Update cashInHand
			this.cashInHand += matchPrice * o.getSize();
			// Update position
			OrderUtility.findAndExtractOrder(position, o.getStockSymbol());
			
		} else if (BuyOrder.class.isInstance(o)) {
			// Update cashInHand
			this.cashInHand -= matchPrice * o.getSize();
			// Update position
			this.position.add(o);

		}
		
		this.ordersPlaced.remove(o);
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : position) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
