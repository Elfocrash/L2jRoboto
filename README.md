# L2jRoboto

L2jRoboto way to create fake players for l2j servers. It is currently coded for aCis (372) but it should be easy to adapt.
It is in a WIP state so it is NOT recommended to use on a live server.

## The end goal
The ultimate goal is to give server administrations the ability to create fake player entities which act as close to real players as possible.
L2jRoboto will provide out of the box as many appropriate AIs as possible for the admins to use, but developers can pick it up and implement their own behaviors by extending the `FakePlayerAI` class.

## Features
- [x] Priority based offensive/defensive/healing/support spell picking
- [x] Attack entity
- [x] Give appropriate armor/weapon for the specific class
- [x] Automatic enchant using the server's chance
- [x] Take control of on of the fake players
- [x] Teleport to village on death
  
## Admin commands
  * //fakes - Brings up the Dashboard
  * //takecontrol - Takes control of a bot and allows you to move it around
  * //releasecontrol - Releases the bot and enables movement for you again
  * //spawnrandom - Spawns a random Class bot with default AI for this class
  * //deletefake - Deleted a bot from the game
  * //spawnenchanter - Spawns an enchanter bot

## Installation

L2jRoboto has minimal dependencies

You can find the patch under the `dist` folder

Have fun

## Creations
https://www.youtube.com/watch?v=RuVnr0nZVl8

https://www.youtube.com/watch?v=X9wEoF7ILmw

https://www.youtube.com/watch?v=e3jBNtkE4yA

https://www.youtube.com/watch?v=Dob4yRtVr0s

https://www.youtube.com/watch?v=ynMtvmLHV5U

https://www.youtube.com/watch?v=7Q14_wgzqS8
