A library for executing actions, such as modifying the game behavior or applying a random potion to the players, that can be controlled by another mod, a datapack or the player.

## Basics
The mod contain some controls by default, some of them are
<code><img src="https://cdn.modrinth.com/data/VqrLRUE8/images/37c29e0087d66d1b1ce04712dc85f3f982de3ac2.png" width="16px"> entity_jump</code>,
<code><img src="https://cdn.modrinth.com/data/VqrLRUE8/images/bea86e9333a8f11bc7fd8cc24c5f20a15d022024.png" width="16px"> potion_chaos</code> and
<code><img src="https://cdn.modrinth.com/data/VqrLRUE8/images/9f8e19532aedd98a8cf94a9e930b4b3de55022dc.png" width="16px"> inventory_shuffle</code>

To modify or read a control information you can use the `/gamecontrol` command  
The command format is `/gamecontrol <control> (get|set|invoke)`

The mod offers a way to always show or hide the information shown on the client screen with the 
NBT `{alwaysVisible: true/false}` and `{hideInfo: true/false}`

## Advanced

To set a control NBT run the command `/gamecontrol <control> set data <NBT>`  
To force the NBT to be exactly what you insert in the command use `/gamecontrol <control> set dataRaw <NBT>`  
**<ins>Be careful! When using `set dataRaw` the NBT will be exactly as you type, so if you don't 
insert it properly it can use more storage/RAM</ins>**

All data is saved on the world data folder: `<gamedir>/saves/<world>/data/cubecontroller.dat`

Any mod can add easily add a new control using the library register methods [developer wiki (WIP)](https://github.com/Awakened-Redstone/CubeController/wiki/Developers)
