//Anthony (Tony) Marsalla for Matt Bailey on behalf of CBOE
//this code generates the same output as my python code but I wanted to write it in Java as well 

package cboePITCH2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

	public class PitchProcessor {
		 //creating a helper class Order, using static to create only one copy in memory
	    static class Order {
	        String type;
	        String orderId;
	        int shares;
	        String symbol;

	        Order(String type, String orderId, int shares, String symbol) {
	            this.type = type;
	            this.orderId = orderId;
	            this.shares = shares;
	            this.symbol = symbol;
	        }
	    }

	    //creating method to parse data
	    static Order parseLine(String line) {
	    	//skipping index 0 which is equivalent to 'S'
	        line = line.substring(1);
	        //timestamp proceeds followed by the message type at position 9 or index 8
	        char messageType = line.charAt(8);
	      //for java, a switch case approach helps the time complexity remain near O(1)
	        switch (messageType) {
	        //the case in which its an add order
	            case 'A':
	            	//trim outside of range
	                String addOrder = line.substring(9, 21).trim();
	                int addShares = Integer.parseInt(line.substring(22, 28).trim());
	                String addSymbol = line.substring(28, 34).trim();
	                //returns a new instance of the order class
	                return new Order("add", addOrder, addShares, addSymbol);
	                //the case in which its an executed order
	            case 'E':
	                String executeOrder = line.substring(9, 21).trim();
	                int executeShares = Integer.parseInt(line.substring(21, 27).trim());
	                //returning new instance of order class
	                return new Order("execute", executeOrder, executeShares, null);
	                //the case in which its a trade order, or hidden order
	            case 'P':
	                String tradeSymbol = line.substring(28, 34).trim();
	                int executeTrade = Integer.parseInt(line.substring(22, 28).trim());
	                //returning new instance of order class
	                return new Order("trade", null, executeTrade, tradeSymbol);
	                //the case in which its a cancelled order
	            case 'X':
	                String orderIdX = line.substring(9, 21).trim();
	                int canceledSharesX = Integer.parseInt(line.substring(21, 27).trim());
	                //returning new instance of order class
	                return new Order("cancel", orderIdX, canceledSharesX, null);
	            default:
	            	//ignore all other messages 
	                return null;  
	        }
	    }

	    public static void main(String[] args) {
	    	//keeping time complexity under O(n), like a dictionary, I'm utilizing a hashmap data structure
	    	//storing orders
	        Map<String, Order> orders = new HashMap<>();  
	        //storing symbols
	        Map<String, Integer> symbolVolumes = new HashMap<>();  
	        //read in file
	        String pitchINFO = "pitch_example_data.txt"; 
	        //buffering the data for the .txt file
	        try (BufferedReader bufferedPitch = new BufferedReader(new FileReader(pitchINFO))) {
	            String line;
	            while ((line = bufferedPitch.readLine()) != null) {
	            	//parsing each line
	                Order parsed = parseLine(line);  
	                //for java, a switch case approach helps the time complexity remain near O(1)
	                if (parsed != null) {
	                    switch (parsed.type) {
	                        case "add":
	                            orders.put(parsed.orderId, parsed);  // Store 'add' order details
	                            break;
	                        case "execute":
	                        	//retrieving the orders that are executed
	                            Order executeOrder = orders.get(parsed.orderId); 
	                            if (executeOrder != null) {
	                            	//getting the symbol from the add order
	                                String symbol = executeOrder.symbol;  
	                                //min function used here will calculate the executed volume, but may still result in remaining shares
	                                int executedVolume = Math.min(parsed.shares, executeOrder.shares);  
	                             //updating the executed volume for the symbol
	                                symbolVolumes.put(symbol, symbolVolumes.getOrDefault(symbol, 0) + executedVolume);  
	                                executeOrder.shares -= executedVolume; 
	                                //if all shares have been executed, remove the order
	                                if (executeOrder.shares == 0) {
	                                    orders.remove(parsed.orderId);  
	                                }
	                            }
	                            break;
	                        case "trade":
	                        	//getting symbol for the trade message
	                            String symbolT = parsed.symbol;  
	                            //update the executed volume in the map
	                            symbolVolumes.put(symbolT, symbolVolumes.getOrDefault(symbolT, 0) + parsed.shares);  
	                            break;
	                        case "cancel":
	                        	//retrieving orders
	                            Order cancelledOrder = orders.get(parsed.orderId);  
	                            //if cancelled orders exist
	                            if (cancelledOrder != null) {
	                            //reducing the number of shares in the order by the number of canceled shares
	                                cancelledOrder.shares -= parsed.shares;  
	                                //if remaining shares are zero or less, remove the order
	                                if (cancelledOrder.shares <= 0) {
	                                    orders.remove(parsed.orderId);  
	                                }
	                            }
	                            break;
	                    }
	                }
	            }
	            //catch should close the buffer to prevent any data leaks
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        //creating a list of maps where the key is the stock symbol and value is the executed volume
	        //since I want to only print out one symbol with the total volume, Map.Entry will get a single pair of the data
	        List<Map.Entry<String, Integer>> topExecutedVolume = new ArrayList<>(symbolVolumes.entrySet());
	        //sorting elements with a lambda expression and utilizing comparison logic to sort in decending order
	        topExecutedVolume.sort((a, b) -> b.getValue() - a.getValue());
	        topExecutedVolume = topExecutedVolume.subList(0, Math.min(topExecutedVolume.size(), 10));

	        //iterate over the entries and print
	        for (Map.Entry<String, Integer> entry : topExecutedVolume) {
	            System.out.println(entry.getKey() + " " + entry.getValue());
	        }
	    }
	}
