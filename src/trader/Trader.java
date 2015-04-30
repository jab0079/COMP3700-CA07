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
	String name;
	double cashInHand;
	ArrayList<Order> position;
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market market, String symbol, int volume)
			throws StockMarketExpection {
		double orderPrice = market.getStockForSymbol(symbol).getPrice();
		
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

	public void placeNewOrder(Market market, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Get total stock price
		double totalPrice = price * volume;
		
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
		
		market.addOrder(theOrder);
		this.ordersPlaced.add(theOrder);
	}

	public void placeNewMarketOrder(Market market, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Similar to the other method, except the order is a market order
		
		double totalPrice = market.getStockForSymbol(symbol).getPrice() * volume;
		
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
		
		market.addOrder(theOrder);
		this.ordersPlaced.add(theOrder);
	}

	public void tradePerformed(Order order, double matchPrice)
			throws StockMarketExpection {
		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		
		if (!OrderUtility.isAlreadyPresent(ordersPlaced, order)) {
			throw new StockMarketExpection("Order does not exist in ordersPlaced");
		}
		
		if (SellOrder.class.isInstance(order)) {
			this.cashInHand += matchPrice * order.getSize();
			OrderUtility.findAndExtractOrder(position, order.getStockSymbol());
			
		} else if (BuyOrder.class.isInstance(order)) {
			this.cashInHand -= matchPrice * order.getSize();
			this.position.add(order);
		}
		
		this.ordersPlaced.remove(order);
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order order : position) {
			order.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order order : ordersPlaced) {
			order.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
