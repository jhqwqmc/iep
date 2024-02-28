package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.ResetReason
import dev.efnilite.iep.generator.Settings
import dev.efnilite.iep.generator.Settings.Companion.asStyle
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.Item
import dev.efnilite.vilib.inventory.item.SliderItem
import dev.efnilite.vilib.util.Task

object SettingsMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(4, Locales.getString(player, "settings.title"))
            .distributeRowsEvenly()
        val generator = player.getGenerator()
        val settings = generator.settings

        if (player.hasPermission("iep.setting.style")) {
            val style = settings.style.asStyle()

            menu.item(
                9,
                Locales.getItem(player, "settings.styles", style.name())
                    .material(style.next())
                    .click({ StylesMenu.open(player) })
            )
        }

        if (player.hasPermission("iep.setting.radius")) {
            val item = Locales.getItem(player, "settings.radius", settings.radius.toString())
                .amount(generator.settings.radius)

            menu.item(
                10, SliderItem()
                    .initial(generator.settings.radius - 3)
                    .add(0, item) {
                        generator.set { settings -> Settings(settings, radius = 3) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(1, item) {
                        generator.set { settings -> Settings(settings, radius = 4) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(2, item) {
                        generator.set { settings -> Settings(settings, radius = 5) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(3, item) {
                        generator.set { settings -> Settings(settings, radius = 6) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
            )
        }

        if (player.hasPermission("iep.setting.time")) {
            val item = Locales.getItem(player, "settings.time", settings.time.toString())

            menu.item(
                11, SliderItem()
                    .initial(settings.time / 6000)
                    .add(0, item) {
                        generator.set { settings -> Settings(settings, time = 0) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(1, item) {
                        generator.set { settings -> Settings(settings, time = 6000) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(2, item) {
                        generator.set { settings -> Settings(settings, time = 12000) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
                    .add(3, item) {
                        generator.set { settings -> Settings(settings, time = 18000) }
                        Task.create(IEP.instance).delay(1).execute { open(player) }.run()
                        return@add true
                    }
            )
        }

        if (player.hasPermission("iep.setting.seed")) {
            val seed = if (settings.seed == -1) Locales.getString(player, "settings.seed.random") else generator.seed.toString()

            menu.item(12, Locales.getItem(player, "settings.seed", seed)
                .click({
                    generator.set { settings -> Settings(settings, seed = -1) }
                    generator.reset(ResetReason.RESET)
                    open(player)
                }))
        }

        if (player.hasPermission("iep.setting.locale")) {
            val locales = Locales.getLocales().toList()
            val item = SliderItem()
                .initial(locales.indexOf(settings.locale))

            repeat(locales.size) {
                val locale = locales[it]

                item.add(
                    it, Locales.getItem(player, "settings.locale", locale)
                ) { _ ->
                    generator.set { settings -> Settings(settings, locale = locale) }

                    Task.create(IEP.instance).delay(1).execute {
                        if (settings.locale != locale) {
                            open(player)
                        }
                    }.run()

                    return@add true
                }
            }

            menu.item(13, item)
        }

        if (player.hasPermission("iep.setting.fall")) {
            menu.item(
                19,
                getBooleanItem(player, "settings.fall", settings.fall)
                    .click({
                        generator.set { settings -> Settings(settings, fall = !settings.fall) }
                        open(player)
                    }))
        }

        if (player.hasPermission("iep.setting.info")) {
            menu.item(
                20,
                getBooleanItem(player, "settings.info", settings.info)
                    .click({
                        generator.set { settings -> Settings(settings, info = !settings.info) }
                        open(player)
                    }))
        }


        if (player.hasPermission("iep.setting.metric")) {
            menu.item(
                21,
                getBooleanItem(player, "settings.metric", settings.metric)
                    .click({
                        generator.set { settings -> Settings(settings, metric = !settings.metric) }
                        open(player)
                    }))
        }

        menu.item(32, Locales.getItem(player, "go back").click({ player.player.closeInventory() }))
            .open(player.player)
    }

    private fun getBooleanItem(player: ElytraPlayer, path: String, boolean: Boolean): Item {
        val base = if (boolean) {
            Locales.getItem(player, "settings.enabled")
        } else {
            Locales.getItem(player, "settings.disabled")
        }
        val item = Locales.getItem(player, path, base.lore[0])

        base.name(base.name + item.name)
        base.lore(item.lore)

        return base
    }
}