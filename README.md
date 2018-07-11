# jblockchain government
# @author   Philip Kretz
# @email    ich ( at ) philipkretz . de
# @date     2018-07-10
# This is a fork of the JBlockchain project (https://github.com/neozo-software/jblockchain).
# Much thanks to Neozo for his work!

I forked and extended his demo project for creating a new blockchain in Java which you can also use for productive private (or public) blockchains.
Here I added a demo case as use in a decentral blockchain government for handling purposes of normal citizens.
Citizens can be added to the blockchain, their marriage or divorce can be added as social status.
Also their new born children can be added or citizens can be declared or alive.
Thus a whole society can simply control itself via a blockchain 3.0.
The blockchain can and should be fitted and compiled to your special needs.
Forks are welcome under Apache 2.0 license.
[![license](https://img.shields.io/badge/license-ASF2-blue.svg)](LICENSE)

# Additional features and changes to the original JBlockchain:
- saving CPU power and voltage by replacing SHA256-algorithm with SCrypt
- added a more complex self ip discovery algorithm similar to Bitcoin
- added a backbone network of trusted root servers, which must be added in config class
- blockchain and entity data are saved, so hosts can go offline (json.gz files)
- saving disk space and data transfer size with gunzip algorithm for transferring and saving data
- added multiple config parameters for client that you must not submit address for communication with nodes 
- added a blockchain government as demo case in client
- keys and blockchain files are saved in folder HOME/blockchain

# build
# - edit config class and files for setting port (default: 21986) and root servers (default: only localhost)
# - fit code for your needs (optional)
cd jblockchain
./mvnw package

# please respect: all json.gz for blockchain files will be deleted after compiling because of unit test case issues, so please create a backup if you change something!

This builds these 3 Spring Boot/Maven modules:
* __node__: part of the blockchain network for handling proof of work mining algorithm, building blocks and distributing transactions
* __client__: a shell client to communicate to local blockchain node
* __common__: Shared code with entities used by the other 2 components

## start node (needed for running)
java -jar node/target/node-0.0.1-SNAPSHOT.jar



# maintaining commands

# this starts the mining process of a local node
java -jar client/target/client-0.0.1-SNAPSHOT.jar --start-miner

# this stops the mining process of a local node
java -jar client/target/client-0.0.1-SNAPSHOT.jar --stop-miner



# write commands:

# Generate keypair in folder HOME/blockchain (call just only once)
# This generates key.priv and key.pub files for user (as you should know in encryption: never share your key.priv-file and keep it as safe as possible).
# The local node will distribute this new public address to all other nodes.
# Note the sender id as result of this command for sending messages and keep it with your key files!
cd client/target
java -jar client-0.0.1-SNAPSHOT.jar --keypair

# add an address to blockchain (normally just needed once)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --publish-address --publickey key.pub

# sending messages (just for explanation, normally not needed)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --transaction --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --message "Hallo Welt" --privatekey key.priv 

# sending special JBlockchain government messages (Kept really simple, I know...)

# add a new city (for what do you need a postcode if you have a blockchain id?)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --add-city --name "Springfield" --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# add a new street
java -jar client/target/client-0.0.1-SNAPSHOT.jar --add-street --name "Simpson av." --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv

# add a new house
java -jar client/target/client-0.0.1-SNAPSHOT.jar --add-house --houseNr 23 --street "Simpson av." --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# add a new citizen
java -jar client/target/client-0.0.1-SNAPSHOT.jar --add-citizen --firstName Homer --lastName Simpson --birthday 19540204 --father "Grandpa Simpson" --mother "Unknown" --address "Simpson av. 23, Springfield" --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# change address of a citizen
java -jar client/target/client-0.0.1-SNAPSHOT.jar --set-address --firstName Bart --lastName Simpson --address "Tree house" --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# note a marriage (hopefully forever)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --marriage --person1 "Homer Simpson" --person2 "Marge Simpson" --date 19740523 --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# note a divorce (hopefully not)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --divorce --person1 "Whitney Tears" --person2 "Lars Vegas" --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# declare death of a citizen (the sad part - I hope)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --declare-death --firstName Pablo --lastName Escobar --date 19930212 --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv 

# declare a citizen alive after death (if a mistake occures, a lost person gets found or also after 3 days for Jesus)
java -jar client/target/client-0.0.1-SNAPSHOT.jar --declare-alive --name Jesus --sender "Tdz0bKDfca3QjFAe5Ccuj9Noy6ah8n+R8DnZznvjic4=" --privatekey key.priv



# read commands:

# list all cities
java -jar client/target/client-0.0.1-SNAPSHOT.jar --show-cities

# list all citizens to a city
java -jar client/target/client-0.0.1-SNAPSHOT.jar --show-citizens --city Springfield

# show all details saved to a citizen
java -jar client/target/client-0.0.1-SNAPSHOT.jar --show-citizen-details --name "Homer Simpson" --city Springfield

# show birth death rate to a city
java -jar client/target/client-0.0.1-SNAPSHOT.jar --show-birth-death-rate --city Springfield



# Hint: the whole blockchain can be viewed in blockchain.json.gz file or via http://localhost:21986/block
# Addresses and connected nodes can be seen in http://localhost:21986/address and http://localhost:21986/nodes (or also in files)
# Newer transactions which are still not collected in a block can be viewed at ttp://localhost:21986/transaction (or file transaction.json.gz)