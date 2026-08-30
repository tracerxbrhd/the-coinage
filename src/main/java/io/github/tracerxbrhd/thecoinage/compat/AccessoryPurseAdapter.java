package io.github.tracerxbrhd.thecoinage.compat;

import dev.uapi.accessory.AccessoryIntegrationService;
import io.github.tracerxbrhd.thecoinage.TheCoinage;
import io.github.tracerxbrhd.thecoinage.purse.PurseHandle;
import io.github.tracerxbrhd.thecoinage.registry.CoinageItems;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Loader-safe mutable adapters. Optional API classes never appear in unconditional signatures. */
public final class AccessoryPurseAdapter {
    private AccessoryPurseAdapter() {}

    public static Optional<PurseHandle> find(Player player) {
        AccessoryIntegrationService.bootstrap();
        if (ModList.get().isLoaded("curios")) {
            Optional<PurseHandle> curios = findCurios(player);
            if (curios.isPresent()) return curios;
        }
        if (ModList.get().isLoaded("accessories")) {
            Optional<PurseHandle> accessories = findAccessories(player);
            if (accessories.isPresent()) return accessories;
        }
        return Optional.empty();
    }

    private static Optional<PurseHandle> findCurios(Player player) {
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi", false,
                AccessoryPurseAdapter.class.getClassLoader());
            Object optional = api.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class)
                .invoke(null, player);
            Object handler = optional instanceof Optional<?> value ? value.orElse(null) : null;
            if (handler == null) return Optional.empty();
            Object inventory = handler.getClass().getMethod("getEquippedCurios").invoke(handler);
            return findInHandler(inventory, "curios");
        } catch (ReflectiveOperationException | LinkageError exception) {
            TheCoinage.LOGGER.warn("Curios is installed but mutable purse access is unavailable", exception);
            return Optional.empty();
        }
    }

    private static Optional<PurseHandle> findAccessories(Player player) {
        try {
            Method capabilityMethod = findMethod(player.getClass(), "accessoriesCapability");
            if (capabilityMethod == null) return Optional.empty();
            Object capability = capabilityMethod.invoke(player);
            if (capability == null) return Optional.empty();
            Object containersObject = capability.getClass().getMethod("getContainers").invoke(capability);
            if (!(containersObject instanceof Map<?, ?> containers)) return Optional.empty();
            List<Map.Entry<?, ?>> ordered = new ArrayList<>(containers.entrySet());
            ordered.sort(Comparator.comparing(entry -> String.valueOf(entry.getKey())));
            for (Map.Entry<?, ?> entry : ordered) {
                Object container = entry.getValue();
                Object inventory = unwrapAccessoriesInventory(container);
                Optional<PurseHandle> found = findInHandler(inventory, "accessories:" + entry.getKey());
                if (found.isPresent()) return found;
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            TheCoinage.LOGGER.warn("Accessories is installed but mutable purse access is unavailable", exception);
        }
        return Optional.empty();
    }

    private static Object unwrapAccessoriesInventory(Object container) throws ReflectiveOperationException {
        for (String methodName : List.of("getAccessories", "getAccessoryInventory", "getInventory", "getStacks")) {
            Method method = findMethod(container.getClass(), methodName);
            if (method != null) return method.invoke(container);
        }
        return container;
    }

    private static Optional<PurseHandle> findInHandler(Object handler, String source) throws ReflectiveOperationException {
        if (handler == null) return Optional.empty();
        Method size = firstMethod(handler.getClass(), List.of("getSlots", "getContainerSize", "getSize"));
        Method get = firstMethod(handler.getClass(), List.of("getStackInSlot", "getItem", "getStack"), int.class);
        Method set = firstMethod(handler.getClass(), List.of("setStackInSlot", "setItem", "setStack"), int.class, ItemStack.class);
        int slots = ((Number) size.invoke(handler)).intValue();
        for (int index = 0; index < slots; index++) {
            Object value = get.invoke(handler, index);
            if (value instanceof ItemStack stack && stack.is(CoinageItems.COIN_PURSE.get())) {
                return Optional.of(new ReflectiveHandle(handler, get, set, index, source));
            }
        }
        return Optional.empty();
    }

    private static Method firstMethod(Class<?> type, List<String> names, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        for (String name : names) {
            Method method = findMethod(type, name, parameterTypes);
            if (method != null) return method;
        }
        throw new NoSuchMethodException(type.getName() + " has none of " + names);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private record ReflectiveHandle(Object handler, Method getter, Method setter, int index, String source)
        implements PurseHandle {
        @Override public ItemStack get() {
            try {
                Object value = getter.invoke(handler, index);
                return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException exception) {
                return ItemStack.EMPTY;
            }
        }

        @Override public boolean set(ItemStack stack) {
            try {
                setter.invoke(handler, index, stack);
                return true;
            } catch (ReflectiveOperationException exception) {
                return false;
            }
        }

        @Override public boolean isValid() { return get().is(CoinageItems.COIN_PURSE.get()); }
    }
}
