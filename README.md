# Ollama MC

Have you ever wanted to chat with an LLM with your (very real) friends (that you totally have) in minecraft?

Well you can today!

## Setup and Requirements

- Install [ollama](https://ollama.com/download) and install an LLM, by default the LLM in config is llama3.1:latest.
- Have the web server running, by default at http://localhost:11434/
- Run your client with the mod

## Features

- /ollamaresetconvo resets the chat, also needed after changing config file in-game
- Setting a name for your LLM, saying this in chat will trigger it to respond (it might take a bit depending on your PC, if it doesn't show up check your logs)
- Other players can talk via the same name you talk to it with

## Known Issues
- In servers with no chat reports, messages are very wonky, so messages have to follow the format (username): (message) or <(username)> (message)
- Some messages from the server such as plugins, /tellraw command, etc that follow the same format as no chat reports chat messages can cause a network protocol error and I'm too lazy to fix it.
