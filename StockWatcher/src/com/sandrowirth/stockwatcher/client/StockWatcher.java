package com.sandrowirth.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public class StockWatcher implements EntryPoint {

  private static final int REFRESH_INTERVAL = 3000;
  private VerticalPanel mainPanel = new VerticalPanel();
  private FlexTable stocksFlexTable = new FlexTable();
  private HorizontalPanel addPanel = new HorizontalPanel();
  private TextBox newSymbolTextBox = new TextBox();
  private Button addStockButton = new Button("Add");
  private Label lastUpdatedLabel = new Label();
  private ArrayList<String> stocks = new ArrayList<String>();

  /**
   * Entry point method.
   */
  public void onModuleLoad() {
	  
    // Create table for stock data.
    stocksFlexTable.setText(0, 0, "Symbol");
    stocksFlexTable.setText(0, 1, "Price");
    stocksFlexTable.setText(0, 2, "Change");
    stocksFlexTable.setText(0, 3, "Remove");
    
    //Add styles to elements in the sock list table
    stocksFlexTable.setCellPadding(6);
    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
    stocksFlexTable.addStyleName("watchList");
    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListPriceColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListChangeColumn");
    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");

    // Assemble Add Stock panel.
    newSymbolTextBox.getElement().setPropertyString("placeholder", "Enter stock symbol");
    addPanel.add(newSymbolTextBox);
    addPanel.add(addStockButton);
    addPanel.addStyleName("addPanel");

    // Assemble Main panel.
    mainPanel.add(stocksFlexTable);
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);

    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);

    // Move cursor focus to the input box.
    newSymbolTextBox.setFocus(true);
    
    //Setup timer to refresh list automatically
    Timer refreshTimer = new Timer() {
		@Override
		public void run() {
			//Update only necessary when list is not empty
			if (!stocks.isEmpty())
				refreshWatchList();
		}
    };
    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
    
    // Listen for mouse events on the Add button
    addStockButton.addClickHandler(new ClickHandler() {
    	public void onClick(ClickEvent event) {
    		addStock();
    	}
    });
    
    //Listen for pressing enter in input box
    newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				addStock();
			}
		}
	});
    
  }

  /**
   * Adds stock to FlexTable when the user clicks on the addStockButton 
   * or presses enter in the newSymbolTextBox
   */
  private void addStock() {
	final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
	newSymbolTextBox.setFocus(true);
	
	if (!symbol.matches("^[0-9A-Z&#92;&#92;.]{1,10}$")) {
		Window.alert("'" + symbol + "' is not a valid symbol.");
		newSymbolTextBox.selectAll();
		return;
	}
	
	if (stocks.contains(symbol)) {
		Window.alert("'" + symbol + "' is already in the list");
		newSymbolTextBox.selectAll();
		return;
	}
		
	
	//Add stock to the table
	int row = stocksFlexTable.getRowCount();
	stocks.add(symbol);
	stocksFlexTable.setText(row, 0, symbol);
	stocksFlexTable.setWidget(row, 2, new Label());
	stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListPriceColumn");
	stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListChangeColumn");
	stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");
	
	//Add a button to remove this stock from the table
	Button removeStockButton = new Button("x");
	removeStockButton.addStyleDependentName("remove");
	removeStockButton.addClickHandler(new ClickHandler() {
		
		
		public void onClick(ClickEvent event) {
			int removedIndex = stocks.indexOf(symbol);
			stocks.remove(removedIndex);
			stocksFlexTable.removeRow(removedIndex + 1);
		}
	});
	
	stocksFlexTable.setWidget(row, 3, removeStockButton);
	newSymbolTextBox.setText("");
	refreshWatchList();
	
  }

  /**
   * Refreshes the list with randomly generated StockPrices
   */
  private void refreshWatchList() {
	final double MAX_PRICE = 100.0;
	final double MAX_PRICE_CHANGE = 0.02;
	
	StockPrice[] prices = new StockPrice[stocks.size()];
	for (int i = 0; i < stocks.size(); i++) {
		double price = Random.nextDouble() * MAX_PRICE;
		double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);
		
		prices[i] = new StockPrice(stocks.get(i), price, change);
	}
	
	updateTable(prices);
  }
  

  /**
   * Updates all price and change fields in the table
   * @param prices The list of all StockPrices in the table
   */
  private void updateTable(StockPrice[] prices) {
	for (int i = 0; i < prices.length; i++) {
		updateTable(prices[i]);
	}
	
	//Display timestamp showing last refresh
	DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
	lastUpdatedLabel.setText("Last update: " + dateFormat.format(new Date()));
  }
  

  /**
   * Updates the price and change of the given StockPrice
   * @param stockPrice The StockPrice that will be updated
   */
  private void updateTable(StockPrice stockPrice) {
	  
	//Return if symbol is not in list
	if (!stocks.contains(stockPrice.getSymbol())) {
		return;
	}
	
	int row = stocks.indexOf(stockPrice.getSymbol()) + 1;
	
	//Format the data in the Price and Change fields
	String priceText = NumberFormat.getFormat("#,##0.00").format(stockPrice.getPrice());
	NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
	String changeText = changeFormat.format(stockPrice.getChange());
	String changePercentText = changeFormat.format(stockPrice.getChangePercent());
	
	//Fills the fields of the table with the new values
	stocksFlexTable.setText(row, 1, priceText);
	Label changeWidget = (Label)stocksFlexTable.getWidget(row, 2);
	changeWidget.setText(changeText + " (" + changePercentText + "%)");
	
	//Change the color of text in the Change field based on its value
	String changeStyleName = "noChange";
	if (stockPrice.getChangePercent() < -0.1f) {
		changeStyleName = "negativeChange";
	} else if (stockPrice.getChangePercent() > 0.1f) {
		changeStyleName = "positiveChange";
	}
	
	changeWidget.setStyleName(changeStyleName);
  }
}











