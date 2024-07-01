#Anthony (Tony) Marsalla for Matt Bailey on behalf of CBOE

"""Please note, there are a couple of other alternative approaches that can be done in order
    to accomplish this task (such as utilizing the pandas library by making the .txt file into a
    \t csv file. In my opinion, I thought my approach was the most straightforward, but can be done
    other ways."""
def parse_line(line):
   #all orders begin with 'S' which can be ignored, so start at index 1
   line = line[1:]
   #timestamp is now from index 0-7, message type is position 9 or index 8
   message_type = line[8]
    #for add orders
   if message_type == 'A':
       #default string for order ID
       order_id = line[9:21].strip()
       #get the amount of shares, cast to an integer since .txt file is a string
       shares = int(line[22:28].strip())
       #get the symbol
       symbol = line[28:34].strip()
       #utilizing dictionary where the type is equivalent to the type of order
       return {'type': 'add', 'order_id': order_id, 'shares': shares, 'symbol': symbol}
    #for executed orders
   elif message_type == 'E':
       order_id = line[9:21].strip()
       executed_shares = int(line[21:27].strip())
       return {'type': 'execute', 'order_id': order_id, 'executed_shares': executed_shares}
   #for short trade messages, or hidden orders
   elif message_type == 'P':
       symbol = line[28:34].strip()
       executed_shares = int(line[22:28].strip())
       return {'type': 'trade', 'symbol': symbol, 'executed_shares': executed_shares}
   #for cancelled orders
   elif message_type == 'X':
       order_id = line[9:21].strip()
       canceled_shares = int(line[21:27].strip())
       return {'type': 'cancel', 'order_id': order_id, 'canceled_shares': canceled_shares}
   else:
       #ignore the rest of the message types
       return None


#utilizing a dictionary data structure to keep track of order volumes and
orders = {}
symbol_volumes = {}

file_path = 'pitch_example_data.txt'
#reading the file
with open(file_path, 'r') as file:
    for line in file:
        parsed = parse_line(line)
        if parsed:
            #after parsing, the order details are stored (or 'remembered') in the dictionary
            if parsed['type'] == 'add':
                orders[parsed['order_id']] = parsed
            # after parsing, the execute details are stored (or 'remembered') in the dictionary
            elif parsed['type'] == 'execute':
                order = orders.get(parsed['order_id'])  # Retrieve the order
                #in the orders exist in the dictionary
                if order:
                    #get the stock of the order
                    symbol = order['symbol']
                    #utilizing min to get the executed shares of the order ensurinbg it doesn't surpass the remaining
                    executed_volume = min(parsed['executed_shares'], order['shares'])
                    symbol_volumes[symbol] = symbol_volumes.get(symbol, 0) + executed_volume
                    #subtractiing the remaing shares from the executed shares
                    order['shares'] -= executed_volume
                    #if all the shares have been executed, remove the order
                    if order['shares'] == 0:
                        del orders[parsed['order_id']]
                    #if the type is P, or 'trade', retrieve the orders from the dictionary
            elif parsed['type'] == 'trade':
                symbol = parsed['symbol']
                #update executed volume
                symbol_volumes[symbol] = symbol_volumes.get(symbol, 0) + parsed['executed_shares']
            elif parsed['type'] == 'cancel':
                order = orders.get(parsed['order_id'])  # Retrieve the order
                if order:
                    order['shares'] -= parsed['canceled_shares']  # Reduce remaining shares
                    if order['shares'] <= 0:
                        del orders[parsed['order_id']]  # Remove order if fully canceled

#using .items() to return a tuple of the dictionary where :10 will return the top ten symbols based on executed volume
#sorting tuple based on descending order
top_symbols = sorted(symbol_volumes.items(), key=lambda x: x[1], reverse=True)[:10]

#top 10 symbols with their executed volumes
for symbol, volume in top_symbols:
    print(f"{symbol} {volume}")