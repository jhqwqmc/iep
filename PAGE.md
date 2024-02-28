## ![Infinite Elytra Parkour](https://i.imgur.com/By15ue1.png)

From the creator of **[Infinite Parkour](https://www.spigotmc.org/resources/87226/)** and **[Infinite Parkour Plus](https://www.spigotmc.org/resources/105019/)**.

Infinite Elytra Parkour (IEP) is an automatically generating,
infinitely long elytra parkour plugin. On join, players get teleported to a separate world
where an elytra parkour is generated for them.

Players get a score based on how far they travel. A player's personal best
is saved, and they can compare their scores with others on the leaderboards.
Players can also customize their parkour, by changing settings like the style, radius, and seed.

If they reach a specific amount, you can enable rewards, to give them something for their achievements.
You can also add custom languages. Almost all items and messages are customizable.
Leaderboards and player settings can be saved using MySQL or using local files.

Scroll down for the full list of features!
- Works on Paper and Spigot. **Paper** is highly recommended.
- This plugin supports **1.20** only.
- Use **/iep** to get more information when you join your server.

### ![Modes](https://i.imgur.com/AB9qcic.png)

With these modes, players can spend a lot of time engaging in competitive and fun modes. 
These work great as a way to pass the time. 
You can enable or disable any of these modes in the config. 
Every mode has its own leaderboard.

**Default**

The default mode. This mode has no special modifiers.
This mode is the one players start in if `join-on-join` is enabled.

https://youtu.be/z05W-72XrIc

**Obstacle**

In this mode, obstacles are randomly generated. 
To avoid being reset, the player has to pass these obstacles.

https://youtu.be/K43cBPN4IK0

**Time Trial**

In this mode, players have to reach a specific score on a specific seed as fast as possible.
Every map will generate in the exact same way for every player on this mode.
This allows players to compete in their ability to speedrun.
The score and seed can be changed in the config.

https://youtu.be/GaS7LuCqPOc

**Speed Demon**

In this mode, score is not determined by how far you've travelled, but by your highest speed at any point.
Players will compete based on who is the fastest.

https://youtu.be/UiYKDMoVgsE

**Min Speed**

In this mode, you have to stay above a certain speed.
Once you've reached the minimum required speed, going any slower will reset the player.
How close you are to the minimum speed is shown in the action bar.
The minimum speed can be changed in the config.

https://youtu.be/SLAnHzfUkms

**Close**

In this mode, you have to stay within a certain radius of the blocks.
This radius can be changed in the settings.
This mode is very difficult.

https://youtu.be/jnDb0ypdotM

### ![Settings](https://i.imgur.com/WlmM5UW.png)

https://youtu.be/esCFAGNQhqs

**Style** 

The style of the parkour. This is the types of blocks that will be set.
If you change the style while flying, it may take a while for changes to apply.
Styles can be changed in the config.

**Radius**

The (circle) radius of the parkour.

**Time**

The current visual time.

**Seed**

The current seed. This number determines the shape of the parkour.
When a player gets reset, the seed will be randomized, unless the player has selected a custom seed.
Use `/iep seed <seed>` to set a custom seed. When a player sets a seed,
all parkour they play will use this seed. They can switch back to random seeds

**Locale**

The language.

**Fall**

Whether to send a message with extra info when a player gets reset.

**Info**

Whether to display extra info in the display bar.

**Metric**

Whether to use the metric system or not.

### ![Rewards](https://i.imgur.com/90aBAOo.png)

To reward players who get good scores and put in effort, you can add custom rewards.
For instance, when a player finishes their first Time Trial run, you could give them a message saying "Good job!",
add $100 to their Vault balance and when they leave, give them a diamond or run a command.
Or you could do any of this every 500 score. To see how this works, check `rewards.yml` (below).

```yaml
# -= Rewards enabled? =-
enabled: false

# Rewards use the following format:
# <time of execution>||<mode>||<command>||<value>
# - <time of execution> is when the reward will be given. It can be "now" or "leave", which will apply rewards on leave.
# - <mode> is the mode the reward will be given in. It can be "all" or a specific mode.
# - <command> is the command that will be executed. It can be "vault" for Vault rewards,
#  "console command" (without /), "player command" (without /) or "send".
# - <value> is the value of the command. It can be a message, a command or the amount of money to give.
# To get the name of the player, use %player%.

# -= Score rewards =-
# Rewards when a player reaches a specific score.
score:
  10:
    - "now||all||send||You've reached 10 points!"
  2500:
    - "leave||all||console command||give %player% diamond 1"
    - "now||all||send||Good job for completing your run!"

# -= Interval rewards =-
# Rewards which will be given at certain intervals.
# An interval of 10 means a player will get a reward at 10, 20, 30, etc.
interval:
  100:
    - "now||all||player command||me hello :)"
    - "leave||time trial||send||<green>Good job!"
    - "now||all||vault||10"

# -= One-time rewards =-
# Rewards which will be given only once.
# Changing this reward will not cause players who have already received it to receive it again.
one-time:
  5000:
    - "now||all||send||You've reached 5000 points! Nice!"
```

### ![FAQ](https://i.imgur.com/FDbh5BV.png)

**Permissions**

For these permissions to apply, you need to enable permissions in the config.

```
- iep.play
- iep.leave
- iep.setting
- iep.setting.style: Style menu
- iep.setting.style.<style>: Specific style
- iep.setting.radius
- iep.setting.seed
- iep.setting.info
- iep.setting.time
- iep.setting.metric
- iep.setting.locale
- iep.setting.fall
- iep.leaderboard: Leaderboards menu
- iep.leaderboard.<mode>: Specific mode leaderboard
```

**Placeholders and leaderboards**

_In-parkour placeholders_

These only work when a player is in the parkour.

- `%iep_score%`
- `%iep_time%`
- `%iep_seed%`
- `%iep_speed%`

_Global placeholders_

`%iep_<mode>_<type>_<rank>%`
- Mode: The mode name. Is the same as the name of the file in the `IEP/leaderboards` folder.
- Type: The type of the data. Can be `name`, `score`, `time`, or `seed`.
- Rank: The rank of the data. Can be any number, staring at 1.


_Example holographic leaderboard_

```
&6#1 &7- %iep_default_name_1% - %iep_default_score_1% (%iep_default_time_1%)
&6#2 &7- %iep_default_name_2% - %iep_default_score_2% (%iep_default_time_2%)
&6#3 &7- %iep_default_name_3% - %iep_default_score_3% (%iep_default_time_3%)
&6#4 &7- %iep_default_name_4% - %iep_default_score_4% (%iep_default_time_4%)
&6#5 &7- %iep_default_name_5% - %iep_default_score_5% (%iep_default_time_5%)
```

**How do I reload the plugin?**

Due to technical constraints, the plugin does not have a reload command. 
Please _restart your server_ after applying changes.
Reloading your server may cause problems.

**How do I add my language?**

- Copy the `en.yml` file in the `IEP/locales` folder and rename it to your language code 
  - (should be _at most_ 5 characters long, examples: de, zh_CN, en_US).
- Translate the file's messages.
- Restart the server.

### ![Terms of Purchase](https://i.imgur.com/RJdzuho.png)

Upon purchase of this resource, you accept the terms of purchase. 
If you break the terms in any way, you won't get access to support for any of my plugins.

1. Redistribution and the reselling of this software is not allowed.
2. Refunds of this software are not allowed.
3. Charging back your money after purchase is not allowed.
4. I hold the right to change the price of this resource at any time.

Please don't download this resource from a site which cracks plugins, 
since these have a high chance of containing malware or other unwanted things.

### ![Reviewing](https://i.imgur.com/RkWYLQ3.png)

Don't leave reviews about bugs or suggestions. 
The only way to properly report bugs or suggestions is through **[Discord](https://efnilite.dev/discord)** or **[email](https://efnilite.dev/#contact)**.
Support usually takes less than a few hours!