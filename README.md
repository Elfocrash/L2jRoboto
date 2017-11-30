# L2jRoboto

L2jRoboto way to create fake players for l2j servers. It is currently coded for aCis but it should be easy to adapt.
It is in a WIP state so it is NOT recommended to use on a live server.

## Installation

L2jRoboto has minimal dependencies

* Add in GameServer.java
`FakePlayerManager.INSTANCE.initialise();`

* Register the admin command handler
`registerAdminCommandHandler(new AdminFakePlayers());`

* Player.java should NOT be final

That should be it

Have fun
