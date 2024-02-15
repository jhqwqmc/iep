package dev.efnilite.iep.menu

import dev.efnilite.iep.IEP
import dev.efnilite.iep.config.Locales
import dev.efnilite.iep.generator.util.Settings
import dev.efnilite.iep.player.ElytraPlayer
import dev.efnilite.vilib.inventory.Menu
import dev.efnilite.vilib.inventory.item.SliderItem
import dev.efnilite.vilib.util.Task

object SettingsMenu {

    fun open(player: ElytraPlayer) {
        val menu = Menu(3, Locales.getString(player, "settings.title"))
            .distributeRowsEvenly()
        val generator = player.getGenerator()
        val settings = generator.settings

        if (player.hasPermission("iep.setting.style")) {
            menu.item(
                9,
                Locales.getItem(player, "settings.styles", settings.style.name())
                    .material(settings.style.next())
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

        if (player.hasPermission("iep.setting.seed")) {
            menu.item(11, Locales.getItem(player, "settings.seed", settings.seed.toString()))
        }

        if (player.hasPermission("iep.setting.info")) {
            menu.item(
                12,
                Locales.getItem(player, "settings.info", settings.info.toString())
                    .click({
                        generator.set { settings -> Settings(settings, info = !settings.info) }
                        open(player)
                    }))
        }

        if (player.hasPermission("iep.setting.locale")) {
            val locales = Locales.getLocales()
            val item = SliderItem()
                .initial(locales.indexOf(settings.locale))

            repeat(locales.size) {
                val locale = locales[it]

                item.add(
                    it, Locales.getItem(player, "settings.locale", locale)
                ) { _ ->
                    generator.set { settings -> Settings(settings, locale = locale) }

                    if (settings.locale != locale) {
                        open(player)
                    }

                    return@add true
                }
            }

            menu.item(13, item)
        }

        menu.item(23, Locales.getItem(player, "go back").click({ player.player.inventory.close() }))
            .open(player.player)
    }
}