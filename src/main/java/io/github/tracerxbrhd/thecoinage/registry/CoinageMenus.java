package io.github.tracerxbrhd.thecoinage.registry;

import io.github.tracerxbrhd.thecoinage.TheCoinage;
import io.github.tracerxbrhd.thecoinage.menu.PurseMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CoinageMenus {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, TheCoinage.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<PurseMenu>> PURSE = TYPES.register("coin_purse",
        () -> IMenuTypeExtension.create(PurseMenu::new));
    private CoinageMenus() {}
}
